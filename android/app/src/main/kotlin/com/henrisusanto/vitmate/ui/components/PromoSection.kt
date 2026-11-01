package com.henrisusanto.vitmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.henrisusanto.vitmate.R
import com.henrisusanto.vitmate.ui.util.rememberIsOnline

/**
 * Grid foto promo + popup viewer dengan aksi Unduh dan Share. Bagian ini hanya
 * dipanggil bila resep punya foto promo valid, sehingga tidak pernah ada gate
 * kosong.
 */
@Composable
fun PromoSection(
    images: List<String>,
    onDownload: (Int) -> Unit,
    onShare: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (images.isEmpty()) return

    var viewerIndex by remember { mutableStateOf<Int?>(null) }
    val isOnline = rememberIsOnline()

    Column(modifier = modifier.fillMaxWidth()) {
        SectionTitle(stringResource(R.string.promo_section_title))

        if (!isOnline) {
            Text(
                text = stringResource(R.string.promo_no_internet),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            Column(
                modifier = Modifier.padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                images.chunked(GRID_COLUMNS).forEachIndexed { rowIndex, row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEachIndexed { columnIndex, url ->
                            val index = rowIndex * GRID_COLUMNS + columnIndex
                            PromoThumbnail(
                                url = url,
                                index = index,
                                onClick = { viewerIndex = index },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size < GRID_COLUMNS) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    viewerIndex?.let { index ->
        PromoViewerDialog(
            images = images,
            initialIndex = index,
            onDismiss = { viewerIndex = null },
            onDownload = onDownload,
            onShare = onShare
        )
    }
}

@Composable
private fun PromoThumbnail(
    url: String,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = imageModel(url),
            contentDescription = stringResource(
                R.string.promo_image_content_desc,
                index + 1
            ),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun PromoViewerDialog(
    images: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit,
    onDownload: (Int) -> Unit,
    onShare: (Int) -> Unit
) {
    var index by remember(initialIndex) { mutableIntStateOf(initialIndex) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.promo_section_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.promo_viewer_close)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = imageModel(images[index]),
                        contentDescription = stringResource(
                            R.string.promo_image_content_desc,
                            index + 1
                        ),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (images.size > 1) {
                        IconButton(
                            onClick = { index = (index - 1 + images.size) % images.size },
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = stringResource(
                                    R.string.promo_viewer_previous
                                )
                            )
                        }
                        IconButton(
                            onClick = { index = (index + 1) % images.size },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = stringResource(
                                    R.string.promo_viewer_next
                                )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onDownload(index) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.button_unduh))
                    }
                    Button(
                        onClick = { onShare(index) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.button_share))
                    }
                }
            }
        }
    }
}

private const val GRID_COLUMNS = 2
