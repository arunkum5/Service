package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.StatusGood

const val WORKSHOP_LAT = 12.9716
const val WORKSHOP_LNG = 77.6412
const val WORKSHOP_ADDRESS = "Plot 42, 100 Feet Road, HAL 2nd Stage, Indiranagar, Bangalore, Karnataka 560038"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleMapWorkshopLocationScreen(
    onBack: () -> Unit,
    onBookServiceClick: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "GVD Auto World Bangalore",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Google Map Location & Direction • Indiranagar Hub",
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
                    IconButton(onClick = { openGoogleMaps(context) }) {
                        Icon(Icons.Default.NearMe, contentDescription = "Open Maps", tint = Color.White)
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
        ) {
            // Interactive Bangalore Map Canvas
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    BangaloreCityMapCanvas()

                    // Floating GPS Overlay Badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(14.dp)
                            .shadow(4.dp, RoundedCornerShape(8.dp)),
                        color = Color.White.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(StatusGood)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Live Workshop GPS: 12.9716° N, 77.6412° E",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }

                    // Direct Open in Google Maps Floating Action Chip
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(14.dp)
                            .shadow(6.dp, RoundedCornerShape(20.dp))
                            .clickable { openGoogleMaps(context) }
                            .testTag("open_google_maps_chip"),
                        color = CrimsonRed,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Start Navigation",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Workshop Location & Contact Card
            item {
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "GVD Auto World Workshop",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = StatusGood, modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = "Central Bangalore Hub • Indiranagar",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "OPEN NOW",
                                    color = StatusGood,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Full Address
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = WORKSHOP_ADDRESS,
                                fontSize = 13.sp,
                                color = Color.DarkGray,
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Distance & Landmark
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF5F5F7))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Estimated Travel Time", fontSize = 11.sp, color = Color.Gray)
                                Text(text = "approx. 8 mins (2.3 km)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CrimsonRed)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Nearby Landmark", fontSize = 11.sp, color = Color.Gray)
                                Text(text = "Near CMH Metro Station", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Primary Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { openGoogleMaps(context) },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(46.dp)
                                    .testTag("google_maps_directions_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Get Directions", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { callWorkshop(context) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("call_workshop_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call Hub")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { shareLocationViaWhatsApp(context) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Bangalore Location via WhatsApp", color = Color.Black, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Workshop Facilities in Bangalore
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Bangalore Workshop Facilities",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        FacilityRow("6 Multi-Brand Service Bays with Hydraulic Hoists")
                        FacilityRow("Dust-Controlled 9H Ceramic & TPU PPF Detailing Studio")
                        FacilityRow("Computerized 3D Wheel Alignment & Dynamic Balancer")
                        FacilityRow("Cashless Insurance Desk with ICICI Lombard, HDFC Ergo, Digit")
                        FacilityRow("Air-Conditioned Customer Lounge with Live CCTV Bay View")
                        FacilityRow("Free Doorstep Pickup & Drop throughout Bangalore City")
                    }
                }
            }

            // Operating Hours
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Working Hours", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Monday - Saturday:", fontSize = 12.sp, color = Color.DarkGray)
                            Text(text = "08:30 AM – 08:00 PM", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Sunday:", fontSize = 12.sp, color = Color.DarkGray)
                            Text(text = "09:00 AM – 04:00 PM", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun BangaloreCityMapCanvas() {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 16f,
        targetValue = 42f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Google Maps map background color
        drawRect(color = Color(0xFFE8ECEF))

        // Green zones (Cubbon Park / Ulsoor Lake / Defense Colony)
        drawRoundRect(
            color = Color(0xFFC8E6C9),
            topLeft = Offset(w * 0.05f, h * 0.1f),
            size = Size(w * 0.22f, h * 0.35f),
            cornerRadius = CornerRadius(12f, 12f)
        )
        drawRoundRect(
            color = Color(0xFFBBDEFB),
            topLeft = Offset(w * 0.12f, h * 0.55f),
            size = Size(w * 0.18f, h * 0.3f),
            cornerRadius = CornerRadius(20f, 20f)
        )

        // Bangalore City Major Arterial Roads
        val roadColor = Color(0xFFFFFFFF)
        val highwayColor = Color(0xFFFFE0B2)

        // 100 Feet Road (Indiranagar Main Axis)
        drawLine(
            color = highwayColor,
            start = Offset(w * 0.55f, 0f),
            end = Offset(w * 0.55f, h),
            strokeWidth = 24f
        )
        drawLine(
            color = Color(0xFFFFB74D),
            start = Offset(w * 0.55f, 0f),
            end = Offset(w * 0.55f, h),
            strokeWidth = 2f
        )

        // CMH Road Crossing
        drawLine(
            color = roadColor,
            start = Offset(0f, h * 0.48f),
            end = Offset(w, h * 0.48f),
            strokeWidth = 18f
        )

        // Swami Vivekananda Road
        drawLine(
            color = roadColor,
            start = Offset(0f, h * 0.18f),
            end = Offset(w, h * 0.18f),
            strokeWidth = 16f
        )

        // Old Airport Road
        drawLine(
            color = roadColor,
            start = Offset(0f, h * 0.78f),
            end = Offset(w, h * 0.78f),
            strokeWidth = 18f
        )

        // Diagonal Connectors (HAL 2nd Stage)
        drawLine(
            color = roadColor,
            start = Offset(w * 0.2f, h * 0.85f),
            end = Offset(w * 0.75f, h * 0.35f),
            strokeWidth = 12f
        )

        // Navigation Route Blue Polyline (Customer to Workshop)
        val routePath = Path().apply {
            moveTo(w * 0.2f, h * 0.78f)
            lineTo(w * 0.4f, h * 0.78f)
            lineTo(w * 0.55f, h * 0.48f)
        }
        drawPath(
            path = routePath,
            color = Color(0xFF2979FF),
            style = Stroke(width = 8f)
        )

        // User Current Location Dot
        drawCircle(color = Color(0xFF2979FF), radius = 10f, center = Offset(w * 0.2f, h * 0.78f))
        drawCircle(color = Color.White, radius = 5f, center = Offset(w * 0.2f, h * 0.78f))

        // GVD Auto World Location Pin at 100ft Road Indiranagar
        val pinCenter = Offset(w * 0.55f, h * 0.48f)

        // Animated pulse ripple
        drawCircle(
            color = Color(0xFFE53935).copy(alpha = (1f - (pulseScale / 42f)).coerceIn(0f, 0.6f)),
            radius = pulseScale,
            center = pinCenter
        )

        // Pin shadow
        drawCircle(
            color = Color.Black.copy(alpha = 0.25f),
            radius = 12f,
            center = Offset(pinCenter.x, pinCenter.y + 12f)
        )

        // Red Map Marker Pin
        drawCircle(color = Color(0xFFC62828), radius = 18f, center = pinCenter)
        drawCircle(color = Color.White, radius = 7f, center = pinCenter)
    }
}

@Composable
fun FacilityRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Verified, contentDescription = null, tint = StatusGood, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 12.sp, color = Color.DarkGray)
    }
}

