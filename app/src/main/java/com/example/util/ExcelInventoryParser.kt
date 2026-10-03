package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.local.entities.InventoryItemEntity
import com.example.model.ItemCategory
import com.example.model.VehicleType
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.ZipInputStream

data class ParsedInventoryItem(
    val partName: String,
    val partNumber: String,
    val category: ItemCategory,
    val compatibleType: VehicleType,
    val unitPrice: Double,
    val quantity: Int,
    val minThreshold: Int = 5,
    val unit: String = "pcs",
    val supplier: String = "OEM Spares Wholesale",
    val invoiceNo: String = ""
)

object ExcelInventoryParser {

    /**
     * Parses an input stream which can be either CSV or XLSX based on mime/extension or content
     */
    fun parseSpreadsheet(
        inputStream: InputStream,
        fileName: String = ""
    ): List<ParsedInventoryItem> {
        return if (fileName.endsWith(".xlsx", ignoreCase = true)) {
            try {
                parseXlsx(inputStream)
            } catch (e: Exception) {
                // fallback to CSV if file extension was inaccurate
                parseCsv(inputStream)
            }
        } else {
            parseCsv(inputStream)
        }
    }

    /**
     * Parses standard CSV file (supports comma, semicolon, tab, and quoted fields)
     */
    fun parseCsv(inputStream: InputStream): List<ParsedInventoryItem> {
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val rows = mutableListOf<List<String>>()
        var line: String? = reader.readLine()

        // Strip UTF-8 BOM if present
        if (line != null && line.startsWith("\uFEFF")) {
            line = line.substring(1)
        }

        while (line != null) {
            val trimmed = line.trim()
            if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                val tokens = parseCsvLine(trimmed)
                if (tokens.isNotEmpty()) {
                    rows.add(tokens)
                }
            }
            line = reader.readLine()
        }

