package com.henrisusanto.vitmate.ui.testutil

import com.henrisusanto.vitmate.data.repository.RecipeSummary

/**
 * Data contoh deterministik: 20 resep, ganjil = Kue (mengandung "tapioka"),
 * genap = Minuman.
 */
fun sampleRecipes(count: Int = 20): List<RecipeSummary> = (1..count).map { index ->
    val isOdd = index % 2 == 1
    RecipeSummary(
        id = "recipe-$index",
        slug = "recipe-$index",
        title = "Resep $index",
        category = if (isOdd) "Kue" else "Minuman",
        tags = listOf(if (isOdd) "gurih" else "manis"),
        ingredientsText = if (isOdd) "tepung tapioka gula" else "tepung beras air",
        coverUrl = "https://example.com/cover-$index.webp",
        thumbUrl = "https://example.com/thumb-$index.webp",
        estCapital = index * 1_000,
        portions = 4,
        isViral = index % 5 == 0,
        isFavorite = false
    )
}
