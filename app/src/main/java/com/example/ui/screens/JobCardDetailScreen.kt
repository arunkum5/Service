package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.JobCardEntity
import com.example.model.ItemCategory
import com.example.model.JobCardStatus
import com.example.model.VehicleType
import com.example.ui.components.PaymentGatewayDialog
import com.example.ui.components.VehicleQrPassDialog
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobCardDetailScreen(
    jobCardId: Long,
    viewModel: GvdViewModel,
    onBack: () -> Unit,
    onOpenTechnicianInspection: () -> Unit
) {
    val context = LocalContext.current
    val allJobCards by viewModel.allJobCards.collectAsState()
    val jobCard = allJobCards.firstOrNull { it.id == jobCardId }

    var selectedTab by remember { mutableIntStateOf(1) } // 0: Estimate, 1: Jobcard, 2: Mechanic Copy
    var showStatusDropdown by remember { mutableStateOf(false) }
    var showQrPassDialog by remember { mutableStateOf(false) }

    val showPaymentDialog by viewModel.showPaymentDialog.collectAsState()
    val paymentTargetJobCard by viewModel.paymentTargetJobCard.collectAsState()

    if (jobCard == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Job Card not found")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${jobCard.jobCardNumber} Details",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "${jobCard.vehicleNumber} • ${jobCard.status.label}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Vehicle QR Pass Action
                    IconButton(
                        onClick = { showQrPassDialog = true },
                        modifier = Modifier.testTag("topbar_qr_pass_btn")
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Vehicle QR Pass", tint = Color(0xFFFFD54F))
                    }

                    IconButton(onClick = { viewModel.sendJobCardWhatsAppNotification(context, jobCard) }) {
                        Icon(Icons.Default.Send, contentDescription = "WhatsApp", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCrimson)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(LightGrayBg)
        ) {
            // Customer Header Card (Matches Screenshot 1)
            CustomerHeaderCard(
                jobCard = jobCard,
                onCall = { viewModel.callCustomer(context, jobCard.customerMobile) },
                onWhatsApp = { viewModel.sendJobCardWhatsAppNotification(context, jobCard) },
                onQrPass = { showQrPassDialog = true }
            )

            // 3 Tabs: Estimate, Jobcard, Mechanic Copy (Matches Screenshot 1 & 4)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = LightGrayCard,
                contentColor = CrimsonRed
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Estimate", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Jobcard", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Mechanic Copy", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Status Changer Bar
                item {
                    StatusChangerRow(
                        currentStatus = jobCard.status,
                        onStatusSelect = { newStatus ->
                            viewModel.updateJobCardStatus(context, jobCard.id, newStatus)
                        }
                    )
                }

                // Technician 3-Option Inspection Shortcut
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenTechnicianInspection() },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFC8E6C9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Engineering, contentDescription = null, tint = Color(0xFF2E7D32))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Technician 360° Inspection",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1B5E20)
                                )
                                Text(
                                    text = "3 options: Need to Replace • Good • Serviced (with notes)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                            Button(
                                onClick = onOpenTechnicianInspection,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Inspect", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Tab Specific Content
                when (selectedTab) {
                    0 -> item { EstimateTabContent(jobCard = jobCard) }
                    1 -> item { JobCardFullTabContent(jobCard = jobCard) }
                    2 -> item { MechanicCopyTabContent(jobCard = jobCard) }
                }

                // Payment & Actions Card
                item {
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
                                    Text(text = "Grand Total:", fontSize = 12.sp, color = Color.Gray)
                                    Text(text = "₹ ${jobCard.totalAmount.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = CrimsonRed)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "Balance Due:", fontSize = 12.sp, color = Color.Gray)
                                    Text(
                                        text = if (jobCard.isPaidOnline) "₹ 0 (PAID)" else "₹ ${jobCard.balanceAmount.toInt()}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (jobCard.isPaidOnline) StatusGood else Color.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (!jobCard.isPaidOnline && jobCard.balanceAmount > 0) {
                                    Button(
                                        onClick = { viewModel.openPaymentDialog(jobCard) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Collect Pay")
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.updateJobCardStatus(context, jobCard.id, JobCardStatus.COMPLETED)
                                        Toast.makeText(context, "Job Card marked as COMPLETED!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Mark Complete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPaymentDialog && paymentTargetJobCard != null) {
        PaymentGatewayDialog(
            jobCard = paymentTargetJobCard!!,
            onPaymentSuccess = { method ->
                viewModel.completeOnlinePayment(context, paymentTargetJobCard!!.id, method)
            },
            onDismiss = { viewModel.dismissPaymentDialog() }
        )
    }

    if (showQrPassDialog) {
        VehicleQrPassDialog(
            jobCard = jobCard,
            onDismiss = { showQrPassDialog = false },
            onTestScan = { _ ->
                showQrPassDialog = false
                viewModel.navigateTo("QR_SCANNER")
            }
        )
    }
}

@Composable
fun CustomerHeaderCard(
    jobCard: JobCardEntity,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onQrPass: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF1F8E9)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFC8E6C9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (jobCard.vehicleType == VehicleType.TWO_WHEELER) Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = jobCard.customerName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = "${jobCard.vehicleNumber} • ${jobCard.make} ${jobCard.model}", fontSize = 12.sp, color = Color.DarkGray)
                    Text(text = jobCard.customerMobile, fontSize = 11.sp, color = Color.Gray)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onQrPass,
                    modifier = Modifier
                        .size(36.dp)
                        .background(CrimsonRed, CircleShape)
                        .testTag("header_qr_pass_btn")
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Vehicle QR Pass", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = onWhatsApp,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF25D366), CircleShape)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "WhatsApp", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = onCall,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF0288D1), CircleShape)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun StatusChangerRow(
    currentStatus: JobCardStatus,
    onStatusSelect: (JobCardStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Job Card Stage Status:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                JobCardStatus.values().forEach { status ->
                    val isSelected = currentStatus == status
                    FilterChip(
                        selected = isSelected,
                        onClick = { onStatusSelect(status) },
                        label = { Text(status.label, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CrimsonRed,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun EstimateTabContent(jobCard: JobCardEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "Estimate Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            BillingLineItem("Total Spares Amount", "₹ ${jobCard.totalSpares.toInt()}")
            BillingLineItem("Total Labour Charges", "₹ ${jobCard.totalLabour.toInt()}")
            BillingLineItem("Total Lubes & Detailing", "₹ ${jobCard.totalLubes.toInt()}")
            BillingLineItem("Advance Paid", "- ₹ ${jobCard.advancePaid.toInt()}")
            Spacer(modifier = Modifier.height(6.dp))
            BillingLineItem("Net Estimate Balance", "₹ ${jobCard.balanceAmount.toInt()}", isBold = true)
        }
    }
}

@Composable
fun JobCardFullTabContent(jobCard: JobCardEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "Vehicle Condition & Checklist", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            BillingLineItem("Odometer Reading", "${jobCard.odometerKm} KM")
            BillingLineItem("Fuel Level", "${jobCard.fuelLevelPercent}%")
            BillingLineItem("Accessories", jobCard.accessoriesNotes.ifBlank { "Standard Tools" })
            BillingLineItem("Customer Voice", jobCard.customerVoice.ifBlank { "Regular Service & Inspection" })
            BillingLineItem("Dent / Scratch Marks", jobCard.dentNotes.ifBlank { "None noted" })
            BillingLineItem("Promised Delivery", jobCard.deliveryDateTime)
        }
    }
}

@Composable
fun MechanicCopyTabContent(jobCard: JobCardEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "Mechanic Work Order", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "1. Periodic Servicing Major - Change engine oil & oil filter\n2. Clean air filter & spark plug\n3. Front and rear brake disc inspection & caliper cleaning\n4. Chain slackness adjustment & lube\n5. Full vehicle 360° health check upload",
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun BillingLineItem(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = if (isBold) CrimsonRed else Color.DarkGray, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(text = value, fontSize = 12.sp, fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold, color = if (isBold) CrimsonRed else Color.Black)
    }
}
