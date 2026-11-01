package com.henrisusanto.vitmate.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.henrisusanto.vitmate.R
import com.henrisusanto.vitmate.domain.HppMarginMode
import com.henrisusanto.vitmate.domain.HppResult
import com.henrisusanto.vitmate.ui.screens.CookingStepUi
import com.henrisusanto.vitmate.ui.screens.DetailUiState
import com.henrisusanto.vitmate.ui.screens.IngredientInput
import com.henrisusanto.vitmate.ui.util.formatRupiah

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

@Composable
fun BusinessAnalysisSection(
    state: DetailUiState,
    modifier: Modifier = Modifier
) {
    val entries = listOf(
        stringResource(R.string.detail_market_demand) to state.marketDemand,
        stringResource(R.string.detail_ease_of_making) to state.easeOfMaking,
        stringResource(R.string.detail_ingredient_availability) to state.ingredientAvailability
    ).filter { it.second.isNotBlank() }
    if (entries.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        SectionTitle(stringResource(R.string.detail_section_analysis))
        Column(
            modifier = Modifier.padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            entries.forEach { (title, body) ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IngredientsSection(
    ingredients: List<IngredientInput>,
    modifier: Modifier = Modifier
) {
    if (ingredients.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        SectionTitle(stringResource(R.string.detail_section_ingredients))
        Card(modifier = Modifier.padding(top = 12.dp)) {
            Column {
                ingredients.forEachIndexed { index, ingredient ->
                    if (index > 0) HorizontalDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ingredient.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (ingredient.quantity.isNotBlank()) {
                                Text(
                                    text = ingredient.quantity,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = formatRupiah(ingredient.defaultPrice),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CookingStepsSection(
    steps: List<CookingStepUi>,
    onTimerClick: (CookingStepUi) -> Unit,
    modifier: Modifier = Modifier
) {
    if (steps.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        SectionTitle(stringResource(R.string.detail_section_steps))
        Column(
            modifier = Modifier.padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            steps.forEachIndexed { index, step ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = step.text,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        step.durationSeconds?.let { seconds ->
                            OutlinedButton(
                                onClick = { onTimerClick(step) },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text(
                                    stringResource(
                                        R.string.detail_timer_button_format,
                                        durationLabel(seconds)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HppCalculatorSection(
    state: DetailUiState,
    onPriceChange: (Int, String) -> Unit,
    onPortionsChange: (String) -> Unit,
    onMarginModeChange: (HppMarginMode) -> Unit,
    onMarginChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionTitle(stringResource(R.string.hpp_calculator))
        Text(
            text = stringResource(R.string.detail_calc_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )

        Column(
            modifier = Modifier.padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.ingredients.forEachIndexed { index, ingredient ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(
                            text = ingredient.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (ingredient.quantity.isNotBlank()) {
                            Text(
                                text = ingredient.quantity,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    OutlinedTextField(
                        value = ingredient.priceText,
                        onValueChange = { onPriceChange(index, it) },
                        modifier = Modifier.weight(1f),
                        label = { Text(stringResource(R.string.detail_calc_ingredient_price)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = state.portionsText,
                onValueChange = onPortionsChange,
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.detail_calc_portions)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = state.marginText,
                onValueChange = onMarginChange,
                modifier = Modifier.weight(1f),
                label = {
                    Text(
                        stringResource(
                            if (state.marginMode == HppMarginMode.PERCENT) {
                                R.string.detail_calc_margin_percent
                            } else {
                                R.string.detail_calc_margin_nominal
                            }
                        )
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (state.marginMode == HppMarginMode.PERCENT) {
                        KeyboardType.Decimal
                    } else {
                        KeyboardType.Number
                    }
                )
            )
        }

        Row(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.marginMode == HppMarginMode.PERCENT,
                onClick = { onMarginModeChange(HppMarginMode.PERCENT) },
                label = { Text(stringResource(R.string.hpp_margin_mode_percent)) }
            )
            FilterChip(
                selected = state.marginMode == HppMarginMode.NOMINAL,
                onClick = { onMarginModeChange(HppMarginMode.NOMINAL) },
                label = { Text(stringResource(R.string.hpp_margin_mode_nominal)) }
            )
        }

        HppResultCard(
            result = state.hpp,
            modifier = Modifier.padding(top = 16.dp)
        )

        Button(
            onClick = onSave,
            enabled = !state.isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(stringResource(R.string.button_save))
        }
    }
}

@Composable
private fun HppResultCard(result: HppResult, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            ResultRow(
                label = stringResource(R.string.hpp_total_modal),
                value = formatRupiah(result.totalModal)
            )
            ResultRow(
                label = stringResource(R.string.hpp_hpp_per_portion),
                value = formatRupiah(result.hppPerPortion)
            )
            ResultRow(
                label = stringResource(R.string.hpp_harga_jual),
                value = formatRupiah(result.sellingPrice)
            )
            ResultRow(
                label = stringResource(R.string.hpp_harga_jual_bulat),
                value = formatRupiah(result.roundedSellingPrice),
                emphasized = true
            )
            ResultRow(
                label = stringResource(R.string.hpp_laba_per_portion),
                value = formatRupiah(result.profitPerPortion)
            )
            ResultRow(
                label = stringResource(R.string.hpp_laba_total),
                value = formatRupiah(result.totalProfit),
                emphasized = true
            )
        }
    }
}

@Composable
private fun ResultRow(
    label: String,
    value: String,
    emphasized: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Medium,
            color = if (emphasized) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun durationLabel(seconds: Int): String = when {
    seconds >= 3_600 -> {
        val hours = seconds / 3_600
        val minutes = (seconds % 3_600) / 60
        stringResource(R.string.detail_duration_hours_minutes, hours, minutes)
    }
    seconds >= 60 -> stringResource(R.string.detail_duration_minutes, seconds / 60)
    else -> stringResource(R.string.detail_duration_seconds, seconds)
}
