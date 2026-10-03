package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.StatusGood
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JobCardDentGallery(
    photos: List<DentPhotoItem>,
    vehicleNumber: String,
    customerName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPhotoForZoom by remember { mutableStateOf<DentPhotoItem?>(null) }

    val capturedPhotos = photos.filter { it.photoUri.isNotBlank() }
    val dentsFoundCount = photos.count { it.hasDent || it.severity != "NO_DENT" }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("jobcard_dent_gallery_card"),
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
                            text = "6 Dent Inspection Photos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextDark
                        )
                        Text(
                            text = if (dentsFoundCount > 0) "$dentsFoundCount dent marks detected on check-in" else "All 6 angles inspected & verified clean",
                            fontSize = 11.sp,
                            color = if (dentsFoundCount > 0) CrimsonRed else StatusGood
                        )
                    }
                }

                Surface(
                    color = if (capturedPhotos.size == 6) StatusGood.copy(alpha = 0.12f) else Color(0xFFF5F5F7),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "${capturedPhotos.size}/6 Captured",
                        color = if (capturedPhotos.size == 6) StatusGood else Color.DarkGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 6 Photos Grid
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                photos.forEach { item ->
                    DentPhotoThumbnailItem(
                        item = item,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (item.photoUri.isNotBlank()) {
                                    selectedPhotoForZoom = item
                                }
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row: Share with vehicle owner via WhatsApp
            OutlinedButton(
                onClick = {
                    shareDentReportToWhatsApp(
                        context = context,
                        vehicleNo = vehicleNumber,
                        customerName = customerName,
                        photos = photos
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = CrimsonRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share 6 Dent Photos Report via WhatsApp", fontSize = 12.sp, color = CrimsonRed, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    // Zoom Dialog
    if (selectedPhotoForZoom != null) {
        Dialog(onDismissRequest = { selectedPhotoForZoom = null }) {
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
                            Text(text = selectedPhotoForZoom!!.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = selectedPhotoForZoom!!.description, fontSize = 11.sp, color = TextMuted)
                        }
                        IconButton(onClick = { selectedPhotoForZoom = null }) {
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
                        AsyncImage(
                            model = selectedPhotoForZoom!!.photoUri,
                            contentDescription = selectedPhotoForZoom!!.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Condition: ${selectedPhotoForZoom!!.severityLabel}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (selectedPhotoForZoom!!.hasDent) CrimsonRed else StatusGood
                        )
                    }

                    if (selectedPhotoForZoom!!.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Inspector Notes: ${selectedPhotoForZoom!!.notes}",
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
fun DentPhotoThumbnailItem(
    item: DentPhotoItem,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .border(
                1.dp,
                if (item.photoUri.isNotBlank()) {
                    if (item.hasDent) CrimsonRed.copy(alpha = 0.5f) else StatusGood.copy(alpha = 0.4f)
                } else LightGrayBorder,
                RoundedCornerShape(8.dp)
            ),
        shape = RoundedCornerShape(8.dp),
        color = LightGrayBg
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFE5E5E5)),
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
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                } else {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.title.replace(Regex("^\\d+\\.\\s*"), ""),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                maxLines = 1
            )

            Surface(
                color = when (item.severity) {
                    "MINOR_SCRATCH" -> Color(0xFFFFF3E0)
                    "MEDIUM_DENT", "MAJOR_DAMAGE" -> Color(0xFFFFEBEE)
                    else -> Color(0xFFE8F5E9)
                },
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = item.severityLabel,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (item.severity) {
                        "MINOR_SCRATCH" -> Color(0xFFE65100)
                        "MEDIUM_DENT", "MAJOR_DAMAGE" -> CrimsonRed
                        else -> StatusGood
                    },
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}

private fun shareDentReportToWhatsApp(
    context: Context,
    vehicleNo: String,
    customerName: String,
    photos: List<DentPhotoItem>
) {
    val sb = StringBuilder()
    sb.append("📋 *GVD AUTO WORLD BANGALORE*\n")
    sb.append("━━━━━━━━━━━━━━━━━━━━\n")
    sb.append("🔍 *6-POINT VEHICLE DENT & BODY INSPECTION REPORT*\n")
    sb.append("🚗 *Vehicle Number:* $vehicleNo\n")
    sb.append("👤 *Customer:* $customerName\n\n")
    sb.append("*CHECK-IN CONDITION LOG:*\n")

    photos.forEachIndexed { i, p ->
        val statusEmoji = if (p.hasDent) "⚠️" else "✅"
        val desc = if (p.notes.isNotBlank()) "Notes: ${p.notes}" else "No remarks"
        sb.append("${i + 1}. $statusEmoji *${p.title}:* ${p.severityLabel}\n   $desc\n\n")
    }

    sb.append("━━━━━━━━━━━━━━━━━━━━\n")
    sb.append("📍 *GVD Auto World Kundalahalli Hub (Whitefield)*\n")
    sb.append("Google Maps: https://maps.app.goo.gl/xgdEGHBKRth1TUrs7\n")
    sb.append("All 6 inspection photos have been digitally verified before service commencement.")

    val msg = sb.toString()
    val uri = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(msg)}")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, msg)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Dent Report"))
    }
}
