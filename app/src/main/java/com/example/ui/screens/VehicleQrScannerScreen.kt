package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TwoWheeler
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.local.entities.JobCardEntity
import com.example.model.VehicleType
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.LightGrayBg
import com.example.ui.theme.LightGrayBorder
import com.example.ui.theme.LightGrayCard
import com.example.ui.theme.StatusGood
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.util.ParsedVehicleQr
import com.example.util.QrCodeUtils
import com.example.viewmodel.GvdViewModel
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleQrScannerScreen(
    viewModel: GvdViewModel,
    onBack: () -> Unit,
    onJobCardFound: (Long) -> Unit,
    onCreateJobCardForVehicle: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val allJobCards by viewModel.allJobCards.collectAsState()

    // Permission state
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            Toast.makeText(context, "Camera permission is needed for live QR scanning", Toast.LENGTH_SHORT).show()
        }
    }

    // Camera control states
    var camera by remember { mutableStateOf<Camera?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }
    var useBackCamera by remember { mutableStateOf(true) }

    // Scan result state
    var scannedRawResult by remember { mutableStateOf<String?>(null) }
    var matchedJobCard by remember { mutableStateOf<JobCardEntity?>(null) }
    var parsedPayload by remember { mutableStateOf<ParsedVehicleQr?>(null) }
    var isProcessingScan by remember { mutableStateOf(false) }

    // Manual search query
    var manualSearchQuery by remember { mutableStateOf("") }

    // Function to trigger vibration feedback
    fun triggerSuccessHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(70)
            }
        } catch (ignored: Exception) {
        }
    }

    // Process a scanned or entered code
    fun handleCodeScanned(code: String) {
        if (code.isBlank() || isProcessingScan) return
        isProcessingScan = true
        scannedRawResult = code
        triggerSuccessHaptic()

        val parsed = QrCodeUtils.parseVehicleQrPayload(code)
        parsedPayload = parsed

        val match = viewModel.findJobCardByQr(code)
        matchedJobCard = match
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Vehicle QR Scanner",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Quick Job Card Pull-up & Inspection",
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
                    // Flashlight / Torch toggle
                    if (hasCameraPermission) {
                        IconButton(
                            onClick = {
                                camera?.let { cam ->
                                    val newState = !isTorchOn
                                    cam.cameraControl.enableTorch(newState)
                                    isTorchOn = newState
                                }
                            }
                        ) {
                            Icon(
                                if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Toggle Torch",
                                tint = if (isTorchOn) Color(0xFFFFD54F) else Color.White
                            )
                        }

                        // Flip camera
                        IconButton(
                            onClick = {
                                useBackCamera = !useBackCamera
                            }
                        ) {
                            Icon(
                                Icons.Default.FlipCameraAndroid,
                                contentDescription = "Switch Camera",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCrimson)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF121418))
        ) {
            if (hasCameraPermission) {
                // Live CameraX Preview View
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        val cameraExecutor = Executors.newSingleThreadExecutor()

                        cameraProviderFuture.addListener({
                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.surfaceProvider = previewView.surfaceProvider
                                }

                                val imageAnalyzer = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()
                                    .also { analysis ->
                                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                            if (!isProcessingScan) {
                                                val decoded = QrCodeUtils.decodeQrFromImageProxy(imageProxy)
                                                if (!decoded.isNullOrBlank()) {
                                                    previewView.post {
                                                        handleCodeScanned(decoded)
                                                    }
                                                }
                                            } else {
                                                imageProxy.close()
                                            }
                                        }
                                    }

                                val cameraSelector = if (useBackCamera) {
                                    CameraSelector.DEFAULT_BACK_CAMERA
                                } else {
                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                }

                                cameraProvider.unbindAll()
                                camera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalyzer
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    update = {
                        // Updates if needed
                    }
                )

                // High-Tech Scanner Reticle & Laser Overlay
                ScannerViewfinderOverlay()
            } else {
                // Camera Permission Rationale Banner
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CrimsonRed.copy(alpha = 0.15f),
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = CrimsonRed,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Camera Permission Required",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Enable camera access to scan vehicle passes and stickers directly with your device's camera.",
                        fontSize = 13.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("grant_camera_permission_btn")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Grant Camera Permission", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Bottom Floating Controls: Manual Search + Quick Workshop Fleet Scans
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xDD121418),
                                Color(0xFF121418)
                            )
                        )
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick Test Scans Bar for Emulator & Staff Fast-Access
                Text(
                    text = "⚡ QUICK TEST SCANS (TAP TO SIMULATE VEHICLE SCAN)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFFFFD54F),
                    letterSpacing = 0.5.sp
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allJobCards.take(6)) { job ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF262C36),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .clickable { handleCodeScanned(job.vehicleNumber) }
                                .testTag("quick_scan_${job.vehicleNumber}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = CrimsonRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Column {
                                    Text(
                                        text = job.vehicleNumber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${job.jobCardNumber} • ${job.make}",
                                        fontSize = 9.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                }

                // Manual Entry Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = manualSearchQuery,
                        onValueChange = { manualSearchQuery = it },
                        placeholder = {
                            Text("Type Vehicle # (e.g. KA04MJ1234) or JC #", fontSize = 12.sp, color = Color.Gray)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("manual_qr_input"),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E222B),
                            unfocusedContainerColor = Color(0xFF1E222B),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = CrimsonRed,
                            unfocusedIndicatorColor = Color.DarkGray
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            if (manualSearchQuery.isNotBlank()) {
                                handleCodeScanned(manualSearchQuery.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("manual_qr_search_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                }
            }

            // Scanned Vehicle Match Modal Card
            AnimatedVisibility(
                visible = isProcessingScan,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(20.dp)
            ) {
                ScannedResultCard(
                    rawCode = scannedRawResult ?: "",
                    matchedJobCard = matchedJobCard,
                    parsedPayload = parsedPayload,
                    onOpenJobCard = { id ->
                        isProcessingScan = false
                        onJobCardFound(id)
                    },
                    onCreateJobCard = { vehNo ->
                        isProcessingScan = false
                        onCreateJobCardForVehicle(vehNo)
                    },
                    onDismiss = {
                        isProcessingScan = false
                        scannedRawResult = null
                        matchedJobCard = null
                    }
                )
            }
        }
    }
}

