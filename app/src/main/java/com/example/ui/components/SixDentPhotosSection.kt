package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.model.DentPhotoItem
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.LightGrayBg
import com.example.ui.theme.LightGrayBorder
import com.example.ui.theme.LightGrayCard
import com.example.ui.theme.StatusGood
import com.example.ui.theme.StatusReplace
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted

@Composable
fun SixDentPhotosSection(
    dentPhotos: List<DentPhotoItem>,
    onUpdatePhoto: (angleIndex: Int, photoUri: String, hasDent: Boolean, severity: String, notes: String) -> Unit,
    onAutoFillDemo: () -> Unit,
    onClearAll: () -> Unit
) {
    val context = LocalContext.current
    var activeTargetIndex by remember { mutableIntStateOf(-1) }
    var zoomedPhoto by remember { mutableStateOf<DentPhotoItem?>(null) }

    // Android Zero-Permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null && activeTargetIndex in dentPhotos.indices) {
            val current = dentPhotos[activeTargetIndex]
            onUpdatePhoto(
                activeTargetIndex,
                uri.toString(),
                current.hasDent,
                if (current.severity == "NO_DENT") "MINOR_SCRATCH" else current.severity,
                current.notes
            )
            Toast.makeText(context, "Photo added for ${current.title}", Toast.LENGTH_SHORT).show()
        }
    }

    val capturedCount = dentPhotos.count { it.photoUri.isNotBlank() }
    val progress = capturedCount / 6f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("six_dent_photos_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(DarkCrimson.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = CrimsonRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "6 Dent Inspection Photos *",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextDark
                        )
                        Text(
                            text = "Capture all 6 angles to record scratches & dents",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Surface(
                    color = if (capturedCount == 6) StatusGood.copy(alpha = 0.12f) else Color(0xFFF0F0F0),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$capturedCount/6 Photos",
                        color = if (capturedCount == 6) StatusGood else Color.DarkGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (capturedCount == 6) StatusGood else CrimsonRed,
                trackColor = Color(0xFFEFEFEF)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onAutoFillDemo,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("autofill_demo_photos_btn")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = CrimsonRed)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Auto-Fill 6 Sample Photos", fontSize = 11.sp, color = CrimsonRed)
                }

                if (capturedCount > 0) {
                    OutlinedButton(
                        onClick = onClearAll,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 6 Dent Angles Grid / List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                dentPhotos.forEachIndexed { index, item ->
                    DentAngleCaptureCard(
                        item = item,
                        onCaptureClick = {
                            activeTargetIndex = index
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onZoomClick = {
                            zoomedPhoto = item
                        },
                        onRemovePhoto = {
                            onUpdatePhoto(index, "", false, "NO_DENT", "")
                        },
                        onUpdateDetails = { hasDent, severity, notes ->
                            onUpdatePhoto(index, item.photoUri, hasDent, severity, notes)
                        }
                    )
                }
            }
        }
    }

    // Full-screen Zoom Preview Dialog
    if (zoomedPhoto != null) {
        Dialog(onDismissRequest = { zoomedPhoto = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = zoomedPhoto!!.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = zoomedPhoto!!.description, fontSize = 11.sp, color = TextMuted)
                        }
                        IconButton(onClick = { zoomedPhoto = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        if (zoomedPhoto!!.photoUri.isNotBlank()) {
                            AsyncImage(
                                model = zoomedPhoto!!.photoUri,
                                contentDescription = zoomedPhoto!!.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Condition: ${zoomedPhoto!!.severityLabel}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (zoomedPhoto!!.hasDent) CrimsonRed else StatusGood
                        )
                    }

                    if (zoomedPhoto!!.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Inspector Notes: ${zoomedPhoto!!.notes}",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DentAngleCaptureCard(
    item: DentPhotoItem,
    onCaptureClick: () -> Unit,
    onZoomClick: () -> Unit,
    onRemovePhoto: () -> Unit,
    onUpdateDetails: (hasDent: Boolean, severity: String, notes: String) -> Unit
) {
    var expandedDetails by remember { mutableStateOf(item.hasDent || item.photoUri.isNotBlank()) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (item.photoUri.isNotBlank()) {
                    if (item.hasDent) CrimsonRed.copy(alpha = 0.5f) else StatusGood.copy(alpha = 0.5f)
                } else LightGrayBorder,
                RoundedCornerShape(10.dp)
            ),
        shape = RoundedCornerShape(10.dp),
        color = LightGrayBg
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Photo Thumbnail or Placeholder
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEAEAEA))
                        .clickable {
                            if (item.photoUri.isNotBlank()) onZoomClick() else onCaptureClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (item.photoUri.isNotBlank()) {
                        AsyncImage(
                            model = item.photoUri,
                            contentDescription = item.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(2.dp)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(text = "Photo", fontSize = 9.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Title and Angle info
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (item.photoUri.isNotBlank()) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Captured",
                                tint = StatusGood,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Text(
                        text = item.description,
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Status chip
                    Surface(
                        color = when (item.severity) {
                            "MINOR_SCRATCH" -> Color(0xFFFFF3E0)
                            "MEDIUM_DENT" -> Color(0xFFFFEBEE)
                            "MAJOR_DAMAGE" -> Color(0xFFFFCDD2)
                            else -> Color(0xFFE8F5E9)
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = item.severityLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (item.severity) {
                                "MINOR_SCRATCH" -> Color(0xFFE65100)
                                "MEDIUM_DENT", "MAJOR_DAMAGE" -> CrimsonRed
                                else -> StatusGood
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Action button
                Column(horizontalAlignment = Alignment.End) {
                    if (item.photoUri.isBlank()) {
                        Button(
                            onClick = onCaptureClick,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkCrimson),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Capture", fontSize = 11.sp)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onCaptureClick,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Retake", tint = CrimsonRed, modifier = Modifier.size(16.dp))
                            }
                            IconButton(
                                onClick = onRemovePhoto,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Dent Severity & Notes Editor
            if (item.photoUri.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "NO_DENT" to "No Dent",
                        "MINOR_SCRATCH" to "Scratch",
                        "MEDIUM_DENT" to "Dent",
                        "MAJOR_DAMAGE" to "Damage"
                    ).forEach { (key, label) ->
                        val isSelected = item.severity == key
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                val hasDent = key != "NO_DENT"
                                onUpdateDetails(hasDent, key, item.notes)
                            },
                            label = { Text(label, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (key == "NO_DENT") StatusGood.copy(alpha = 0.2f) else CrimsonRed.copy(alpha = 0.2f),
                                selectedLabelColor = if (key == "NO_DENT") StatusGood else CrimsonRed
                            ),
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }

                if (item.severity != "NO_DENT") {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = item.notes,
                        onValueChange = { newNote ->
                            onUpdateDetails(true, item.severity, newNote)
                        },
                        placeholder = { Text("Specific scratch/dent position notes...", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                    )
                }
            }
        }
    }
}
