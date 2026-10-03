package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ItemCategory
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.LightGrayBg
import com.example.ui.theme.LightGrayBorder
import com.example.ui.theme.StatusGood
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import com.example.util.ExcelInventoryParser
import com.example.util.ParsedInventoryItem

@Composable
fun ExcelPartsImportDialog(
    onDismiss: () -> Unit,
    onConfirmImport: (items: List<ParsedInventoryItem>, supplier: String, invoiceNo: String) -> Unit
) {
    val context = LocalContext.current

    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var parsedItems by remember { mutableStateOf<List<ParsedInventoryItem>>(emptyList()) }
    var supplierName by remember { mutableStateOf("Bangalore OEM Spares Wholesale Hub") }
    var invoiceNumber by remember { mutableStateOf("INV-2026-OCT-881") }
    var isParsingError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // File picker launcher supporting .xlsx, .xls, and .csv
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val contentResolver = context.contentResolver
                val inputStream = contentResolver.openInputStream(uri)

                // Try to get file name
                var name = "parts_import.xlsx"
                val cursor = contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            name = it.getString(nameIndex) ?: name
                        }
                    }
                }
                selectedFileName = name

                if (inputStream != null) {
                    val result = ExcelInventoryParser.parseSpreadsheet(inputStream, name)
                    inputStream.close()

                    if (result.isNotEmpty()) {
                        parsedItems = result
                        isParsingError = false
                        if (result.firstOrNull()?.supplier?.isNotBlank() == true) {
                            supplierName = result.first().supplier
                        }
                        if (result.firstOrNull()?.invoiceNo?.isNotBlank() == true) {
                            invoiceNumber = result.first().invoiceNo
                        }
                        Toast.makeText(context, "Parsed ${result.size} parts from $name!", Toast.LENGTH_SHORT).show()
                    } else {
                        isParsingError = true
                        errorMessage = "No parts rows detected. Please check columns format."
                    }
                }
            } catch (e: Exception) {
                isParsingError = true
                errorMessage = "Error reading file: ${e.message}"
            }
        }
    }

    val totalUnits = parsedItems.sumOf { it.quantity }
    val totalValuation = parsedItems.sumOf { it.quantity * it.unitPrice }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .padding(vertical = 12.dp)
                .testTag("excel_import_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Dialog Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(DarkCrimson.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileUpload,
                                contentDescription = null,
                                tint = CrimsonRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Import Parts to Main Inventory",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextDark
                            )
                            Text(
                                text = "Upload Excel (.xlsx) or CSV delivery list",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Upload & Quick Action Area
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = LightGrayBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, LightGrayBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    filePickerLauncher.launch("*/*")
                                },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(44.dp)
                                    .testTag("select_excel_file_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Select File (.xlsx / .csv)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val demoList = ExcelInventoryParser.getDemoIncomingPartsBatch()
                                    parsedItems = demoList
                                    selectedFileName = "Incoming_OEM_Parts_Invoice_OCT2026.xlsx"
                                    supplierName = "Bosch & Castrol South India Depot"
                                    invoiceNumber = "INV-2026-OCT-881"
                                    isParsingError = false
                                    Toast.makeText(context, "Loaded demo delivery list (8 parts, 121 units)", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("demo_invoice_load_btn"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = CrimsonRed)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Load Demo Invoice", fontSize = 11.sp, color = CrimsonRed)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Template action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedFileName != null) "Selected: $selectedFileName" else "Supports .xlsx, .xls, .csv files from vendors",
                                fontSize = 11.sp,
                                color = if (selectedFileName != null) CrimsonRed else TextMuted,
                                fontWeight = if (selectedFileName != null) FontWeight.Bold else FontWeight.Normal
                            )

                            TextButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Parts CSV Template", ExcelInventoryParser.getCsvTemplateText())
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Excel CSV Template copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.DarkGray)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Excel Template", fontSize = 10.sp, color = Color.DarkGray)
                            }
                        }
                    }
                }

                if (isParsingError) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = errorMessage, color = CrimsonRed, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Supplier & Invoice details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = supplierName,
                        onValueChange = { supplierName = it },
                        label = { Text("Supplier / Vendor Name", fontSize = 11.sp) },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                    )

                    OutlinedTextField(
                        value = invoiceNumber,
                        onValueChange = { invoiceNumber = it },
                        label = { Text("Invoice / DC Number", fontSize = 11.sp) },
                        modifier = Modifier.weight(0.8f),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Summary chips
                if (parsedItems.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = StatusGood.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("PARTS DETECTED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = StatusGood)
                                Text("${parsedItems.size} SKUs", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = StatusGood)
                            }
                        }

                        Surface(
                            color = Color(0xFFE3F2FD),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("TOTAL UNITS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                                Text("$totalUnits pcs", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1976D2))
                            }
                        }

                        Surface(
                            color = Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("INWARD VALUE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                Text("₹ ${totalValuation.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFE65100))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Parsed items list
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (parsedItems.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = Color.LightGray,
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No file uploaded yet", color = Color.Gray, fontSize = 13.sp)
                                    Text("Select an Excel/CSV file or tap 'Load Demo Invoice' to test", color = Color.LightGray, fontSize = 11.sp)
                                }
                            }
                        }
                    } else {
                        itemsIndexed(parsedItems) { index, item ->
                            ParsedItemRow(index = index + 1, item = item)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (parsedItems.isEmpty()) {
                                Toast.makeText(context, "Please upload or load a parts list first", Toast.LENGTH_SHORT).show()
                            } else {
                                onConfirmImport(parsedItems, supplierName, invoiceNumber)
                            }
                        },
                        enabled = parsedItems.isNotEmpty(),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(46.dp)
                            .testTag("confirm_import_parts_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add to Main Inventory", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ParsedItemRow(
    index: Int,
    item: ParsedInventoryItem
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = LightGrayBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, LightGrayBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$index.",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.width(22.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.partName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = TextDark,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SKU: ${item.partNumber}",
                        fontSize = 10.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${item.category.name}",
                        fontSize = 10.sp,
                        color = when (item.category) {
                            ItemCategory.SPARE -> CrimsonRed
                            ItemCategory.LUBE -> Color(0xFF1976D2)
                            ItemCategory.DETAILING -> Color(0xFF7B1FA2)
                            ItemCategory.LABOUR -> Color(0xFFE65100)
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${item.quantity} ${item.unit}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = StatusGood
                )
                Text(
                    text = "@ ₹${item.unitPrice.toInt()}/ea",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
