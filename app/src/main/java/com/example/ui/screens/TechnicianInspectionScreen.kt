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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.local.entities.ComponentInspectionEntity
import com.example.model.ComponentStatus
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.StatusGood
import com.example.ui.theme.StatusReplace
import com.example.ui.theme.StatusServiced
import com.example.viewmodel.GvdViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TechnicianInspectionScreen(
    viewModel: GvdViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val selectedVehicleNo by viewModel.selectedVehicleNumber.collectAsState()
    val inspections by viewModel.currentVehicleInspections.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Technician Health Inspection",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Vehicle: $selectedVehicleNo • 3-Option Assessment",
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
                    IconButton(onClick = {
                        viewModel.shareHealthReportViaWhatsApp(context, selectedVehicleNo, "8698761486")
                    }) {
                        Icon(Icons.Default.Send, contentDescription = "Send WhatsApp", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCrimson)
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
            // Instructions Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Technician Work Order Checklist",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Choose 1 of 3 options for every component: 'Good', 'Serviced (works till next service)', or 'Need to replace'. Add photos of completed work.",
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            items(inspections) { item ->
                TechnicianComponentCard(
                    inspection = item,
                    onSave = { newStatus, notes, worksTill, photoUrl ->
                        viewModel.updateComponentStatus(
                            context = context,
                            inspection = item,
                            newStatus = newStatus,
                            technicianNotes = notes,
                            worksTill = worksTill,
                            photoUrl = photoUrl
                        )
                        Toast.makeText(context, "${item.componentName} updated!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Share Health Report Action Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "Send Assessment to Customer", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "Broadcast updated 360° health report with photos to customer phone", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.shareHealthReportViaWhatsApp(context, selectedVehicleNo, "8698761486")
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("WhatsApp Report", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.shareHealthReportViaSMS(context, selectedVehicleNo, "8698761486")
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SMS Report", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TechnicianComponentCard(
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
            // Header: Component name and angle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = inspection.componentName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = "View Angle: ${inspection.angle} • ${inspection.category}", fontSize = 11.sp, color = Color.Gray)
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

            Spacer(modifier = Modifier.height(12.dp))

            // 3 OPTIONS FOR TECHNICIAN (Exact Requirement from User Prompt!)
            Text(text = "Technician Assessment Status (3 Options):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Option 1: Good (Green)
                StatusOptionButton(
                    title = "Good",
                    isSelected = selectedStatus == ComponentStatus.GOOD,
                    activeColor = StatusGood,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedStatus = ComponentStatus.GOOD
                        worksTill = "Fit for normal operation"
                    }
                )

                // Option 2: Serviced with notes works till next service (Blue)
                StatusOptionButton(
                    title = "Serviced\n(Next Svc)",
                    isSelected = selectedStatus == ComponentStatus.SERVICED,
                    activeColor = StatusServiced,
                    modifier = Modifier.weight(1.2f),
                    onClick = {
                        selectedStatus = ComponentStatus.SERVICED
                        worksTill = "Works till next service (5,000 km)"
                    }
                )

                // Option 3: Need to replace (Red)
                StatusOptionButton(
                    title = "Need to\nReplace",
                    isSelected = selectedStatus == ComponentStatus.NEED_REPLACE,
                    activeColor = StatusReplace,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        selectedStatus = ComponentStatus.NEED_REPLACE
                        worksTill = "Replace immediately - Worn out"
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Works till input
            OutlinedTextField(
                value = worksTill,
                onValueChange = { worksTill = it },
                label = { Text("Lifespan / Works Till Condition *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Technician observation notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Technician Observation / Work Details") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Photo upload button & preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (hasPhoto) Color(0xFFE8F5E9) else Color(0xFFEEEEEE))
                            .clickable { hasPhoto = !hasPhoto },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (hasPhoto) Icons.Default.CheckCircle else Icons.Default.AddAPhoto,
                            contentDescription = "Photo",
                            tint = if (hasPhoto) StatusGood else Color.DarkGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasPhoto) "Work Photo Attached" else "Attach Work Photo",
                        fontSize = 12.sp,
                        color = if (hasPhoto) StatusGood else Color.Gray,
                        fontWeight = if (hasPhoto) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Button(
                    onClick = {
                        onSave(selectedStatus, notes, worksTill, if (hasPhoto) "https://gvd.example/photo.jpg" else "")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun StatusOptionButton(
    title: String,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) activeColor else Color.LightGray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ),
        color = if (isSelected) activeColor.copy(alpha = 0.12f) else Color(0xFFFAFAFA)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isSelected) activeColor else Color.DarkGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
