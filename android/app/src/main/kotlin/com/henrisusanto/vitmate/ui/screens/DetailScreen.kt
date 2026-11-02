package com.henrisusanto.vitmate.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.henrisusanto.vitmate.R
import com.henrisusanto.vitmate.data.model.UnlockFeature
import com.henrisusanto.vitmate.domain.HppMarginMode
import com.henrisusanto.vitmate.timer.TimerPermissionPrompt
import com.henrisusanto.vitmate.ui.components.ActiveTimersSection
import com.henrisusanto.vitmate.ui.components.BusinessAnalysisSection
import com.henrisusanto.vitmate.ui.components.CookingStepsSection
import com.henrisusanto.vitmate.ui.components.ErrorState
import com.henrisusanto.vitmate.ui.components.FeatureGate
import com.henrisusanto.vitmate.ui.components.HppCalculatorSection
import com.henrisusanto.vitmate.ui.components.IngredientsSection
import com.henrisusanto.vitmate.ui.components.PromoSection
import com.henrisusanto.vitmate.ui.components.TimerDialog
import com.henrisusanto.vitmate.ui.components.TimerDraft
import com.henrisusanto.vitmate.ui.components.imageModel
import com.henrisusanto.vitmate.ui.util.categoryEmoji
import com.henrisusanto.vitmate.ui.util.findActivity
import com.henrisusanto.vitmate.ui.util.formatRupiah
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TABLET_CONTENT_MAX_WIDTH_DP = 840
private const val DEFAULT_TIMER_SECONDS = 60

