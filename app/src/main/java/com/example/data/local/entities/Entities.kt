package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.ComponentStatus
import com.example.model.ItemCategory
import com.example.model.JobCardStatus
import com.example.model.VehicleType

@Entity(tableName = "job_cards")
data class JobCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jobCardNumber: String,
    val customerName: String,
    val customerMobile: String,
    val customerEmail: String = "",
    val vehicleNumber: String,
    val vehicleType: VehicleType = VehicleType.TWO_WHEELER,
    val make: String,
    val model: String,
    val variant: String = "",
    val odometerKm: Int = 0,
    val fuelLevelPercent: Int = 50,
    val accessoriesNotes: String = "",
    val customerVoice: String = "",
    val dentNotes: String = "",
    val status: JobCardStatus = JobCardStatus.OPEN,
    val totalSpares: Double = 0.0,
    val totalLabour: Double = 0.0,
    val totalLubes: Double = 0.0,
    val totalAmount: Double = 0.0,
    val advancePaid: Double = 0.0,
    val balanceAmount: Double = 0.0,
    val deliveryDateTime: String = "",
    val isSmsAlertEnabled: Boolean = true,
    val isPaidOnline: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "job_card_items")
data class JobCardItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jobCardId: Long,
    val category: ItemCategory,
    val name: String,
    val quantity: Int = 1,
    val unitPrice: Double,
    val discount: Double = 0.0,
    val totalAmount: Double = 0.0
)

@Entity(tableName = "component_inspections")
data class ComponentInspectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleNumber: String,
    val jobCardId: Long = 0,
    val componentKey: String, // e.g. "ENGINE_OIL", "BRAKE_PADS", "FRONT_TIRES", "AC_SYSTEM"
    val componentName: String,
    val category: String, // Mechanical, Electrical, Body, Fluid, Interior
    val angle: String, // FRONT, SIDE_LEFT, REAR, SIDE_RIGHT, ENGINE_BAY, INTERIOR, UNDERBODY
    val status: ComponentStatus,
    val technicianNotes: String,
    val worksTillInfo: String = "Works till next service (5,000 km)",
    val recommendedAction: String = "",
    val replacementCost: Double = 0.0,
    val workPhotoUrl: String = "",
    val lastServicedDate: String = "2026-06-15",
    val updatedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val partName: String,
    val partNumber: String,
    val category: ItemCategory,
    val compatibleType: VehicleType = VehicleType.TWO_WHEELER,
    val unitPrice: Double,
    val stockQuantity: Int,
    val minThresholdAlert: Int = 5,
    val unit: String = "pcs"
)

@Entity(tableName = "service_appointments")
data class ServiceAppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerName: String,
    val customerPhone: String,
    val vehicleNumber: String,
    val vehicleType: VehicleType,
    val servicePackage: String, // Bike Service, Car Service, PPF & Detailing, Ceramic Coating, Insurance & Body Works
    val preferredDate: String,
    val preferredSlot: String,
    val isDoorstepPickup: Boolean = false,
    val customerVoice: String = "",
    val status: String = "CONFIRMED", // CONFIRMED, IN_SERVICE, COMPLETED
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "vehicle_showroom_inventory")
data class VehicleInventoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val vehicleType: VehicleType,
    val brand: String,
    val model: String,
    val year: Int,
    val kmDriven: Int,
    val fuelType: String,
    val price: Double,
    val emiStartingAt: Double,
    val specsSummary: String,
    val condition: String = "Certified Pre-Owned",
    val warrantyMonths: Int = 12,
    val isAvailable: Boolean = true
)

@Entity(tableName = "service_reminders")
data class ServiceReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleNumber: String,
    val customerName: String,
    val customerMobile: String,
    val reminderType: String, // Periodic Service, Engine Oil, Insurance Expiry, Ceramic Coating Maintenance
    val dueDate: String,
    val messageText: String,
    val status: String = "SCHEDULED", // SCHEDULED, SENT
    val isWhatsAppTriggered: Boolean = false
)

@Entity(tableName = "customer_reviews")
data class CustomerReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerName: String,
    val customerMobile: String = "",
    val vehicleNumber: String = "",
    val vehicleModel: String = "",
    val serviceType: String = "Periodic Service",
    val rating: Int = 5, // 1 to 5 stars
    val aspectPunctuality: Int = 5,
    val aspectCleanliness: Int = 5,
    val aspectPricing: Int = 5,
    val tags: String = "", // Comma-separated tags
    val comment: String = "",
    val jobCardNumber: String = "",
    val adminResponse: String = "",
    val adminRespondedAt: Long = 0L,
    val isVerifiedClient: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "purchase_orders")
data class PurchaseOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val poNumber: String, // e.g. PO-GVD-2026-0923
    val supplierName: String = "Bangalore OEM Spares & Lubricants Wholesale Hub",
    val supplierAddress: String = "JC Road, Kalasipalya, Bangalore 560002",
    val supplierContact: String = "+91 98450 12345",
    val status: String = "PENDING", // PENDING, ORDERED, RECEIVED, CANCELLED
    val totalItemsCount: Int = 0,
    val totalUnitsCount: Int = 0,
    val subtotal: Double = 0.0,
    val gstAmount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val notes: String = "",
    val itemsJson: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val receivedAt: Long = 0L
)

data class PurchaseOrderItem(
    val inventoryId: Long,
    val partName: String,
    val partNumber: String,
    val category: String,
    val currentStock: Int,
    val minThreshold: Int,
    val orderQuantity: Int,
    val unitPrice: Double,
    val lineTotal: Double
)

fun List<PurchaseOrderItem>.toJsonString(): String {
    val arr = org.json.JSONArray()
    for (item in this) {
        val obj = org.json.JSONObject()
        obj.put("inventoryId", item.inventoryId)
        obj.put("partName", item.partName)
        obj.put("partNumber", item.partNumber)
        obj.put("category", item.category)
        obj.put("currentStock", item.currentStock)
        obj.put("minThreshold", item.minThreshold)
        obj.put("orderQuantity", item.orderQuantity)
        obj.put("unitPrice", item.unitPrice)
        obj.put("lineTotal", item.lineTotal)
        arr.put(obj)
    }
    return arr.toString()
}

fun String.toPurchaseOrderItems(): List<PurchaseOrderItem> {
    if (this.isBlank()) return emptyList()
    val list = mutableListOf<PurchaseOrderItem>()
    try {
        val arr = org.json.JSONArray(this)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                PurchaseOrderItem(
                    inventoryId = obj.optLong("inventoryId", 0L),
                    partName = obj.optString("partName", ""),
                    partNumber = obj.optString("partNumber", ""),
                    category = obj.optString("category", "SPARE"),
                    currentStock = obj.optInt("currentStock", 0),
                    minThreshold = obj.optInt("minThreshold", 0),
                    orderQuantity = obj.optInt("orderQuantity", 0),
                    unitPrice = obj.optDouble("unitPrice", 0.0),
                    lineTotal = obj.optDouble("lineTotal", 0.0)
                )
            )
        }
    } catch (e: Exception) {
        // Safe fallback
    }
    return list
}