        return mapRowsToInventoryItems(rows)
    }

    /**
     * Parses modern Microsoft Excel OpenXML (.xlsx) file using pure Java standard ZipInputStream & XmlPullParser
     */
    fun parseXlsx(inputStream: InputStream): List<ParsedInventoryItem> {
        val zip = ZipInputStream(inputStream)
        val sharedStrings = mutableListOf<String>()
        val sheetRows = mutableListOf<List<String>>()

        var entry = zip.nextEntry
        val filesInMemory = mutableMapOf<String, ByteArray>()

        while (entry != null) {
            val name = entry.name
            if (name.equals("xl/sharedStrings.xml", ignoreCase = true) ||
                name.equals("xl/worksheets/sheet1.xml", ignoreCase = true)
            ) {
                filesInMemory[name] = zip.readBytes()
            }
            zip.closeEntry()
            entry = zip.nextEntry
        }

        // 1. Read shared strings if present
        filesInMemory["xl/sharedStrings.xml"]?.let { bytes ->
            val parser = XmlPullParserFactory.newInstance().newPullParser()
            parser.setInput(bytes.inputStream(), "UTF-8")
            var event = parser.eventType
            var inTextTag = false
            val currentSb = StringBuilder()

            while (event != XmlPullParser.END_DOCUMENT) {
                when (event) {
                    XmlPullParser.START_TAG -> {
                        if (parser.name.equals("t", ignoreCase = true)) {
                            inTextTag = true
                            currentSb.setLength(0)
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inTextTag) {
                            currentSb.append(parser.text)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name.equals("t", ignoreCase = true)) {
                            inTextTag = false
                            sharedStrings.add(currentSb.toString())
                        }
                    }
                }
                event = parser.next()
            }
        }

        // 2. Read sheet1.xml
        filesInMemory["xl/worksheets/sheet1.xml"]?.let { bytes ->
            val parser = XmlPullParserFactory.newInstance().newPullParser()
            parser.setInput(bytes.inputStream(), "UTF-8")
            var event = parser.eventType

            val currentRow = mutableListOf<String>()
            var currentCellType = ""
            var inValueTag = false
            var inInlineText = false
            val cellValue = StringBuilder()

            while (event != XmlPullParser.END_DOCUMENT) {
                when (event) {
                    XmlPullParser.START_TAG -> {
                        val tagName = parser.name
                        if (tagName.equals("row", ignoreCase = true)) {
                            currentRow.clear()
                        } else if (tagName.equals("c", ignoreCase = true)) {
                            currentCellType = parser.getAttributeValue(null, "t") ?: ""
                            cellValue.setLength(0)
                        } else if (tagName.equals("v", ignoreCase = true)) {
                            inValueTag = true
                        } else if (tagName.equals("t", ignoreCase = true)) {
                            inInlineText = true
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inValueTag || inInlineText) {
                            cellValue.append(parser.text)
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        val tagName = parser.name
                        if (tagName.equals("v", ignoreCase = true)) {
                            inValueTag = false
                        } else if (tagName.equals("t", ignoreCase = true)) {
                            inInlineText = false
                        } else if (tagName.equals("c", ignoreCase = true)) {
                            val raw = cellValue.toString().trim()
                            val text = if (currentCellType == "s") {
                                val index = raw.toIntOrNull()
                                if (index != null && index in sharedStrings.indices) {
                                    sharedStrings[index]
                                } else raw
                            } else {
                                raw
                            }
                            currentRow.add(text)
                        } else if (tagName.equals("row", ignoreCase = true)) {
                            if (currentRow.isNotEmpty()) {
                                sheetRows.add(ArrayList(currentRow))
                            }
                        }
                    }
                }
                event = parser.next()
            }
        }

        return mapRowsToInventoryItems(sheetRows)
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        // Detect delimiter if line contains tabs or semicolons without commas
        val delimiter = if (line.contains('\t') && !line.contains(',')) '\t'
        else if (line.contains(';') && !line.contains(',')) ';'
        else ','

        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    sb.append('"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == delimiter && !inQuotes) {
                tokens.add(sb.toString().trim())
                sb.setLength(0)
            } else {
                sb.append(c)
            }
            i++
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    private fun mapRowsToInventoryItems(rows: List<List<String>>): List<ParsedInventoryItem> {
        if (rows.isEmpty()) return emptyList()

        // Detect header row index
        var headerRowIndex = -1
        var nameCol = -1
        var skuCol = -1
        var catCol = -1
        var qtyCol = -1
        var priceCol = -1
        var typeCol = -1
        var supplierCol = -1
        var invoiceCol = -1

        for (r in 0 until minOf(rows.size, 5)) {
            val row = rows[r]
            for (c in row.indices) {
                val header = row[c].lowercase().replace("_", "").replace(" ", "").replace("-", "")
                if (header.contains("partname") || header.contains("itemname") || header == "item" || header == "description" || header == "part") {
                    nameCol = c
                } else if (header.contains("sku") || header.contains("partno") || header.contains("partnumber") || header.contains("code") || header == "itemno") {
                    skuCol = c
                } else if (header.contains("category") || header.contains("itemcategory")) {
                    catCol = c
                } else if (header.contains("qty") || header.contains("quantity") || header.contains("units") || header.contains("received") || header == "count") {
                    qtyCol = c
                } else if (header.contains("price") || header.contains("rate") || header.contains("cost") || header.contains("unitprice") || header == "mrp") {
                    priceCol = c
                } else if (header.contains("vehicle") || header.contains("type") || header.contains("compatible")) {
                    typeCol = c
                } else if (header.contains("supplier") || header.contains("vendor")) {
                    supplierCol = c
                } else if (header.contains("invoice") || header.contains("bill") || header.contains("dcno")) {
                    invoiceCol = c
                }
            }

            if (nameCol != -1 && (qtyCol != -1 || priceCol != -1 || skuCol != -1)) {
                headerRowIndex = r
                break
            }
        }

        // If no header found, assume standard column order: Name, SKU, Category, Qty, Price, Type
        val dataStartIndex = if (headerRowIndex != -1) headerRowIndex + 1 else 0
        if (nameCol == -1) nameCol = 0
        if (skuCol == -1) skuCol = 1
        if (catCol == -1) catCol = 2
        if (qtyCol == -1) qtyCol = 3
        if (priceCol == -1) priceCol = 4
        if (typeCol == -1) typeCol = 5

        val result = mutableListOf<ParsedInventoryItem>()

        for (r in dataStartIndex until rows.size) {
            val row = rows[r]
            if (row.isEmpty()) continue

            val name = row.getOrNull(nameCol)?.trim().orEmpty()
            if (name.isBlank() || name.equals("total", ignoreCase = true)) continue

            val sku = row.getOrNull(skuCol)?.trim().orEmpty().ifBlank {
                "SKU-${name.take(4).uppercase()}-${(r * 137) % 900 + 100}"
            }

            val categoryStr = row.getOrNull(catCol)?.trim()?.uppercase().orEmpty()
            val category = when {
                categoryStr.contains("LUBE") || categoryStr.contains("OIL") || categoryStr.contains("FLUID") -> ItemCategory.LUBE
                categoryStr.contains("DETAIL") || categoryStr.contains("COAT") || categoryStr.contains("PPF") -> ItemCategory.DETAILING
                categoryStr.contains("LABOUR") || categoryStr.contains("SERVICE") -> ItemCategory.LABOUR
                else -> ItemCategory.SPARE
            }

            val rawQty = row.getOrNull(qtyCol)?.trim()?.replace(",", "")?.replace("pcs", "", ignoreCase = true)?.trim()
            val quantity = rawQty?.toDoubleOrNull()?.toInt() ?: 1

            val rawPrice = row.getOrNull(priceCol)?.trim()?.replace("₹", "")?.replace("Rs.", "")?.replace(",", "")?.trim()
            val unitPrice = rawPrice?.toDoubleOrNull() ?: 500.0

            val typeStr = row.getOrNull(typeCol)?.trim()?.uppercase().orEmpty()
            val vehicleType = if (typeStr.contains("2W") || typeStr.contains("BIKE") || typeStr.contains("TWO")) {
                VehicleType.TWO_WHEELER
            } else {
                VehicleType.FOUR_WHEELER
            }

            val supplier = row.getOrNull(supplierCol)?.trim().orEmpty().ifBlank {
                "Bangalore OEM Spares & Lubricants Wholesale Hub"
            }

            val invoiceNo = row.getOrNull(invoiceCol)?.trim().orEmpty().ifBlank {
                "INV-GVD-2026-OCT"
            }

            result.add(
                ParsedInventoryItem(
                    partName = name,
                    partNumber = sku,
                    category = category,
                    compatibleType = vehicleType,
                    unitPrice = unitPrice,
                    quantity = quantity,
                    minThreshold = 5,
                    unit = if (category == ItemCategory.LUBE) "L" else "pcs",
                    supplier = supplier,
                    invoiceNo = invoiceNo
                )
            )
        }

        return result
    }

    /**
     * Provides a standard CSV template for workshop staff to share with parts distributors
     */
    fun getCsvTemplateText(): String {
        return """
            Part Name,Part Number (SKU),Category,Quantity Received,Unit Price (INR),Vehicle Type,Supplier,Invoice Number
            Brembo Ceramic Front Brake Pads,BRK-BREM-019,SPARE,15,1850.00,4W,Bosch & Brembo Wholesale India,INV-2026-OCT-881
            Castrol EDGE 5W-40 Synthetic Engine Oil (4L),LUB-CAS-5W40,LUBE,20,3200.00,4W,Castrol India Distributors,INV-2026-OCT-881
            Bosch Platinum Spark Plugs (Set of 4),SPK-BOS-PLT4,SPARE,25,920.00,4W,Bosch & Brembo Wholesale India,INV-2026-OCT-881
            Mann Air Filter Element,FLT-MAN-AF02,SPARE,12,650.00,4W,Mann+Hummel Filters,INV-2026-OCT-881
            Motul 7100 4T 10W50 Motorcycle Oil (1L),LUB-MOT-7100,LUBE,30,890.00,2W,Motul Lubricants South,INV-2026-OCT-881
            3M Automotive Ceramic Paint Sealant (500ml),DET-3M-CRM500,DETAILING,10,2400.00,4W,3M Car Care Wholesale,INV-2026-OCT-881
            OEM Suspension Lower Arm Bush Kit,SUS-OEM-LB08,SPARE,8,1450.00,4W,OEM Spares Hub Bangalore,INV-2026-OCT-881
            LED Headlight Bulb H4 6500K 55W,ELE-LED-H4-55,SPARE,18,1250.00,2W,Philips Automotive Lighting,INV-2026-OCT-881
        """.trimIndent()
    }

    /**
     * Generates a realistic mock batch of incoming parts for 1-tap testing
     */
    fun getDemoIncomingPartsBatch(): List<ParsedInventoryItem> {
        return listOf(
            ParsedInventoryItem(
                partName = "Brembo Ceramic Front Brake Pads",
                partNumber = "BRK-BREM-019",
                category = ItemCategory.SPARE,
                compatibleType = VehicleType.FOUR_WHEELER,
                unitPrice = 1850.00,
                quantity = 15,
                unit = "set",
                supplier = "Bosch & Brembo Wholesale India",
                invoiceNo = "INV-2026-OCT-881"
            ),
            ParsedInventoryItem(
                partName = "Castrol EDGE 5W-40 Synthetic Engine Oil (4L)",
                partNumber = "LUB-CAS-5W40",
                category = ItemCategory.LUBE,
                compatibleType = VehicleType.FOUR_WHEELER,
                unitPrice = 3200.00,
                quantity = 20,
                unit = "can",
                supplier = "Castrol India Distributors",
                invoiceNo = "INV-2026-OCT-881"
            ),
            ParsedInventoryItem(
                partName = "Bosch Platinum Spark Plugs (Set of 4)",
                partNumber = "SPK-BOS-PLT4",
                category = ItemCategory.SPARE,
                compatibleType = VehicleType.FOUR_WHEELER,
                unitPrice = 920.00,
                quantity = 25,
                unit = "set",
                supplier = "Bosch & Brembo Wholesale India",
                invoiceNo = "INV-2026-OCT-881"
            ),
            ParsedInventoryItem(
                partName = "Mann Cabin & AC Antibacterial Filter",
                partNumber = "FLT-MAN-AC08",
                category = ItemCategory.SPARE,
                compatibleType = VehicleType.FOUR_WHEELER,
                unitPrice = 750.00,
                quantity = 14,
                unit = "pcs",
                supplier = "Mann+Hummel Filters",
                invoiceNo = "INV-2026-OCT-881"
            ),
            ParsedInventoryItem(
                partName = "Motul 7100 4T 10W50 Motorcycle Oil (1L)",
                partNumber = "LUB-MOT-7100",
                category = ItemCategory.LUBE,
                compatibleType = VehicleType.TWO_WHEELER,
                unitPrice = 890.00,
                quantity = 30,
                unit = "bottle",
                supplier = "Motul Lubricants South",
                invoiceNo = "INV-2026-OCT-881"
            ),
            ParsedInventoryItem(
                partName = "3M Gloss Ceramic Detailer (500ml)",
                partNumber = "DET-3M-CRM500",
                category = ItemCategory.DETAILING,
                compatibleType = VehicleType.FOUR_WHEELER,
                unitPrice = 2400.00,
                quantity = 10,
                unit = "bottle",
                supplier = "3M Car Care Wholesale",
                invoiceNo = "INV-2026-OCT-881"
            ),
            ParsedInventoryItem(
                partName = "Michelin City Pro Tubeless Bike Tire 90/90-19",
                partNumber = "TIR-MCH-9090",
                category = ItemCategory.SPARE,
                compatibleType = VehicleType.TWO_WHEELER,
                unitPrice = 2150.00,
                quantity = 8,
                unit = "pcs",
                supplier = "Michelin India Authorized Depot",
                invoiceNo = "INV-2026-OCT-881"
            ),
            ParsedInventoryItem(
                partName = "OEM Ceramic Front Brake Discs",
                partNumber = "ROT-OEM-FR02",
                category = ItemCategory.SPARE,
                compatibleType = VehicleType.FOUR_WHEELER,
                unitPrice = 4200.00,
                quantity = 6,
                unit = "pair",
                supplier = "OEM Spares Hub Bangalore",
                invoiceNo = "INV-2026-OCT-881"
            )
        )
    }
}
