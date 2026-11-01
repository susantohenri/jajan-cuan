package com.henrisusanto.vitmate.ui.screens

/**
 * Peristiwa toggle favorit yang dikonsumsi UI untuk Snackbar + haptic.
 * Status data sendiri selalu datang dari Flow Room (bukan dari event ini).
 */
sealed interface FavoriteUiEvent {
    data object Added : FavoriteUiEvent
    data object Removed : FavoriteUiEvent
    data object NeedsInternet : FavoriteUiEvent
    data class Error(val message: String) : FavoriteUiEvent
}
