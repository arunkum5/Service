package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ComponentInspectionEntity
import com.example.data.local.entities.CustomerReviewEntity
import com.example.data.local.entities.JobCardEntity
import com.example.data.local.entities.ServiceAppointmentEntity
import com.example.data.local.entities.VehicleInventoryEntity
import com.example.model.ComponentStatus
import com.example.model.JobCardStatus
import com.example.model.VehicleType
import com.example.ui.components.CustomerReviewCard
import com.example.ui.components.GvdTopBar
import com.example.ui.components.PaymentGatewayDialog
import com.example.ui.components.RateServiceDialog
import com.example.ui.components.StarRatingBar
import com.example.ui.components.Vehicle360Viewer
import com.example.ui.theme.AccentGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.LightGrayBg
import com.example.ui.theme.LightGrayBorder
import com.example.ui.theme.LightGrayCard
import com.example.ui.theme.LightGraySection
import com.example.ui.theme.LightGraySurface
import com.example.ui.theme.StatusGood
import com.example.ui.theme.StatusReplace
import com.example.ui.theme.StatusServiced
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.viewmodel.GvdViewModel

@Composable
fun CustomerHomeScreen(
    viewModel: GvdViewModel
) {
    val context = LocalContext.current
    val currentRole by viewModel.currentRole.collectAsState()
    val selectedVehicleNo by viewModel.selectedVehicleNumber.collectAsState()
    val currentAngle by viewModel.currentViewAngle.collectAsState()
    val inspections by viewModel.currentVehicleInspections.collectAsState()
    val selectedComponent by viewModel.selectedComponent.collectAsState()
    val allJobCards by viewModel.allJobCards.collectAsState()
    val showroomVehicles by viewModel.allShowroomVehicles.collectAsState()
    val showPaymentDialog by viewModel.showPaymentDialog.collectAsState()
    val paymentJobCard by viewModel.paymentTargetJobCard.collectAsState()
    val allReviews by viewModel.allReviews.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: 360 Health & Status, 1: Book Service, 2: Vehicle Inventory, 3: Workshop Map, 4: Reviews
    var showBookAppointmentDialog by remember { mutableStateOf(false) }
    var showRateServiceDialog by remember { mutableStateOf(false) }

    // Find the latest job card for the customer's vehicle
    val activeJobCard = allJobCards.firstOrNull { it.vehicleNumber.equals(selectedVehicleNo, ignoreCase = true) }
        ?: allJobCards.firstOrNull()

    Scaffold(
        topBar = {
            GvdTopBar(
                currentRole = currentRole,
                onRoleSelected = { viewModel.switchRole(it) },
                title = "GVD AUTO WORLD",
                subtitle = "Vehicle Health • 360 Inspection • Online Pay"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(LightGrayBg)
        ) {
            // Vehicle switcher bar
            VehicleSelectorBar(
                selectedVehicleNo = selectedVehicleNo,
                availableVehicles = listOf("MH12RY1234", "MH12DY7698"),
                onVehicleSelected = { viewModel.setSelectedVehicle(it) }
            )

            // Category Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = LightGrayCard,
                contentColor = CrimsonRed,
                edgePadding = 8.dp
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("360° Health Report", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Book Service", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Vehicles", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Workshop Map (Bangalore)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = { Text("Reviews (${allReviews.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    CustomerHealthAndTrackingTab(
                        viewModel = viewModel,
                        vehicleNumber = selectedVehicleNo,
                        activeJobCard = activeJobCard,
                        inspections = inspections,
                        selectedComponent = selectedComponent,
                        onOpenPayment = { card -> viewModel.openPaymentDialog(card) },
                        onShareWhatsApp = { viewModel.shareHealthReportViaWhatsApp(context, selectedVehicleNo, activeJobCard?.customerMobile ?: "8698761486") },
                        onShareSMS = { viewModel.shareHealthReportViaSMS(context, selectedVehicleNo, activeJobCard?.customerMobile ?: "8698761486") },
                        onRateExperienceClick = { showRateServiceDialog = true }
                    )
                }
                1 -> {
                    CustomerServiceBookingTab(
                        onBookServiceClick = { showBookAppointmentDialog = true }
                    )
                }
                2 -> {
                    CustomerVehicleInventoryTab(
                        vehicles = showroomVehicles,
                        onBookTestDrive = { veh ->
                            Toast.makeText(context, "Test drive requested for ${veh.title}! GVD team will contact you.", Toast.LENGTH_LONG).show()
                        }
                    )
                }
                3 -> {
                    GoogleMapWorkshopLocationScreen(
                        onBack = { selectedTab = 0 },
                        onBookServiceClick = { selectedTab = 1 }
                    )
                }
                4 -> {
                    CustomerReviewsTab(
                        reviews = allReviews,
                        onRateExperienceClick = { showRateServiceDialog = true }
                    )
                }
            }
        }
    }

    // Appointment Booking Dialog
    if (showBookAppointmentDialog) {
        BookAppointmentDialog(
            defaultVehicleNo = selectedVehicleNo,
            onDismiss = { showBookAppointmentDialog = false },
            onSubmit = { appointment ->
                viewModel.bookAppointment(appointment, context) {
                    showBookAppointmentDialog = false
                }
            }
        )
    }

    // Payment Gateway Dialog
    if (showPaymentDialog && paymentJobCard != null) {
        PaymentGatewayDialog(
            jobCard = paymentJobCard!!,
            onPaymentSuccess = { method ->
                viewModel.completeOnlinePayment(context, paymentJobCard!!.id, method)
            },
            onDismiss = { viewModel.dismissPaymentDialog() }
        )
    }

    // Rate Service & Feedback Dialog
    if (showRateServiceDialog) {
        RateServiceDialog(
            initialVehicleNo = selectedVehicleNo,
            initialCustomerName = activeJobCard?.customerName ?: "Client",
            initialMobile = activeJobCard?.customerMobile ?: "",
            initialJobCardNo = activeJobCard?.jobCardNumber ?: "",
            onDismiss = { showRateServiceDialog = false },
            onSubmit = { review ->
                viewModel.submitReview(context, review) {
                    showRateServiceDialog = false
                }
            }
        )
    }
}

@Composable
fun VehicleSelectorBar(
    selectedVehicleNo: String,
    availableVehicles: List<String>,
    onVehicleSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF212124)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "MY VEHICLE:",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                availableVehicles.forEach { vehNo ->
                    val isSelected = selectedVehicleNo.equals(vehNo, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) CrimsonRed else Color(0xFF333339))
                            .clickable { onVehicleSelected(vehNo) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = vehNo,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerHealthAndTrackingTab(
    viewModel: GvdViewModel,
    vehicleNumber: String,
    activeJobCard: JobCardEntity?,
    inspections: List<ComponentInspectionEntity>,
    selectedComponent: ComponentInspectionEntity?,
    onOpenPayment: (JobCardEntity) -> Unit,
    onShareWhatsApp: () -> Unit,
    onShareSMS: () -> Unit,
    onRateExperienceClick: () -> Unit = {}
) {
    val currentAngle by viewModel.currentViewAngle.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Real-time Service Status Tracking Card
        item {
            activeJobCard?.let { card ->
                RealtimeServiceStatusCard(
                    jobCard = card,
                    onPayNowClick = { onOpenPayment(card) }
                )
            }
        }

        // 2. Service Rating & Feedback Prompt Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onRateExperienceClick() },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AccentGold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.RateReview,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Rate Your Service Experience",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E2022)
                            )
                            Text(
                                text = "Share your rating & comments with GVD Auto Bangalore",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Button(
                        onClick = onRateExperienceClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("rate_service_quick_btn")
                    ) {
                        Text("Rate ★", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 3. 360-Degree Interactive Vehicle Viewer
        item {
            val vehicleType = activeJobCard?.vehicleType ?: VehicleType.TWO_WHEELER
            Vehicle360Viewer(
                vehicleNumber = vehicleNumber,
                vehicleType = vehicleType,
                currentAngle = currentAngle,
                onAngleChange = { viewModel.setViewAngle(it) },
                onRotateNext = { viewModel.rotateAngleNext() },
                onRotatePrev = { viewModel.rotateAnglePrev() },
                inspections = inspections,
                selectedComponent = selectedComponent,
                onComponentSelect = { viewModel.selectComponent(it) },
                onApproveRepairClick = { comp ->
                    activeJobCard?.let { onOpenPayment(it) }
                }
            )
        }

        // 3. Share / Dispatch Health Report via SMS & WhatsApp
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Share Health Report & Recommendations",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Send full digital assessment to customer/family phone via WhatsApp or SMS",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onShareWhatsApp,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_whatsapp_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send WhatsApp", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onShareSMS,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_sms_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send SMS", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 4. Component Inspection Summary Matrix
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Component Health Scorecard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        val goodCount = inspections.count { it.status == ComponentStatus.GOOD }
                        val replaceCount = inspections.count { it.status == ComponentStatus.NEED_REPLACE }
                        Text(
                            text = "$goodCount Good • $replaceCount Need Action",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (replaceCount > 0) StatusReplace else StatusGood
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    inspections.forEach { inspection ->
                        InspectionItemRow(
                            inspection = inspection,
                            onClick = { viewModel.selectComponent(inspection) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RealtimeServiceStatusCard(
    jobCard: JobCardEntity,
    onPayNowClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LIVE SERVICE TRACKING",
                        color = CrimsonRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${jobCard.make} ${jobCard.model} (${jobCard.vehicleNumber})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFFEBEE))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = jobCard.status.label,
                        color = CrimsonRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step Progress Timeline
            ServiceProgressTimeline(currentStatus = jobCard.status)

            Spacer(modifier = Modifier.height(12.dp))

            // Bill & Payment status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF9F9FB))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (jobCard.isPaidOnline) "Payment Completed" else "Pending Balance",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = if (jobCard.isPaidOnline) "₹ 0.00 (Paid Online)" else "₹ ${jobCard.balanceAmount.toInt()}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (jobCard.isPaidOnline) StatusGood else CrimsonRed
                    )
                }

                if (!jobCard.isPaidOnline && jobCard.balanceAmount > 0) {
                    Button(
                        onClick = onPayNowClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("pay_service_bill_btn")
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Direct Pay", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (jobCard.isPaidOnline) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = StatusGood, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Receipt Issued", color = StatusGood, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceProgressTimeline(currentStatus: JobCardStatus) {
    val steps = listOf(
        Pair("Open", JobCardStatus.OPEN),
        Pair("In Service", JobCardStatus.IN_PROGRESS),
        Pair("Quality Check", JobCardStatus.QUALITY_CHECK),
        Pair("Ready", JobCardStatus.READY)
    )

    val currentStepIndex = when (currentStatus) {
        JobCardStatus.OPEN -> 0
        JobCardStatus.IN_PROGRESS -> 1
        JobCardStatus.QUALITY_CHECK -> 2
        JobCardStatus.READY, JobCardStatus.COMPLETED, JobCardStatus.CLOSED -> 3
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, (label, _) ->
            val isDone = index <= currentStepIndex
            val isCurrent = index == currentStepIndex

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isDone) CrimsonRed else Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    } else {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.Gray))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isDone) Color.Black else Color.Gray
                )
            }

            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(horizontal = 4.dp)
                        .background(if (index < currentStepIndex) CrimsonRed else Color(0xFFE0E0E0))
                )
            }
        }
    }
}

