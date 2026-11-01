package com.henrisusanto.vitmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.henrisusanto.vitmate.ui.theme.JajanCuanTheme
import com.henrisusanto.vitmate.ui.screens.HomeScreen
import com.henrisusanto.vitmate.ui.screens.FavoritesScreen
import com.henrisusanto.vitmate.ui.screens.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JajanCuanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val items = listOf(
                        BottomNavigationItem(0, "Home", R.string.tab_home),
                        BottomNavigationItem(1, "Favorit", R.string.tab_favorites),
                        BottomNavigationItem(2, "Pengaturan", R.string.tab_settings)
                    )
                    var selected by remember { mutableIntStateOf(0) }

                    Scaffold(
                        bottomBar = {
                            if (selected != 0) {
                                NavigationBar {
                                    items.forEach { item ->
                                        NavigationItem(
                                            icon = { /* TODO: Replace with vector icons */ },
                                            label = { stringResource(id = item.labelId) },
                                            selected = selected == item.index,
                                            onClick = { selected = item.index }
                                        )
                                    }
                                }
                            }
                        }
                    ) { padding ->
                        when (selected) {
                            0 -> HomeScreen(modifier = Modifier.padding(padding))
                            1 -> FavoritesScreen(modifier = Modifier.padding(padding))
                            2 -> SettingsScreen(modifier = Modifier.padding(padding))
                        }
                    }
                }
            }
        }
    }
}

data class BottomNavigationItem(
    val index: Int,
    val label: String,
    val labelId: Int
)