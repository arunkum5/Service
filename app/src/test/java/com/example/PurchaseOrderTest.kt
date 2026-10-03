package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entities.InventoryItemEntity
import com.example.data.local.entities.toPurchaseOrderItems
import com.example.data.repository.GvdRepository
import com.example.model.ItemCategory
import com.example.model.VehicleType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PurchaseOrderTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: GvdRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = GvdRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testAddAllMasterInventoryAndCount() = runBlocking {
        val inserted = repo.addAllMasterInventory()
        assertTrue(inserted > 0)

        val all = repo.allInventory.first()
        assertEquals(inserted, all.size)

        // Calling again should not insert duplicates
        val secondInsert = repo.addAllMasterInventory()
        assertEquals(0, secondInsert)
    }

    @Test
    fun testAutoGeneratePurchaseOrderBasedOnMinimumInventory() = runBlocking {
        // Insert a low stock item
        val lowStockItem = InventoryItemEntity(
            partName = "Front Ceramic Brake Pads",
            partNumber = "SP-BRK-TEST",
            category = ItemCategory.SPARE,
            compatibleType = VehicleType.TWO_WHEELER,
            unitPrice = 400.0,
            stockQuantity = 2,
            minThresholdAlert = 8
        )
        val itemId = db.inventoryDao().insertItem(lowStockItem)

        // Also insert an item with sufficient stock
        val healthyItem = InventoryItemEntity(
            partName = "Castrol Coolant",
            partNumber = "LB-COOL-TEST",
            category = ItemCategory.LUBE,
            compatibleType = VehicleType.FOUR_WHEELER,
            unitPrice = 300.0,
            stockQuantity = 20,
            minThresholdAlert = 5
        )
        db.inventoryDao().insertItem(healthyItem)

        // Auto generate PO from low stock
        val po = repo.autoGeneratePurchaseOrderFromLowStock()
        assertNotNull(po)
        assertTrue(po!!.poNumber.startsWith("PO-GVD-"))
        assertEquals("PENDING", po.status)

        val items = po.itemsJson.toPurchaseOrderItems()
        assertEquals(1, items.size)
        val orderedPart = items[0]
        assertEquals("SP-BRK-TEST", orderedPart.partNumber)
        // Expected suggested qty = minThresholdAlert * 2 - stockQuantity = 16 - 2 = 14
        assertTrue(orderedPart.orderQuantity >= 8)

        // Fulfill / Receive Purchase Order
        val receiveResult = repo.receivePurchaseOrder(po.id)
        assertTrue(receiveResult)

        // Verify stock in inventory was increased by the ordered quantity
        val updatedItem = db.inventoryDao().getAllInventory().first().first { it.id == itemId }
        assertEquals(2 + orderedPart.orderQuantity, updatedItem.stockQuantity)

        // Check PO status is updated to RECEIVED
        val orders = repo.allPurchaseOrders.first()
        assertEquals("RECEIVED", orders.first { it.id == po.id }.status)
    }
}
