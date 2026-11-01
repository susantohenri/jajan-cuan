package com.henrisusanto.vitmate.timer

import android.content.Context
import android.content.Intent
import com.henrisusanto.vitmate.MainActivity

/** Intent yang dipakai notifikasi timer untuk membuka resep terkait. */
object TimerIntents {

    const val EXTRA_RECIPE_ID = "com.henrisusanto.vitmate.extra.RECIPE_ID"
    private const val ACTION_OPEN_RECIPE = "com.henrisusanto.vitmate.action.OPEN_RECIPE"

    fun openRecipe(context: Context, recipeId: String): Intent =
        Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_RECIPE
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_RECIPE_ID, recipeId)
        }

    fun recipeIdFrom(intent: Intent?): String? =
        intent?.getStringExtra(EXTRA_RECIPE_ID)?.takeIf { it.isNotBlank() }
}
