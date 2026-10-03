package com.example.model

import org.json.JSONArray
import org.json.JSONObject

data class DentPhotoItem(
    val angleIndex: Int, // 0 to 5
    val angleKey: String, // FRONT, REAR, LEFT_SIDE, RIGHT_SIDE, ROOF_GLASS, CLOSEUP_DENT
    val title: String,
    val description: String,
    val photoUri: String = "",
    val hasDent: Boolean = false,
    val severity: String = "NO_DENT", // NO_DENT, MINOR_SCRATCH, MEDIUM_DENT, MAJOR_DAMAGE
    val notes: String = ""
) {
    val severityLabel: String
        get() = when (severity) {
            "MINOR_SCRATCH" -> "Minor Scratch"
            "MEDIUM_DENT" -> "Medium Dent"
            "MAJOR_DAMAGE" -> "Major Damage"
            else -> "No Dents / Clear"
        }
}

fun getDefaultDentInspectionAngles(): List<DentPhotoItem> = listOf(
    DentPhotoItem(
        angleIndex = 0,
        angleKey = "FRONT",
        title = "1. Front Angle",
        description = "Front Bumper, Hood, Grille, Headlights & Fog Lamps"
    ),
    DentPhotoItem(
        angleIndex = 1,
        angleKey = "REAR",
        title = "2. Rear Angle",
        description = "Boot / Tailgate, Rear Bumper, Taillights & Exhaust Tip"
    ),
    DentPhotoItem(
        angleIndex = 2,
        angleKey = "LEFT_SIDE",
        title = "3. Left Side Profile",
        description = "Left Doors, Front/Rear Fenders, Running Board & Mirrors"
    ),
    DentPhotoItem(
        angleIndex = 3,
        angleKey = "RIGHT_SIDE",
        title = "4. Right Side Profile",
        description = "Right Doors, Quarter Panel, Side Skirts & Window Frame"
    ),
    DentPhotoItem(
        angleIndex = 4,
        angleKey = "ROOF_GLASS",
        title = "5. Roof & Glass",
        description = "Roof Panel, Sunroof, Front Windshield & Rear Screen"
    ),
    DentPhotoItem(
        angleIndex = 5,
        angleKey = "CLOSEUP_DENT",
        title = "6. Dent / Scratch Close-Up",
        description = "Detailed Zoom of Existing Scratches, Dents or Deep Scrapes"
    )
)

// Pre-packaged demo inspection photos for quick 1-tap testing in emulator/streaming
fun getDemoDentInspectionPhotos(): List<DentPhotoItem> = listOf(
    DentPhotoItem(
        angleIndex = 0,
        angleKey = "FRONT",
        title = "1. Front Angle",
        description = "Front Bumper, Hood, Grille, Headlights & Fog Lamps",
        photoUri = "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?w=800&auto=format&fit=crop&q=80",
        hasDent = true,
        severity = "MINOR_SCRATCH",
        notes = "Hairline stone-chip scratches on lower bumper lip"
    ),
    DentPhotoItem(
        angleIndex = 1,
        angleKey = "REAR",
        title = "2. Rear Angle",
        description = "Boot / Tailgate, Rear Bumper, Taillights & Exhaust Tip",
        photoUri = "https://images.unsplash.com/photo-1617814076367-b759c7d7e738?w=800&auto=format&fit=crop&q=80",
        hasDent = false,
        severity = "NO_DENT",
        notes = "Rear bootlid and bumper completely pristine"
    ),
    DentPhotoItem(
        angleIndex = 2,
        angleKey = "LEFT_SIDE",
        title = "3. Left Side Profile",
        description = "Left Doors, Front/Rear Fenders, Running Board & Mirrors",
        photoUri = "https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=800&auto=format&fit=crop&q=80",
        hasDent = true,
        severity = "MEDIUM_DENT",
        notes = "1.5 inch parking dent on rear left passenger door"
    ),
    DentPhotoItem(
        angleIndex = 3,
        angleKey = "RIGHT_SIDE",
        title = "4. Right Side Profile",
        description = "Right Doors, Quarter Panel, Side Skirts & Window Frame",
        photoUri = "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&auto=format&fit=crop&q=80",
        hasDent = false,
        severity = "NO_DENT",
        notes = "Right side profile verified clean, no panel distortion"
    ),
    DentPhotoItem(
        angleIndex = 4,
        angleKey = "ROOF_GLASS",
        title = "5. Roof & Glass",
        description = "Roof Panel, Sunroof, Front Windshield & Rear Screen",
        photoUri = "https://images.unsplash.com/photo-1511919884226-fd3cad34687c?w=800&auto=format&fit=crop&q=80",
        hasDent = false,
        severity = "NO_DENT",
        notes = "Roof paint intact, windshield wipers & glass clear"
    ),
    DentPhotoItem(
        angleIndex = 5,
        angleKey = "CLOSEUP_DENT",
        title = "6. Dent / Scratch Close-Up",
        description = "Detailed Zoom of Existing Scratches, Dents or Deep Scrapes",
        photoUri = "https://images.unsplash.com/photo-1486006920555-c77dce18193b?w=800&auto=format&fit=crop&q=80",
        hasDent = true,
        severity = "MINOR_SCRATCH",
        notes = "Surface scuff mark on rear bumper corner near arch"
    )
)

fun List<DentPhotoItem>.toDentPhotosJson(): String {
    val arr = JSONArray()
    for (item in this) {
        val obj = JSONObject()
        obj.put("angleIndex", item.angleIndex)
        obj.put("angleKey", item.angleKey)
        obj.put("title", item.title)
        obj.put("description", item.description)
        obj.put("photoUri", item.photoUri)
        obj.put("hasDent", item.hasDent)
        obj.put("severity", item.severity)
        obj.put("notes", item.notes)
        arr.put(obj)
    }
    return arr.toString()
}

fun String.toDentPhotoItems(): List<DentPhotoItem> {
    if (this.isBlank()) return getDefaultDentInspectionAngles()
    val list = mutableListOf<DentPhotoItem>()
    try {
        val arr = JSONArray(this)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                DentPhotoItem(
                    angleIndex = obj.optInt("angleIndex", i),
                    angleKey = obj.optString("angleKey", "ANGLE_$i"),
                    title = obj.optString("title", "Angle ${i + 1}"),
                    description = obj.optString("description", ""),
                    photoUri = obj.optString("photoUri", ""),
                    hasDent = obj.optBoolean("hasDent", false),
                    severity = obj.optString("severity", "NO_DENT"),
                    notes = obj.optString("notes", "")
                )
            )
        }
    } catch (e: Exception) {
        return getDefaultDentInspectionAngles()
    }
    return if (list.isNotEmpty()) list else getDefaultDentInspectionAngles()
}
