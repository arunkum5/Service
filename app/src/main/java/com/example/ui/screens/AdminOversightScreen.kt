package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import com.example.data.local.entities.CustomerReviewEntity
import com.example.data.local.entities.InventoryItemEntity
import com.example.model.ItemCategory
import com.example.model.VehicleType
import com.example.ui.components.ExcelPartsImportDialog
import com.example.util.ParsedInventoryItem
import com.example.data.local.entities.ServiceReminderEntity
import com.example.model.JobCardStatus
import com.example.ui.components.AdminReplyDialog
import com.example.ui.components.CustomerReviewCard
import com.example.ui.components.GvdTopBar
import com.example.ui.components.StarRatingBar
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
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.viewmodel.GvdViewModel

@Composable
fun AdminOversightScreen(
    viewModel: GvdViewModel
) {
    val context = LocalContext.current
    val currentRole by viewModel.currentRole.collectAsState()
    val allJobCards by viewModel.allJobCards.collectAsState()
    val allReminders by viewModel.allReminders.collectAsState()
    val allAppointments by viewModel.allAppointments.collectAsState()
    val allReviews by viewModel.allReviews.collectAsState()
    val allInventory by viewModel.allInventory.collectAsState()
    val lowStockInventory by viewModel.lowStockInventory.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Sales, 1: Main Inventory & Excel, 2: Reminders, 3: Bookings, 4: Reviews
    var selectedReviewForReply by remember { mutableStateOf<CustomerReviewEntity?>(null) }
    var showExcelImportDialog by remember { mutableStateOf(false) }
    var showAddPartDialog by remember { mutableStateOf(false) }

    val todaySales = allJobCards.sumOf { it.totalAmount }
    val monthlySales = todaySales * 4.2 + 84200.0 // monthly projection
    val totalPaidOnline = allJobCards.filter { it.isPaidOnline }.sumOf { it.totalAmount }
    val totalCounterPending = allJobCards.filter { !it.isPaidOnline }.sumOf { it.balanceAmount }
    val averageRating = if (allReviews.isNotEmpty()) allReviews.map { it.rating }.average() else 0.0

    Scaffold(
        topBar = {
            GvdTopBar(
                currentRole = currentRole,
                onRoleSelected = { viewModel.switchRole(it) },
                title = "GVD AUTO WORLD",
                subtitle = "Executive Admin Oversight • Financial Reports"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(LightGrayBg)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = LightGrayCard,
                contentColor = CrimsonRed,
                edgePadding = 8.dp
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Sales & Reports", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Main Inventory (${allInventory.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Auto Reminders (${allReminders.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("Bookings (${allAppointments.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    text = { Text("Reviews & Ratings (${allReviews.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            when (selectedTab) {
                0 -> {
                    AdminReportsTab(
                        todaySales = todaySales,
                        monthlySales = monthlySales,
                        totalPaidOnline = totalPaidOnline,
                        pendingBalance = totalCounterPending,
                        totalJobs = allJobCards.size,
                        averageRating = averageRating,
                        totalReviews = allReviews.size,
                        onViewReviewsClick = { selectedTab = 4 }
                    )
                }
                1 -> {
                    AdminMainInventoryTab(
                        allInventory = allInventory,
                        lowStockInventory = lowStockInventory,
                        onUploadExcelClick = { showExcelImportDialog = true },
                        onAddSinglePartClick = { showAddPartDialog = true },
                        onAdjustStock = { id, amount -> viewModel.adjustStock(id, amount, context) },
                        onQuickDemoLoad = {
                            val demoList = com.example.util.ExcelInventoryParser.getDemoIncomingPartsBatch()
                            viewModel.importExcelPartsBatch(context, demoList, "Bosch & Castrol South India Depot", "INV-2026-OCT-881")
                        }
                    )
                }
                2 -> {
                    AdminRemindersTab(
                        reminders = allReminders,
                        onDispatch = { reminder ->
                            viewModel.dispatchReminder(reminder, context)
                        }
                    )
                }
                3 -> {
                    AdminAppointmentsTab(appointments = allAppointments)
                }
                4 -> {
                    AdminReviewsTab(
                        reviews = allReviews,
                        onReplyClick = { review -> selectedReviewForReply = review },
                        onDeleteClick = { review -> viewModel.deleteReview(context, review.id) }
                    )
                }
            }
        }
    }

    if (showExcelImportDialog) {
        ExcelPartsImportDialog(
            onDismiss = { showExcelImportDialog = false },
            onConfirmImport = { items, supplier, invoiceNo ->
                viewModel.importExcelPartsBatch(context, items, supplier, invoiceNo) { _, _ ->
                    showExcelImportDialog = false
                }
            }
        )
    }

    if (showAddPartDialog) {
        AdminAddPartDialog(
            onDismiss = { showAddPartDialog = false },
            onAdd = { newItem ->
                viewModel.addInventoryItem(newItem, context)
                showAddPartDialog = false
            }
        )
    }

    if (selectedReviewForReply != null) {
        AdminReplyDialog(
            review = selectedReviewForReply!!,
            onDismiss = { selectedReviewForReply = null },
            onSubmitReply = { reply ->
                viewModel.addAdminResponseToReview(context, selectedReviewForReply!!.id, reply) {
                    selectedReviewForReply = null
                }
            }
        )
    }
}

@Composable
fun AdminReportsTab(
    todaySales: Double,
    monthlySales: Double,
    totalPaidOnline: Double,
    pendingBalance: Double,
    totalJobs: Int,
    averageRating: Double = 0.0,
    totalReviews: Int = 0,
    onViewReviewsClick: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Customer Experience & Ratings KPI
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onViewReviewsClick() },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AccentGold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = AccentGold, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Client Satisfaction Rating", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "$totalReviews verified customer reviews in Bangalore", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = String.format(java.util.Locale.getDefault(), "%.1f", averageRating),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = AccentGold
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(Icons.Default.Star, contentDescription = null, tint = AccentGold, modifier = Modifier.size(18.dp))
                        }
                        Text(text = "View Feedback →", fontSize = 11.sp, color = CrimsonRed, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Daily Sales Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE8F5E9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = StatusGood)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "Daily Sales (Today)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(text = "GVD Auto World Bangalore Workshop Hub", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        Text(
                            text = "₹ ${todaySales.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = CrimsonRed
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ReportMiniCard(title = "Paid Online", value = "₹ ${totalPaidOnline.toInt()}", color = StatusGood, modifier = Modifier.weight(1f))
                        ReportMiniCard(title = "Counter / Pending", value = "₹ ${pendingBalance.toInt()}", color = Color(0xFFE65100), modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Monthly Revenue Report Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE3F2FD)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF1565C0))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "Monthly Revenue Report", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(text = "Current Month (MTD Total)", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        Text(
                            text = "₹ ${monthlySales.toInt()}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color(0xFF1565C0)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(text = "Revenue Stream Breakdown:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(6.dp))

                    CategoryProgressBar(category = "Periodic Services & Labour (45%)", fraction = 0.45f, color = CrimsonRed)
                    CategoryProgressBar(category = "OEM Spares & Parts (25%)", fraction = 0.25f, color = Color(0xFF1E88E5))
                    CategoryProgressBar(category = "PPF & Ceramic Detailing (20%)", fraction = 0.20f, color = Color(0xFFFFB300))
                    CategoryProgressBar(category = "Insurance & Accidental Claims (10%)", fraction = 0.10f, color = Color(0xFF43A047))
                }
            }
        }

        // Workshop Performance & Operational KPI Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Workshop Operations Overview", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Active Service Bays", fontSize = 12.sp, color = Color.Gray)
                        Text(text = "4 of 6 Bays Occupied", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Technicians on Duty", fontSize = 12.sp, color = Color.Gray)
                        Text(text = "3 Techs • 1 Detailer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Average Repair Turnaround", fontSize = 12.sp, color = Color.Gray)
                        Text(text = "4.2 Hours", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusGood)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminRemindersTab(
    reminders: List<ServiceReminderEntity>,
    onDispatch: (ServiceReminderEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFE65100))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Automated Service Reminder Engine", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFE65100))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Reminders are automatically prepared based on customer last service date (90 days / 3,000 km), ceramic coating booster, and insurance renewals. Dispatch via WhatsApp or SMS in 1 click.",
                        fontSize = 11.sp,
                        color = Color.DarkGray,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        item {
            Text(text = "Configured Service Reminder Queue (${reminders.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        items(reminders) { reminder ->
            val isSent = reminder.status == "SENT" || reminder.isWhatsAppTriggered
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "${reminder.customerName} (${reminder.vehicleNumber})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Type: ${reminder.reminderType} • Due: ${reminder.dueDate}", fontSize = 11.sp, color = Color.Gray)
                        }

                        if (isSent) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = "Sent", color = StatusGood, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "\"${reminder.messageText}\"",
                        fontSize = 11.sp,
                        color = Color.DarkGray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onDispatch(reminder) },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isSent) Color(0xFF455A64) else Color(0xFF25D366)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isSent) "Resend via WhatsApp / SMS" else "Send WhatsApp Reminder Now")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAppointmentsTab(
    appointments: List<com.example.data.local.entities.ServiceAppointmentEntity>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(text = "Customer Online Appointments (${appointments.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        if (appointments.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No appointments scheduled yet.")
                }
            }
        }

        items(appointments) { appt ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = appt.customerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "${appt.vehicleNumber} • ${appt.customerPhone}", fontSize = 11.sp, color = Color.Gray)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE3F2FD))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = appt.status, color = Color(0xFF1565C0), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Service: ${appt.servicePackage}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Text(text = "Slot: ${appt.preferredDate} (${appt.preferredSlot})", fontSize = 11.sp, color = Color.DarkGray)
                    if (appt.isDoorstepPickup) {
                        Text(text = "Pickup: Doorstep pickup in Bangalore requested", fontSize = 11.sp, color = CrimsonRed)
                    }
                    if (appt.customerVoice.isNotBlank()) {
                        Text(text = "Notes: ${appt.customerVoice}", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun ReportMiniCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.08f)),
        color = color.copy(alpha = 0.08f)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 11.sp, color = Color.Gray)
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun CategoryProgressBar(
    category: String,
    fraction: Float,
    color: Color
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = category, fontSize = 11.sp, color = Color.DarkGray)
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFFEEEEEE))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun AdminReviewsTab(
    reviews: List<CustomerReviewEntity>,
    onReplyClick: (CustomerReviewEntity) -> Unit,
    onDeleteClick: (CustomerReviewEntity) -> Unit
) {
    var selectedRatingFilter by remember { mutableIntStateOf(0) }

    val averageRating = if (reviews.isNotEmpty()) {
        reviews.map { it.rating }.average()
    } else 0.0

    val fiveStarCount = reviews.count { it.rating == 5 }
    val fourStarCount = reviews.count { it.rating == 4 }
    val threeStarCount = reviews.count { it.rating == 3 }
    val twoOrOneStarCount = reviews.count { it.rating <= 2 }
    val pendingReplyCount = reviews.count { it.adminResponse.isBlank() }
    val positivePercent = if (reviews.isNotEmpty()) {
        ((fiveStarCount + fourStarCount).toDouble() / reviews.size * 100).toInt()
    } else 100

    val filteredReviews = when (selectedRatingFilter) {
        5 -> reviews.filter { it.rating == 5 }
        4 -> reviews.filter { it.rating == 4 }
        3 -> reviews.filter { it.rating <= 3 }
        -1 -> reviews.filter { it.adminResponse.isBlank() }
        else -> reviews
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary KPI Banner
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AccentGold.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AccentGold,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Client Satisfaction Index",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1E2022)
                                )
                                Text(
                                    text = "GVD Auto World Bangalore Workshop",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE8F5E9)
                        ) {
                            Text(
                                text = "$positivePercent% Positive",
                                color = Color(0xFF2E7D32),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Average rating block
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF9E6))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = String.format(java.util.Locale.getDefault(), "%.1f", averageRating),
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF795548)
                            )
                            StarRatingBar(
                                rating = averageRating.toInt().coerceAtLeast(1),
                                starSize = 16.dp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${reviews.size} Total Ratings",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        // Breakdown bars
                        Column(modifier = Modifier.weight(1.6f)) {
                            RatingBreakdownRow(label = "5 ★", count = fiveStarCount, total = reviews.size, color = Color(0xFF2E7D32))
                            RatingBreakdownRow(label = "4 ★", count = fourStarCount, total = reviews.size, color = Color(0xFF66BB6A))
                            RatingBreakdownRow(label = "3 ★", count = threeStarCount, total = reviews.size, color = Color(0xFFFFA726))
                            RatingBreakdownRow(label = "2 ★", count = reviews.count { it.rating == 2 }, total = reviews.size, color = Color(0xFFFF7043))
                            RatingBreakdownRow(label = "1 ★", count = reviews.count { it.rating == 1 }, total = reviews.size, color = CrimsonRed)
                        }
                    }
                }
            }
        }

        // Filter chips
        item {
            Column {
                Text(
                    text = "Filter Client Reviews:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedRatingFilter == 0,
                        onClick = { selectedRatingFilter = 0 },
                        label = { Text("All (${reviews.size})", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedRatingFilter == 5,
                        onClick = { selectedRatingFilter = 5 },
                        label = { Text("5 ★ (${fiveStarCount})", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedRatingFilter == 4,
                        onClick = { selectedRatingFilter = 4 },
                        label = { Text("4 ★ (${fourStarCount})", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedRatingFilter == 3,
                        onClick = { selectedRatingFilter = 3 },
                        label = { Text("≤3 ★ (${threeStarCount + twoOrOneStarCount})", fontSize = 11.sp) }
                    )
                    if (pendingReplyCount > 0) {
                        FilterChip(
                            selected = selectedRatingFilter == -1,
                            onClick = { selectedRatingFilter = -1 },
                            label = { Text("Needs Reply ($pendingReplyCount)", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CrimsonRed.copy(alpha = 0.15f),
                                selectedLabelColor = CrimsonRed
                            )
                        )
                    }
                }
            }
        }

        if (filteredReviews.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.RateReview,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No reviews in this category",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            items(filteredReviews, key = { it.id }) { review ->
                CustomerReviewCard(
                    review = review,
                    isAdminMode = true,
                    onReplyClick = { onReplyClick(review) },
                    onDeleteClick = { onDeleteClick(review) }
                )
            }
        }
    }
}

@Composable
fun RatingBreakdownRow(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val fraction = if (total > 0) count.toFloat() / total.toFloat() else 0f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(24.dp), color = Color.Gray)
        Spacer(modifier = Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFFEEEEEE))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = "$count", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.width(16.dp))
    }
}

