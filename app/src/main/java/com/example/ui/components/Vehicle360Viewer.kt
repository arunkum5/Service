package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ComponentInspectionEntity
import com.example.model.ComponentStatus
import com.example.model.VehicleType
import com.example.model.VehicleViewAngle
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.StatusGood
import com.example.ui.theme.StatusReplace
import com.example.ui.theme.StatusServiced
import kotlin.math.roundToInt

@Composable
fun Vehicle360Viewer(
    vehicleNumber: String,
    vehicleType: VehicleType,
    currentAngle: VehicleViewAngle,
    onAngleChange: (VehicleViewAngle) -> Unit,
    onRotateNext: () -> Unit,
    onRotatePrev: () -> Unit,
    inspections: List<ComponentInspectionEntity>,
    selectedComponent: ComponentInspectionEntity?,
    onComponentSelect: (ComponentInspectionEntity?) -> Unit,
    onApproveRepairClick: ((ComponentInspectionEntity) -> Unit)? = null
) {
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: 360 title & Angle selector
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
                            .background(Color(0xFFFFEBEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RotateRight,
                            contentDescription = "360",
                            tint = CrimsonRed,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "360° Vehicle Health View",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = vehicleNumber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                        Text(
                            text = "Tap on hotspots or drag to rotate angles",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Prev / Next quick rotation buttons
                Row {
                    IconButton(
                        onClick = onRotatePrev,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFF5F5F5), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Rotate Prev", modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onRotateNext,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFF5F5F5), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "Rotate Next", modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Angle chips carousel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VehicleViewAngle.values().forEach { angle ->
                    val isSelected = currentAngle == angle
                    FilterChip(
                        selected = isSelected,
                        onClick = { onAngleChange(angle) },
                        label = { Text(angle.label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CrimsonRed,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 360 Interactive Canvas with Drag Support & Component Hotspots
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1F1F24),
                                Color(0xFF141416)
                            )
                        )
                    )
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { _, dragAmount ->
                                dragAccumulator += dragAmount.x
                                if (dragAccumulator > 60f) {
                                    onRotatePrev()
                                    dragAccumulator = 0f
                                } else if (dragAccumulator < -60f) {
                                    onRotateNext()
                                    dragAccumulator = 0f
                                }
                            }
                        )
                    }
            ) {
                // Background visual angle graphic
                VehicleAngleCanvas(
                    angle = currentAngle,
                    vehicleType = vehicleType
                )

                // Angle HUD Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Viewing: ${currentAngle.label} (${currentAngle.description})",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Hotspots for the current angle
                val angleInspections = inspections.filter { it.angle == currentAngle.name }
                angleInspections.forEachIndexed { index, inspection ->
                    val (xPercent, yPercent) = getHotspotCoordinates(currentAngle, index, angleInspections.size)
                    ComponentHotspotButton(
                        inspection = inspection,
                        isSelected = selectedComponent?.id == inspection.id,
                        xPercent = xPercent,
                        yPercent = yPercent,
                        onClick = { onComponentSelect(inspection) }
                    )
                }

                // If no specific component pinned to this angle, show fallback all-angle parts
                if (angleInspections.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "360° View Ready • Tap parts below for detailed report",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Component Quick Status List
            Text(
                text = "Components on Vehicle (${inspections.size} Total)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                inspections.forEach { insp ->
                    val isSelected = selectedComponent?.id == insp.id
                    val statusColor = when (insp.status) {
                        ComponentStatus.GOOD -> StatusGood
                        ComponentStatus.SERVICED -> StatusServiced
                        ComponentStatus.NEED_REPLACE -> StatusReplace
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onComponentSelect(insp) }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) CrimsonRed else Color.LightGray.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        color = if (isSelected) Color(0xFFFFEBEE) else Color(0xFFFAFAFA)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = insp.componentName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CrimsonRed else Color.Black
                            )
                        }
                    }
                }
            }

            // Detailed inspection card when a component is selected
            AnimatedVisibility(visible = selectedComponent != null) {
                selectedComponent?.let { comp ->
                    Spacer(modifier = Modifier.height(14.dp))
                    ComponentDetailCard(
                        inspection = comp,
                        onClose = { onComponentSelect(null) },
                        onApproveRepair = { onApproveRepairClick?.invoke(comp) }
                    )
                }
            }
        }
    }
}

