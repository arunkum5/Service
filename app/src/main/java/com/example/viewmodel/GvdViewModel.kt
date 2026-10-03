package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.data.repository.GvdRepository
import com.example.model.ComponentStatus
import com.example.model.ItemCategory
import com.example.model.JobCardStatus
import com.example.model.UserRole
import com.example.model.VehicleType
import com.example.model.VehicleViewAngle
import com.example.model.DentPhotoItem
import com.example.model.getDefaultDentInspectionAngles
import com.example.model.getDemoDentInspectionPhotos
import com.example.model.toDentPhotosJson
import com.example.model.toDentPhotoItems
import com.example.util.ExcelInventoryParser
import com.example.util.ParsedInventoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CreateJobCardDraft(
    val vehicleNumber: String = "",
    val vehicleType: VehicleType = VehicleType.TWO_WHEELER,
    val make: String = "",
    val model: String = "",
    val variant: String = "",
    val customerName: String = "",
    val customerMobile: String = "",
    val customerEmail: String = "",
    val odometerKm: Int = 0,
    val fuelLevelPercent: Int = 50,
    val accessories: String = "",
    val customerVoice: String = "",
    val dentNotes: String = "",
    val dentPhotos: List<DentPhotoItem> = getDefaultDentInspectionAngles(),
    val items: List<JobCardItemEntity> = emptyList(),
    val advancePaid: Double = 0.0,
    val deliveryDateTime: String = "",
    val isSmsAlertEnabled: Boolean = true
)

class GvdViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GvdRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = GvdRepository(db)
        repository.seedDatabaseIfEmpty(viewModelScope)
    }

    // Role state
    private val _currentRole = MutableStateFlow(UserRole.CUSTOMER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    // Customer selected vehicle for health report & tracking
    private val _selectedVehicleNumber = MutableStateFlow("MH12RY1234")
    val selectedVehicleNumber: StateFlow<String> = _selectedVehicleNumber.asStateFlow()

    // 360 View Angle
    private val _currentViewAngle = MutableStateFlow(VehicleViewAngle.FRONT)
    val currentViewAngle: StateFlow<VehicleViewAngle> = _currentViewAngle.asStateFlow()

    // Selected component hotspot for inspection popup
    private val _selectedComponent = MutableStateFlow<ComponentInspectionEntity?>(null)
    val selectedComponent: StateFlow<ComponentInspectionEntity?> = _selectedComponent.asStateFlow()

    // Payment Dialog state
    private val _showPaymentDialog = MutableStateFlow(false)
    val showPaymentDialog: StateFlow<Boolean> = _showPaymentDialog.asStateFlow()

    private val _paymentTargetJobCard = MutableStateFlow<JobCardEntity?>(null)
    val paymentTargetJobCard: StateFlow<JobCardEntity?> = _paymentTargetJobCard.asStateFlow()

    // Create Job Card Draft
    private val _jobCardDraft = MutableStateFlow(CreateJobCardDraft())
    val jobCardDraft: StateFlow<CreateJobCardDraft> = _jobCardDraft.asStateFlow()

    // Active screen navigation
    private val _currentScreen = MutableStateFlow("HOME")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _activeJobCardDetailId = MutableStateFlow<Long?>(null)
    val activeJobCardDetailId: StateFlow<Long?> = _activeJobCardDetailId.asStateFlow()

    // Repository Flows
    val allJobCards: StateFlow<List<JobCardEntity>> = repository.allJobCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInventory: StateFlow<List<InventoryItemEntity>> = repository.allInventory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockInventory: StateFlow<List<InventoryItemEntity>> = repository.lowStockInventory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAppointments: StateFlow<List<ServiceAppointmentEntity>> = repository.allAppointments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allShowroomVehicles: StateFlow<List<VehicleInventoryEntity>> = repository.allShowroomVehicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReminders: StateFlow<List<ServiceReminderEntity>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInspections: StateFlow<List<ComponentInspectionEntity>> = repository.allInspections
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReviews: StateFlow<List<CustomerReviewEntity>> = repository.allReviews
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPurchaseOrders: StateFlow<List<PurchaseOrderEntity>> = repository.allPurchaseOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // In-app Push Notification stream for customer
    private val _customerNotifications = MutableStateFlow<List<com.example.util.CustomerNotification>>(
        listOf(
            com.example.util.CustomerNotification(
                title = "Job Card JC-8921 Created",
                message = "Vehicle MH12RY1234 checked in for Periodic Service at GVD Auto Bangalore.",
                type = "STATUS_UPDATE",
                vehicleNumber = "MH12RY1234"
            ),
            com.example.util.CustomerNotification(
                title = "Front Disc Brakes Serviced",
                message = "Technician Raju attached photo: Calipers cleaned and brake pads adjusted.",
                type = "PHOTO_UPLOAD",
                vehicleNumber = "MH12RY1234",
                photoUrl = "https://images.unsplash.com/photo-1486006920555-c77dce18193b?w=600"
            )
        )
    )
    val customerNotifications: StateFlow<List<com.example.util.CustomerNotification>> = _customerNotifications.asStateFlow()

    // Technician Daily Labour (STRICTLY LABOUR ONLY - No admin financial ledger!)
    val technicianDailyLabour: StateFlow<Double> = allJobCards.combine(_selectedVehicleNumber) { jobs, _ ->
        // Technician only sees the labour charge he has generated today
        val todayLabourSum = jobs.sumOf { it.totalLabour }
        if (todayLabourSum > 0) todayLabourSum else 2450.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2450.0)

    // Filtered inspections for currently selected customer vehicle
    val currentVehicleInspections: StateFlow<List<ComponentInspectionEntity>> = combine(
        repository.allInspections,
        _selectedVehicleNumber
    ) { inspections, vehNo ->
        val filtered = inspections.filter { it.vehicleNumber.equals(vehNo, ignoreCase = true) }
        if (filtered.isNotEmpty()) filtered else inspections
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun switchRole(role: UserRole) {
        _currentRole.value = role
        _currentScreen.value = "HOME"
    }

    fun navigateTo(screen: String, jobCardId: Long? = null) {
        _currentScreen.value = screen
        _activeJobCardDetailId.value = jobCardId
    }

    fun setSelectedVehicle(vehNo: String) {
        _selectedVehicleNumber.value = vehNo
    }

    fun setViewAngle(angle: VehicleViewAngle) {
        _currentViewAngle.value = angle
    }

    fun rotateAngleNext() {
        val angles = VehicleViewAngle.values()
        val nextIndex = (angles.indexOf(_currentViewAngle.value) + 1) % angles.size
        _currentViewAngle.value = angles[nextIndex]
    }

    fun rotateAnglePrev() {
        val angles = VehicleViewAngle.values()
        val curIndex = angles.indexOf(_currentViewAngle.value)
        val prevIndex = if (curIndex - 1 < 0) angles.size - 1 else curIndex - 1
        _currentViewAngle.value = angles[prevIndex]
    }

    fun selectComponent(component: ComponentInspectionEntity?) {
        _selectedComponent.value = component
    }

    // Technician component status updater (3 options: NEED_REPLACE, GOOD, SERVICED)
    fun updateComponentStatus(
        context: Context,
        inspection: ComponentInspectionEntity,
        newStatus: ComponentStatus,
        technicianNotes: String,
        worksTill: String,
        photoUrl: String = ""
    ) {
        viewModelScope.launch {
            val updated = inspection.copy(
                status = newStatus,
                technicianNotes = technicianNotes,
                worksTillInfo = worksTill,
                workPhotoUrl = if (photoUrl.isNotBlank()) photoUrl else inspection.workPhotoUrl,
                updatedTimestamp = System.currentTimeMillis()
            )
            repository.updateComponentInspection(updated)
            if (_selectedComponent.value?.id == inspection.id) {
                _selectedComponent.value = updated
            }

            // Real-time Push Notification to Customer
            val notifTitle = "Component Status: ${inspection.componentName}"
            val notifMsg = "Technician marked as ${newStatus.label}. Note: $technicianNotes ($worksTill)"
            com.example.util.NotificationHelper.postServiceUpdateNotification(
                context = context,
                title = notifTitle,
                message = notifMsg,
                vehicleNo = inspection.vehicleNumber
            )

            // Add to in-app stream
            val currentList = _customerNotifications.value.toMutableList()
            currentList.add(
                0,
                com.example.util.CustomerNotification(
                    title = notifTitle,
                    message = notifMsg,
                    type = if (photoUrl.isNotBlank()) "PHOTO_UPLOAD" else "COMPONENT_HEALTH",
                    vehicleNumber = inspection.vehicleNumber,
                    photoUrl = if (photoUrl.isNotBlank()) photoUrl else null
                )
            )
            _customerNotifications.value = currentList
        }
    }

    // Job Card Creation Actions
    fun updateDraft(update: (CreateJobCardDraft) -> CreateJobCardDraft) {
        _jobCardDraft.value = update(_jobCardDraft.value)
    }

    fun addDraftItem(item: JobCardItemEntity) {
        val currentItems = _jobCardDraft.value.items.toMutableList()
        currentItems.add(item)
        _jobCardDraft.value = _jobCardDraft.value.copy(items = currentItems)
    }

    fun removeDraftItem(index: Int) {
        val currentItems = _jobCardDraft.value.items.toMutableList()
        if (index in currentItems.indices) {
            currentItems.removeAt(index)
            _jobCardDraft.value = _jobCardDraft.value.copy(items = currentItems)
        }
    }

    fun submitJobCard(context: Context, onSuccess: (Long) -> Unit) {
        val draft = _jobCardDraft.value
        if (draft.vehicleNumber.isBlank() || draft.customerName.isBlank() || draft.customerMobile.isBlank()) {
            Toast.makeText(context, "Please enter vehicle number, customer name and mobile", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            val totalSpares = draft.items.filter { it.category == ItemCategory.SPARE }.sumOf { it.totalAmount }
            val totalLabour = draft.items.filter { it.category == ItemCategory.LABOUR }.sumOf { it.totalAmount }
            val totalLubes = draft.items.filter { it.category == ItemCategory.LUBE || it.category == ItemCategory.DETAILING }.sumOf { it.totalAmount }
            val grandTotal = totalSpares + totalLabour + totalLubes
            val balance = (grandTotal - draft.advancePaid).coerceAtLeast(0.0)

            val jcNumber = "JC-${System.currentTimeMillis() % 10000}"

            val entity = JobCardEntity(
                jobCardNumber = jcNumber,
                customerName = draft.customerName,
                customerMobile = draft.customerMobile,
                customerEmail = draft.customerEmail,
                vehicleNumber = draft.vehicleNumber.uppercase().trim(),
                vehicleType = draft.vehicleType,
                make = draft.make.ifBlank { "Vehicle" },
                model = draft.model.ifBlank { "Model" },
                variant = draft.variant,
                odometerKm = draft.odometerKm,
                fuelLevelPercent = draft.fuelLevelPercent,
                accessoriesNotes = draft.accessories,
                customerVoice = draft.customerVoice,
                dentNotes = if (draft.dentNotes.isNotBlank()) draft.dentNotes else {
                    val marked = draft.dentPhotos.filter { it.hasDent || it.severity != "NO_DENT" }
                    if (marked.isNotEmpty()) marked.joinToString("; ") { "${it.title}: ${it.severityLabel} (${it.notes})" }
                    else "6-Angle Photo Inspection: All panels clean & verified"
                },
                dentPhotosJson = draft.dentPhotos.toDentPhotosJson(),
                status = JobCardStatus.OPEN,
                totalSpares = totalSpares,
                totalLabour = totalLabour,
                totalLubes = totalLubes,
                totalAmount = grandTotal,
                advancePaid = draft.advancePaid,
                balanceAmount = balance,
                deliveryDateTime = draft.deliveryDateTime.ifBlank { "Next Day 17:00" },
                isSmsAlertEnabled = draft.isSmsAlertEnabled
            )

            val id = repository.createJobCard(entity, draft.items)
            _jobCardDraft.value = CreateJobCardDraft() // reset

            // Automatically open WhatsApp / SMS if enabled
            if (entity.isSmsAlertEnabled) {
                sendJobCardWhatsAppNotification(context, entity)
            }

            Toast.makeText(context, "Job Card $jcNumber created successfully!", Toast.LENGTH_LONG).show()
            onSuccess(id)
        }
    }

    // 6 Dent Inspection Photos helpers
    fun updateDentPhoto(
        angleIndex: Int,
        photoUri: String,
        hasDent: Boolean,
        severity: String,
        notes: String
    ) {
        val currentPhotos = _jobCardDraft.value.dentPhotos.toMutableList()
        if (angleIndex in currentPhotos.indices) {
            currentPhotos[angleIndex] = currentPhotos[angleIndex].copy(
                photoUri = photoUri,
                hasDent = hasDent,
                severity = severity,
                notes = notes
            )
            _jobCardDraft.value = _jobCardDraft.value.copy(dentPhotos = currentPhotos)
        }
    }

    fun populateDemoDentPhotos() {
        _jobCardDraft.value = _jobCardDraft.value.copy(
            dentPhotos = getDemoDentInspectionPhotos(),
            dentNotes = "Minor scratch on front bumper; 1.5-inch parking dent on rear left door; surface scuff on bumper corner."
        )
    }

    fun clearDentPhotos() {
        _jobCardDraft.value = _jobCardDraft.value.copy(
            dentPhotos = getDefaultDentInspectionAngles()
        )
    }

    // Main Inventory Excel / Delivery List Import for Admin
    fun importExcelPartsBatch(
        context: Context,
        items: List<ParsedInventoryItem>,
        supplier: String,
        invoiceNo: String,
        onComplete: (Int, Int) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            if (items.isEmpty()) {
                Toast.makeText(context, "No valid items found in spreadsheet", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val (updated, newCount) = repository.importIncomingPartsBatch(items, supplier, invoiceNo)
            val totalUnits = items.sumOf { it.quantity }

            Toast.makeText(
                context,
                "Imported ${items.size} parts ($totalUnits units)! $updated updated, $newCount new added to stock.",
                Toast.LENGTH_LONG
            ).show()

            onComplete(updated, newCount)
        }
    }

    fun prefillDraftWithVehicleNumber(vehicleNumber: String) {
        _jobCardDraft.value = CreateJobCardDraft(
            vehicleNumber = vehicleNumber.uppercase().trim()
        )
    }

    /**
     * Resolves a scanned vehicle QR string to a JobCardEntity in the local database.
     */
    fun findJobCardByQr(rawScan: String): JobCardEntity? {
        val clean = rawScan.trim()
        if (clean.isBlank()) return null

        val parsed = com.example.util.QrCodeUtils.parseVehicleQrPayload(clean)
        val list = allJobCards.value

        // 1. By ID if present
        parsed.jobCardId?.let { id ->
            val byId = list.firstOrNull { it.id == id }
            if (byId != null) return byId
        }

        // 2. By Job Card Number (e.g. JC-8921)
        parsed.jobCardNumber?.let { jcNum ->
            val byNum = list.firstOrNull { it.jobCardNumber.equals(jcNum, ignoreCase = true) }
            if (byNum != null) return byNum
        }

        // 3. By Vehicle Number (normalized: ignore hyphens, spaces, and casing)
        val targetVeh = parsed.vehicleNumber ?: clean
        val normTarget = targetVeh.replace("-", "").replace(" ", "").uppercase()

        val byVeh = list.firstOrNull { job ->
            job.vehicleNumber.replace("-", "").replace(" ", "").uppercase() == normTarget
        }
        if (byVeh != null) return byVeh

        // 4. Fallback: match raw clean string directly against jobCardNumber or vehicleNumber
        return list.firstOrNull { job ->
            job.jobCardNumber.equals(clean, ignoreCase = true) ||
            job.vehicleNumber.equals(clean, ignoreCase = true)
        }
    }

    fun updateJobCardStatus(context: Context, id: Long, status: JobCardStatus) {
        viewModelScope.launch {
            repository.updateJobCardStatus(id, status)

            // Trigger real-time push notification
            val job = allJobCards.value.firstOrNull { it.id == id }
            val vehNo = job?.vehicleNumber ?: "Your Vehicle"
            val title = "Service Status: ${status.label}"
            val msg = "Stage updated to ${status.label} for $vehNo at GVD Auto World Bangalore."

            com.example.util.NotificationHelper.postServiceUpdateNotification(
                context = context,
                title = title,
                message = msg,
                vehicleNo = vehNo
            )

            val currentList = _customerNotifications.value.toMutableList()
            currentList.add(
                0,
                com.example.util.CustomerNotification(
                    title = title,
                    message = msg,
                    type = "STATUS_UPDATE",
                    vehicleNumber = vehNo
                )
            )
            _customerNotifications.value = currentList
        }
    }

    // Direct Service Billing / Payment Gateway
    fun openPaymentDialog(jobCard: JobCardEntity) {
        _paymentTargetJobCard.value = jobCard
        _showPaymentDialog.value = true
    }

    fun dismissPaymentDialog() {
        _showPaymentDialog.value = false
        _paymentTargetJobCard.value = null
    }

    fun completeOnlinePayment(context: Context, jobCardId: Long, paymentMethod: String) {
        viewModelScope.launch {
            repository.markJobCardPaidOnline(jobCardId)
            _showPaymentDialog.value = false
            Toast.makeText(context, "Payment received via $paymentMethod! Invoice generated.", Toast.LENGTH_LONG).show()
        }
    }

    // Inventory adjustments
    fun addInventoryItem(item: InventoryItemEntity, context: Context) {
        viewModelScope.launch {
            repository.addInventoryItem(item)
            Toast.makeText(context, "Item added to inventory!", Toast.LENGTH_SHORT).show()
        }
    }

    fun adjustStock(id: Long, amount: Int, context: Context) {
        viewModelScope.launch {
            repository.adjustInventoryStock(id, amount)
            Toast.makeText(context, "Stock updated!", Toast.LENGTH_SHORT).show()
        }
    }

    // Batch Add All Inventory & Restock
    fun addAllMasterInventory(context: Context) {
        viewModelScope.launch {
            val addedCount = repository.addAllMasterInventory()
            if (addedCount > 0) {
                Toast.makeText(context, "Added $addedCount master workshop items to inventory!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "All master catalog items already present in inventory!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun restockAllInventory(amount: Int = 10, context: Context) {
        viewModelScope.launch {
            repository.restockAllInventory(amount)
            Toast.makeText(context, "Restocked all inventory items by +$amount units!", Toast.LENGTH_SHORT).show()
        }
    }

    // Auto-Generate Purchase Order Form from Low / Minimum Inventory
    fun autoGeneratePurchaseOrder(
        supplierName: String = "Bangalore OEM Spares Wholesale Hub",
        supplierContact: String = "+91 98450 12345",
        context: Context,
        onGenerated: (PurchaseOrderEntity) -> Unit
    ) {
        viewModelScope.launch {
            val po = repository.autoGeneratePurchaseOrderFromLowStock(supplierName, supplierContact)
            if (po != null) {
                Toast.makeText(context, "Order Form ${po.poNumber} generated for ${po.totalItemsCount} low-stock items!", Toast.LENGTH_SHORT).show()
                onGenerated(po)
            } else {
                Toast.makeText(context, "All inventory items are currently above minimum threshold!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun savePurchaseOrder(order: PurchaseOrderEntity, context: Context) {
        viewModelScope.launch {
            repository.savePurchaseOrder(order)
            Toast.makeText(context, "Purchase Order ${order.poNumber} saved!", Toast.LENGTH_SHORT).show()
        }
    }

    fun receivePurchaseOrder(orderId: Long, context: Context) {
        viewModelScope.launch {
            val success = repository.receivePurchaseOrder(orderId)
            if (success) {
                Toast.makeText(context, "Stock received! Inventory automatically updated.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Could not fulfill order.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deletePurchaseOrder(orderId: Long, context: Context) {
        viewModelScope.launch {
            repository.deletePurchaseOrder(orderId)
            Toast.makeText(context, "Purchase Order deleted.", Toast.LENGTH_SHORT).show()
        }
    }

    fun sharePurchaseOrder(context: Context, order: PurchaseOrderEntity, viaWhatsApp: Boolean = true) {
        val items = order.itemsJson.toPurchaseOrderItems()
        val sb = StringBuilder()
        sb.append("📋 *GVD AUTO WORLD BANGALORE*\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("📦 *OFFICIAL PURCHASE ORDER: ${order.poNumber}*\n")
        sb.append("📅 *Status:* ${order.status}\n")
        sb.append("🏢 *Supplier:* ${order.supplierName}\n")
        sb.append("📍 *Delivery To:* GVD Auto World Workshop, Vibgyor High School Road, Kundalahalli, Bengaluru 560037 (Maps: https://maps.app.goo.gl/xgdEGHBKRth1TUrs7)\n\n")
        sb.append("*REPLENISHMENT ITEMS (Based on Min Stock):*\n")
        items.forEachIndexed { idx, it ->
            sb.append("${idx + 1}. *${it.partName}* [${it.partNumber}]\n")
            sb.append("   • Current: ${it.currentStock} | Min Alert: ${it.minThreshold}\n")
            sb.append("   • *ORDER QTY: ${it.orderQuantity} units* @ ₹${it.unitPrice.toInt()}/ea\n")
            sb.append("   • Subtotal: ₹${it.lineTotal.toInt()}\n\n")
        }
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("💵 *Subtotal:* ₹${order.subtotal.toInt()}\n")
        sb.append("📑 *GST (18%):* ₹${order.gstAmount.toInt()}\n")
        sb.append("💰 *GRAND TOTAL:* ₹${order.grandTotal.toInt()}\n\n")
        sb.append("Notes: ${order.notes}\n")
        sb.append("Please confirm delivery date to Bangalore workshop.")

        val message = sb.toString()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "GVD Auto World Purchase Order - ${order.poNumber}")
            putExtra(Intent.EXTRA_TEXT, message)
            if (viaWhatsApp) {
                setPackage("com.whatsapp")
            }
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            context.startActivity(Intent.createChooser(fallbackIntent, "Share Purchase Order Form"))
        }
    }

    // Booking appointment
    fun bookAppointment(appointment: ServiceAppointmentEntity, context: Context, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.bookAppointment(appointment)
            Toast.makeText(context, "Appointment booked! GVD Auto World will confirm via SMS/WhatsApp.", Toast.LENGTH_LONG).show()
            onComplete()
        }
    }

    // Reminders
    fun dispatchReminder(reminder: ServiceReminderEntity, context: Context) {
        viewModelScope.launch {
            repository.sendReminder(reminder.id)
            val msg = reminder.messageText
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=91${reminder.customerMobile}&text=${Uri.encode(msg)}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback to SMS
                val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${reminder.customerMobile}")).apply {
                    putExtra("sms_body", msg)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(smsIntent)
            }
        }
    }

    // Sharing Health Report via SMS & WhatsApp
    fun shareHealthReportViaWhatsApp(context: Context, vehicleNumber: String, customerPhone: String) {
        val inspections = currentVehicleInspections.value
        val goodCount = inspections.count { it.status == ComponentStatus.GOOD }
        val servicedCount = inspections.count { it.status == ComponentStatus.SERVICED }
        val replaceCount = inspections.count { it.status == ComponentStatus.NEED_REPLACE }

        val msg = """
            *GVD Auto World - 360° Vehicle Health Report*
            Vehicle: $vehicleNumber
            Status Summary:
            ✅ $goodCount Components in Good Condition
            🔧 $servicedCount Serviced & Fit till Next Service
            ⚠️ $replaceCount Components Need Immediate Replacement
            
            View complete interactive 360° assessment, photos, and direct online billing on your GVD Auto World app.
            Helpline: +91 8698761486
        """.trimIndent()

        val uri = Uri.parse("https://api.whatsapp.com/send?phone=91$customerPhone&text=${Uri.encode(msg)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$customerPhone")).apply {
                putExtra("sms_body", msg)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(smsIntent)
        }
    }

    fun shareHealthReportViaSMS(context: Context, vehicleNumber: String, customerPhone: String) {
        val msg = "GVD Auto World: Health report for $vehicleNumber is ready. Please review recommended repairs & pay online in your GVD Auto World app."
        val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$customerPhone")).apply {
            putExtra("sms_body", msg)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(smsIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open SMS app", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareHealthReportViaEmail(context: Context, vehicleNumber: String, customerEmail: String) {
        val inspections = currentVehicleInspections.value
        val goodCount = inspections.count { it.status == ComponentStatus.GOOD }
        val servicedCount = inspections.count { it.status == ComponentStatus.SERVICED }
        val replaceCount = inspections.count { it.status == ComponentStatus.NEED_REPLACE }

        val subject = "GVD Auto World: 360° Vehicle Health Report for $vehicleNumber"
        val body = """
            Dear Customer,
            
            Here is your GVD Auto World 360° Visual Vehicle Inspection Report for vehicle: $vehicleNumber.
            
            SUMMARY:
            - Good Condition: $goodCount components
            - Serviced (Works till next service): $servicedCount components
            - Needs Replacement: $replaceCount components
            
            DETAILED COMPONENT BREAKDOWN:
            ${inspections.joinToString("\n") { "- ${it.componentName} (${it.angle}): ${it.status.label} | Notes: ${it.technicianNotes.ifBlank { "Standard check" }} | Lifespan: ${it.worksTillInfo}" }}
            
            Workshop: GVD Auto World, Vibgyor High School Road, Kundalahalli, Bengaluru 560037
            Google Maps Location: https://maps.app.goo.gl/xgdEGHBKRth1TUrs7
            Helpline: +91 8698761486
            
            Please view live interactive 360° views and approve repairs on the GVD Auto World App.
        """.trimIndent()

        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$customerEmail")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(emailIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open email app", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendJobCardWhatsAppNotification(context: Context, jobCard: JobCardEntity) {
        val msg = """
            *GVD Auto World Workshop*
            Job Card Generated: ${jobCard.jobCardNumber}
            Vehicle: ${jobCard.vehicleNumber} (${jobCard.make} ${jobCard.model})
            Total Estimate: Rs ${jobCard.totalAmount}
            Advance Paid: Rs ${jobCard.advancePaid}
            Balance: Rs ${jobCard.balanceAmount}
            Expected Delivery: ${jobCard.deliveryDateTime}
            
            Track live service progress and view 360° health report in your GVD Auto World app.
        """.trimIndent()

        val uri = Uri.parse("https://api.whatsapp.com/send?phone=91${jobCard.customerMobile}&text=${Uri.encode(msg)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Silently fall back if WhatsApp not installed on test device
        }
    }

    fun callCustomer(context: Context, phone: String) {
        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(dialIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open dialer", Toast.LENGTH_SHORT).show()
        }
    }

    fun submitReview(
        context: Context,
        review: CustomerReviewEntity,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.submitReview(review)
            Toast.makeText(context, "Thank you for your rating & feedback!", Toast.LENGTH_LONG).show()
            val newNotif = com.example.util.CustomerNotification(
                title = "Review Submitted (${review.rating}★)",
                message = "Your rating for ${review.vehicleNumber} was recorded. We appreciate your valuable review!",
                type = "STATUS_UPDATE",
                vehicleNumber = review.vehicleNumber
            )
            _customerNotifications.value = listOf(newNotif) + _customerNotifications.value
            onComplete()
        }
    }

    fun addAdminResponseToReview(
        context: Context,
        reviewId: Long,
        adminReply: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.updateAdminResponse(reviewId, adminReply)
            Toast.makeText(context, "Response posted to client review!", Toast.LENGTH_SHORT).show()
            onComplete()
        }
    }

    fun deleteReview(context: Context, reviewId: Long) {
        viewModelScope.launch {
            repository.deleteReview(reviewId)
            Toast.makeText(context, "Review deleted", Toast.LENGTH_SHORT).show()
        }
    }
}