@Composable
fun AdminMainInventoryTab(
    allInventory: List<InventoryItemEntity>,
    lowStockInventory: List<InventoryItemEntity>,
    onUploadExcelClick: () -> Unit,
    onAddSinglePartClick: () -> Unit,
    onAdjustStock: (Long, Int) -> Unit,
    onQuickDemoLoad: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    val totalStockUnits = allInventory.sumOf { it.stockQuantity }
    val totalValuation = allInventory.sumOf { it.stockQuantity * it.unitPrice }

    val filteredList = allInventory.filter { item ->
        val matchesCategory = when (selectedCategoryFilter) {
            "SPARE" -> item.category == ItemCategory.SPARE
            "LUBE" -> item.category == ItemCategory.LUBE
            "DETAILING" -> item.category == ItemCategory.DETAILING
            "LOW_STOCK" -> item.stockQuantity <= item.minThresholdAlert
            else -> true
        }
        val matchesSearch = searchQuery.isBlank() ||
                item.partName.contains(searchQuery, ignoreCase = true) ||
                item.partNumber.contains(searchQuery, ignoreCase = true)

        matchesCategory && matchesSearch
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Warehouse Stats Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Main Parts Inventory", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = TextDark)
                            Text("Live Warehouse Valuation & Parts Inward", fontSize = 11.sp, color = TextMuted)
                        }
                        Surface(
                            color = DarkCrimson.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Inventory2, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${allInventory.size} SKUs", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = CrimsonRed)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = LightGrayBg
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("TOTAL UNITS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text("$totalStockUnits pcs", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(8.dp),
                            color = LightGrayBg
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("VALUATION (MRP)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text("₹ ${totalValuation.toInt()}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = CrimsonRed)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = if (lowStockInventory.isNotEmpty()) Color(0xFFFFEBEE) else LightGrayBg
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("LOW STOCK", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (lowStockInventory.isNotEmpty()) CrimsonRed else Color.Gray)
                                Text("${lowStockInventory.size} items", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = if (lowStockInventory.isNotEmpty()) CrimsonRed else StatusGood)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Primary Excel File Upload Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onUploadExcelClick,
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp)
                                .testTag("admin_upload_excel_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Excel / CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onAddSinglePartClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Add Part", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = onQuickDemoLoad,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = CrimsonRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("1-Tap Demo: Import Incoming OEM Parts Delivery", fontSize = 11.sp, color = CrimsonRed, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Search and Filters
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search by part name or SKU...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "ALL" to "All (${allInventory.size})",
                            "SPARE" to "Spares",
                            "LUBE" to "Lubes",
                            "DETAILING" to "Detailing",
                            "LOW_STOCK" to "Low Alert (${lowStockInventory.size})"
                        ).forEach { (key, label) ->
                            val isSelected = selectedCategoryFilter == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategoryFilter = key },
                                label = { Text(label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (key == "LOW_STOCK") Color(0xFFFFCDD2) else CrimsonRed.copy(alpha = 0.2f),
                                    selectedLabelColor = if (key == "LOW_STOCK") CrimsonRed else CrimsonRed
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                }
            }
        }

        // Inventory Items List
        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No inventory items found matching filter", color = Color.Gray, fontSize = 13.sp)
                }
            }
        } else {
            items(filteredList, key = { it.id }) { item ->
                AdminInventoryItemCard(
                    item = item,
                    onAdjustStock = onAdjustStock
                )
            }
        }
    }
}

