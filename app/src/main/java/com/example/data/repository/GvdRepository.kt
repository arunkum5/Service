package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.ComponentInspectionEntity
import com.example.data.local.entities.CustomerReviewEntity
import com.example.data.local.entities.InventoryItemEntity
import com.example.data.local.entities.JobCardEntity
import com.example.data.local.entities.JobCardItemEntity
import com.example.data.local.entities.PurchaseOrderEntity
import com.example.data.local.entities.PurchaseOrderItem
import com.example.data.local.entities.ServiceAppointmentEntity
import com.example.data.local.entities.ServiceReminderEntity
import com.example.data.local.entities.VehicleInventoryEntity
import com.example.data.local.entities.toJsonString
import com.example.data.local.entities.toPurchaseOrderItems
import com.example.model.ComponentStatus
import com.example.model.ItemCategory
import com.example.model.JobCardStatus
import com.example.model.VehicleType
import com.example.util.ParsedInventoryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GvdRepository(private val db: AppDatabase) {

    val allJobCards: Flow<List<JobCardEntity>> = db.jobCardDao().getAllJobCards()
    val allInventory: Flow<List<InventoryItemEntity>> = db.inventoryDao().getAllInventory()
    val lowStockInventory: Flow<List<InventoryItemEntity>> = db.inventoryDao().getLowStockItems()
    val allAppointments: Flow<List<ServiceAppointmentEntity>> = db.serviceDao().getAllAppointments()
    val allShowroomVehicles: Flow<List<VehicleInventoryEntity>> = db.serviceDao().getAllShowroomVehicles()
    val allReminders: Flow<List<ServiceReminderEntity>> = db.reminderDao().getAllReminders()
    val allInspections: Flow<List<ComponentInspectionEntity>> = db.componentInspectionDao().getAllInspections()
    val allReviews: Flow<List<CustomerReviewEntity>> = db.customerReviewDao().getAllReviews()
    val allPurchaseOrders: Flow<List<PurchaseOrderEntity>> = db.purchaseOrderDao().getAllOrders()

    fun getJobCardById(id: Long): Flow<JobCardEntity?> = db.jobCardDao().getJobCardById(id)
    fun getItemsForJobCard(id: Long): Flow<List<JobCardItemEntity>> = db.jobCardDao().getItemsForJobCard(id)
    fun getInspectionsForVehicle(vehNo: String): Flow<List<ComponentInspectionEntity>> =
        db.componentInspectionDao().getInspectionsForVehicle(vehNo)

    suspend fun createJobCard(
        jobCard: JobCardEntity,
        items: List<JobCardItemEntity>
    ): Long = withContext(Dispatchers.IO) {
        val id = db.jobCardDao().insertJobCard(jobCard)
        val itemsWithId = items.map { it.copy(jobCardId = id) }
        db.jobCardDao().insertJobCardItems(itemsWithId)

        // Seed default component inspections for this vehicle if not exists
        val existing = db.componentInspectionDao().getInspectionsForVehicle(jobCard.vehicleNumber).firstOrNull()
        if (existing.isNullOrEmpty()) {
            val defaultComponents = generateDefaultComponentsForVehicle(jobCard.vehicleNumber, id, jobCard.vehicleType)
            db.componentInspectionDao().insertAll(defaultComponents)
        }
        id
    }

    suspend fun updateJobCardStatus(id: Long, status: JobCardStatus) = withContext(Dispatchers.IO) {
        db.jobCardDao().updateStatus(id, status)
    }

    suspend fun markJobCardPaidOnline(id: Long) = withContext(Dispatchers.IO) {
        db.jobCardDao().markAsPaidOnline(id)
    }

    suspend fun updateComponentInspection(inspection: ComponentInspectionEntity) = withContext(Dispatchers.IO) {
        db.componentInspectionDao().updateInspection(inspection)
    }

    suspend fun addInventoryItem(item: InventoryItemEntity) = withContext(Dispatchers.IO) {
        db.inventoryDao().insertItem(item)
    }

    suspend fun adjustInventoryStock(id: Long, amount: Int) = withContext(Dispatchers.IO) {
        db.inventoryDao().adjustStock(id, amount)
    }

    suspend fun importIncomingPartsBatch(
        items: List<ParsedInventoryItem>,
        supplier: String,
        invoiceNo: String
    ): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val currentInventory = db.inventoryDao().getAllInventory().firstOrNull() ?: emptyList()
        val currentBySku = currentInventory.associateBy { it.partNumber.uppercase().trim() }.toMutableMap()

        var updatedCount = 0
        var newCount = 0
        val poItems = mutableListOf<PurchaseOrderItem>()

        for (parsed in items) {
            val sku = parsed.partNumber.uppercase().trim()
            val existing = currentBySku[sku]
            if (existing != null) {
                val updated = existing.copy(
                    stockQuantity = existing.stockQuantity + parsed.quantity,
                    unitPrice = parsed.unitPrice
                )
                db.inventoryDao().updateItem(updated)
                updatedCount++
                poItems.add(
                    PurchaseOrderItem(
                        inventoryId = existing.id,
                        partName = existing.partName,
                        partNumber = existing.partNumber,
                        category = existing.category.name,
                        currentStock = existing.stockQuantity,
                        minThreshold = existing.minThresholdAlert,
                        orderQuantity = parsed.quantity,
                        unitPrice = parsed.unitPrice,
                        lineTotal = parsed.quantity * parsed.unitPrice
                    )
                )
            } else {
                val newEntity = InventoryItemEntity(
                    partName = parsed.partName,
                    partNumber = parsed.partNumber,
                    category = parsed.category,
                    compatibleType = parsed.compatibleType,
                    unitPrice = parsed.unitPrice,
                    stockQuantity = parsed.quantity,
                    minThresholdAlert = parsed.minThreshold,
                    unit = parsed.unit
                )
                val newId = db.inventoryDao().insertItem(newEntity)
                newCount++
                poItems.add(
                    PurchaseOrderItem(
                        inventoryId = newId,
                        partName = parsed.partName,
                        partNumber = parsed.partNumber,
                        category = parsed.category.name,
                        currentStock = 0,
                        minThreshold = parsed.minThreshold,
                        orderQuantity = parsed.quantity,
                        unitPrice = parsed.unitPrice,
                        lineTotal = parsed.quantity * parsed.unitPrice
                    )
                )
            }
        }

        // Record incoming delivery / invoice
        if (poItems.isNotEmpty()) {
            val totalUnits = poItems.sumOf { it.orderQuantity }
            val subtotal = poItems.sumOf { it.lineTotal }
            val gst = subtotal * 0.18
            val grandTotal = subtotal + gst
            val poNumber = if (invoiceNo.isNotBlank()) invoiceNo else "INW-GVD-${System.currentTimeMillis() % 10000}"

            val po = PurchaseOrderEntity(
                poNumber = poNumber,
                supplierName = supplier.ifBlank { "Incoming OEM Parts Wholesale Distributor" },
                supplierAddress = "JC Road Auto Parts Wholesale Market, Bangalore",
                supplierContact = "+91 98450 12345",
                status = "RECEIVED",
                totalItemsCount = poItems.size,
                totalUnitsCount = totalUnits,
                subtotal = subtotal,
                gstAmount = gst,
                grandTotal = grandTotal,
                notes = "Inward delivery imported via Excel parts list ($poNumber). Added directly to Main Inventory.",
                itemsJson = poItems.toJsonString(),
                receivedAt = System.currentTimeMillis()
            )
            db.purchaseOrderDao().insertOrder(po)
        }

        Pair(updatedCount, newCount)
    }

    suspend fun bookAppointment(appointment: ServiceAppointmentEntity): Long = withContext(Dispatchers.IO) {
        db.serviceDao().insertAppointment(appointment)
    }

    suspend fun updateAppointmentStatus(id: Long, status: String) = withContext(Dispatchers.IO) {
        db.serviceDao().updateAppointmentStatus(id, status)
    }

    suspend fun sendReminder(reminderId: Long) = withContext(Dispatchers.IO) {
        db.reminderDao().markAsSent(reminderId)
    }

    suspend fun addServiceReminder(reminder: ServiceReminderEntity) = withContext(Dispatchers.IO) {
        db.reminderDao().insertReminder(reminder)
    }

    suspend fun submitReview(review: CustomerReviewEntity): Long = withContext(Dispatchers.IO) {
        db.customerReviewDao().insertReview(review)
    }

    suspend fun updateAdminResponse(id: Long, response: String) = withContext(Dispatchers.IO) {
        db.customerReviewDao().updateAdminResponse(id, response)
    }

    suspend fun deleteReview(id: Long) = withContext(Dispatchers.IO) {
        db.customerReviewDao().deleteReview(id)
    }

    fun getMasterInventoryCatalog(): List<InventoryItemEntity> {
        return listOf(
            InventoryItemEntity(partName = "AIR FILTER BIG (Bajaj/Universal)", partNumber = "SP-BAJ-AF01", category = ItemCategory.SPARE, compatibleType = VehicleType.TWO_WHEELER, unitPrice = 205.0, stockQuantity = 32, minThresholdAlert = 10),
            InventoryItemEntity(partName = "Front Disc Brake Pads (Ceramic)", partNumber = "SP-BRK-092", category = ItemCategory.SPARE, compatibleType = VehicleType.TWO_WHEELER, unitPrice = 420.0, stockQuantity = 4, minThresholdAlert = 8),
            InventoryItemEntity(partName = "Tata Nexon OEM Brake Pad Set", partNumber = "SP-TAT-BP44", category = ItemCategory.SPARE, compatibleType = VehicleType.FOUR_WHEELER, unitPrice = 2400.0, stockQuantity = 3, minThresholdAlert = 5),
            InventoryItemEntity(partName = "Activated Carbon AC Cabin Filter", partNumber = "SP-AC-CF12", category = ItemCategory.SPARE, compatibleType = VehicleType.FOUR_WHEELER, unitPrice = 800.0, stockQuantity = 18, minThresholdAlert = 6),
            InventoryItemEntity(partName = "Motul 7100 10W50 100% Synthetic 1L", partNumber = "LB-MOT-10W50", category = ItemCategory.LUBE, compatibleType = VehicleType.TWO_WHEELER, unitPrice = 450.0, stockQuantity = 45, minThresholdAlert = 15),
            InventoryItemEntity(partName = "Castrol Magnatec 5W30 Full Synth 3.5L", partNumber = "LB-CAS-5W30", category = ItemCategory.LUBE, compatibleType = VehicleType.FOUR_WHEELER, unitPrice = 1850.0, stockQuantity = 2, minThresholdAlert = 4),
            InventoryItemEntity(partName = "TPU Paint Protection Film (Roll 15m)", partNumber = "DT-PPF-TPU15", category = ItemCategory.DETAILING, compatibleType = VehicleType.FOUR_WHEELER, unitPrice = 18500.0, stockQuantity = 3, minThresholdAlert = 2),
            InventoryItemEntity(partName = "9H Diamond Ceramic Coating Kit (50ml)", partNumber = "DT-CRM-9H", category = ItemCategory.DETAILING, compatibleType = VehicleType.FOUR_WHEELER, unitPrice = 3500.0, stockQuantity = 7, minThresholdAlert = 3),
            InventoryItemEntity(partName = "Chain Lube & Cleaner Combo Pack", partNumber = "LB-CHN-CMB", category = ItemCategory.LUBE, compatibleType = VehicleType.TWO_WHEELER, unitPrice = 380.0, stockQuantity = 22, minThresholdAlert = 6),
            InventoryItemEntity(partName = "NGK Iridium Spark Plug CR8EIX", partNumber = "SP-NGK-CR8", category = ItemCategory.SPARE, compatibleType = VehicleType.TWO_WHEELER, unitPrice = 650.0, stockQuantity = 2, minThresholdAlert = 6),
            InventoryItemEntity(partName = "Motul DOT 4 High Temp Brake Fluid 500ml", partNumber = "LB-DOT-4", category = ItemCategory.LUBE, compatibleType = VehicleType.TWO_WHEELER, unitPrice = 320.0, stockQuantity = 3, minThresholdAlert = 6),
            InventoryItemEntity(partName = "Castrol Radicool Longlife Coolant 1L", partNumber = "LB-COOL-PRE", category = ItemCategory.LUBE, compatibleType = VehicleType.FOUR_WHEELER, unitPrice = 360.0, stockQuantity = 14, minThresholdAlert = 5),
            InventoryItemEntity(partName = "Royal Enfield OEM Oil Filter Classic/Hunter", partNumber = "SP-RE-OC01", category = ItemCategory.SPARE, compatibleType = VehicleType.TWO_WHEELER, unitPrice = 180.0, stockQuantity = 3, minThresholdAlert = 8),
            InventoryItemEntity(partName = "Bosch Halogen Headlamp Bulb H4 12V", partNumber = "SP-BOS-H4", category = ItemCategory.SPARE, compatibleType = VehicleType.TWO_WHEELER, unitPrice = 240.0, stockQuantity = 20, minThresholdAlert = 6),
            InventoryItemEntity(partName = "Amaron Pro 12V 5Ah VRLA Battery", partNumber = "SP-AMR-BAT", category = ItemCategory.SPARE, compatibleType = VehicleType.TWO_WHEELER, unitPrice = 1650.0, stockQuantity = 3, minThresholdAlert = 4),
            InventoryItemEntity(partName = "Rolon Brass O-Ring Heavy Duty Chain 520", partNumber = "SP-CHN-520", category = ItemCategory.SPARE, compatibleType = VehicleType.TWO_WHEELER, unitPrice = 2250.0, stockQuantity = 5, minThresholdAlert = 3),
            InventoryItemEntity(partName = "Michelin Pilot Street 140/70-17 Rear Tire", partNumber = "SP-MIC-TYR", category = ItemCategory.SPARE, compatibleType = VehicleType.TWO_WHEELER, unitPrice = 4100.0, stockQuantity = 2, minThresholdAlert = 3),
            InventoryItemEntity(partName = "Bosch Symphony Dual Windtone Horn Set", partNumber = "SP-BOS-HRN", category = ItemCategory.SPARE, compatibleType = VehicleType.FOUR_WHEELER, unitPrice = 850.0, stockQuantity = 11, minThresholdAlert = 4),
            InventoryItemEntity(partName = "3M Hyper Snow Foam Car Shampoo 5L", partNumber = "DT-FOAM-5L", category = ItemCategory.DETAILING, compatibleType = VehicleType.FOUR_WHEELER, unitPrice = 1200.0, stockQuantity = 8, minThresholdAlert = 3),
            InventoryItemEntity(partName = "600 GSM Edgeless Microfiber Towels (6pk)", partNumber = "DT-MIC-6PK", category = ItemCategory.DETAILING, compatibleType = VehicleType.FOUR_WHEELER, unitPrice = 499.0, stockQuantity = 15, minThresholdAlert = 5),
            InventoryItemEntity(partName = "Automotive Blade Fuse Assortment (60 Pcs)", partNumber = "SP-FUS-SET", category = ItemCategory.SPARE, compatibleType = VehicleType.TWO_WHEELER, unitPrice = 350.0, stockQuantity = 12, minThresholdAlert = 4),
            InventoryItemEntity(partName = "Liqui Moly Pro-Line Engine Flush 500ml", partNumber = "LB-ENG-FLS", category = ItemCategory.LUBE, compatibleType = VehicleType.FOUR_WHEELER, unitPrice = 580.0, stockQuantity = 8, minThresholdAlert = 4),
            InventoryItemEntity(partName = "Motul Fork Oil Expert Medium 10W 1L", partNumber = "LB-FORK-10", category = ItemCategory.LUBE, compatibleType = VehicleType.TWO_WHEELER, unitPrice = 620.0, stockQuantity = 7, minThresholdAlert = 4),
            InventoryItemEntity(partName = "Bosch Clear Advantage Wiper Set 24+16", partNumber = "SP-WIP-BLD", category = ItemCategory.SPARE, compatibleType = VehicleType.FOUR_WHEELER, unitPrice = 790.0, stockQuantity = 14, minThresholdAlert = 5),
            InventoryItemEntity(partName = "Meguiar's Ultimate Ceramic Wax 473ml", partNumber = "DT-MEG-CER", category = ItemCategory.DETAILING, compatibleType = VehicleType.FOUR_WHEELER, unitPrice = 1850.0, stockQuantity = 6, minThresholdAlert = 2),
            InventoryItemEntity(partName = "Exedy OEM Heavy Duty Clutch Plate Set", partNumber = "SP-CLU-PLT", category = ItemCategory.SPARE, compatibleType = VehicleType.TWO_WHEELER, unitPrice = 1450.0, stockQuantity = 4, minThresholdAlert = 3)
        )
    }

    suspend fun addAllMasterInventory(): Int = withContext(Dispatchers.IO) {
        val existing = db.inventoryDao().getAllInventory().firstOrNull() ?: emptyList()
        val existingPartNumbers = existing.map { it.partNumber.trim().uppercase() }.toSet()
        val catalog = getMasterInventoryCatalog()
        val toInsert = catalog.filter { it.partNumber.trim().uppercase() !in existingPartNumbers }
        if (toInsert.isNotEmpty()) {
            db.inventoryDao().insertAll(toInsert)
        }
        toInsert.size
    }

    suspend fun restockAllInventory(amount: Int = 10) = withContext(Dispatchers.IO) {
        val existing = db.inventoryDao().getAllInventory().firstOrNull() ?: emptyList()
        existing.forEach { item ->
            db.inventoryDao().adjustStock(item.id, amount)
        }
    }

    suspend fun autoGeneratePurchaseOrderFromLowStock(
        supplierName: String = "Bangalore OEM Spares Wholesale Hub",
        supplierContact: String = "+91 98450 12345"
    ): PurchaseOrderEntity? = withContext(Dispatchers.IO) {
        val allItems = db.inventoryDao().getAllInventory().firstOrNull() ?: emptyList()
        val candidateItems = allItems.filter { it.stockQuantity <= it.minThresholdAlert }
            .ifEmpty { allItems.sortedBy { it.stockQuantity }.take(4) }

        if (candidateItems.isEmpty()) return@withContext null

        val orderItems = candidateItems.map { item ->
            val suggestedQty = (item.minThresholdAlert * 2 - item.stockQuantity).coerceAtLeast(item.minThresholdAlert).coerceAtLeast(2)
            val wholesalePrice = (item.unitPrice * 0.75).coerceAtLeast(50.0)
            val lineTotal = suggestedQty * wholesalePrice
            PurchaseOrderItem(
                inventoryId = item.id,
                partName = item.partName,
                partNumber = item.partNumber,
                category = item.category.name,
                currentStock = item.stockQuantity,
                minThreshold = item.minThresholdAlert,
                orderQuantity = suggestedQty,
                unitPrice = wholesalePrice,
                lineTotal = lineTotal
            )
        }

        val totalUnits = orderItems.sumOf { it.orderQuantity }
        val subtotal = orderItems.sumOf { it.lineTotal }
        val gst = subtotal * 0.18
        val grandTotal = subtotal + gst

        val poNumber = "PO-GVD-${System.currentTimeMillis() % 100000}"
        val po = PurchaseOrderEntity(
            poNumber = poNumber,
            supplierName = supplierName,
            supplierContact = supplierContact,
            status = "PENDING",
            totalItemsCount = orderItems.size,
            totalUnitsCount = totalUnits,
            subtotal = subtotal,
            gstAmount = gst,
            grandTotal = grandTotal,
            notes = "Auto-generated order based on minimum inventory thresholds. Priority dispatch to Indiranagar workshop.",
            itemsJson = orderItems.toJsonString()
        )
        val id = db.purchaseOrderDao().insertOrder(po)
        po.copy(id = id)
    }

    suspend fun receivePurchaseOrder(orderId: Long): Boolean = withContext(Dispatchers.IO) {
        val order = db.purchaseOrderDao().getOrderById(orderId) ?: return@withContext false
        val items = order.itemsJson.toPurchaseOrderItems()
        for (item in items) {
            if (item.inventoryId > 0 && item.orderQuantity > 0) {
                db.inventoryDao().adjustStock(item.inventoryId, item.orderQuantity)
            }
        }
        db.purchaseOrderDao().updateOrderStatus(orderId, "RECEIVED", System.currentTimeMillis())
        true
    }

    suspend fun savePurchaseOrder(order: PurchaseOrderEntity): Long = withContext(Dispatchers.IO) {
        db.purchaseOrderDao().insertOrder(order)
    }

    suspend fun deletePurchaseOrder(orderId: Long) = withContext(Dispatchers.IO) {
        db.purchaseOrderDao().deleteOrder(orderId)
    }

    fun seedDatabaseIfEmpty(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            val existingCards = db.jobCardDao().getAllJobCards().firstOrNull()
            if (existingCards.isNullOrEmpty()) {
                seedInitialData()
            }
            val existingReviews = db.customerReviewDao().getAllReviews().firstOrNull()
            if (existingReviews.isNullOrEmpty()) {
                seedInitialReviews()
            }
        }
    }

    suspend fun seedInitialReviews() {
        val initialReviews = listOf(
            CustomerReviewEntity(
                customerName = "Shashi G",
                customerMobile = "9845112233",
                vehicleNumber = "KA01MJ9821",
                vehicleModel = "Royal Enfield Classic 350",
                serviceType = "Periodic Lube & Brake Service",
                rating = 5,
                aspectPunctuality = 5,
                aspectCleanliness = 5,
                aspectPricing = 5,
                tags = "Transparent Pricing, WhatsApp Alerts, Genuine Spares",
                comment = "Superb service experience at GVD Auto World Indiranagar! The 360-degree digital inspection report sent to my WhatsApp showed exactly which brake pads needed replacement. Completely transparent billing.",
                jobCardNumber = "JC-2026-0427",
                adminResponse = "Thank you Mr. Shashi! Delighted that our digital 360 inspection provided total clarity. Looking forward to serving your Classic 350 again!",
                adminRespondedAt = System.currentTimeMillis() - 86400000L,
                isVerifiedClient = true,
                createdAt = System.currentTimeMillis() - 172800000L
            ),
            CustomerReviewEntity(
                customerName = "Rahul Menon",
                customerMobile = "9822019942",
                vehicleNumber = "KA03HA4321",
                vehicleModel = "Hyundai Creta SX(O)",
                serviceType = "9H Ceramic Coating & Detailing",
                rating = 5,
                aspectPunctuality = 5,
                aspectCleanliness = 5,
                aspectPricing = 4,
                tags = "Ceramic Coating, Doorstep Pickup, Flawless Shine",
                comment = "Got full body 9H ceramic coating and interior detailing done. The paint depth and water beading are unbelievable! The doorstep pickup from HAL 2nd Stage Bangalore was punctual.",
                jobCardNumber = "JC-2026-0812",
                adminResponse = "Thank you Rahul! Our detailing masters love working on the Creta. Enjoy the unmatched ceramic gloss on Bangalore roads.",
                adminRespondedAt = System.currentTimeMillis() - 43200000L,
                isVerifiedClient = true,
                createdAt = System.currentTimeMillis() - 86400000L
            ),
            CustomerReviewEntity(
                customerName = "Priya Sharma",
                customerMobile = "9766554433",
                vehicleNumber = "KA05EB7712",
                vehicleModel = "Honda Activa 6G",
                serviceType = "General Service & Oil Change",
                rating = 4,
                aspectPunctuality = 4,
                aspectCleanliness = 5,
                aspectPricing = 5,
                tags = "Courteous Staff, Quality Work, Clean Lounge",
                comment = "Very prompt service and clean air-conditioned customer lounge. Staff was courteous and explained the labour invoice line-by-line. Took about 20 mins longer than scheduled because of morning rush, but the bike feels brand new.",
                jobCardNumber = "JC-2026-0904",
                adminResponse = "Thanks for your kind words Priya! We appreciate your patience during our morning rush hours and are thrilled your Activa is riding smoothly.",
                adminRespondedAt = System.currentTimeMillis() - 21600000L,
                isVerifiedClient = true,
                createdAt = System.currentTimeMillis() - 36000000L
            ),
            CustomerReviewEntity(
                customerName = "Anand Kulkarni",
                customerMobile = "9980123456",
                vehicleNumber = "KA04NB5500",
                vehicleModel = "KTM Duke 390",
                serviceType = "Chain Sprocket & Suspension Overhaul",
                rating = 5,
                aspectPunctuality = 5,
                aspectCleanliness = 5,
                aspectPricing = 5,
                tags = "Expert Technicians, Digital Health Report, Fast Delivery",
                comment = "Technician Raju was outstanding. Inspected the chain slack and showed high-res photos on the portal before replacing worn components. You won't find this level of honesty in traditional workshops.",
                jobCardNumber = "JC-2026-0915",
                adminResponse = "",
                adminRespondedAt = 0L,
                isVerifiedClient = true,
                createdAt = System.currentTimeMillis() - 14400000L
            ),
            CustomerReviewEntity(
                customerName = "Vikram Rathore",
                customerMobile = "9844009988",
                vehicleNumber = "KA51MD3322",
                vehicleModel = "Mahindra Thar 4x4",
                serviceType = "Underbody Anti-Rust Coating",
                rating = 5,
                aspectPunctuality = 5,
                aspectCleanliness = 5,
                aspectPricing = 5,
                tags = "Durable Coating, Honest Advice, Professional Bay",
                comment = "Had heavy monsoons in Bangalore so booked underbody rubberized coating and rust protection. Thorough job done, technician even cleaned the wheel arches. Top tier service!",
                jobCardNumber = "JC-2026-0931",
                adminResponse = "",
                adminRespondedAt = 0L,
                isVerifiedClient = true,
                createdAt = System.currentTimeMillis() - 7200000L
            )
        )
        db.customerReviewDao().insertAll(initialReviews)
    }

    private suspend fun seedInitialData() {
        // 1. Initial Job Card 1 (Matching User Screenshot!)
        val jobCard1 = JobCardEntity(
            jobCardNumber = "JC-2026-0427",
            customerName = "Ashay Kohad",
            customerMobile = "8698761486",
            customerEmail = "tightthenut@gmail.com",
            vehicleNumber = "MH12RY1234",
            vehicleType = VehicleType.TWO_WHEELER,
            make = "Bajaj",
            model = "Bajaj Avenger",
            variant = "150 Street",
            odometerKm = 25625,
            fuelLevelPercent = 51,
            accessoriesNotes = "Luggage carrier, Crash guard installed",
            customerVoice = "Chain noise on deceleration, front brake squeal, minor vibration above 60 km/h",
            dentNotes = "Minor scratch on left silencer shield, front fender clear",
            status = JobCardStatus.IN_PROGRESS,
            totalSpares = 205.0,
            totalLabour = 800.0,
            totalLubes = 450.0,
            totalAmount = 1455.0,
            advancePaid = 500.0,
            balanceAmount = 955.0,
            deliveryDateTime = "2026-09-24 17:30",
            isSmsAlertEnabled = true
        )
        val jc1Id = db.jobCardDao().insertJobCard(jobCard1)

        val jc1Items = listOf(
            JobCardItemEntity(jobCardId = jc1Id, category = ItemCategory.SPARE, name = "AIR FILTER BIG", quantity = 1, unitPrice = 205.0, totalAmount = 205.0),
            JobCardItemEntity(jobCardId = jc1Id, category = ItemCategory.LABOUR, name = "SERVICING MAJOR (2W)", quantity = 1, unitPrice = 800.0, totalAmount = 800.0),
            JobCardItemEntity(jobCardId = jc1Id, category = ItemCategory.LUBE, name = "MOTUL 7100 10W50 4T (1L)", quantity = 1, unitPrice = 450.0, totalAmount = 450.0)
        )
        db.jobCardDao().insertJobCardItems(jc1Items)

        // 2. Job Card 2 (4W Premium Detailing & Mechanical Service)
        val jobCard2 = JobCardEntity(
            jobCardNumber = "JC-2026-0812",
            customerName = "Vikram Deshmukh",
            customerMobile = "9822019942",
            customerEmail = "vikram.deshmukh@gmail.com",
            vehicleNumber = "MH12DY7698",
            vehicleType = VehicleType.FOUR_WHEELER,
            make = "Tata",
            model = "Tata Nexon",
            variant = "XZ+ Dark Edition",
            odometerKm = 21400,
            fuelLevelPercent = 65,
            accessoriesNotes = "Dashcam front/rear, 7D Floor mats",
            customerVoice = "Need 9H Ceramic coating maintenance, AC filter replacement, Brake pads inspection",
            dentNotes = "Slight swirl marks on bonnet, right door small stone chip",
            status = JobCardStatus.OPEN,
            totalSpares = 3200.0,
            totalLabour = 7500.0,
            totalLubes = 1800.0,
            totalAmount = 12500.0,
            advancePaid = 3000.0,
            balanceAmount = 9500.0,
            deliveryDateTime = "2026-09-25 18:00",
            isSmsAlertEnabled = true
        )
        val jc2Id = db.jobCardDao().insertJobCard(jobCard2)

        val jc2Items = listOf(
            JobCardItemEntity(jobCardId = jc2Id, category = ItemCategory.DETAILING, name = "9H Ceramic Coating Annual Re-Coat", quantity = 1, unitPrice = 5500.0, totalAmount = 5500.0),
            JobCardItemEntity(jobCardId = jc2Id, category = ItemCategory.SPARE, name = "Front Brake Pad Set (OEM)", quantity = 1, unitPrice = 2400.0, totalAmount = 2400.0),
            JobCardItemEntity(jobCardId = jc2Id, category = ItemCategory.SPARE, name = "Activated Carbon AC Cabin Filter", quantity = 1, unitPrice = 800.0, totalAmount = 800.0),
            JobCardItemEntity(jobCardId = jc2Id, category = ItemCategory.LABOUR, name = "AC Evaporator Foam Deep Clean", quantity = 1, unitPrice = 2000.0, totalAmount = 2000.0),
            JobCardItemEntity(jobCardId = jc2Id, category = ItemCategory.LUBE, name = "DOT 4 High Temp Brake Fluid Flush", quantity = 1, unitPrice = 1800.0, totalAmount = 1800.0)
        )
        db.jobCardDao().insertJobCardItems(jc2Items)

        // Seed 360 Health Inspections for MH12RY1234
        val inspectionsMH12 = generateDefaultComponentsForVehicle("MH12RY1234", jc1Id, VehicleType.TWO_WHEELER)
        db.componentInspectionDao().insertAll(inspectionsMH12)

        // Seed 360 Health Inspections for MH12DY7698
        val inspectionsNexon = generateDefaultComponentsForVehicle("MH12DY7698", jc2Id, VehicleType.FOUR_WHEELER)
        db.componentInspectionDao().insertAll(inspectionsNexon)

        // Seed Master Inventory (Spares, Lubes, Labour & Detailing)
        val inventorySeed = listOf(
            InventoryItemEntity(partName = "AIR FILTER BIG (Bajaj/Universal)", partNumber = "SP-BAJ-AF01", category = ItemCategory.SPARE, compatibleType = VehicleType.TWO_WHEELER, unitPrice = 205.0, stockQuantity = 32, minThresholdAlert = 10),
            InventoryItemEntity(partName = "Front Disc Brake Pads (Ceramic)", partNumber = "SP-BRK-092", category = ItemCategory.SPARE, compatibleType = VehicleType.TWO_WHEELER, unitPrice = 420.0, stockQuantity = 14, minThresholdAlert = 5),
            InventoryItemEntity(partName = "Tata Nexon OEM Brake Pad Set", partNumber = "SP-TAT-BP44", category = ItemCategory.SPARE, compatibleType = VehicleType.FOUR_WHEELER, unitPrice = 2400.0, stockQuantity = 8, minThresholdAlert = 4),
            InventoryItemEntity(partName = "Activated Carbon AC Cabin Filter", partNumber = "SP-AC-CF12", category = ItemCategory.SPARE, compatibleType = VehicleType.FOUR_WHEELER, unitPrice = 800.0, stockQuantity = 18, minThresholdAlert = 5),
            InventoryItemEntity(partName = "Motul 7100 10W50 100% Synthetic 1L", partNumber = "LB-MOT-10W50", category = ItemCategory.LUBE, compatibleType = VehicleType.TWO_WHEELER, unitPrice = 450.0, stockQuantity = 45, minThresholdAlert = 15),
            InventoryItemEntity(partName = "Castrol Magnatec 5W30 Full Synth 3.5L", partNumber = "LB-CAS-5W30", category = ItemCategory.LUBE, compatibleType = VehicleType.FOUR_WHEELER, unitPrice = 1850.0, stockQuantity = 12, minThresholdAlert = 4),
            InventoryItemEntity(partName = "TPU Paint Protection Film (Roll 15m)", partNumber = "DT-PPF-TPU15", category = ItemCategory.DETAILING, compatibleType = VehicleType.FOUR_WHEELER, unitPrice = 18500.0, stockQuantity = 3, minThresholdAlert = 2),
            InventoryItemEntity(partName = "9H Diamond Ceramic Coating Kit (50ml)", partNumber = "DT-CRM-9H", category = ItemCategory.DETAILING, compatibleType = VehicleType.FOUR_WHEELER, unitPrice = 3500.0, stockQuantity = 7, minThresholdAlert = 3),
            InventoryItemEntity(partName = "Chain Lube & Cleaner Combo Pack", partNumber = "LB-CHN-CMB", category = ItemCategory.LUBE, compatibleType = VehicleType.TWO_WHEELER, unitPrice = 380.0, stockQuantity = 22, minThresholdAlert = 6),
            InventoryItemEntity(partName = "NGK Iridium Spark Plug CR8EIX", partNumber = "SP-NGK-CR8", category = ItemCategory.SPARE, compatibleType = VehicleType.TWO_WHEELER, unitPrice = 650.0, stockQuantity = 2, minThresholdAlert = 5) // Low stock alert!
        )
        db.inventoryDao().insertAll(inventorySeed)

        // Seed Appointments
        val appointmentsSeed = listOf(
            ServiceAppointmentEntity(customerName = "Rohan Shinde", customerPhone = "9823456781", vehicleNumber = "MH14JB8899", vehicleType = VehicleType.FOUR_WHEELER, servicePackage = "PPF & Ceramic Detailing", preferredDate = "2026-09-24", preferredSlot = "10:00 AM", isDoorstepPickup = true, customerVoice = "Need self-healing TPU PPF on bumper, bonnet, and mirrors"),
            ServiceAppointmentEntity(customerName = "Amit Patil", customerPhone = "9766554433", vehicleNumber = "MH12KL3321", vehicleType = VehicleType.TWO_WHEELER, servicePackage = "Bike Service & Chain Overhaul", preferredDate = "2026-09-24", preferredSlot = "02:00 PM", isDoorstepPickup = false, customerVoice = "General 10,000 km periodic service")
        )
        appointmentsSeed.forEach { db.serviceDao().insertAppointment(it) }

        // Seed Showroom Vehicles
        val vehiclesSeed = listOf(
            VehicleInventoryEntity(title = "Royal Enfield Hunter 350 Dapper Ash", vehicleType = VehicleType.TWO_WHEELER, brand = "Royal Enfield", model = "Hunter 350", year = 2024, kmDriven = 4800, fuelType = "Petrol", price = 158000.0, emiStartingAt = 3999.0, specsSummary = "349cc Single Cylinder, Dual-channel ABS, GVD 50-Point Certified, Ceramic Coated"),
            VehicleInventoryEntity(title = "KTM Duke 390 Gen 3 Electronic Orange", vehicleType = VehicleType.TWO_WHEELER, brand = "KTM", model = "Duke 390", year = 2024, kmDriven = 3200, fuelType = "Petrol", price = 285000.0, emiStartingAt = 6499.0, specsSummary = "399cc Liquid Cooled, Cornering ABS, Quickshifter+, 1st Owner, Showroom Condition"),
            VehicleInventoryEntity(title = "Tata Nexon XZ+ (S) Dark Edition", vehicleType = VehicleType.FOUR_WHEELER, brand = "Tata", model = "Nexon", year = 2023, kmDriven = 19200, fuelType = "Diesel", price = 1145000.0, emiStartingAt = 18450.0, specsSummary = "1.5L Revotorq Diesel, Sunroof, Harman Sound, Full Body TPU PPF Installed, 5-Star Safety"),
            VehicleInventoryEntity(title = "Hyundai Creta SX (O) Turbo Knight", vehicleType = VehicleType.FOUR_WHEELER, brand = "Hyundai", model = "Creta", year = 2024, kmDriven = 11000, fuelType = "Petrol", price = 1620000.0, emiStartingAt = 24900.0, specsSummary = "1.5L Turbo GDi 7-speed DCT, Panoramic Sunroof, ADAS Level 2, GVD Auto Warranty"),
            VehicleInventoryEntity(title = "Yamaha Aerox 155 MotoGP Edition", vehicleType = VehicleType.TWO_WHEELER, brand = "Yamaha", model = "Aerox 155", year = 2024, kmDriven = 2600, fuelType = "Petrol", price = 129000.0, emiStartingAt = 3450.0, specsSummary = "155cc VVA Liquid Cooled, Traction Control, Keyless Ignition, Pristine Condition")
        )
        db.serviceDao().insertVehicles(vehiclesSeed)

        // Seed Reminders
        val remindersSeed = listOf(
            ServiceReminderEntity(vehicleNumber = "MH12RY1234", customerName = "Ashay Kohad", customerMobile = "8698761486", reminderType = "Periodic Service", dueDate = "2026-10-15", messageText = "Dear Ashay, your Bajaj Avenger (MH12RY1234) is due for periodic lube service in 20 days. Book online with GVD Auto World to avail 10% discount."),
            ServiceReminderEntity(vehicleNumber = "MH12DY7698", customerName = "Vikram Deshmukh", customerMobile = "9822019942", reminderType = "Ceramic Coating Booster", dueDate = "2026-10-01", messageText = "Hi Vikram, your Tata Nexon's 9H Ceramic hydrophobic booster is due on 1st Oct. Keeps gloss and swirl-protection intact."),
            ServiceReminderEntity(vehicleNumber = "MH14EP4521", customerName = "Sunil Gaikwad", customerMobile = "9422001122", reminderType = "Insurance Policy Renewal", dueDate = "2026-09-28", messageText = "Alert: GVD Auto World insurance partner reminder — Comprehensive Zero-Dep policy for MH14EP4521 expires on 28th Sep. Renew with 1-click.")
        )
        db.reminderDao().insertAll(remindersSeed)
    }

    private fun generateDefaultComponentsForVehicle(
        vehNo: String,
        jobCardId: Long,
        vehicleType: VehicleType
    ): List<ComponentInspectionEntity> {
        val isBike = vehicleType == VehicleType.TWO_WHEELER
        return listOf(
            ComponentInspectionEntity(
                vehicleNumber = vehNo,
                jobCardId = jobCardId,
                componentKey = "ENGINE_OIL",
                componentName = if (isBike) "Engine Oil & Sump Filter" else "Engine Oil & Synthetic Filter",
                category = "Fluid & Lubrication",
                angle = "ENGINE_BAY",
                status = ComponentStatus.SERVICED,
                technicianNotes = "Flushed with engine flush. Refilled high-grade full synthetic Motul/Castrol oil. Viscosity optimal.",
                worksTillInfo = "Works till next service (5,000 km / 6 months)",
                recommendedAction = "Check level periodically on level window / dipstick",
                replacementCost = 450.0,
                lastServicedDate = "2026-09-23"
            ),
            ComponentInspectionEntity(
                vehicleNumber = vehNo,
                jobCardId = jobCardId,
                componentKey = "BRAKE_PADS",
                componentName = "Front & Rear Brakes (Pads & Fluid)",
                category = "Braking & Safety",
                angle = "SIDE_LEFT",
                status = ComponentStatus.NEED_REPLACE,
                technicianNotes = "Front pads worn down to 1.8mm (minimum limit 2.0mm). Rotor has minor score lines. Immediate replacement recommended for highway braking.",
                worksTillInfo = "Replace immediately (Safe for ~200 km city only)",
                recommendedAction = "Replace with Ceramic Disc Pads & bleed with DOT 4 fluid",
                replacementCost = if (isBike) 420.0 else 2400.0,
                lastServicedDate = "2026-03-10"
            ),
            ComponentInspectionEntity(
                vehicleNumber = vehNo,
                jobCardId = jobCardId,
                componentKey = "TIRES",
                componentName = "Tires, Alignment & Pressure",
                category = "Wheels & Suspension",
                angle = "FRONT",
                status = ComponentStatus.GOOD,
                technicianNotes = "Tread depth 5.2mm across all grooves. Pressure set to 32 PSI cold. No punctures or sidewall bulges.",
                worksTillInfo = "Works till next service (15,000 km remaining)",
                recommendedAction = "Rotate tires after 8,000 km",
                replacementCost = 0.0,
                lastServicedDate = "2026-09-23"
            ),
            ComponentInspectionEntity(
                vehicleNumber = vehNo,
                jobCardId = jobCardId,
                componentKey = "BATTERY",
                componentName = "Battery & Charging System",
                category = "Electrical",
                angle = "ENGINE_BAY",
                status = ComponentStatus.GOOD,
                technicianNotes = "Resting voltage 12.6V, Cranking voltage 10.4V. Alternator charge rate 14.2V. Terminals cleaned & petroleum jelly applied.",
                worksTillInfo = "Health 92%. Works till next service (12+ months)",
                recommendedAction = "Keep terminals clean",
                replacementCost = 0.0,
                lastServicedDate = "2026-09-23"
            ),
            ComponentInspectionEntity(
                vehicleNumber = vehNo,
                jobCardId = jobCardId,
                componentKey = "BODY_COATING",
                componentName = if (isBike) "Ceramic Glaze & Tank PPF" else "Paint Protection Film (PPF) & Ceramic Coat",
                category = "Exterior Detailing",
                angle = "SIDE_RIGHT",
                status = ComponentStatus.SERVICED,
                technicianNotes = "Hydrophobic ceramic top coat refreshed. Minor swirl marks buffed out with DA polisher. High gloss finish.",
                worksTillInfo = "High water beading active. Works till next booster (6 months)",
                recommendedAction = "Use pH-neutral wash shampoo only",
                replacementCost = 1200.0,
                lastServicedDate = "2026-09-23"
            ),
            ComponentInspectionEntity(
                vehicleNumber = vehNo,
                jobCardId = jobCardId,
                componentKey = "CABIN_AC",
                componentName = if (isBike) "Air Intake & Throttle Body" else "AC Evaporator & Cabin Filter",
                category = if (isBike) "Fuel & Air Delivery" else "Climate & Air Quality",
                angle = "INTERIOR",
                status = ComponentStatus.SERVICED,
                technicianNotes = if (isBike) "Throttle body de-carbonized, idle RPM calibrated to 1400." else "AC vent disinfectant fogging completed. Pollen filter replaced. Vent output 7°C.",
                worksTillInfo = "Works till next service (10,000 km)",
                recommendedAction = "Replace pollen filter annually",
                replacementCost = 800.0,
                lastServicedDate = "2026-09-23"
            ),
            ComponentInspectionEntity(
                vehicleNumber = vehNo,
                jobCardId = jobCardId,
                componentKey = "SUSPENSION",
                componentName = "Shock Absorbers & Bushings",
                category = "Suspension & Handling",
                angle = "UNDERBODY",
                status = ComponentStatus.GOOD,
                technicianNotes = "No oil weeping on front fork seals / rear dampers. Rubber bushings pliable with zero excessive play.",
                worksTillInfo = "Works till next service",
                recommendedAction = "Inspect during next monsoon service",
                replacementCost = 0.0,
                lastServicedDate = "2026-09-23"
            ),
            ComponentInspectionEntity(
                vehicleNumber = vehNo,
                jobCardId = jobCardId,
                componentKey = "EXHAUST",
                componentName = "Exhaust Pipe, Catalytic Converter & O2 Sensor",
                category = "Exhaust & Emissions",
                angle = "REAR",
                status = ComponentStatus.GOOD,
                technicianNotes = "Emission levels within BS6 norms. O2 sensor resistance in spec. Heat shield bolts torqued.",
                worksTillInfo = "Works till next service",
                recommendedAction = "PUC emission certificate valid till 2027",
                replacementCost = 0.0,
                lastServicedDate = "2026-09-23"
            )
        )
    }
}
