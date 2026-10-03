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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ComponentInspectionEntity
import com.example.model.ComponentStatus
import com.example.model.UserRole
import com.example.ui.components.GvdTopBar
import com.example.ui.components.Vehicle360Viewer
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.StatusGood
import com.example.ui.theme.StatusReplace
import com.example.ui.theme.StatusServiced
import com.example.viewmodel.GvdViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicianDashboardScreen(
    viewModel: GvdViewModel
) {
    val context = LocalContext.current
    val currentRole by viewModel.currentRole.collectAsState()
    val dailyLabour by viewModel.technicianDailyLabour.collectAsState()
    val selectedVehicleNo by viewModel.selectedVehicleNumber.collectAsState()
    val inspections by viewModel.currentVehicleInspections.collectAsState()
    val allJobCards by viewModel.allJobCards.collectAsState()
    val currentAngle by viewModel.currentViewAngle.collectAsState()
    val selectedComponent by viewModel.selectedComponent.collectAsState()

    var show360Viewer by remember { mutableStateOf(false) }

    val goodCount = inspections.count { it.status == ComponentStatus.GOOD }
    val servicedCount = inspections.count { it.status == ComponentStatus.SERVICED }
    val replaceCount = inspections.count { it.status == ComponentStatus.NEED_REPLACE }

    Scaffold(
        topBar = {
            GvdTopBar(
                currentRole = currentRole,
                onRoleSelected = { viewModel.switchRole(it) },
                title = "GVD AUTO WORLD",
                subtitle = "Technician Workspace • Bangalore Workshop"
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF7F8FA))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. DAILY LABOUR GENERATED (STRICT PRIVACY: ONLY LABOUR EXPOSED, NO FINANCIAL LEDGER)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE8F5E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Engineering, contentDescription = null, tint = StatusGood)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Technician Raju • Bay #3",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "GVD Auto World Bangalore Hub",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFE3F2FD))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ACTIVE SHIFT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1565C0)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Total Labour Generated
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFF1F8E9),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Total Labour Generated Today",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2E7D32)
                                    )
                                    Text(
                                        text = "₹ ${dailyLabour.toInt()}",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1B5E20)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Labour Task Points",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "4 Tasks Done",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Confidentiality Notice: Only Labour is visible to technician
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Technician Access: Restricted to daily labour generated. Overall garage sales & balance are reserved for Admin oversight.",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }

            // 2. ACTIVE ASSIGNED VEHICLE SELECTOR
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Assigned Vehicles on Ramp (Bangalore)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val sampleVehicles = listOf("MH12RY1234", "KA03AB9876", "KA01XY4321")
                            items(sampleVehicles) { veh ->
                                val isSelected = veh == selectedVehicleNo
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setSelectedVehicle(veh) },
                                    label = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(veh, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                        }
                                    },
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

            // 3. 360° VISUAL HEALTH REPORT GENERATOR OVERVIEW
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "360° Visual Health Assessment",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Vehicle: $selectedVehicleNo • Real-time Diagnostic Summary",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }

                            Button(
                                onClick = { show360Viewer = !show360Viewer },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkCrimson),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (show360Viewer) "Hide 360°" else "View 360°", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 3 Component Status Counters
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatusMetricChip("Good", goodCount, StatusGood, Modifier.weight(1f))
                            StatusMetricChip("Serviced", servicedCount, StatusServiced, Modifier.weight(1f))
                            StatusMetricChip("Replace", replaceCount, StatusReplace, Modifier.weight(1f))
                        }

                        // Embedded 360 Viewer if toggled
                        if (show360Viewer) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                Vehicle360Viewer(
                                    vehicleNumber = selectedVehicleNo,
                                    vehicleType = com.example.model.VehicleType.TWO_WHEELER,
                                    currentAngle = currentAngle,
                                    onAngleChange = { viewModel.setViewAngle(it) },
                                    onRotateNext = { viewModel.rotateAngleNext() },
                                    onRotatePrev = { viewModel.rotateAnglePrev() },
                                    inspections = inspections,
                                    selectedComponent = selectedComponent,
                                    onComponentSelect = { viewModel.selectComponent(it) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // SEND HEALTH REPORT TO CUSTOMER (SMS / EMAIL / WHATSAPP)
                        Text(
                            text = "Send Generated Health Report to Customer:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.shareHealthReportViaSMS(context, selectedVehicleNo, "8698761486")
                                    Toast.makeText(context, "Sending Health Report via SMS...", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SMS", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.shareHealthReportViaEmail(context, selectedVehicleNo, "customer@gvdautoworld.com")
                                    Toast.makeText(context, "Opening Email with Health Report...", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C6BC0)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Email", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.shareHealthReportViaWhatsApp(context, selectedVehicleNo, "8698761486")
                                },
                                modifier = Modifier.weight(1.2f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // 4. COMPONENT STATUS UPDATE ITEMS (3 OPTIONS + PHOTO UPLOAD)
            item {
                Text(
                    text = "Update Component Assessment & Work Photos (${inspections.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            items(inspections) { item ->
                TechnicianWorkItemCard(
                    inspection = item,
                    onSave = { status, notes, worksTill, photoUrl ->
                        viewModel.updateComponentStatus(
                            context = context,
                            inspection = item,
                            newStatus = status,
                            technicianNotes = notes,
                            worksTill = worksTill,
                            photoUrl = photoUrl
                        )
                        Toast.makeText(context, "${item.componentName} saved & customer notified!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun TechnicianWorkItemCard(
    inspection: ComponentInspectionEntity,
    onSave: (ComponentStatus, String, String, String) -> Unit
) {
    var selectedStatus by remember(inspection.status) { mutableStateOf(inspection.status) }
    var notes by remember(inspection.technicianNotes) { mutableStateOf(inspection.technicianNotes) }
    var worksTill by remember(inspection.worksTillInfo) { mutableStateOf(inspection.worksTillInfo) }
    var hasPhoto by remember { mutableStateOf(inspection.workPhotoUrl.isNotBlank()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = inspection.componentName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "Angle: ${inspection.angle} • ${inspection.category}", fontSize = 11.sp, color = Color.Gray)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (selectedStatus) {
                                ComponentStatus.GOOD -> Color(0xFFE8F5E9)
                                ComponentStatus.SERVICED -> Color(0xFFE1F5FE)
                                ComponentStatus.NEED_REPLACE -> Color(0xFFFFEBEE)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = selectedStatus.label,
                        color = when (selectedStatus) {
                            ComponentStatus.GOOD -> StatusGood
                            ComponentStatus.SERVICED -> StatusServiced
                            ComponentStatus.NEED_REPLACE -> StatusReplace
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3 Options for Every Component (replace, good, serviced with notes works till next service)
            Text(text = "Choose Component Status (3 Options):", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Option 1: Good
                StatusOptionButton(
                    title = "Good",
                    isSelected = selectedStatus == ComponentStatus.GOOD,
                    activeColor = StatusGood,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedStatus = ComponentStatus.GOOD
                        worksTill = "Healthy - Normal Operation"
                    }
                )

                // Option 2: Serviced with notes works till next service
                StatusOptionButton(
                    title = "Serviced\n(Works Till Svc)",
                    isSelected = selectedStatus == ComponentStatus.SERVICED,
                    activeColor = StatusServiced,
                    modifier = Modifier.weight(1.3f),
                    onClick = {
                        selectedStatus = ComponentStatus.SERVICED
                        worksTill = "Works till next service (5,000 km)"
                    }
                )

                // Option 3: Replace
                StatusOptionButton(
                    title = "Replace",
                    isSelected = selectedStatus == ComponentStatus.NEED_REPLACE,
                    activeColor = StatusReplace,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedStatus = ComponentStatus.NEED_REPLACE
                        worksTill = "Replace immediately - Worn out"
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = worksTill,
                onValueChange = { worksTill = it },
                label = { Text("Lifespan / Works Till Condition *", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Technician Notes & Actions Performed", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Photo upload & save
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { hasPhoto = !hasPhoto }
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (hasPhoto) Color(0xFFE8F5E9) else Color(0xFFEEEEEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (hasPhoto) Icons.Default.CheckCircle else Icons.Default.AddAPhoto,
                            contentDescription = null,
                            tint = if (hasPhoto) StatusGood else Color.DarkGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasPhoto) "Completed Photo Attached" else "Upload Work Photo",
                        fontSize = 11.sp,
                        fontWeight = if (hasPhoto) FontWeight.Bold else FontWeight.Normal,
                        color = if (hasPhoto) StatusGood else Color.Gray
                    )
                }

                Button(
                    onClick = {
                        onSave(selectedStatus, notes, worksTill, if (hasPhoto) "https://gvd.example/photo.jpg" else "")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save & Notify", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun StatusMetricChip(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.08f)),
        color = color.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "$count", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = title, fontSize = 10.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
        }
    }
}
