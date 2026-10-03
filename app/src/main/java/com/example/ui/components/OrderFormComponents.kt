package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entities.InventoryItemEntity
import com.example.data.local.entities.PurchaseOrderEntity
import com.example.data.local.entities.PurchaseOrderItem
import com.example.data.local.entities.toJsonString
import com.example.data.local.entities.toPurchaseOrderItems
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

/**
 * Interactive dialog displaying the auto-generated Purchase Order Form
 * calculated based on minimum inventory thresholds.
 */
@Composable
fun AutoOrderFormDialog(
    initialOrder: PurchaseOrderEntity,
    viewModel: GvdViewModel,
    onDismiss: () -> Unit,
    onOrderSavedOrReceived: () -> Unit
) {
    val context = LocalContext.current
    val parsedItems = remember(initialOrder) { initialOrder.itemsJson.toPurchaseOrderItems() }
    val editableItems = remember { mutableStateListOf<PurchaseOrderItem>().apply { addAll(parsedItems) } }

    var supplierName by remember { mutableStateOf(initialOrder.supplierName) }
    var supplierContact by remember { mutableStateOf(initialOrder.supplierContact) }
    var notes by remember { mutableStateOf(initialOrder.notes) }

    val subtotal = editableItems.sumOf { it.lineTotal }
    val gst = subtotal * 0.18
    val grandTotal = subtotal + gst
    val totalUnits = editableItems.sumOf { it.orderQuantity }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(20.dp),
            color = LightGrayBg,
            border = BorderStroke(1.dp, LightGrayBorder),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkCrimson),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Purchase Order Form",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = "Auto-Calculated from Minimum Stock",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(LightGraySection)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextDark)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // PO Badge & Supplier Info Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LightGrayCard),
                    border = BorderStroke(1.dp, LightGrayBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = initialOrder.poNumber,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = DarkCrimson
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (initialOrder.status == "RECEIVED") StatusGood.copy(alpha = 0.15f) else Color(0xFFFFF3E0))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = initialOrder.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (initialOrder.status == "RECEIVED") StatusGood else Color(0xFFE65100)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Distributor: $supplierName ($supplierContact)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark
                        )
                        Text(
                            text = "Delivery To: GVD Auto World Bangalore • Kundalahalli Hub (Vibgyor High School Rd)",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Items list header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ORDER ITEMS (${editableItems.size} Line Items, $totalUnits Units)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Text(
                        text = "Threshold Replenishment",
                        fontSize = 11.sp,
                        color = CrimsonRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Line items
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(editableItems) { index, item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = LightGrayCard),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, LightGrayBorder)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.partName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = TextDark
                                        )
                                        Text(
                                            text = "SKU: ${item.partNumber} • Rate: ₹${item.unitPrice.toInt()}",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }

                                    IconButton(
                                        onClick = { editableItems.removeAt(index) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Stock vs Min badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFFFEBEE))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Stock: ${item.currentStock} | Min: ${item.minThreshold}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = CrimsonRed
                                        )
                                    }

                                    // Quantity Stepper
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                if (item.orderQuantity > 1) {
                                                    val newQty = item.orderQuantity - 1
                                                    editableItems[index] = item.copy(
                                                        orderQuantity = newQty,
                                                        lineTotal = newQty * item.unitPrice
                                                    )
                                                }
                                            },
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(LightGraySection)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Dec", modifier = Modifier.size(14.dp), tint = TextDark)
                                        }

                                        Text(
                                            text = "${item.orderQuantity} pcs",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = TextDark,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )

                                        IconButton(
                                            onClick = {
                                                val newQty = item.orderQuantity + 1
                                                editableItems[index] = item.copy(
                                                    orderQuantity = newQty,
                                                    lineTotal = newQty * item.unitPrice
                                                )
                                            },
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(LightGraySection)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Inc", modifier = Modifier.size(14.dp), tint = TextDark)
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Text(
                                            text = "₹${item.lineTotal.toInt()}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = DarkCrimson
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Price Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LightGrayCard),
                    border = BorderStroke(1.dp, LightGrayBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Subtotal ($totalUnits units)", fontSize = 12.sp, color = TextMuted)
                            Text(text = "₹${subtotal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "GST (18% Auto Parts)", fontSize = 12.sp, color = TextMuted)
                            Text(text = "₹${gst.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = LightGrayBorder)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Grand Total Order Value", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text(text = "₹${grandTotal.toInt()}", fontSize = 17.sp, fontWeight = FontWeight.Black, color = DarkCrimson)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Send to WhatsApp
                    Button(
                        onClick = {
                            val updatedPo = initialOrder.copy(
                                totalItemsCount = editableItems.size,
                                totalUnitsCount = totalUnits,
                                subtotal = subtotal,
                                gstAmount = gst,
                                grandTotal = grandTotal,
                                itemsJson = editableItems.toList().toJsonString(),
                                status = "ORDERED"
                            )
                            viewModel.savePurchaseOrder(updatedPo, context)
                            viewModel.sharePurchaseOrder(context, updatedPo, viaWhatsApp = true)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp PO", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Receive Goods (Stock In)
                    if (initialOrder.status != "RECEIVED") {
                        Button(
                            onClick = {
                                val updatedPo = initialOrder.copy(
                                    totalItemsCount = editableItems.size,
                                    totalUnitsCount = totalUnits,
                                    subtotal = subtotal,
                                    gstAmount = gst,
                                    grandTotal = grandTotal,
                                    itemsJson = editableItems.toList().toJsonString()
                                )
                                viewModel.savePurchaseOrder(updatedPo, context)
                                viewModel.receivePurchaseOrder(initialOrder.id, context)
                                onOrderSavedOrReceived()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusGood),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Stock In (+Units)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dialog to add all master workshop inventory or bulk restock.
 */
@Composable
fun AddAllInventoryDialog(
    masterCatalog: List<InventoryItemEntity>,
    currentInventory: List<InventoryItemEntity>,
    onAddAllMaster: () -> Unit,
    onRestockAll: () -> Unit,
    onAddCustomItem: () -> Unit,
    onDismiss: () -> Unit
) {
    val existingPartNumbers = remember(currentInventory) {
        currentInventory.map { it.partNumber.trim().uppercase() }.toSet()
    }
    val missingCount = masterCatalog.count { it.partNumber.trim().uppercase() !in existingPartNumbers }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            shape = RoundedCornerShape(18.dp),
            color = LightGrayBg,
            border = BorderStroke(1.dp, LightGrayBorder),
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkCrimson),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Inventory Provisioning",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextDark
                            )
                            Text(
                                text = "Catalog Import & Bulk Restock",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextDark)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Option 1: Add All Master Inventory Items
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onAddAllMaster()
                            onDismiss()
                        },
                    colors = CardDefaults.cardColors(containerColor = LightGrayCard),
                    border = BorderStroke(1.5.dp, CrimsonRed.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CrimsonRed.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, tint = CrimsonRed)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Add All Master Inventory",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextDark
                            )
                            Text(
                                text = "Imports all 26 official GVD genuine spares, synthetic lubes, fluids, and detailing kits into catalog ($missingCount new items).",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Option 2: Restock All Items
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onRestockAll()
                            onDismiss()
                        },
                    colors = CardDefaults.cardColors(containerColor = LightGrayCard),
                    border = BorderStroke(1.dp, LightGrayBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(StatusGood.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null, tint = StatusGood)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Restock All Inventory (+10 Units)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextDark
                            )
                            Text(
                                text = "Instantly replenishes all current parts and lubes in stock by +10 units to clear shortages.",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Option 3: Add Individual Item
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onAddCustomItem()
                            onDismiss()
                        },
                    colors = CardDefaults.cardColors(containerColor = LightGrayCard),
                    border = BorderStroke(1.dp, LightGrayBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(LightGraySection),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = TextDark)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Add Single Custom Part",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextDark
                            )
                            Text(
                                text = "Specify a custom SKU, part name, category, and minimum stock alert.",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Purchase Order Card displayed in the Purchase Orders history tab.
 */
@Composable
fun PurchaseOrderCard(
    order: PurchaseOrderEntity,
    onViewOrder: () -> Unit,
    onReceiveOrder: () -> Unit,
    onShareOrder: () -> Unit,
    onDeleteOrder: () -> Unit
) {
    val isReceived = order.status == "RECEIVED"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewOrder),
        colors = CardDefaults.cardColors(containerColor = LightGrayCard),
        border = BorderStroke(1.dp, LightGrayBorder),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isReceived) StatusGood.copy(alpha = 0.12f) else DarkCrimson.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isReceived) Icons.Default.CheckCircle else Icons.Default.PendingActions,
                            contentDescription = null,
                            tint = if (isReceived) StatusGood else DarkCrimson,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = order.poNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                        Text(text = "Vendor: ${order.supplierName}", fontSize = 11.sp, color = TextMuted)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isReceived) StatusGood.copy(alpha = 0.15f) else Color(0xFFFFF3E0))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = order.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isReceived) StatusGood else Color(0xFFE65100)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(LightGraySection)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "${order.totalItemsCount} items (${order.totalUnitsCount} units)", fontSize = 11.sp, color = TextDark)
                Text(text = "Total: ₹${order.grandTotal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkCrimson)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDeleteOrder, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.width(4.dp))

                OutlinedButton(
                    onClick = onShareOrder,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF25D366))
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(6.dp))

                if (!isReceived) {
                    Button(
                        onClick = onReceiveOrder,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusGood),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stock In", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = onViewOrder,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("View PO", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
