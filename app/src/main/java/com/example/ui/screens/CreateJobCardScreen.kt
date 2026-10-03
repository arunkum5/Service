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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.data.local.entities.JobCardItemEntity
import com.example.model.ItemCategory
import com.example.model.VehicleType
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.StatusGood
import com.example.viewmodel.GvdViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateJobCardScreen(
    viewModel: GvdViewModel,
    onBack: () -> Unit,
    onJobCardCreated: (Long) -> Unit
) {
    val context = LocalContext.current
    val draft by viewModel.jobCardDraft.collectAsState()
    var currentStep by remember { mutableIntStateOf(1) } // 1: Vehicle & Customer, 2: Odometer & Voice, 3: Spares & Labour

    var showAddItemDialog by remember { mutableStateOf(false) }
    var selectedItemCategory by remember { mutableStateOf(ItemCategory.SPARE) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create JobCard",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 1) currentStep-- else onBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkCrimson
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF7F8FA))
        ) {
            // Step Progress Indicator
            StepProgressHeader(currentStep = currentStep)

            when (currentStep) {
                1 -> {
                    Step1VehicleAndCustomer(
                        draft = draft,
                        onUpdateDraft = { viewModel.updateDraft(it) },
                        onNext = {
                            if (draft.vehicleNumber.isBlank() || draft.customerName.isBlank() || draft.customerMobile.isBlank()) {
                                Toast.makeText(context, "Vehicle number, customer name and mobile are required", Toast.LENGTH_SHORT).show()
                            } else {
                                currentStep = 2
                            }
                        }
                    )
                }
                2 -> {
                    Step2OdometerAndIssues(
                        draft = draft,
                        onUpdateDraft = { viewModel.updateDraft(it) },
                        onBack = { currentStep = 1 },
                        onNext = { currentStep = 3 }
                    )
                }
                3 -> {
                    Step3SparesAndLabour(
                        draft = draft,
                        onUpdateDraft = { viewModel.updateDraft(it) },
                        onAddItemClick = { category ->
                            selectedItemCategory = category
                            showAddItemDialog = true
                        },
                        onRemoveItem = { index -> viewModel.removeDraftItem(index) },
                        onBack = { currentStep = 2 },
                        onSubmit = {
                            viewModel.submitJobCard(context) { id ->
                                onJobCardCreated(id)
                            }
                        }
                    )
                }
            }
        }
    }

    if (showAddItemDialog) {
        AddItemDialog(
            defaultCategory = selectedItemCategory,
            onDismiss = { showAddItemDialog = false },
            onAdd = { item ->
                viewModel.addDraftItem(item)
                showAddItemDialog = false
            }
        )
    }
}

@Composable
fun StepProgressHeader(currentStep: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepChip(step = 1, title = "Vehicle & Customer", isActive = currentStep == 1, isDone = currentStep > 1)
            Box(modifier = Modifier.width(20.dp).height(2.dp).background(if (currentStep > 1) CrimsonRed else Color.LightGray))
            StepChip(step = 2, title = "Odo & Issues", isActive = currentStep == 2, isDone = currentStep > 2)
            Box(modifier = Modifier.width(20.dp).height(2.dp).background(if (currentStep > 2) CrimsonRed else Color.LightGray))
            StepChip(step = 3, title = "Spares & Labour", isActive = currentStep == 3, isDone = currentStep > 3)
        }
    }
}

@Composable
fun StepChip(step: Int, title: String, isActive: Boolean, isDone: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isDone || isActive) CrimsonRed else Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "$step", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) CrimsonRed else Color.DarkGray
        )
    }
}

