package com.henrisusanto.vitmate.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.henrisusanto.vitmate.R
import com.henrisusanto.vitmate.consent.ConsentManager
import com.henrisusanto.vitmate.settings.ThemeMode
import com.henrisusanto.vitmate.ui.util.findActivity
import kotlinx.coroutines.launch

private const val TABLET_CONTENT_MAX_WIDTH_DP = 840
private const val PRIVACY_POLICY_URL = "https://susantohenri.github.io/privacy-policy"

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    consentManager: ConsentManager,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun showError(messageRes: Int) {
        scope.launch {
            snackbarHostState.showSnackbar(context.getString(messageRes))
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = TABLET_CONTENT_MAX_WIDTH_DP.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineMedium
                )

                ThemeSection(
                    selected = state.themeMode,
                    onSelect = viewModel::onThemeSelected
                )

                HorizontalDivider()

                SettingsValueRow(
                    label = stringResource(R.string.settings_version),
                    value = state.versionName
                )

                HorizontalDivider()

                SettingsClickableRow(
                    label = stringResource(R.string.settings_privacy_policy),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, PRIVACY_POLICY_URL.toUri())
                        val opened = runCatching { context.startActivity(intent) }.isSuccess
                        if (!opened) {
                            showError(R.string.settings_privacy_policy_error)
                        }
                    }
                )

                if (state.showAdsPrivacyOptions) {
                    HorizontalDivider()
                    SettingsClickableRow(
                        label = stringResource(R.string.settings_ads_privacy),
                        onClick = {
                            val activity = context.findActivity()
                            if (activity == null) {
                                showError(R.string.settings_ads_privacy_error)
                            } else {
                                consentManager.showPrivacyOptionsForm(activity) { formError ->
                                    if (formError != null) {
                                        showError(R.string.settings_ads_privacy_error)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeSection(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.settings_theme),
            style = MaterialTheme.typography.titleMedium
        )
        Column(modifier = Modifier.selectableGroup()) {
            ThemeOption(
                mode = ThemeMode.SYSTEM,
                labelRes = R.string.settings_theme_system,
                selected = selected,
                onSelect = onSelect
            )
            ThemeOption(
                mode = ThemeMode.LIGHT,
                labelRes = R.string.settings_theme_light,
                selected = selected,
                onSelect = onSelect
            )
            ThemeOption(
                mode = ThemeMode.DARK,
                labelRes = R.string.settings_theme_dark,
                selected = selected,
                onSelect = onSelect
            )
        }
    }
}

@Composable
private fun ThemeOption(
    mode: ThemeMode,
    labelRes: Int,
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .selectable(
                selected = selected == mode,
                onClick = { onSelect(mode) },
                role = Role.RadioButton
            )
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(
            selected = selected == mode,
            onClick = null
        )
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun SettingsValueRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsClickableRow(
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(R.string.settings_open),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