fun openGoogleMaps(context: Context) {
    val gmmIntentUri = Uri.parse("geo:$WORKSHOP_LAT,$WORKSHOP_LNG?q=$WORKSHOP_LAT,$WORKSHOP_LNG(GVD+Auto+World+Bangalore)")
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
        setPackage("com.google.android.apps.maps")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        // Fallback to Google Maps Web URL
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$WORKSHOP_LAT,$WORKSHOP_LNG")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(browserIntent)
        } catch (e2: Exception) {
            Toast.makeText(context, "Unable to open Google Maps", Toast.LENGTH_SHORT).show()
        }
    }
}

fun callWorkshop(context: Context) {
    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+918698761486")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(dialIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to open phone dialer", Toast.LENGTH_SHORT).show()
    }
}

fun shareLocationViaWhatsApp(context: Context) {
    val msg = """
        📍 *GVD Auto World Bangalore Workshop*
        $WORKSHOP_ADDRESS
        
        Google Maps Navigation Link:
        https://maps.google.com/?q=$WORKSHOP_LAT,$WORKSHOP_LNG
        
        Working Hours: Mon-Sat 8:30 AM - 8:00 PM
        Contact: +91 8698761486
    """.trimIndent()

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
        context.startActivity(Intent.createChooser(shareIntent, "Share GVD Location"))
    }
}