/**
 * Layar Detail Resep. Sumber detail (favorit -> cache -> jaringan) sudah
 * ditentukan repository sehingga favorit tetap tampil penuh saat offline.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var timerStep by remember { mutableStateOf<CookingStepUi?>(null) }
    var pendingTimer by remember { mutableStateOf<TimerDraft?>(null) }
    val permissionPrompt = remember(context) { TimerPermissionPrompt(context) }
    val permissionDeniedMessage = stringResource(R.string.timer_permission_denied)
    val timerFailedMessage = stringResource(R.string.timer_failed)

    fun startTimer(draft: TimerDraft) {
        viewModel.startTimer(draft.label, draft.durationSeconds)
        if (!permissionPrompt.isGranted()) {
            scope.launch { snackbarHostState.showSnackbar(permissionDeniedMessage) }
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        val draft = pendingTimer
        pendingTimer = null
        // Timer tetap berjalan walau izin ditolak; pesan izin tampil dari startTimer.
        if (draft != null) startTimer(draft)
    }

    var pendingPromoDownload by remember { mutableStateOf<Int?>(null) }
    val promoPermissionDeniedMessage = stringResource(R.string.promo_permission_denied)
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val index = pendingPromoDownload
        pendingPromoDownload = null
        if (granted && index != null) {
            viewModel.onPromoDownload(index)
        } else if (!granted) {
            scope.launch {
                snackbarHostState.showSnackbar(promoPermissionDeniedMessage)
            }
        }
    }

    fun downloadPromo(index: Int) {
        val needsLegacyPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        if (needsLegacyPermission) {
            pendingPromoDownload = index
            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            viewModel.onPromoDownload(index)
        }
    }

    val activity = context.findActivity()

    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val activeTimerIds = remember(state.activeTimers) { state.activeTimers.map { it.id } }
    LaunchedEffect(activeTimerIds) {
        if (state.activeTimers.isEmpty()) return@LaunchedEffect
        nowMillis = System.currentTimeMillis()
        while (true) {
            delay(1_000L)
            nowMillis = System.currentTimeMillis()
        }
    }

    val shareText = stringResource(
        R.string.detail_share_text_format,
        state.title,
        state.excerpt
    )
    val shareChooserTitle = stringResource(R.string.detail_share_chooser)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                DetailUiEvent.FavoriteAdded -> {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.snackbar_saved)
                    )
                }
                DetailUiEvent.FavoriteRemoved -> {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.snackbar_removed)
                    )
                }
                DetailUiEvent.NeedsInternet -> snackbarHostState.showSnackbar(
                    context.getString(R.string.snackbar_need_internet)
                )
                DetailUiEvent.CalculationSaved -> snackbarHostState.showSnackbar(
                    context.getString(R.string.detail_calc_saved)
                )
                DetailUiEvent.SaveFailed -> snackbarHostState.showSnackbar(
                    context.getString(R.string.detail_calc_save_failed)
                )
                DetailUiEvent.TimerFailed -> snackbarHostState.showSnackbar(timerFailedMessage)
                DetailUiEvent.RewardedFallback -> snackbarHostState.showSnackbar(
                    context.getString(R.string.rewarded_fallback_msg)
                )
                DetailUiEvent.PromoDownloaded -> snackbarHostState.showSnackbar(
                    context.getString(R.string.promo_download_success)
                )
                DetailUiEvent.PromoDownloadFailed -> snackbarHostState.showSnackbar(
                    context.getString(R.string.promo_download_failed)
                )
                DetailUiEvent.PromoPermissionDenied -> snackbarHostState.showSnackbar(
                    promoPermissionDeniedMessage
                )
                DetailUiEvent.PromoNoInternet -> snackbarHostState.showSnackbar(
                    context.getString(R.string.promo_no_internet)
                )
                DetailUiEvent.PromoShareFailed -> snackbarHostState.showSnackbar(
                    context.getString(R.string.promo_share_failed)
                )
                is DetailUiEvent.PromoShareReady -> {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = event.mimeType
                        putExtra(Intent.EXTRA_STREAM, event.uri)
                        clipData = ClipData.newUri(
                            context.contentResolver,
                            "promo",
                            event.uri
                        )
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(
                        Intent.createChooser(
                            sendIntent,
                            context.getString(R.string.promo_share_chooser)
                        )
                    )
                }
                is DetailUiEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(
                                R.string.detail_back_content_desc
                            )
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::onFavoriteToggle) {
                        Icon(
                            imageVector = if (state.isFavorite) {
                                Icons.Default.Favorite
                            } else {
                                Icons.Default.FavoriteBorder
                            },
                            contentDescription = stringResource(
                                if (state.isFavorite) {
                                    R.string.favorite_remove_content_desc
                                } else {
                                    R.string.favorite_add_content_desc
                                }
                            ),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.onShareOpened()
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(
                                Intent.createChooser(sendIntent, shareChooserTitle)
                            )
                        },
                        enabled = !state.isLoading && state.errorMessage == null &&
                            !state.notFound && state.title.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(
                                R.string.detail_share_content_desc
                            )
                        )
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                state.isLoading && state.title.isEmpty() -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.errorMessage != null -> {
                    ErrorState(
                        message = state.errorMessage.orEmpty(),
                        onRetry = viewModel::retry
                    )
                }
                state.notFound -> {
                    NotFoundState(
                        onBack = onNavigateBack,
                        modifier = Modifier.widthIn(
                            max = TABLET_CONTENT_MAX_WIDTH_DP.dp
                        )
                    )
                }
                else -> {
                    DetailContent(
                        state = state,
                        nowMillis = nowMillis,
                        onPriceChange = viewModel::onPriceChange,
                        onPortionsChange = viewModel::onPortionsChange,
                        onMarginModeChange = viewModel::onMarginModeChange,
                        onMarginChange = viewModel::onMarginChange,
                        onSaveCalculation = viewModel::onSaveCalculation,
                        onTimerClick = { timerStep = it },
                        onCancelTimer = viewModel::onCancelTimer,
                        onGateClick = viewModel::onGateClick,
                        onGateDialogDismiss = viewModel::onGateDialogDismiss,
                        onGateDialogConfirm = {
                            if (activity != null) {
                                viewModel.onGateDialogConfirm(activity)
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Gagal memuat iklan: aktivitas tidak ditemukan")
                                }
                            }
                        },
                        onPromoDownload = { downloadPromo(it) },
                        onPromoShare = viewModel::onPromoShare,
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = TABLET_CONTENT_MAX_WIDTH_DP.dp)
                    )
                }
            }
        }
    }

    timerStep?.let { step ->
        TimerDialog(
            initialLabel = step.text,
            initialDurationSeconds = step.durationSeconds ?: DEFAULT_TIMER_SECONDS,
            onDismiss = { timerStep = null },
            onConfirm = { draft ->
                timerStep = null
                if (permissionPrompt.shouldRequest()) {
                    pendingTimer = draft
                    permissionPrompt.markRequested()
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    startTimer(draft)
                }
            }
        )
    }
}

@Composable
private fun DetailContent(
    state: DetailUiState,
    nowMillis: Long,
    onPriceChange: (Int, String) -> Unit,
    onPortionsChange: (String) -> Unit,
    onMarginModeChange: (HppMarginMode) -> Unit,
    onMarginChange: (String) -> Unit,
    onSaveCalculation: () -> Unit,
    onTimerClick: (CookingStepUi) -> Unit,
    onCancelTimer: (String) -> Unit,
    onGateClick: (UnlockFeature) -> Unit,
    onGateDialogDismiss: () -> Unit,
    onGateDialogConfirm: () -> Unit,
    onPromoDownload: (Int) -> Unit,
    onPromoShare: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.imePadding(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item(key = "hero") {
            DetailHero(state = state)
        }
        item(key = "header") {
            DetailHeader(state = state)
        }
        item(key = "analysis") {
            BusinessAnalysisSection(
                state = state,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
        item(key = "ingredients") {
            IngredientsSection(
                ingredients = state.ingredients,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
        item(key = "steps") {
            CookingStepsSection(
                steps = state.steps,
                onTimerClick = onTimerClick,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
        item(key = "timers") {
            ActiveTimersSection(
                timers = state.activeTimers,
                nowMillis = nowMillis,
                onCancel = onCancelTimer,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
        item(key = "calculator") {
            FeatureGate(
                featureLabel = stringResource(R.string.hpp_calculator),
                state = state.calculatorGate,
                onUnlockClick = { onGateClick(UnlockFeature.CALCULATOR) },
                onDialogDismiss = onGateDialogDismiss,
                onDialogConfirm = onGateDialogConfirm,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                HppCalculatorSection(
                    state = state,
                    onPriceChange = onPriceChange,
                    onPortionsChange = onPortionsChange,
                    onMarginModeChange = onMarginModeChange,
                    onMarginChange = onMarginChange,
                    onSave = onSaveCalculation,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
        if (state.promoImages.isNotEmpty()) {
            item(key = "promo") {
                FeatureGate(
                    featureLabel = stringResource(R.string.promo_section_title),
                    state = state.promoGate,
                    onUnlockClick = { onGateClick(UnlockFeature.PROMO) },
                    onDialogDismiss = onGateDialogDismiss,
                    onDialogConfirm = onGateDialogConfirm,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    PromoSection(
                        images = state.promoImages,
                        onDownload = onPromoDownload,
                        onShare = onPromoShare,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailHero(state: DetailUiState) {
    val model = remember(state.coverUrl) { imageModel(state.coverUrl) }
    var failed by remember(model) { mutableStateOf(false) }

    val shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
    if (model == null || failed) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .clip(shape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = categoryEmoji(state.category),
                style = MaterialTheme.typography.displayLarge
            )
        }
    } else {
        AsyncImage(
            model = model,
            contentDescription = state.title,
            contentScale = ContentScale.Crop,
            onError = { failed = true },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 10f)
                .clip(shape)
        )
    }
}

@Composable
private fun DetailHeader(state: DetailUiState) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = state.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        if (state.category.isNotBlank() || state.tags.isNotEmpty()) {
            Text(
                text = (listOf(state.category) + state.tags)
                    .filter { it.isNotBlank() }
                    .distinct()
                    .joinToString(" • "),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (state.excerpt.isNotBlank()) {
            Text(
                text = state.excerpt,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            InfoStat(
                label = stringResource(R.string.detail_label_capital),
                value = formatRupiah(state.estimatedCapital)
            )
            InfoStat(
                label = stringResource(R.string.detail_calc_portions),
                value = stringResource(
                    R.string.home_portions_format,
                    state.defaultPortions
                )
            )
        }
    }
}

@Composable
private fun InfoStat(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun NotFoundState(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "\uD83C\uDF5C",
            style = MaterialTheme.typography.displayMedium
        )
        Text(
            text = stringResource(R.string.detail_not_found_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = stringResource(R.string.detail_not_found_msg),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )
        Button(onClick = onBack) {
            Text(stringResource(R.string.detail_back_content_desc))
        }
    }
}