@Composable
fun AdminInventoryItemCard(
    item: InventoryItemEntity,
    onAdjustStock: (Long, Int) -> Unit
) {
    val isLowStock = item.stockQuantity <= item.minThresholdAlert

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.partName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextDark
                    )
                    if (isLowStock) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Low Stock",
                            tint = CrimsonRed,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SKU: ${item.partNumber}",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${item.category.name}",
                        fontSize = 10.sp,
                        color = when (item.category) {
                            ItemCategory.SPARE -> CrimsonRed
                            ItemCategory.LUBE -> Color(0xFF1976D2)
                            ItemCategory.DETAILING -> Color(0xFF7B1FA2)
                            ItemCategory.LABOUR -> Color(0xFFE65100)
                        },
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${item.compatibleType.label.take(2)}",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Unit MRP: ₹ ${item.unitPrice.toInt()}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CrimsonRed
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = if (isLowStock) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${item.stockQuantity} ${item.unit}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isLowStock) CrimsonRed else StatusGood,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onAdjustStock(item.id, -1) },
                        modifier = Modifier.size(28.dp),
                        enabled = item.stockQuantity > 0
                    ) {
                        Icon(Icons.Default.RemoveCircle, contentDescription = "Decrease", tint = if (item.stockQuantity > 0) Color.Gray else Color.LightGray, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = { onAdjustStock(item.id, +1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Increase", tint = StatusGood, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAddPartDialog(
    onDismiss: () -> Unit,
    onAdd: (InventoryItemEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ItemCategory.SPARE) }
    var vehicleType by remember { mutableStateOf(VehicleType.TWO_WHEELER) }
    var priceStr by remember { mutableStateOf("") }
    var qtyStr by remember { mutableStateOf("") }
    var unitStr by remember { mutableStateOf("pcs") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Part to Main Inventory", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Part Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    label = { Text("SKU / Part Number *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Price (₹)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = qtyStr,
                        onValueChange = { qtyStr = it },
                        label = { Text("Initial Qty") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(ItemCategory.SPARE, ItemCategory.LUBE, ItemCategory.DETAILING).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.name, fontSize = 10.sp) },
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val newPart = InventoryItemEntity(
                            partName = name,
                            partNumber = sku.ifBlank { "SKU-${System.currentTimeMillis() % 1000}" },
                            category = category,
                            compatibleType = vehicleType,
                            unitPrice = priceStr.toDoubleOrNull() ?: 500.0,
                            stockQuantity = qtyStr.toIntOrNull() ?: 10,
                            minThresholdAlert = 5,
                            unit = unitStr
                        )
                        onAdd(newPart)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
            ) {
                Text("Add to Stock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


