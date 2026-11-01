package com.henrisusanto.vitmate.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.preferencesDataStore
import com.henrisusanto.vitmate.util.AnalyticsLogger
import com.henrisusanto.vitmate.util.RemoteConfigManager
import com.henrisusanto.vitmate.util.AdUnitProvider
import com.henrisusanto.vitmate.util.AdManager
import com.henrisusanto.vitmate.util.ConsentManager
import com.henrisusanto.vitmate.data.database.AppDatabase
import com.henrisusanto.vitmate.data.repository.RecipeRepository
import com.henrisusanto.vitmate.data.repository.FavoriteRepository
import com.henrisusanto.vitmate.data.repository.UnlockRepository
import com.henrisusanto.vitmate.data.repository.CalcRepository
import com.henrisusanto.vitmate.data.repository.TimerRepository
import com.henrisusanto.vitmate.data.api.RtdbApiService
import com.henrisusanto.vitmate.data.api.ApiClient
import com.henrisusanto.vitmate.data.network.NetworkMonitor

private val Context.dataStore: DataStore<androidx.datastore.preferences.CorePreferences> by preferencesDataStore(
    name = "jajan-cuan-settings"
)

class AppContainer(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val apiClient = ApiClient()
    private val rtdbService = apiClient.create(RtdbApiService::class.java)

    val networkMonitor = NetworkMonitor(context)

    val recipeRepository = RecipeRepository(database, rtdbService)
    val favoriteRepository = FavoriteRepository(database)
    val unlockRepository = UnlockRepository(database)
    val calcRepository = CalcRepository(database)
    val timerRepository = TimerRepository(database)

    val remoteConfigManager = RemoteConfigManager(context)
    val adUnitProvider = AdUnitProvider(remoteConfigManager)
    val consentManager = ConsentManager(context)
    val adManager = AdManager(context, adUnitProvider, consentManager)

    val analyticsLogger = AnalyticsLogger

    val settingsDataStore = context.dataStore
}