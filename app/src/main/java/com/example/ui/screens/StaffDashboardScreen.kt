package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Garage
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.JobCardEntity
import com.example.model.JobCardStatus
import com.example.model.UserRole
import com.example.model.VehicleType
import com.example.ui.components.GvdTopBar
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.StatusGood
import com.example.ui.theme.StatusReplace
import com.example.viewmodel.GvdViewModel

@Composable
fun StaffDashboardScreen(
    viewModel: GvdViewModel,
    onCreateJobCardClick: () -> Unit,
    onJobCardClick: (Long) -> Unit,
    onOpenInventoryClick: () -> Unit,
    onOpenTechnicianInspectionClick: () -> Unit,
    onOpenAppointmentsClick: () -> Unit,
    onOpenQrScannerClick: () -> Unit = {}
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val allJobCards by viewModel.allJobCards.collectAsState()
    val allAppointments by viewModel.allAppointments.collectAsState()
    val lowStock by viewModel.lowStockInventory.collectAsState()

    val totalEarnings = allJobCards.sumOf { it.totalAmount }
    val openCards = allJobCards.count { it.status == JobCardStatus.OPEN || it.status == JobCardStatus.IN_PROGRESS }
    val completeCards = allJobCards.count { it.status == JobCardStatus.READY || it.status == JobCardStatus.COMPLETED }
    val closedCards = allJobCards.count { it.status == JobCardStatus.CLOSED }
    val pendingBalance = allJobCards.filter { !it.isPaidOnline }.sumOf { it.balanceAmount }

    Scaffold(
        topBar = {
            GvdTopBar(
                currentRole = currentRole,
                onRoleSelected = { viewModel.switchRole(it) },
                title = "GVD AUTO WORLD",
                subtitle = "Staff & Service Advisor Portal • Bangalore Hub"
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Secondary FAB: Quick QR Scanner
                SmallFloatingActionButton(
                    onClick = onOpenQrScannerClick,
                    containerColor = DarkCrimson,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(6.dp, CircleShape)
                        .testTag("scan_vehicle_qr_fab")
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = "Scan Vehicle QR",
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Primary FAB: Create Job Card
                FloatingActionButton(
                    onClick = onCreateJobCardClick,
                    containerColor = CrimsonRed,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(62.dp)
                        .shadow(8.dp, CircleShape)
                        .testTag("create_jobcard_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Job Card", modifier = Modifier.size(32.dp))
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF7F8FA))
        ) {
            // 1. Red Gradient Workshop Header (Matches User Screenshot 1!)
            item {
                WorkshopHeaderSummary(
                    totalEarnings = totalEarnings,
                    totalJobCards = allJobCards.size,
                    openCount = openCards,
                    completeCount = completeCards,
                    closedCount = closedCards
                )
            }

            // 2. Alert cards (Today's Service Due & Pending Balance)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StaffAlertCard(
                        title = "Today's Service Due",
                        subtitle = "${allAppointments.size} Scheduled",
                        icon = Icons.Default.SupportAgent,
                        iconColor = Color(0xFF0288D1),
                        modifier = Modifier.weight(1f),
                        onClick = onOpenAppointmentsClick
                    )

                    StaffAlertCard(
                        title = "Pending Balance",
                        subtitle = "₹ ${pendingBalance.toInt()}",
                        icon = Icons.Default.Payments,
                        iconColor = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f),
                        onClick = { /* View unpaid */ }
                    )
                }
            }

            // 2b. Hero Vehicle QR Scanner Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable { onOpenQrScannerClick() }
                        .testTag("staff_scan_qr_banner"),
                    colors = CardDefaults.cardColors(containerColor = DarkCrimson),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Scan Vehicle QR Pass",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Quick pull-up for vehicle job cards & bays",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFD54F)
                        ) {
                            Text(
                                text = "SCAN",
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 3. "What are you looking for?" Grid Section
            item {
                Text(
                    text = "What are you looking for?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = Color.DarkGray
                )
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActionTile(
                            title = "Vehicle QR Scan",
                            subtitle = "Camera / Barcode Reader",
                            icon = Icons.Default.QrCodeScanner,
                            badge = "Fast",
                            modifier = Modifier.weight(1f),
                            onClick = onOpenQrScannerClick
                        )

                        ActionTile(
                            title = "Inventory",
                            subtitle = "Labour, Spare & Lube Stock",
                            icon = Icons.Default.Inventory2,
                            badge = if (lowStock.isNotEmpty()) "${lowStock.size} Low" else null,
                            modifier = Modifier.weight(1f),
                            onClick = onOpenInventoryClick
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ActionTile(
                            title = "Technician",
                            subtitle = "3-Option Realtime Check",
                            icon = Icons.Default.Engineering,
                            badge = "Inspect",
                            modifier = Modifier.weight(1f),
                            onClick = onOpenTechnicianInspectionClick
                        )

                        ActionTile(
                            title = "Appointments",
                            subtitle = "${allAppointments.size} Scheduled Bookings",
                            icon = Icons.Default.CalendarMonth,
                            modifier = Modifier.weight(1f),
                            onClick = onOpenAppointmentsClick
                        )
                    }
                }
            }

            // 4. Active Job Cards list
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Job Cards (${allJobCards.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Tap to view/edit",
                        fontSize = 11.sp,
                        color = CrimsonRed
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(allJobCards) { jobCard ->
                StaffJobCardItem(
                    jobCard = jobCard,
                    onClick = { onJobCardClick(jobCard.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Spacer(modifier = Modifier.height(70.dp)) // padding for FAB
            }
        }
    }
}

@Composable
fun WorkshopHeaderSummary(
    totalEarnings: Double,
    totalJobCards: Int,
    openCount: Int,
    completeCount: Int,
    closedCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkCrimson,
                        Color(0xFF900C0C),
                        Color(0xFF5A0006)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Column {
            // Row with Total Earnings and Bangalore badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Total Earning, Today",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Rs ${totalEarnings.toInt()}",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "GVD Auto Certified • Bangalore",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Total Job Cards: ",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$totalJobCards",
                                color = CrimsonRed,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Bangalore Hub",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3 Circular Stats (Open Job Card, Complete Job Card, Close Job Card)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CircularStatPill(count = openCount, label = "Open Job\nCard")
                CircularStatPill(count = completeCount, label = "Complete\nJob Card")
                CircularStatPill(count = closedCount, label = "Close Job\nCard")
            }
        }
    }
}

@Composable
fun CircularStatPill(count: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(Color.White)
                .shadow(4.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$count",
                color = Color.Black,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp
        )
    }
}

