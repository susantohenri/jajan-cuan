package com.henrisusanto.vitmate

import android.content.Context
import com.henrisusanto.vitmate.ads.AdManager
import com.henrisusanto.vitmate.ads.AdsInitializer
import com.henrisusanto.vitmate.analytics.AnalyticsLogger
import com.henrisusanto.vitmate.config.AdUnitProvider
import com.henrisusanto.vitmate.config.RemoteConfigManager
import com.henrisusanto.vitmate.consent.ConsentManager
import com.henrisusanto.vitmate.data.image.ImageUrlResolver
import com.henrisusanto.vitmate.data.image.OkHttpImageDownloader
import com.henrisusanto.vitmate.data.local.JajanCuanDatabase
import com.henrisusanto.vitmate.data.remote.OkHttpRecipeRemoteDataSource
import com.henrisusanto.vitmate.data.repository.RecipeRepository
import com.henrisusanto.vitmate.data.repository.SessionUnlocks
import com.henrisusanto.vitmate.data.repository.TimerRepository
import com.henrisusanto.vitmate.data.repository.UnlockRepository
import com.henrisusanto.vitmate.data.sync.SharedPreferencesSyncStateStore
import com.henrisusanto.vitmate.data.sync.SyncManager
import com.henrisusanto.vitmate.data.sync.SyncOutcome
import com.henrisusanto.vitmate.promo.AndroidPromoActions
import com.henrisusanto.vitmate.promo.PromoActions
import com.henrisusanto.vitmate.settings.ThemePreferences
import com.henrisusanto.vitmate.timer.AlarmTimerScheduler
import com.henrisusanto.vitmate.timer.TimerNotifier
import com.henrisusanto.vitmate.timer.TimerPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

/**
 * DI manual aplikasi. Satu instance per proses, dibuat oleh JajanCuanApplication.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val httpClient = OkHttpRecipeRemoteDataSource.defaultClient()

    val analytics: AnalyticsLogger = AnalyticsLogger.getInstance(appContext)

    val themePreferences = ThemePreferences.from(appContext)

    val remoteConfigManager = RemoteConfigManager(isDebugBuild = BuildConfig.DEBUG)

    val adUnitProvider = AdUnitProvider(
        isDebugBuild = BuildConfig.DEBUG,
        valuesProvider = { remoteConfigManager.values.value }
    )

    val consentManager = ConsentManager(
        context = appContext,
        isDebugBuild = BuildConfig.DEBUG,
        debugEea = BuildConfig.UMP_DEBUG_EEA,
        debugTestDeviceHashedId = BuildConfig.UMP_TEST_DEVICE_HASHED_ID
    )

    val adManager = AdManager(
        context = appContext,
        analytics = analytics,
        adUnitProvider = adUnitProvider,
        config = remoteConfigManager.values,
        canRequestAds = consentManager::canRequestAds
    )

    val adsInitializer = AdsInitializer(
        context = appContext,
        consentManager = consentManager,
        adManager = adManager
    )

    private val database = JajanCuanDatabase.getInstance(appContext)

    val sessionUnlocks = SessionUnlocks()

    val unlockRepository = UnlockRepository(
        dao = database.unlockDao(),
        sessionUnlocks = sessionUnlocks,
        config = remoteConfigManager.values
    )

    val promoActions: PromoActions = AndroidPromoActions(appContext, httpClient)

    private val recipeRemoteDataSource = OkHttpRecipeRemoteDataSource(client = httpClient)

    val imageUrlResolver = ImageUrlResolver()

    val recipeRepository = RecipeRepository(
        database = database,
        remote = recipeRemoteDataSource,
        imageDownloader = OkHttpImageDownloader(httpClient),
        imageUrlResolver = imageUrlResolver,
        coversDirectory = File(appContext.filesDir, "covers")
    )

    val timerNotifier = TimerNotifier(appContext)

    val timerPreferences = TimerPreferences.from(appContext)

    val timerRepository = TimerRepository(
        dao = database.activeTimerDao(),
        scheduler = AlarmTimerScheduler(appContext),
        notificationCounter = timerPreferences
    )

    val syncManager = SyncManager(
        database = database,
        remote = recipeRemoteDataSource,
        stateStore = SharedPreferencesSyncStateStore(appContext),
        refreshFavorite = recipeRepository::refreshFavoriteFromNetwork,
        metaCheckIntervalHours = { remoteConfigManager.values.value.metaCheckIntervalHours }
    )

    init {
        timerNotifier.ensureChannel()
        applicationScope.launch { timerRepository.pruneExpired() }

        remoteConfigManager.start()

        applicationScope.launch {
            themePreferences.themeMode.collect { mode ->
                analytics.setThemeMode(mode.analyticsValue)
            }
        }

        applicationScope.launch {
            remoteConfigManager.values.collect { values ->
                analytics.setAdsEnabled(values.isAdsEnabled)
                adManager.onConfigChanged(values)
            }
        }
    }

    /**
     * Dipanggil saat app dibuka/kembali foreground dan oleh pull-to-refresh.
     * [force] untuk pull-to-refresh agar throttle /meta dilewati.
     */
    fun syncInBackground(force: Boolean = false) {
        applicationScope.launch {
            syncManager.sync(force = force)
        }
    }

    /**
     * Versi suspend untuk pull-to-refresh: UI menunggu sampai sync selesai
     * agar indikator refresh berhenti tepat waktu.
     */
    suspend fun syncNow(force: Boolean = false): SyncOutcome =
        syncManager.sync(force = force)
}
