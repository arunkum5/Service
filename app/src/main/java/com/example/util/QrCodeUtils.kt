package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import androidx.camera.core.ImageProxy
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeWriter
import org.json.JSONObject

data class ParsedVehicleQr(
    val rawText: String,
    val jobCardId: Long? = null,
    val jobCardNumber: String? = null,
    val vehicleNumber: String? = null
)

object QrCodeUtils {

    /**
     * Generates a QR Code Bitmap from given content string using ZXing.
     */
    fun generateQrBitmap(
        content: String,
        sizePx: Int = 512,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE
    ): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to 1
            )
            val bitMatrix: BitMatrix = QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                sizePx,
                sizePx,
                hints
            )
            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) foregroundColor else backgroundColor
                }
            }
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Creates a standardized vehicle QR pass payload string.
     */
    fun buildVehicleQrPayload(
        jobCardId: Long,
        jobCardNumber: String,
        vehicleNumber: String,
        customerName: String
    ): String {
        return try {
            val json = JSONObject()
            json.put("app", "GVD_AUTO_WORLD")
            json.put("type", "VEHICLE_JOB_CARD")
            json.put("jobCardId", jobCardId)
            json.put("jobCardNumber", jobCardNumber)
            json.put("vehicleNumber", vehicleNumber.uppercase().trim())
            json.put("customerName", customerName)
            json.toString()
        } catch (e: Exception) {
            "GVD:JC:$jobCardId:VEHICLE:$vehicleNumber"
        }
    }

    /**
     * Decodes a QR code from a CameraX ImageProxy frame.
     */
    fun decodeQrFromImageProxy(imageProxy: ImageProxy): String? {
        return try {
            val planes = imageProxy.planes
            if (planes.isEmpty()) return null
            val buffer = planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)

            val width = imageProxy.width
            val height = imageProxy.height

            val source = PlanarYUVLuminanceSource(
                bytes,
                width,
                height,
                0,
                0,
                width,
                height,
                false
            )
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val hints = mapOf(
                DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE),
                DecodeHintType.TRY_HARDER to true
            )
            val reader = MultiFormatReader().apply { setHints(hints) }
            val result = reader.decodeWithState(binaryBitmap)
            result.text
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Intelligently parses raw scanned content into vehicle / job card identifiers.
     */
    fun parseVehicleQrPayload(raw: String): ParsedVehicleQr {
        val trimmed = raw.trim()

        // 1. Try parsing JSON
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            try {
                val json = JSONObject(trimmed)
                val jcId = if (json.has("jobCardId")) json.optLong("jobCardId") else null
                val jcNumber = json.optString("jobCardNumber", null)
                val vehNo = json.optString("vehicleNumber", json.optString("regNo", null))
                return ParsedVehicleQr(
                    rawText = trimmed,
                    jobCardId = if (jcId != null && jcId > 0) jcId else null,
                    jobCardNumber = jcNumber?.ifBlank { null },
                    vehicleNumber = vehNo?.ifBlank { null }
                )
            } catch (ignored: Exception) {
            }
        }

        // 2. Try URI or colon format: GVD:JC:123 or GVD:VEHICLE:KA04MJ1234
        if (trimmed.startsWith("GVD:", ignoreCase = true)) {
            val parts = trimmed.split(":")
            var jcId: Long? = null
            var jcNo: String? = null
            var vehNo: String? = null

            for (i in parts.indices) {
                when (parts[i].uppercase()) {
                    "JC", "JOBCARD" -> {
                        if (i + 1 < parts.size) {
                            val v = parts[i + 1]
                            jcId = v.toLongOrNull()
                            if (jcId == null) jcNo = v
                        }
                    }
                    "VEHICLE", "REGNO" -> {
                        if (i + 1 < parts.size) {
                            vehNo = parts[i + 1]
                        }
                    }
                }
            }
            return ParsedVehicleQr(
                rawText = trimmed,
                jobCardId = jcId,
                jobCardNumber = jcNo,
                vehicleNumber = vehNo
            )
        }

        // 3. Try URL format: https://gvdautoworld.com/jobcard/123 or .../vehicle/KA04MJ1234
        if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            val lastSegment = trimmed.substringAfterLast("/")
            val isNumeric = lastSegment.toLongOrNull() != null
            if (isNumeric) {
                return ParsedVehicleQr(rawText = trimmed, jobCardId = lastSegment.toLongOrNull())
            } else if (trimmed.contains("jobcard", ignoreCase = true)) {
                return ParsedVehicleQr(rawText = trimmed, jobCardNumber = lastSegment)
            } else {
                return ParsedVehicleQr(rawText = trimmed, vehicleNumber = lastSegment)
            }
        }

        // 4. Check if it's purely a Job Card Number pattern (e.g. JC-8921 or JC1234)
        if (trimmed.startsWith("JC-", ignoreCase = true) || trimmed.startsWith("JC", ignoreCase = true) && trimmed.length <= 10) {
            return ParsedVehicleQr(rawText = trimmed, jobCardNumber = trimmed)
        }

        // 5. Default: treat as Vehicle Registration Number or general code
        return ParsedVehicleQr(
            rawText = trimmed,
            vehicleNumber = trimmed
        )
    }
}