@Composable
fun InspectionItemRow(
    inspection: ComponentInspectionEntity,
    onClick: () -> Unit
) {
    val statusColor = when (inspection.status) {
        ComponentStatus.GOOD -> StatusGood
        ComponentStatus.SERVICED -> StatusServiced
        ComponentStatus.NEED_REPLACE -> StatusReplace
    }
    val statusBg = when (inspection.status) {
        ComponentStatus.GOOD -> Color(0xFFE8F5E9)
        ComponentStatus.SERVICED -> Color(0xFFE1F5FE)
        ComponentStatus.NEED_REPLACE -> Color(0xFFFFEBEE)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = Color(0xFFF9F9FB)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = inspection.componentName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = inspection.worksTillInfo,
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = when (inspection.status) {
                        ComponentStatus.GOOD -> "Good"
                        ComponentStatus.SERVICED -> "Serviced (Fit)"
                        ComponentStatus.NEED_REPLACE -> "Replace"
                    },
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CustomerServiceBookingTab(
    onBookServiceClick: () -> Unit
) {
    val serviceCatalog = listOf(
        ServicePackageItem("Bike Service & Tune-up", "Periodic oil change, brake tune, chain cleaning & wash", "₹ 799", Icons.Default.TwoWheeler),
        ServicePackageItem("Car Full Periodic Service", "Engine oil flush, synthetic filter, AC disinfectant & 40-pt check", "₹ 2,499", Icons.Default.DirectionsCar),
        ServicePackageItem("9H Ceramic Coating (3 Yrs)", "Diamond hydrophobic shield, paint depth enhancement & scratch resist", "₹ 7,999", Icons.Default.AutoAwesome),
        ServicePackageItem("Paint Protection Film (PPF)", "Self-healing thermoplastic polyurethane wrap against road rash", "₹ 14,999", Icons.Default.Security),
        ServicePackageItem("Interior Deep Cleaning", "Upholstery shampoo, steam vent sterilization & dashboard rejuvenation", "₹ 1,899", Icons.Default.CleaningServices),
        ServicePackageItem("Insurance & Body Works", "Cashless claims, accidental denting, panel paint & paint booth finish", "Custom", Icons.Default.Build)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CrimsonRed),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "GVD Auto World Workshop Bangalore",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Book Your Next Maintenance or Detailing",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Free doorstep pickup & delivery available in Bangalore • Real-time status alerts via WhatsApp",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onBookServiceClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Schedule Service", color = CrimsonRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Popular Service & Detailing Packages",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        items(serviceCatalog) { pkg ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFEBEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(pkg.icon, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = pkg.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = pkg.description, fontSize = 11.sp, color = Color.Gray, lineHeight = 15.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Starting at ${pkg.price}", color = CrimsonRed, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = onBookServiceClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Book", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

data class ServicePackageItem(
    val title: String,
    val description: String,
    val price: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun CustomerVehicleInventoryTab(
    vehicles: List<VehicleInventoryEntity>,
    onBookTestDrive: (VehicleInventoryEntity) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filtered = when (selectedFilter) {
        "2W" -> vehicles.filter { it.vehicleType == VehicleType.TWO_WHEELER }
        "4W" -> vehicles.filter { it.vehicleType == VehicleType.FOUR_WHEELER }
        else -> vehicles
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" },
                label = { Text("All Vehicles (${vehicles.size})") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CrimsonRed, selectedLabelColor = Color.White)
            )
            FilterChip(
                selected = selectedFilter == "2W",
                onClick = { selectedFilter = "2W" },
                label = { Text("Bikes / Scooters") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CrimsonRed, selectedLabelColor = Color.White)
            )
            FilterChip(
                selected = selectedFilter == "4W",
                onClick = { selectedFilter = "4W" },
                label = { Text("Cars / SUVs") },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = CrimsonRed, selectedLabelColor = Color.White)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(filtered) { veh ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (veh.vehicleType == VehicleType.TWO_WHEELER) Color(0xFFFFF3E0) else Color(0xFFE3F2FD)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (veh.vehicleType == VehicleType.TWO_WHEELER) Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        tint = if (veh.vehicleType == VehicleType.TWO_WHEELER) Color(0xFFE65100) else Color(0xFF1565C0),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = veh.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(text = "${veh.year} • ${veh.kmDriven} km • ${veh.fuelType}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = "Certified", color = Color(0xFF2E7D32), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = veh.specsSummary, fontSize = 11.sp, color = Color.DarkGray)

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "₹ ${veh.price.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = CrimsonRed)
                                Text(text = "EMI from ₹${veh.emiStartingAt.toInt()}/mo", fontSize = 11.sp, color = Color.Gray)
                            }

                            Button(
                                onClick = { onBookTestDrive(veh) },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Book Test Drive", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookAppointmentDialog(
    defaultVehicleNo: String,
    onDismiss: () -> Unit,
    onSubmit: (ServiceAppointmentEntity) -> Unit
) {
    var customerName by remember { mutableStateOf("Ashay Kohad") }
    var customerPhone by remember { mutableStateOf("8698761486") }
    var vehicleNumber by remember { mutableStateOf(defaultVehicleNo) }
    var selectedVehicleType by remember { mutableStateOf(VehicleType.TWO_WHEELER) }
    var servicePackage by remember { mutableStateOf("Bike Periodic Service") }
    var preferredDate by remember { mutableStateOf("2026-09-25") }
    var preferredSlot by remember { mutableStateOf("10:00 AM - 12:00 PM") }
    var isDoorstepPickup by remember { mutableStateOf(true) }
    var customerVoice by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Schedule Service Appointment", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = vehicleNumber,
                        onValueChange = { vehicleNumber = it.uppercase() },
                        label = { Text("Vehicle Registration No.*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Full Name*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Mobile Number (WhatsApp)*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = servicePackage,
                        onValueChange = { servicePackage = it },
                        label = { Text("Service Type (e.g. PPF, Ceramic, General)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = customerVoice,
                        onValueChange = { customerVoice = it },
                        label = { Text("Customer Voice / Specific Issues") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isDoorstepPickup,
                            onCheckedChange = { isDoorstepPickup = it }
                        )
                        Text("Doorstep Vehicle Pickup & Delivery (Bangalore)", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val appointment = ServiceAppointmentEntity(
                        customerName = customerName,
                        customerPhone = customerPhone,
                        vehicleNumber = vehicleNumber,
                        vehicleType = selectedVehicleType,
                        servicePackage = servicePackage,
                        preferredDate = preferredDate,
                        preferredSlot = preferredSlot,
                        isDoorstepPickup = isDoorstepPickup,
                        customerVoice = customerVoice
                    )
                    onSubmit(appointment)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
            ) {
                Text("Confirm Booking")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CustomerReviewsTab(
    reviews: List<CustomerReviewEntity>,
    onRateExperienceClick: () -> Unit
) {
    val averageRating = if (reviews.isNotEmpty()) reviews.map { it.rating }.average() else 0.0
    val fiveStarCount = reviews.count { it.rating == 5 }
    val fourStarCount = reviews.count { it.rating == 4 }
    val positivePercent = if (reviews.isNotEmpty()) {
        ((fiveStarCount + fourStarCount).toDouble() / reviews.size * 100).toInt()
    } else 100

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Workshop rating hero banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = String.format(java.util.Locale.getDefault(), "%.1f", averageRating),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E2022)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    StarRatingBar(
                                        rating = averageRating.toInt().coerceAtLeast(1),
                                        starSize = 16.dp
                                    )
                                    Text(
                                        text = "${reviews.size} Verified Reviews",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Text(
                                text = "GVD Auto World Workshop • Bangalore",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        Button(
                            onClick = onRateExperienceClick,
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("rate_experience_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RateReview,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Rate Service",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color(0xFFF1F3F5))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ReviewStatItem(title = "Satisfaction", value = "$positivePercent%")
                        ReviewStatItem(title = "5-Star Rating", value = "$fiveStarCount")
                        ReviewStatItem(title = "Quality Checked", value = "100%")
                    }
                }
            }
        }

        // Section header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Customer Feedback & Testimonials",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E2022)
                )
                Text(
                    text = "${reviews.size} Reviews",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        // Reviews list
        items(reviews, key = { it.id }) { review ->
            CustomerReviewCard(
                review = review,
                isAdminMode = false
            )
        }
    }
}

@Composable
private fun ReviewStatItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = CrimsonRed)
        Text(text = title, fontSize = 11.sp, color = Color.Gray)
    }
}