@Composable
fun StaffAlertCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Text(text = subtitle, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}

@Composable
fun ActionTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badge: String? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(20.dp))
                }

                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFFEBEE))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = badge, color = CrimsonRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = subtitle, fontSize = 10.sp, color = Color.Gray, maxLines = 1)
        }
    }
}

@Composable
fun StaffJobCardItem(
    jobCard: JobCardEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
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
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (jobCard.vehicleType == VehicleType.TWO_WHEELER) Color(0xFFFFF3E0) else Color(0xFFE3F2FD)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (jobCard.vehicleType == VehicleType.TWO_WHEELER) Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = if (jobCard.vehicleType == VehicleType.TWO_WHEELER) Color(0xFFE65100) else Color(0xFF1565C0),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = jobCard.customerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "• ${jobCard.jobCardNumber}", fontSize = 11.sp, color = Color.Gray)
                }
                Text(text = "${jobCard.vehicleNumber} (${jobCard.make} ${jobCard.model})", fontSize = 12.sp, color = Color.DarkGray)
                Text(text = "Est: ₹${jobCard.totalAmount.toInt()} • Bal: ₹${jobCard.balanceAmount.toInt()}", fontSize = 11.sp, color = CrimsonRed, fontWeight = FontWeight.SemiBold)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when (jobCard.status) {
                            JobCardStatus.OPEN -> Color(0xFFFFF3E0)
                            JobCardStatus.IN_PROGRESS -> Color(0xFFE3F2FD)
                            JobCardStatus.READY -> Color(0xFFE8F5E9)
                            JobCardStatus.COMPLETED, JobCardStatus.CLOSED -> Color(0xFFEEEEEE)
                            else -> Color(0xFFF5F5F5)
                        }
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = jobCard.status.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (jobCard.status) {
                        JobCardStatus.OPEN -> Color(0xFFE65100)
                        JobCardStatus.IN_PROGRESS -> Color(0xFF0288D1)
                        JobCardStatus.READY -> Color(0xFF2E7D32)
                        else -> Color.DarkGray
                    }
                )
            }
        }
    }
}
