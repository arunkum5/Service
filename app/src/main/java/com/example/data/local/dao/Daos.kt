package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.ComponentInspectionEntity
import com.example.data.local.entities.CustomerReviewEntity
import com.example.data.local.entities.InventoryItemEntity
import com.example.data.local.entities.JobCardEntity
import com.example.data.local.entities.JobCardItemEntity
import com.example.data.local.entities.PurchaseOrderEntity
import com.example.data.local.entities.ServiceAppointmentEntity
import com.example.data.local.entities.ServiceReminderEntity
import com.example.data.local.entities.VehicleInventoryEntity
import com.example.model.JobCardStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface JobCardDao {
    @Query("SELECT * FROM job_cards ORDER BY createdAt DESC")
    fun getAllJobCards(): Flow<List<JobCardEntity>>

    @Query("SELECT * FROM job_cards WHERE id = :id LIMIT 1")
    fun getJobCardById(id: Long): Flow<JobCardEntity?>

    @Query("SELECT * FROM job_cards WHERE vehicleNumber = :vehNo ORDER BY createdAt DESC LIMIT 1")
    fun getLatestJobCardByVehicle(vehNo: String): Flow<JobCardEntity?>

    @Query("SELECT * FROM job_cards WHERE status = :status ORDER BY createdAt DESC")
    fun getJobCardsByStatus(status: JobCardStatus): Flow<List<JobCardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobCard(jobCard: JobCardEntity): Long

    @Update
    suspend fun updateJobCard(jobCard: JobCardEntity)

    @Query("UPDATE job_cards SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: JobCardStatus)

    @Query("UPDATE job_cards SET isPaidOnline = 1, balanceAmount = 0.0 WHERE id = :id")
    suspend fun markAsPaidOnline(id: Long)

    @Query("SELECT * FROM job_card_items WHERE jobCardId = :jobCardId")
    fun getItemsForJobCard(jobCardId: Long): Flow<List<JobCardItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobCardItems(items: List<JobCardItemEntity>)

    @Query("DELETE FROM job_card_items WHERE jobCardId = :jobCardId")
    suspend fun deleteItemsForJobCard(jobCardId: Long)
}

@Dao
interface ComponentInspectionDao {
    @Query("SELECT * FROM component_inspections WHERE vehicleNumber = :vehicleNumber")
    fun getInspectionsForVehicle(vehicleNumber: String): Flow<List<ComponentInspectionEntity>>

    @Query("SELECT * FROM component_inspections ORDER BY updatedTimestamp DESC")
    fun getAllInspections(): Flow<List<ComponentInspectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(inspections: List<ComponentInspectionEntity>)

    @Update
    suspend fun updateInspection(inspection: ComponentInspectionEntity)
}

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items ORDER BY partName ASC")
    fun getAllInventory(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE stockQuantity <= minThresholdAlert")
    fun getLowStockItems(): Flow<List<InventoryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<InventoryItemEntity>)

    @Update
    suspend fun updateItem(item: InventoryItemEntity)

    @Query("UPDATE inventory_items SET stockQuantity = stockQuantity + :adjustment WHERE id = :id")
    suspend fun adjustStock(id: Long, adjustment: Int)
}

@Dao
interface ServiceDao {
    @Query("SELECT * FROM service_appointments ORDER BY createdAt DESC")
    fun getAllAppointments(): Flow<List<ServiceAppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: ServiceAppointmentEntity): Long

    @Query("UPDATE service_appointments SET status = :status WHERE id = :id")
    suspend fun updateAppointmentStatus(id: Long, status: String)

    @Query("SELECT * FROM vehicle_showroom_inventory WHERE isAvailable = 1 ORDER BY id DESC")
    fun getAllShowroomVehicles(): Flow<List<VehicleInventoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicles(vehicles: List<VehicleInventoryEntity>)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM service_reminders ORDER BY dueDate ASC")
    fun getAllReminders(): Flow<List<ServiceReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reminders: List<ServiceReminderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ServiceReminderEntity): Long

    @Query("UPDATE service_reminders SET status = 'SENT', isWhatsAppTriggered = 1 WHERE id = :id")
    suspend fun markAsSent(id: Long)
}

@Dao
interface CustomerReviewDao {
    @Query("SELECT * FROM customer_reviews ORDER BY createdAt DESC")
    fun getAllReviews(): Flow<List<CustomerReviewEntity>>

    @Query("SELECT * FROM customer_reviews WHERE vehicleNumber = :vehNo ORDER BY createdAt DESC")
    fun getReviewsForVehicle(vehNo: String): Flow<List<CustomerReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: CustomerReviewEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reviews: List<CustomerReviewEntity>)

    @Query("UPDATE customer_reviews SET adminResponse = :response, adminRespondedAt = :respondedAt WHERE id = :id")
    suspend fun updateAdminResponse(id: Long, response: String, respondedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM customer_reviews WHERE id = :id")
    suspend fun deleteReview(id: Long)
}

@Dao
interface PurchaseOrderDao {
    @Query("SELECT * FROM purchase_orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<PurchaseOrderEntity>>

    @Query("SELECT * FROM purchase_orders WHERE id = :id")
    suspend fun getOrderById(id: Long): PurchaseOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: PurchaseOrderEntity): Long

    @Update
    suspend fun updateOrder(order: PurchaseOrderEntity)

    @Query("UPDATE purchase_orders SET status = :status, receivedAt = :receivedAt WHERE id = :id")
    suspend fun updateOrderStatus(id: Long, status: String, receivedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM purchase_orders WHERE id = :id")
    suspend fun deleteOrder(id: Long)
}

