package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.InventoryItemEntity
import com.example.data.local.entities.PurchaseOrderEntity
import com.example.model.ItemCategory
import com.example.model.VehicleType
import com.example.ui.components.AddAllInventoryDialog
import com.example.ui.components.AutoOrderFormDialog
import com.example.ui.components.PurchaseOrderCard
import com.example.ui.theme.AccentGold
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.LightGrayBg
import com.example.ui.theme.LightGrayBorder
import com.example.ui.theme.LightGrayCard
import com.example.ui.theme.LightGraySection
import com.example.ui.theme.LightGraySurface
import com.example.ui.theme.StatusGood
import com.example.ui.theme.StatusReplace
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.viewmodel.GvdViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryManagementScreen(
    viewModel: GvdViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val allInventory by viewModel.allInventory.collectAsState()
    val lowStockList by viewModel.lowStockInventory.collectAsState()
    val allPurchaseOrders by viewModel.allPurchaseOrders.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Stock Inventory, 1: Purchase Orders & Order Forms
    var selectedFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog States
    var showAddCustomDialog by remember { mutableStateOf(false) }
    var showAddAllInventoryDialog by remember { mutableStateOf(false) }
    var activeOrderFormForViewing by remember { mutableStateOf<PurchaseOrderEntity?>(null) }

    val filteredList = when (selectedFilter) {
        "LOW" -> lowStockList
        "SPARE" -> allInventory.filter { it.category == ItemCategory.SPARE }
        "LUBE" -> allInventory.filter { it.category == ItemCategory.LUBE }
        "LABOUR" -> allInventory.filter { it.category == ItemCategory.LABOUR }
        "DETAILING" -> allInventory.filter { it.category == ItemCategory.DETAILING }
        else -> allInventory
    }.filter {
        searchQuery.isBlank() ||
                it.partName.contains(searchQuery, ignoreCase = true) ||
                it.partNumber.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Inventory & Stock Hub",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Live Stock • Auto Replenishment • Purchase Orders",
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
                    // Quick Action: Add All Inventory
                    IconButton(onClick = { showAddAllInventoryDialog = true }) {
                        Icon(Icons.Default.DoneAll, contentDescription = "Add All Inventory", tint = Color.White)
                    }
                    // Quick Action: Auto Generate PO Form
                    IconButton(onClick = {
                        viewModel.autoGeneratePurchaseOrder(context = context) { generatedPo ->
                            activeOrderFormForViewing = generatedPo
                        }
                    }) {
                        Icon(Icons.Default.PostAdd, contentDescription = "Auto Order Form", tint = AccentGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkCrimson)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddAllInventoryDialog = true },
                containerColor = CrimsonRed,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Inventory")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(LightGrayBg)
        ) {
            // Main Hub Tabs (Stock Inventory vs Purchase Orders)
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = LightGrayCard,
                contentColor = CrimsonRed,
                modifier = Modifier.border(BorderStroke(1.dp, LightGrayBorder))
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Inventory Stock (${allInventory.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Purchase Orders (${allPurchaseOrders.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                )
            }

            if (activeTab == 0) {
                // ================= TAB 0: LIVE INVENTORY MANAGEMENT =================
                // 1. High-Priority Action Banner for "Add All Inventory" & "Auto Order Form"
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = LightGrayCard),
                    border = BorderStroke(1.dp, LightGrayBorder),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Automated Stock Operations",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextDark
                                )
                                Text(
                                    text = "Batch catalog loading & minimum stock replenishment",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }

                            if (lowStockList.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFFEBEE))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "${lowStockList.size} Shortage Alerts",
                                        color = CrimsonRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Button 1: Add All Inventory
                            Button(
                                onClick = { showAddAllInventoryDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkCrimson),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add All Inventory", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Button 2: Automatically Generate Order Form based on Minimum Inventory
                            Button(
                                onClick = {
                                    viewModel.autoGeneratePurchaseOrder(context = context) { generatedPo ->
                                        activeOrderFormForViewing = generatedPo
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Auto Order Form", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Low Stock Warning Banner
                if (lowStockList.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFF3E0),
                        border = BorderStroke(1.dp, Color(0xFFFFCC80))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${lowStockList.size} items below threshold! Deficit needs reordering.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFE65100)
                                )
                            }

                            TextButton(
                                onClick = {
                                    viewModel.autoGeneratePurchaseOrder(context = context) { generatedPo ->
                                        activeOrderFormForViewing = generatedPo
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text("Order Now", color = DarkCrimson, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }

                // Search & Filter Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by part name or SKU...", fontSize = 12.sp, color = TextMuted) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = LightGrayCard,
                            unfocusedContainerColor = LightGrayCard,
                            focusedBorderColor = DarkCrimson,
                            unfocusedBorderColor = LightGrayBorder
                        ),
                        singleLine = true
                    )
                }

                // Category Filter Chips
                ScrollableTabRow(
                    selectedTabIndex = listOf("ALL", "LOW", "SPARE", "LUBE", "DETAILING").indexOf(selectedFilter).coerceAtLeast(0),
                    containerColor = Color.Transparent,
                    edgePadding = 14.dp,
                    divider = {}
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("All (${allInventory.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkCrimson,
                            selectedLabelColor = Color.White,
                            containerColor = LightGrayCard,
                            labelColor = TextDark
                        ),
                        border = BorderStroke(1.dp, if (selectedFilter == "ALL") DarkCrimson else LightGrayBorder),
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    FilterChip(
                        selected = selectedFilter == "LOW",
                        onClick = { selectedFilter = "LOW" },
                        label = { Text("Shortage (${lowStockList.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CrimsonRed,
                            selectedLabelColor = Color.White,
                            containerColor = LightGrayCard,
                            labelColor = TextDark
                        ),
                        border = BorderStroke(1.dp, if (selectedFilter == "LOW") CrimsonRed else LightGrayBorder),
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    FilterChip(
                        selected = selectedFilter == "SPARE",
                        onClick = { selectedFilter = "SPARE" },
                        label = { Text("Spares", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkCrimson,
                            selectedLabelColor = Color.White,
                            containerColor = LightGrayCard,
                            labelColor = TextDark
                        ),
                        border = BorderStroke(1.dp, if (selectedFilter == "SPARE") DarkCrimson else LightGrayBorder),
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    FilterChip(
                        selected = selectedFilter == "LUBE",
                        onClick = { selectedFilter = "LUBE" },
                        label = { Text("Lubes & Oils", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkCrimson,
                            selectedLabelColor = Color.White,
                            containerColor = LightGrayCard,
                            labelColor = TextDark
                        ),
                        border = BorderStroke(1.dp, if (selectedFilter == "LUBE") DarkCrimson else LightGrayBorder),
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    FilterChip(
                        selected = selectedFilter == "DETAILING",
                        onClick = { selectedFilter = "DETAILING" },
                        label = { Text("Detailing & PPF", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkCrimson,
                            selectedLabelColor = Color.White,
                            containerColor = LightGrayCard,
                            labelColor = TextDark
                        ),
                        border = BorderStroke(1.dp, if (selectedFilter == "DETAILING") DarkCrimson else LightGrayBorder),
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Inventory List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        RichInventoryItemCard(
                            item = item,
                            onIssue = { viewModel.adjustStock(item.id, -1, context) },
                            onAddStock = { viewModel.adjustStock(item.id, 5, context) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            } else {
                // ================= TAB 1: PURCHASE ORDERS & REORDER FORMS =================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Quick Action: Generate New PO
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = LightGrayCard),
                        border = BorderStroke(1.dp, LightGrayBorder),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Auto-Generate Order Form",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextDark
                                )
                                Text(
                                    text = "Evaluates all items against minimum stock thresholds and computes exact replenishment quantities.",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.autoGeneratePurchaseOrder(context = context) { generatedPo ->
                                        activeOrderFormForViewing = generatedPo
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New PO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (allPurchaseOrders.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(LightGraySection),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(32.dp), tint = TextMuted)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No Purchase Orders generated yet", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                                Text("Click 'Auto Order Form' above to generate an order based on minimum stock.", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(allPurchaseOrders, key = { it.id }) { po ->
                                PurchaseOrderCard(
                                    order = po,
                                    onViewOrder = { activeOrderFormForViewing = po },
                                    onReceiveOrder = { viewModel.receivePurchaseOrder(po.id, context) },
                                    onShareOrder = { viewModel.sharePurchaseOrder(context, po, viaWhatsApp = true) },
                                    onDeleteOrder = { viewModel.deletePurchaseOrder(po.id, context) }
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(72.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Auto-Generated Order Form
    activeOrderFormForViewing?.let { po ->
        AutoOrderFormDialog(
            initialOrder = po,
            viewModel = viewModel,
            onDismiss = { activeOrderFormForViewing = null },
            onOrderSavedOrReceived = {
                activeOrderFormForViewing = null
            }
        )
    }

    // Modal: Add All Inventory / Master Provisioning
    if (showAddAllInventoryDialog) {
        val masterCatalog = remember { viewModel.allInventory.value } // fallback or empty
        AddAllInventoryDialog(
            masterCatalog = masterCatalog,
            currentInventory = allInventory,
            onAddAllMaster = { viewModel.addAllMasterInventory(context) },
            onRestockAll = { viewModel.restockAllInventory(10, context) },
            onAddCustomItem = { showAddCustomDialog = true },
            onDismiss = { showAddAllInventoryDialog = false }
        )
    }

    // Modal: Add Single Custom Item
    if (showAddCustomDialog) {
        AddNewInventoryDialog(
            onDismiss = { showAddCustomDialog = false },
            onAdd = { newItem ->
                viewModel.addInventoryItem(newItem, context)
                showAddCustomDialog = false
            }
        )
    }
}

/**
 * Rich, user-friendly light gray inventory item card with minimum threshold visibility.
 */
@Composable
fun RichInventoryItemCard(
    item: InventoryItemEntity,
    onIssue: () -> Unit,
    onAddStock: () -> Unit
) {
    val isLowStock = item.stockQuantity <= item.minThresholdAlert
    val deficit = (item.minThresholdAlert - item.stockQuantity).coerceAtLeast(0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightGrayCard),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (isLowStock) CrimsonRed.copy(alpha = 0.5f) else LightGrayBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.partName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextDark
                    )
                    if (isLowStock) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFFEBEE))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SHORTAGE (${deficit}u)",
                                color = CrimsonRed,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "SKU: ${item.partNumber} • Unit: ${item.unit} • ${item.category.name}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Text(
                    text = "Price: ₹${item.unitPrice.toInt()} • Min Alert Threshold: ${item.minThresholdAlert} ${item.unit}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isLowStock) DarkCrimson else TextDark
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${item.stockQuantity}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        color = if (isLowStock) CrimsonRed else StatusGood
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "in stock",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Issue Stock Button
                    Button(
                        onClick = onIssue,
                        colors = ButtonDefaults.buttonColors(containerColor = LightGraySection),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Issue (-1)", color = TextDark, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Add Stock Button
                    Button(
                        onClick = onAddStock,
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "+5", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddNewInventoryDialog(
    onDismiss: () -> Unit,
    onAdd: (InventoryItemEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var partNumber by remember { mutableStateOf("GVD-${System.currentTimeMillis() % 1000}") }
    var selectedCategory by remember { mutableStateOf(ItemCategory.SPARE) }
    var unitPrice by remember { mutableStateOf("450") }
    var stockQuantity by remember { mutableStateOf("10") }
    var lowStockThreshold by remember { mutableStateOf("4") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Single Inventory Item", fontWeight = FontWeight.Bold, color = TextDark) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Part / Lube Name *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = partNumber,
                    onValueChange = { partNumber = it },
                    label = { Text("SKU / Part Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = unitPrice,
                        onValueChange = { unitPrice = it },
                        label = { Text("Selling Price (₹)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = stockQuantity,
                        onValueChange = { stockQuantity = it },
                        label = { Text("Initial Stock") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = lowStockThreshold,
                    onValueChange = { lowStockThreshold = it },
                    label = { Text("Minimum Threshold (Reorder Alert)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val item = InventoryItemEntity(
                            category = selectedCategory,
                            partName = name,
                            partNumber = partNumber,
                            unitPrice = unitPrice.toDoubleOrNull() ?: 0.0,
                            stockQuantity = stockQuantity.toIntOrNull() ?: 0,
                            minThresholdAlert = lowStockThreshold.toIntOrNull() ?: 4
                        )
                        onAdd(item)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
            ) {
                Text("Save Item")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
