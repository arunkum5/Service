package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.ComponentInspectionDao
import com.example.data.local.dao.CustomerReviewDao
import com.example.data.local.dao.InventoryDao
import com.example.data.local.dao.JobCardDao
import com.example.data.local.dao.PurchaseOrderDao
import com.example.data.local.dao.ReminderDao
import com.example.data.local.dao.ServiceDao
import com.example.data.local.entities.ComponentInspectionEntity
import com.example.data.local.entities.CustomerReviewEntity
import com.example.data.local.entities.InventoryItemEntity
import com.example.data.local.entities.JobCardEntity
import com.example.data.local.entities.JobCardItemEntity
import com.example.data.local.entities.PurchaseOrderEntity
import com.example.data.local.entities.ServiceAppointmentEntity
import com.example.data.local.entities.ServiceReminderEntity
import com.example.data.local.entities.VehicleInventoryEntity

@Database(
    entities = [
        JobCardEntity::class,
        JobCardItemEntity::class,
        ComponentInspectionEntity::class,
        InventoryItemEntity::class,
        ServiceAppointmentEntity::class,
        VehicleInventoryEntity::class,
        ServiceReminderEntity::class,
        CustomerReviewEntity::class,
        PurchaseOrderEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun jobCardDao(): JobCardDao
    abstract fun componentInspectionDao(): ComponentInspectionDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun serviceDao(): ServiceDao
    abstract fun reminderDao(): ReminderDao
    abstract fun customerReviewDao(): CustomerReviewDao
    abstract fun purchaseOrderDao(): PurchaseOrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gvd_autoworld.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
