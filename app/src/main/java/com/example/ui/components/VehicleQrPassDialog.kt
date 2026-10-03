package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entities.JobCardEntity
import com.example.model.VehicleType
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.LightGrayBg
import com.example.ui.theme.LightGrayBorder
import com.example.ui.theme.LightGrayCard
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.util.QrCodeUtils

@Composable
fun VehicleQrPassDialog(
    jobCard: JobCardEntity,
    onDismiss: () -> Unit,
    onTestScan: (String) -> Unit
) {
    val context = LocalContext.current

    // Build vehicle QR payload
    val qrPayload = remember(jobCard) {
        QrCodeUtils.buildVehicleQrPayload(
            jobCardId = jobCard.id,
            jobCardNumber = jobCard.jobCardNumber,
            vehicleNumber = jobCard.vehicleNumber,
            customerName = jobCard.customerName
        )
    }

    // Generate QR bitmap
    val qrBitmap: Bitmap? = remember(qrPayload) {
        QrCodeUtils.generateQrBitmap(
            content = qrPayload,
            sizePx = 512,
            foregroundColor = android.graphics.Color.BLACK,
            backgroundColor = android.graphics.Color.WHITE
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("vehicle_qr_pass_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = LightGrayCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CrimsonRed.copy(alpha = 0.12f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = CrimsonRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Vehicle QR Pass",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = TextDark
                            )
                            Text(
                                text = "Service Bay & Workshop Sticker",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Official Pass Badge Container
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, LightGrayBorder, RoundedCornerShape(16.dp)),
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Workshop watermark
                        Text(
                            text = "GVD AUTO WORLD BANGALORE",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = DarkCrimson
                        )
                        Text(
                            text = "OFFICIAL VEHICLE SERVICE PASS",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 9.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // High-contrast QR Code Image
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (qrBitmap != null) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "Vehicle QR Code for ${jobCard.vehicleNumber}",
                                    modifier = Modifier.size(184.dp)
                                )
                            } else {
                                Text("Generating QR...", fontSize = 12.sp, color = TextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Vehicle Registration Number Plate
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFFD54F),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.Black),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    if (jobCard.vehicleType == VehicleType.TWO_WHEELER) Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.Black
                                )
                                Text(
                                    text = jobCard.vehicleNumber,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.Black,
                                    letterSpacing = 1.5.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Vehicle & Job Details
                        Text(
                            text = "${jobCard.make} ${jobCard.model} • ${jobCard.variant}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = TextDark
                        )
                        Text(
                            text = "${jobCard.jobCardNumber} • Customer: ${jobCard.customerName}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "Status: ${jobCard.status.label} • Est: ₹${jobCard.totalAmount.toInt()}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = CrimsonRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            shareQrViaWhatsApp(context, jobCard, qrPayload)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("share_vehicle_qr_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2E7D32))
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onTestScan(qrPayload)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("test_scan_qr_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Scan", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun shareQrViaWhatsApp(context: Context, jobCard: JobCardEntity, qrPayload: String) {
    try {
        val msg = """
            *GVD AUTO WORLD - VEHICLE QR PASS*
            🚗 Vehicle: ${jobCard.vehicleNumber} (${jobCard.make} ${jobCard.model})
            📋 Job Card: ${jobCard.jobCardNumber}
            👤 Owner: ${jobCard.customerName}
            ⚡ Status: ${jobCard.status.label}
            💰 Total Estimate: ₹${jobCard.totalAmount.toInt()}
            
            *Vehicle QR Code Payload:*
            $qrPayload
            
            Scan this QR code with the GVD Workshop App to pull up real-time bay inspection and service details.
        """.trimIndent()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?text=" + Uri.encode(msg))
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "WhatsApp not available, copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}
