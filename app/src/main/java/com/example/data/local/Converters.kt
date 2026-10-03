package com.example.data.local

import androidx.room.TypeConverter
import com.example.model.ComponentStatus
import com.example.model.ItemCategory
import com.example.model.JobCardStatus
import com.example.model.VehicleType

class Converters {
    @TypeConverter
    fun fromVehicleType(value: VehicleType?): String = value?.name ?: VehicleType.TWO_WHEELER.name

    @TypeConverter
    fun toVehicleType(value: String?): VehicleType =
        try { VehicleType.valueOf(value ?: VehicleType.TWO_WHEELER.name) } catch (e: Exception) { VehicleType.TWO_WHEELER }

    @TypeConverter
    fun fromComponentStatus(value: ComponentStatus?): String = value?.name ?: ComponentStatus.GOOD.name

    @TypeConverter
    fun toComponentStatus(value: String?): ComponentStatus =
        try { ComponentStatus.valueOf(value ?: ComponentStatus.GOOD.name) } catch (e: Exception) { ComponentStatus.GOOD }

    @TypeConverter
    fun fromJobCardStatus(value: JobCardStatus?): String = value?.name ?: JobCardStatus.OPEN.name

    @TypeConverter
    fun toJobCardStatus(value: String?): JobCardStatus =
        try { JobCardStatus.valueOf(value ?: JobCardStatus.OPEN.name) } catch (e: Exception) { JobCardStatus.OPEN }

    @TypeConverter
    fun fromItemCategory(value: ItemCategory?): String = value?.name ?: ItemCategory.SPARE.name

    @TypeConverter
    fun toItemCategory(value: String?): ItemCategory =
        try { ItemCategory.valueOf(value ?: ItemCategory.SPARE.name) } catch (e: Exception) { ItemCategory.SPARE }
}
