package com.henrisusanto.vitmate.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.henrisusanto.vitmate.R
import com.henrisusanto.vitmate.data.repository.ActiveTimer
import com.henrisusanto.vitmate.domain.TimerCountdown

/** Nilai yang dipilih di dialog timer sebelum dijadwalkan. */
data class TimerDraft(
    val label: String,
    val durationSeconds: Long
)

/**
 * Dialog timer: label dan durasi (menit + detik) sudah terisi dari langkah,
 * keduanya bisa diedit. Durasi minimal 1 detik.
 */
@Composable
fun TimerDialog(
    initialLabel: String,
    initialDurationSeconds: Int,
    onDismiss: () -> Unit,
    onConfirm: (TimerDraft) -> Unit
) {
    var label by remember(initialLabel) { mutableStateOf(initialLabel) }
    var minutesText by remember(initialDurationSeconds) {
        mutableStateOf((initialDurationSeconds / 60).toString())
    }
    var secondsText by remember(initialDurationSeconds) {
        mutableStateOf((initialDurationSeconds % 60).toString())
    }

    val minutes = minutesText.toLongOrNull() ?: 0L
    val seconds = secondsText.toLongOrNull() ?: 0L
    val totalSeconds = minutes * 60L + seconds
    val isValid = totalSeconds >= 1L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.timer_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.timer_dialog_label)) },
                    singleLine = true
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = minutesText,
                        onValueChange = { minutesText = it.filter(Char::isDigit).take(4) },
                        modifier = Modifier.weight(1f),
                        label = { Text(stringResource(R.string.timer_dialog_minutes)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = secondsText,
                        onValueChange = { secondsText = it.filter(Char::isDigit).take(2) },
                        modifier = Modifier.weight(1f),
                        label = { Text(stringResource(R.string.timer_dialog_seconds)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                if (!isValid) {
                    Text(
                        text = stringResource(R.string.timer_dialog_duration_error),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        TimerDraft(
                            label = label.trim().ifBlank { initialLabel },
                            durationSeconds = totalSeconds
                        )
                    )
                },
                enabled = isValid
            ) {
                Text(stringResource(R.string.timer_dialog_start))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_button_batal))
            }
        }
    )
}

/** Daftar semua timer aktif dengan hitung mundur dari waktu selesai absolut. */
@Composable
fun ActiveTimersSection(
    timers: List<ActiveTimer>,
    nowMillis: Long,
    onCancel: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (timers.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        SectionTitle(stringResource(R.string.timer_active_title))
        Card(modifier = Modifier.padding(top = 12.dp)) {
            Column {
                timers.forEachIndexed { index, timer ->
                    if (index > 0) HorizontalDivider()
                    val remaining = TimerCountdown.remainingSeconds(
                        endAtEpochMillis = timer.endAtEpochMillis,
                        nowMillis = nowMillis
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = timer.label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (timer.recipeTitle.isNotBlank()) {
                                Text(
                                    text = stringResource(
                                        R.string.timer_recipe_format,
                                        timer.recipeTitle
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = if (remaining == 0L) {
                                stringResource(R.string.timer_finished)
                            } else {
                                TimerCountdown.format(remaining)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { onCancel(timer.id) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(
                                    R.string.timer_cancel_content_desc
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