// STEP 1: Matches Screenshot 2 & 3
@Composable
fun Step1VehicleAndCustomer(
    draft: com.example.viewmodel.CreateJobCardDraft,
    onUpdateDraft: ((com.example.viewmodel.CreateJobCardDraft) -> com.example.viewmodel.CreateJobCardDraft) -> Unit,
    onNext: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Vehicle Details Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFFF3E0))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFE65100)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Vehicle Details", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = draft.vehicleNumber,
                        onValueChange = { num -> onUpdateDraft { it.copy(vehicleNumber = num.uppercase()) } },
                        label = { Text("Vehicle Number * (e.g. MH12DY7698)") },
                        modifier = Modifier.fillMaxWidth().testTag("vehicle_number_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "Vehicle Type:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = draft.vehicleType == VehicleType.TWO_WHEELER,
                                onCheckedChange = { if (it) onUpdateDraft { d -> d.copy(vehicleType = VehicleType.TWO_WHEELER) } }
                            )
                            Text("2W (Bike/Scooter)", fontSize = 13.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = draft.vehicleType == VehicleType.FOUR_WHEELER,
                                onCheckedChange = { if (it) onUpdateDraft { d -> d.copy(vehicleType = VehicleType.FOUR_WHEELER) } }
                            )
                            Text("4W (Car/SUV)", fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = draft.make,
                            onValueChange = { m -> onUpdateDraft { it.copy(make = m) } },
                            label = { Text("Make (e.g. Bajaj, Tata)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = draft.model,
                            onValueChange = { m -> onUpdateDraft { it.copy(model = m) } },
                            label = { Text("Model (e.g. Avenger, Nexon)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = draft.variant,
                        onValueChange = { v -> onUpdateDraft { it.copy(variant = v) } },
                        label = { Text("Variant (e.g. 150 Street, XZ+ Dark)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        // Customer Details Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF2E7D32)))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Customer Details", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Add from contacts", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = draft.customerName,
                        onValueChange = { name -> onUpdateDraft { it.copy(customerName = name) } },
                        label = { Text("Full Name * (e.g. Ashay Kohad)") },
                        modifier = Modifier.fillMaxWidth().testTag("customer_name_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = draft.customerMobile,
                        onValueChange = { mob -> onUpdateDraft { it.copy(customerMobile = mob) } },
                        label = { Text("Mobile Number * (e.g. 8698761486)") },
                        modifier = Modifier.fillMaxWidth().testTag("customer_mobile_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = draft.customerEmail,
                        onValueChange = { email -> onUpdateDraft { it.copy(customerEmail = email) } },
                        label = { Text("Email (e.g. customer@gmail.com)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        item {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("step1_next_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Next: Odometer & Fuel Reading", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

// STEP 2: Matches Screenshot 4
@Composable
fun Step2OdometerAndIssues(
    draft: com.example.viewmodel.CreateJobCardDraft,
    onUpdateDraft: ((com.example.viewmodel.CreateJobCardDraft) -> com.example.viewmodel.CreateJobCardDraft) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    var odoText by remember { mutableStateOf(if (draft.odometerKm > 0) draft.odometerKm.toString() else "25625") }
    var fuelSlider by remember { mutableFloatStateOf(draft.fuelLevelPercent.toFloat()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Vehicle Profile Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
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
                            .background(Color(0xFFC8E6C9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (draft.vehicleType == VehicleType.TWO_WHEELER) Icons.Default.TwoWheeler else Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = draft.customerName.ifBlank { "Customer" }, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "${draft.vehicleNumber} • ${draft.make} ${draft.model}", fontSize = 12.sp, color = Color.DarkGray)
                        Text(text = draft.customerMobile, fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Odometer & Fuel Level
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Odometer Reading", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = odoText,
                            onValueChange = {
                                odoText = it
                                val km = it.toIntOrNull() ?: 0
                                onUpdateDraft { d -> d.copy(odometerKm = km) }
                            },
                            label = { Text("KM Reading *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Fuel Level", fontSize = 11.sp, color = Color.Gray)
                                Text(text = "${fuelSlider.toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CrimsonRed)
                            }
                            Slider(
                                value = fuelSlider,
                                onValueChange = {
                                    fuelSlider = it
                                    onUpdateDraft { d -> d.copy(fuelLevelPercent = it.toInt()) }
                                },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(thumbColor = CrimsonRed, activeTrackColor = CrimsonRed)
                            )
                        }
                    }
                }
            }
        }

        // Quick Tag Buttons (Vehicle Accessories, Customer Voice, Dent Photos, Dent Marks)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Vehicle Information and Issues", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onUpdateDraft { it.copy(accessories = "Luggage Carrier, Engine Guard, Mobile Holder") }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Accessories", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                onUpdateDraft { it.copy(dentNotes = "Minor scratch left fairing, bumper intact") }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Dent Marks", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = draft.customerVoice,
                        onValueChange = { voice -> onUpdateDraft { it.copy(customerVoice = voice) } },
                        label = { Text("Customer Voice / Problem Statement *") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = draft.accessories,
                        onValueChange = { acc -> onUpdateDraft { it.copy(accessories = acc) } },
                        label = { Text("Vehicle Inventory Checklist / Accessories") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = draft.dentNotes,
                        onValueChange = { dents -> onUpdateDraft { it.copy(dentNotes = dents) } },
                        label = { Text("Dent / Scratch Inspection Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Back")
                }

                Button(
                    onClick = onNext,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Next: Spares & Labour", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// STEP 3: Matches Screenshot 5
@Composable
fun Step3SparesAndLabour(
    draft: com.example.viewmodel.CreateJobCardDraft,
    onUpdateDraft: ((com.example.viewmodel.CreateJobCardDraft) -> com.example.viewmodel.CreateJobCardDraft) -> Unit,
    onAddItemClick: (ItemCategory) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    var activeTab by remember { mutableIntStateOf(1) } // 0: Jobs/Labour, 1: Spare, 2: Lube
    var advanceText by remember { mutableStateOf(if (draft.advancePaid > 0) draft.advancePaid.toString() else "500") }
    var deliveryDateTimeText by remember { mutableStateOf(draft.deliveryDateTime.ifBlank { "2026-09-24 17:30" }) }
    var isSmsAlert by remember { mutableStateOf(draft.isSmsAlertEnabled) }

    val spares = draft.items.filter { it.category == ItemCategory.SPARE }
    val labour = draft.items.filter { it.category == ItemCategory.LABOUR }
    val lubes = draft.items.filter { it.category == ItemCategory.LUBE || it.category == ItemCategory.DETAILING }

    val totalSpares = spares.sumOf { it.totalAmount }
    val totalLabour = labour.sumOf { it.totalAmount }
    val totalLubes = lubes.sumOf { it.totalAmount }
    val grandTotal = totalSpares + totalLabour + totalLubes

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Tab Row: Jobs, Spare, Lube with subtotals
        item {
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = Color.White,
                contentColor = CrimsonRed
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Jobs (Labour)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("₹ ${totalLabour.toInt()}", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Spare Parts", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("₹ ${totalSpares.toInt()}", fontSize = 11.sp, color = CrimsonRed)
                        }
                    }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Lubes & Oil", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("₹ ${totalLubes.toInt()}", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                )
            }
        }

        // Section for selected category items
        item {
            val (currentCategory, categoryTitle) = when (activeTab) {
                0 -> Pair(ItemCategory.LABOUR, "Labour Charges List")
                1 -> Pair(ItemCategory.SPARE, "Spares List")
                else -> Pair(ItemCategory.LUBE, "Lube & Detailing List")
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE3F2FD))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = categoryTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1565C0))
                        Button(
                            onClick = { onAddItemClick(currentCategory) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("+ Add Item", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val itemsInTab = draft.items.filter {
                        if (activeTab == 2) it.category == ItemCategory.LUBE || it.category == ItemCategory.DETAILING
                        else it.category == currentCategory
                    }

                    if (itemsInTab.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No items added in this category yet. Tap '+ Add Item'.", fontSize = 12.sp, color = Color.Gray)
                        }
                    } else {
                        itemsInTab.forEachIndexed { idx, itm ->
                            ItemRowView(
                                item = itm,
                                onDelete = {
                                    val realIndex = draft.items.indexOf(itm)
                                    if (realIndex >= 0) onRemoveItem(realIndex)
                                }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }

        // Totals & Estimate
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Estimate (Spares + Labour + Lube):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "₹ ${grandTotal.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = CrimsonRed)
                    }
                }
            }
        }

        // Other Details (Advance, Delivery Date/Time, SMS Alert)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Other Details", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = deliveryDateTimeText,
                            onValueChange = {
                                deliveryDateTimeText = it
                                onUpdateDraft { d -> d.copy(deliveryDateTime = it) }
                            },
                            label = { Text("Delivery Date & Time") },
                            modifier = Modifier.weight(1.3f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = advanceText,
                            onValueChange = {
                                advanceText = it
                                val adv = it.toDoubleOrNull() ?: 0.0
                                onUpdateDraft { d -> d.copy(advancePaid = adv) }
                            },
                            label = { Text("Advance (₹)") },
                            modifier = Modifier.weight(0.9f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "WhatsApp & SMS Alerts", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "Automatically dispatch job card to customer phone", fontSize = 11.sp, color = Color.Gray)
                        }

                        Switch(
                            checked = isSmsAlert,
                            onCheckedChange = {
                                isSmsAlert = it
                                onUpdateDraft { d -> d.copy(isSmsAlertEnabled = it) }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = CrimsonRed, checkedTrackColor = Color(0xFFFFCDD2))
                        )
                    }
                }
            }
        }

        // Create Button
        item {
            Button(
                onClick = onSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_create_jobcard_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Create JobCard & Send Alert", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ItemRowView(
    item: JobCardItemEntity,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF9F9FB),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "Qty: ${item.quantity} * Unit Price: ₹${item.unitPrice.toInt()}", fontSize = 11.sp, color = Color.Gray)
            }

            Text(text = "₹ ${item.totalAmount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun AddItemDialog(
    defaultCategory: ItemCategory,
    onDismiss: () -> Unit,
    onAdd: (JobCardItemEntity) -> Unit
) {
    var name by remember {
        mutableStateOf(
            when (defaultCategory) {
                ItemCategory.SPARE -> "AIR FILTER BIG"
                ItemCategory.LABOUR -> "SERVICING MAJOR"
                ItemCategory.LUBE -> "MOTUL 7100 10W50 (1L)"
                ItemCategory.DETAILING -> "9H Ceramic Coating Application"
            }
        )
    }
    var unitPrice by remember {
        mutableStateOf(
            when (defaultCategory) {
                ItemCategory.SPARE -> "205"
                ItemCategory.LABOUR -> "800"
                ItemCategory.LUBE -> "450"
                ItemCategory.DETAILING -> "3500"
            }
        )
    }
    var quantity by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Item to Estimate", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unitPrice,
                        onValueChange = { unitPrice = it },
                        label = { Text("Unit Price (₹)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantity.toIntOrNull() ?: 1
                    val price = unitPrice.toDoubleOrNull() ?: 0.0
                    val total = qty * price
                    val item = JobCardItemEntity(
                        jobCardId = 0,
                        category = defaultCategory,
                        name = name,
                        quantity = qty,
                        unitPrice = price,
                        totalAmount = total
                    )
                    onAdd(item)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