/**
 * Animated Viewfinder with corner target brackets and moving laser scanline.
 */
@Composable
private fun ScannerViewfinderOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "laser_transition")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_progress"
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val boxWidth = maxWidth
        val boxHeight = maxHeight
        val scanBoxSize = 260.dp

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(scanBoxSize)
                    .testTag("scanner_viewfinder_box"),
                contentAlignment = Alignment.Center
            ) {
                // Reticle corner brackets
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 4.dp.toPx()
                    val cornerLen = 32.dp.toPx()
                    val cornerRadius = 12.dp.toPx()
                    val color = Color(0xFFE53935)

                    // Top-Left corner
                    drawLine(color, Offset(0f, 0f), Offset(cornerLen, 0f), stroke)
                    drawLine(color, Offset(0f, 0f), Offset(0f, cornerLen), stroke)

                    // Top-Right corner
                    drawLine(color, Offset(size.width, 0f), Offset(size.width - cornerLen, 0f), stroke)
                    drawLine(color, Offset(size.width, 0f), Offset(size.width, cornerLen), stroke)

                    // Bottom-Left corner
                    drawLine(color, Offset(0f, size.height), Offset(cornerLen, size.height), stroke)
                    drawLine(color, Offset(0f, size.height), Offset(0f, size.height - cornerLen), stroke)

                    // Bottom-Right corner
                    drawLine(color, Offset(size.width, size.height), Offset(size.width - cornerLen, size.height), stroke)
                    drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - cornerLen), stroke)
                }

                // Laser scan line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .offset(y = ((scanBoxSize.value * laserProgress) - (scanBoxSize.value / 2)).dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFFFF5252),
                                    Color(0xFFFF1744),
                                    Color(0xFFFF5252),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.7f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = CrimsonRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Align Vehicle QR Pass / Sticker Inside Frame",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Result Dialog showing match details and instant action buttons.
 */
@Composable
private fun ScannedResultCard(
    rawCode: String,
    matchedJobCard: JobCardEntity?,
    parsedPayload: ParsedVehicleQr?,
    onOpenJobCard: (Long) -> Unit,
    onCreateJobCard: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("scanned_result_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LightGrayCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dismiss button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        if (matchedJobCard != null) Icons.Default.CheckCircle else Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = if (matchedJobCard != null) StatusGood else CrimsonRed,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = if (matchedJobCard != null) "Vehicle Identified!" else "QR Scanned",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextDark
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (matchedJobCard != null) {
                // Vehicle Plate Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFFD54F),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.Black),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (matchedJobCard.vehicleType == VehicleType.TWO_WHEELER) Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = matchedJobCard.vehicleNumber,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Black,
                            letterSpacing = 1.5.sp
                        )
                    }
                }

                // Details Grid
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, LightGrayBorder, RoundedCornerShape(12.dp)),
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Job Card #", fontSize = 12.sp, color = TextMuted)
                            Text(
                                text = matchedJobCard.jobCardNumber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = DarkCrimson
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Make & Model", fontSize = 12.sp, color = TextMuted)
                            Text(
                                text = "${matchedJobCard.make} ${matchedJobCard.model}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = TextDark
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Customer", fontSize = 12.sp, color = TextMuted)
                            Text(
                                text = "${matchedJobCard.customerName} (${matchedJobCard.customerMobile})",
                                fontSize = 12.sp,
                                color = TextDark
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Status", fontSize = 12.sp, color = TextMuted)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = CrimsonRed.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = matchedJobCard.status.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonRed,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Total / Balance", fontSize = 12.sp, color = TextMuted)
                            Text(
                                text = "₹${matchedJobCard.totalAmount.toInt()} (Due: ₹${matchedJobCard.balanceAmount.toInt()})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Scan Next", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onOpenJobCard(matchedJobCard.id) },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(46.dp)
                            .testTag("pull_up_jobcard_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pull Up Job Card", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Vehicle not found in active Job Cards
                val detectedVeh = parsedPayload?.vehicleNumber ?: rawCode
                Text(
                    text = "No active Job Card found for:",
                    fontSize = 13.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFFD54F),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black)
                ) {
                    Text(
                        text = detectedVeh,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onCreateJobCard(detectedVeh) },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(46.dp)
                            .testTag("create_jobcard_for_vehicle_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Job Card", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
