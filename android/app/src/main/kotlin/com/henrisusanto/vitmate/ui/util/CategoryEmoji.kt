package com.henrisusanto.vitmate.ui.util

/** Emoji fallback kategori, sama dengan fallback gambar di web. */
fun categoryEmoji(category: String): String {
    val normalized = category.lowercase()
    return when {
        "minum" in normalized || "es" in normalized -> "\uD83E\uDD64"
        "goreng" in normalized -> "\uD83C\uDF5F"
        "kue" in normalized || "cake" in normalized || "bolu" in normalized -> "\uD83C\uDF70"
        "roti" in normalized || "bakery" in normalized -> "\uD83C\uDF5E"
        "cemilan" in normalized || "snack" in normalized -> "\uD83C\uDF7F"
        else -> "\uD83C\uDF61"
    }
}
