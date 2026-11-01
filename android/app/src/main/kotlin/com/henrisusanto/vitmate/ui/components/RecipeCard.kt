package com.henrisusanto.vitmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.henrisusanto.vitmate.R
import com.henrisusanto.vitmate.data.repository.RecipeSummary
import com.henrisusanto.vitmate.ui.util.categoryEmoji
import com.henrisusanto.vitmate.ui.util.formatRupiah
import java.io.File

/**
 * Kartu resep untuk Home dan Favorit. Sumber gambar bisa URL remote (Home)
 * atau file lokal (Favorit); keduanya ditangani Coil.
 */
@Composable
fun RecipeCard(
    recipe: RecipeSummary,
    onClick: () -> Unit,
    onActionClick: () -> Unit,
    actionContentDescription: String,
    actionIcon: ImageVector = Icons.Default.Favorite,
    actionTint: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RecipeThumbnail(recipe = recipe)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = recipe.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (recipe.isViral) {
                        ViralBadge()
                    }
                }
                Text(
                    text = stringResource(
                        R.string.home_modal_format,
                        formatRupiah(recipe.estCapital)
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.home_portions_format, recipe.portions),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onActionClick) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = actionContentDescription,
                    tint = actionTint
                )
            }
        }
    }
}

@Composable
private fun ViralBadge() {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Text(
            text = stringResource(R.string.badge_viral),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun RecipeThumbnail(recipe: RecipeSummary) {
    val model = remember(recipe.thumbUrl, recipe.coverUrl) {
        imageModel(recipe.thumbUrl ?: recipe.coverUrl)
    }
    var failed by remember(model) { mutableStateOf(false) }

    val shape = RoundedCornerShape(14.dp)
    if (model == null || failed) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = categoryEmoji(recipe.category),
                style = MaterialTheme.typography.headlineMedium
            )
        }
    } else {
        AsyncImage(
            model = model,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            onError = { failed = true },
            modifier = Modifier
                .size(88.dp)
                .clip(shape)
        )
    }
}

/** Path absolut dari filesDir dibaca sebagai File; URL remote tetap String. */
fun imageModel(raw: String?): Any? {
    val value = raw?.takeIf { it.isNotBlank() } ?: return null
    return if (value.startsWith("/")) File(value) else value
}
