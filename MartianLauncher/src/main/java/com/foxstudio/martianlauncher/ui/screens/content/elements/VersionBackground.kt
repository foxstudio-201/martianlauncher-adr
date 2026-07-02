package com.foxstudio.martianlauncher.ui.screens.content.elements

import androidx.annotation.DrawableRes
import com.foxstudio.martianlauncher.R

@DrawableRes
fun versionBackgroundRes(minecraftVersion: String?): Int {
    return when (majorVersionKey(minecraftVersion)) {
        "1.12" -> R.drawable.bg_mc_1_12
        "1.15" -> R.drawable.bg_mc_1_15
        "1.16" -> R.drawable.bg_mc_1_16
        "1.17" -> R.drawable.bg_mc_1_17
        "1.18" -> R.drawable.bg_mc_1_18
        "1.19" -> R.drawable.bg_mc_1_19
        "1.20" -> R.drawable.bg_mc_1_20
        "1.21" -> R.drawable.bg_mc_1_21
        "26" -> R.drawable.bg_mc_26
        else -> R.drawable.bg_mc_default
    }
}

fun versionUpdateName(versionKey: String?): String? {
    return when (majorVersionKey(versionKey)) {
        "1.0" -> "Adventure Update"
        "1.4" -> "Pretty Scary Update"
        "1.5" -> "Redstone Update"
        "1.6" -> "Horse Update"
        "1.7" -> "The Update that Changed the World"
        "1.8" -> "Bountiful Update"
        "1.9" -> "Combat Update"
        "1.10" -> "Frostburn Update"
        "1.11" -> "Exploration Update"
        "1.12" -> "World of Color Update"
        "1.13" -> "Update Aquatic"
        "1.14" -> "Village & Pillage"
        "1.15" -> "Buzzy Bees"
        "1.16" -> "Nether Update"
        "1.17" -> "Caves & Cliffs: Part I"
        "1.18" -> "Caves & Cliffs: Part II"
        "1.19" -> "The Wild Update"
        "1.20" -> "Trails & Tales"
        "1.21" -> "Tricky Trials"
        else -> null
    }
}

internal fun majorVersionKey(version: String?): String? {
    if (version.isNullOrBlank()) return null
    val parts = version.split(".")
    val first = parts[0].toIntOrNull()
    if (first != null && first >= 20 && parts[0].length <= 2) return parts[0]
    if (parts.size >= 2) return "${parts[0]}.${parts[1]}"
    return version
}