@Composable
fun ComponentHotspotButton(
    inspection: ComponentInspectionEntity,
    isSelected: Boolean,
    xPercent: Float,
    yPercent: Float,
    onClick: () -> Unit
) {
    val statusColor = when (inspection.status) {
        ComponentStatus.GOOD -> StatusGood
        ComponentStatus.SERVICED -> StatusServiced
        ComponentStatus.NEED_REPLACE -> StatusReplace
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset {
                    IntOffset(
                        x = (xPercent * 700).roundToInt().coerceIn(40, 620),
                        y = (yPercent * 400).roundToInt().coerceIn(40, 360)
                    )
                }
                .clickable { onClick() }
        ) {
            // Ripple background
            Box(
                modifier = Modifier
                    .size(if (isSelected) 36.dp else 28.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.35f))
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
            }
        }
    }
}

@Composable
fun ComponentDetailCard(
    inspection: ComponentInspectionEntity,
    onClose: () -> Unit,
    onApproveRepair: () -> Unit
) {
    val statusBg = when (inspection.status) {
        ComponentStatus.GOOD -> Color(0xFFE8F5E9)
        ComponentStatus.SERVICED -> Color(0xFFE1F5FE)
        ComponentStatus.NEED_REPLACE -> Color(0xFFFFEBEE)
    }
    val statusColor = when (inspection.status) {
        ComponentStatus.GOOD -> StatusGood
        ComponentStatus.SERVICED -> StatusServiced
        ComponentStatus.NEED_REPLACE -> StatusReplace
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFCFD)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = inspection.componentName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Category: ${inspection.category} • Angle: ${inspection.angle}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3-Option Status Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when (inspection.status) {
                                ComponentStatus.GOOD -> Icons.Default.CheckCircle
                                ComponentStatus.SERVICED -> Icons.Default.Build
                                ComponentStatus.NEED_REPLACE -> Icons.Default.Error
                            },
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = inspection.status.label,
                            color = statusColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Last: ${inspection.lastServicedDate}",
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Technician observation
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F7))
                    .padding(10.dp)
            ) {
                Text(
                    text = "Technician Assessment & Life:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Text(
                    text = inspection.technicianNotes,
                    fontSize = 12.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "⏳ Lifespan: ${inspection.worksTillInfo}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (inspection.status == ComponentStatus.NEED_REPLACE) StatusReplace else Color(0xFF2E7D32)
                )
            }

            // Recommended Action & Replacement Cost
            if (inspection.recommendedAction.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Recommended Action:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Text(
                            text = inspection.recommendedAction,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (inspection.replacementCost > 0) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Est. Cost",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "₹ ${inspection.replacementCost.toInt()}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = CrimsonRed
                            )
                        }
                    }
                }
            }

            // Work Completion Photos
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEEEEEE))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Work Photo",
                        tint = CrimsonRed,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Work Evidence Photo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Technician verified at GVD Auto Workshop Bangalore",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }

                if (inspection.status == ComponentStatus.NEED_REPLACE && inspection.replacementCost > 0) {
                    Button(
                        onClick = onApproveRepair,
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("approve_repair_btn")
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pay & Replace", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleAngleCanvas(
    angle: VehicleViewAngle,
    vehicleType: VehicleType
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Draw automotive perspective grid floor
        for (i in 0..5) {
            val yOffset = cy + 40f + (i * 18f)
            drawLine(
                color = Color(0x15FFFFFF),
                start = Offset(cx - (w * 0.45f) - (i * 20f), yOffset),
                end = Offset(cx + (w * 0.45f) + (i * 20f), yOffset),
                strokeWidth = 1f
            )
        }

        // Draw vehicle graphics according to angle
        val bodyColor = Color(0xFFC62828) // GVD Crimson Red
        val darkBody = Color(0xFF8E0000)
        val glassColor = Color(0xFF37474F)
        val highlightColor = Color(0xFFE53935)
        val metallicSilver = Color(0xFFCFD8DC)

        when (angle) {
            VehicleViewAngle.FRONT -> {
                // Front Car / Bike silhouette
                drawRoundRect(
                    color = darkBody,
                    topLeft = Offset(cx - 130f, cy - 40f),
                    size = Size(260f, 90f),
                    cornerRadius = CornerRadius(24f, 24f)
                )
                // Windshield / Cabin
                drawRoundRect(
                    color = glassColor,
                    topLeft = Offset(cx - 95f, cy - 80f),
                    size = Size(190f, 50f),
                    cornerRadius = CornerRadius(16f, 16f)
                )
                // Grille & Headlights
                drawRoundRect(
                    color = Color.Black,
                    topLeft = Offset(cx - 70f, cy + 5f),
                    size = Size(140f, 35f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                // LED Headlights (Left & Right)
                drawCircle(color = Color(0xFFFFF9C4), radius = 14f, center = Offset(cx - 100f, cy + 5f))
                drawCircle(color = Color(0xFFFFF9C4), radius = 14f, center = Offset(cx + 100f, cy + 5f))
                // Front Tires
                drawRoundRect(color = Color.Black, topLeft = Offset(cx - 145f, cy + 10f), size = Size(26f, 60f), cornerRadius = CornerRadius(6f, 6f))
                drawRoundRect(color = Color.Black, topLeft = Offset(cx + 120f, cy + 10f), size = Size(26f, 60f), cornerRadius = CornerRadius(6f, 6f))
            }
            VehicleViewAngle.SIDE_LEFT -> {
                // Profile View
                val carPath = Path().apply {
                    moveTo(cx - 180f, cy + 20f)
                    lineTo(cx - 150f, cy - 10f)
                    lineTo(cx - 60f, cy - 25f)
                    lineTo(cx - 10f, cy - 70f)
                    lineTo(cx + 90f, cy - 70f)
                    lineTo(cx + 140f, cy - 10f)
                    lineTo(cx + 190f, cy + 15f)
                    lineTo(cx + 180f, cy + 45f)
                    lineTo(cx - 180f, cy + 45f)
                    close()
                }
                drawPath(path = carPath, color = bodyColor)
                // Wheels
                drawCircle(color = Color.Black, radius = 28f, center = Offset(cx - 105f, cy + 45f))
                drawCircle(color = metallicSilver, radius = 14f, center = Offset(cx - 105f, cy + 45f))
                drawCircle(color = Color.Black, radius = 28f, center = Offset(cx + 115f, cy + 45f))
                drawCircle(color = metallicSilver, radius = 14f, center = Offset(cx + 115f, cy + 45f))
                // Window tint
                drawRoundRect(color = glassColor, topLeft = Offset(cx - 50f, cy - 65f), size = Size(130f, 40f), cornerRadius = CornerRadius(8f, 8f))
            }
            VehicleViewAngle.REAR -> {
                // Rear silhouette
                drawRoundRect(color = darkBody, topLeft = Offset(cx - 130f, cy - 35f), size = Size(260f, 90f), cornerRadius = CornerRadius(20f, 20f))
                drawRoundRect(color = glassColor, topLeft = Offset(cx - 95f, cy - 75f), size = Size(190f, 48f), cornerRadius = CornerRadius(14f, 14f))
                // Tail lights (Bright Red)
                drawRoundRect(color = Color(0xFFFF1744), topLeft = Offset(cx - 120f, cy - 10f), size = Size(45f, 20f), cornerRadius = CornerRadius(6f, 6f))
                drawRoundRect(color = Color(0xFFFF1744), topLeft = Offset(cx + 75f, cy - 10f), size = Size(45f, 20f), cornerRadius = CornerRadius(6f, 6f))
                // Dual exhaust tips
                drawCircle(color = metallicSilver, radius = 10f, center = Offset(cx - 80f, cy + 55f))
                drawCircle(color = metallicSilver, radius = 10f, center = Offset(cx + 80f, cy + 55f))
            }
            VehicleViewAngle.SIDE_RIGHT -> {
                // Right profile view (PPF & Ceramic highlight)
                val rightPath = Path().apply {
                    moveTo(cx - 180f, cy + 45f)
                    lineTo(cx - 170f, cy + 15f)
                    lineTo(cx - 130f, cy - 10f)
                    lineTo(cx - 80f, cy - 70f)
                    lineTo(cx + 20f, cy - 70f)
                    lineTo(cx + 70f, cy - 25f)
                    lineTo(cx + 160f, cy - 10f)
                    lineTo(cx + 185f, cy + 20f)
                    lineTo(cx + 180f, cy + 45f)
                    close()
                }
                drawPath(path = rightPath, color = bodyColor)
                // Gloss Ceramic Coating Sheen Line
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = Offset(cx - 120f, cy - 10f),
                    end = Offset(cx + 140f, cy - 10f),
                    strokeWidth = 3f
                )
                // Wheels
                drawCircle(color = Color.Black, radius = 28f, center = Offset(cx - 105f, cy + 45f))
                drawCircle(color = metallicSilver, radius = 14f, center = Offset(cx - 105f, cy + 45f))
                drawCircle(color = Color.Black, radius = 28f, center = Offset(cx + 115f, cy + 45f))
                drawCircle(color = metallicSilver, radius = 14f, center = Offset(cx + 115f, cy + 45f))
            }
            VehicleViewAngle.ENGINE_BAY -> {
                // Engine Compartment View
                drawRoundRect(color = Color(0xFF263238), topLeft = Offset(cx - 140f, cy - 75f), size = Size(280f, 150f), cornerRadius = CornerRadius(16f, 16f))
                // Engine block
                drawRoundRect(color = Color(0xFF455A64), topLeft = Offset(cx - 70f, cy - 50f), size = Size(140f, 100f), cornerRadius = CornerRadius(10f, 10f))
                // Battery
                drawRoundRect(color = Color(0xFF1565C0), topLeft = Offset(cx - 125f, cy - 60f), size = Size(45f, 40f), cornerRadius = CornerRadius(6f, 6f))
                // Coolant tank
                drawCircle(color = Color(0xFF00E676), radius = 16f, center = Offset(cx + 100f, cy - 40f))
                // Oil Cap
                drawCircle(color = Color(0xFFFFD600), radius = 12f, center = Offset(cx, cy - 25f))
            }
            VehicleViewAngle.INTERIOR -> {
                // Cabin Interior Outline
                drawRoundRect(color = Color(0xFF212121), topLeft = Offset(cx - 130f, cy - 70f), size = Size(260f, 140f), cornerRadius = CornerRadius(14f, 14f))
                // Steering Wheel
                drawCircle(color = Color.DarkGray, radius = 26f, center = Offset(cx - 70f, cy - 10f), style = Stroke(width = 6f))
                // Center Console / Infotainment
                drawRoundRect(color = Color(0xFF0D47A1), topLeft = Offset(cx - 20f, cy - 35f), size = Size(40f, 30f), cornerRadius = CornerRadius(4f, 4f))
                // AC Vents
                drawRoundRect(color = Color.Gray, topLeft = Offset(cx - 15f, cy - 50f), size = Size(30f, 10f), cornerRadius = CornerRadius(2f, 2f))
                // Seats
                drawRoundRect(color = Color(0xFF37474F), topLeft = Offset(cx - 95f, cy + 10f), size = Size(50f, 50f), cornerRadius = CornerRadius(8f, 8f))
                drawRoundRect(color = Color(0xFF37474F), topLeft = Offset(cx + 45f, cy + 10f), size = Size(50f, 50f), cornerRadius = CornerRadius(8f, 8f))
            }
            VehicleViewAngle.UNDERBODY -> {
                // Underbody chassis & suspension
                drawRoundRect(color = Color(0xFF1E1E24), topLeft = Offset(cx - 120f, cy - 80f), size = Size(240f, 160f), cornerRadius = CornerRadius(12f, 12f))
                // Exhaust line pipe
                drawLine(color = metallicSilver, start = Offset(cx - 20f, cy - 60f), end = Offset(cx + 10f, cy + 70f), strokeWidth = 8f)
                // Suspension Springs
                drawCircle(color = Color(0xFFFF5252), radius = 18f, center = Offset(cx - 90f, cy - 50f), style = Stroke(width = 4f))
                drawCircle(color = Color(0xFFFF5252), radius = 18f, center = Offset(cx + 90f, cy - 50f), style = Stroke(width = 4f))
                drawCircle(color = Color(0xFFFF5252), radius = 18f, center = Offset(cx - 90f, cy + 50f), style = Stroke(width = 4f))
                drawCircle(color = Color(0xFFFF5252), radius = 18f, center = Offset(cx + 90f, cy + 50f), style = Stroke(width = 4f))
            }
        }
    }
}

fun getHotspotCoordinates(angle: VehicleViewAngle, index: Int, total: Int): Pair<Float, Float> {
    return when (angle) {
        VehicleViewAngle.FRONT -> Pair(0.42f, 0.45f)
        VehicleViewAngle.SIDE_LEFT -> Pair(0.35f, 0.52f)
        VehicleViewAngle.REAR -> Pair(0.48f, 0.48f)
        VehicleViewAngle.SIDE_RIGHT -> Pair(0.50f, 0.38f)
        VehicleViewAngle.ENGINE_BAY -> if (index == 0) Pair(0.48f, 0.42f) else Pair(0.30f, 0.35f)
        VehicleViewAngle.INTERIOR -> Pair(0.48f, 0.32f)
        VehicleViewAngle.UNDERBODY -> Pair(0.45f, 0.50f)
    }
}
