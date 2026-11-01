package com.henrisusanto.vitmate.ui.theme

import com.henrisusanto.vitmate.R
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.henrisusanto.vitmate.data.AppContainer
import com.henrisusanto.vitmate.util.AnalyticsLogger

@Composable
fun JajanCuanTheme(content: @Composable () -> Unit) {
    MaterialTheme {
        // Static warm brand colors - orange/yellow, no dynamic color
        CompositionLocalProvider(
            localColorProvider = remember { MaterialTheme.colorScheme.copy(
                primaryColor = Color(0xFFFF9800),       // brand_primary
                primaryContainerColor = Color(0xFFFFF3E0), // brand_primary_container
                secondaryColor = Color(0xFFFFB74D),     // brand_secondary
                secondaryContainerColor = Color(0xffe0b2), // brand_secondary_container
                surfaceColor = Color(0xFFFFDF6),        // brand_surface
                onSurfaceColor = Color(0xFF1A1A1A),     // brand_on_surface
                errorColor = Color(0xFFD32F2F),         // brand_error
                successColor = Color(0xFF388E3C)        // brand_success
            )}
        ) {
            content()
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    var filter by remember { mutableIntStateOf(0) }
    val items = remember { listOf("Semua", "gorengan", "kue", "minuman") }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = stringResource(R.string.home_header_title),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Search bar
        OutlinedTextField(
            value = "",
            onValueChange = {},
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            expandableIcon = { Icon(Icons.Outlined.ExpandMore, contentDescription = null) },
            label = { Text("Cari resep jajanan...") },
            placeholder = { Text("Cari resep jajanan…") },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Category chips
        ScrollableColumn(
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            items.forEach { category ->
                FilterChip(
                    selected = (filter == items.indexOf(category)),
                    onSelect = { filter = if (it == filter) 0 else items.indexOf(category) },
                    label = { Text(category) },
                    filterIcon = null,
                    value = filter
                )
            }
        }
        
        // TODO: LazyColumn for recipe cards
        // TODO: Native ad every 5 items
        // TODO: Banner ad above bottom nav
        
        Text(
            text = "Home Screen - Resep akan ditampilkan di sini",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun FavoritesScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.favorites_header),
            style = MaterialTheme.typography.headlineMedium
        )
        
        // TODO: RecyclerView/Favorites grid with local data
        // TODO: Delete favorite button
        // TODO: Native ad placement
        // TODO: Banner ad
        
        Text(
            text = "Favorites Screen - Resep favorit akan ditampilkan di sini",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium
        )
        
        // TODO: Theme toggle (DataStore)
        // TODO: Version info
        // TODO: Privacy policy link
        // TODO: Ads privacy options
        
        Text(
            text = "Settings Screen - Pengaturan akan ditampilkan di sini",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}