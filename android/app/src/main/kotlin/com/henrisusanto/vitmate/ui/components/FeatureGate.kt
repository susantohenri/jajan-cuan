package com.henrisusanto.vitmate.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.henrisusanto.vitmate.R
import com.henrisusanto.vitmate.ui.gate.FeatureGateStatus
import com.henrisusanto.vitmate.ui.gate.FeatureGateUiState

/**
 * Pembungkus fitur terkunci Rewarded. Saat [FeatureGateUiState.status] OPEN,
 * isi fitur ditampilkan; selain itu kartu kunci yang sesuai status.
 */
@Composable
fun FeatureGate(
    featureLabel: String,
    state: FeatureGateUiState,
    onUnlockClick: () -> Unit,
    onDialogDismiss: () -> Unit,
    onDialogConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    when (state.status) {
        FeatureGateStatus.OPEN -> content()
        FeatureGateStatus.PREPARING -> LockedGateCard(
            featureLabel = featureLabel,
            status = state.status,
            onUnlockClick = onUnlockClick,
            modifier = modifier
        )
        FeatureGateStatus.READY -> LockedGateCard(
            featureLabel = featureLabel,
            status = state.status,
            onUnlockClick = onUnlockClick,
            modifier = modifier
        )
    }

    if (state.dialogVisible) {
        AlertDialog(
            onDismissRequest = onDialogDismiss,
            title = {
                Text(stringResource(R.string.dialog_rewarded_title, featureLabel))
            },
            text = {
                Text(stringResource(R.string.dialog_rewarded_msg, featureLabel))
            },
            confirmButton = {
                TextButton(onClick = onDialogConfirm) {
                    Text(stringResource(R.string.dialog_button_tonton))
                }
            },
            dismissButton = {
                TextButton(onClick = onDialogDismiss) {
                    Text(stringResource(R.string.dialog_button_batal))
                }
            }
        )
    }
}

@Composable
private fun LockedGateCard(
    featureLabel: String,
    status: FeatureGateStatus,
    onUnlockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = featureLabel,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (status == FeatureGateStatus.PREPARING) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = stringResource(R.string.rewarded_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Button(onClick = onUnlockClick) {
                    Text(stringResource(R.string.rewarded_button_format, featureLabel))
                }
            }
        }
    }
}
