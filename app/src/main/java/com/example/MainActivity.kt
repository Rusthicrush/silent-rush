package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.ui.*
import com.example.ui.components.BackgroundStars
import com.example.ui.components.DreamComposerDialog
import com.example.ui.components.GlassCard
import com.example.ui.components.MiniPlayer
import com.example.ui.theme.CosmicGray
import com.example.ui.theme.GlowBlue
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StarWhite

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Fullscreen edge-to-edge layout for immersive night stars aesthetic
        enableEdgeToEdge()

        val viewModel = ViewModelProvider(
            this,
            SilentRushViewModelFactory(application)
        )[SilentRushViewModel::class.java]

        setContent {
            MyApplicationTheme {
                SilentRushApp(viewModel)
            }
        }
    }
}

@Composable
fun SilentRushApp(viewModel: SilentRushViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val nowPlayingExpanded by viewModel.nowPlayingExpanded.collectAsState()
    val showComposerDialog by viewModel.showComposerDialog.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()

    BackgroundStars {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent, // Flow background stars behind scaffold
            bottomBar = {
                Column {
                    // Render MiniPlayer floating right above navigation bar if a track is active
                    if (currentTrack != null) {
                        MiniPlayer(
                            viewModel = viewModel,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Immersive Bottom Tab bar with safe area padding
                    SilentRushNavigationBar(
                        currentTab = currentTab,
                        onTabSelected = { viewModel.setTab(it) }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                // Crossfade view swaps so there is zero layout flicker
                Crossfade(
                    targetState = currentTab,
                    animationSpec = tween(300),
                    modifier = Modifier.fillMaxSize()
                ) { tab ->
                    when (tab) {
                        HomeTab.HOME -> HomeView(
                            viewModel = viewModel,
                            modifier = Modifier.statusBarsPadding()
                        )
                        HomeTab.SEARCH -> SearchView(
                            viewModel = viewModel,
                            modifier = Modifier.statusBarsPadding()
                        )
                        HomeTab.PLAYLISTS -> PlaylistView(
                            viewModel = viewModel,
                            modifier = Modifier.statusBarsPadding()
                        )
                        HomeTab.EQUALIZER -> EqualizerView(
                            viewModel = viewModel,
                            modifier = Modifier.statusBarsPadding()
                        )
                    }
                }
            }
        }

        // Expanded fullscreen Now Playing sliding up with elegant transitions
        AnimatedVisibility(
            visible = nowPlayingExpanded,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(400)
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(400)
            ) + fadeOut(animationSpec = tween(300))
        ) {
            NowPlayingView(viewModel = viewModel)
        }

        // Live Dream Composer popup dialog
        if (showComposerDialog) {
            DreamComposerDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.setComposerDialogVisible(false) }
            )
        }
    }
}

@Composable
fun SilentRushNavigationBar(
    currentTab = HomeTab.HOME,
    onTabSelected: (HomeTab) -> Unit
) {
    // Glassmorphic navigation bar respecting system navigation safe zones
    NavigationBar(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .height(64.dp)
            .testTag("app_navigation_bar"),
        containerColor = Color(0x3B080C14), // translucent deep midnight
        tonalElevation = 0.dp
    ) {
        val tabs = listOf(
            NavigationTabInfo(HomeTab.HOME, Icons.Default.Home, "Aura"),
            NavigationTabInfo(HomeTab.SEARCH, Icons.Default.Search, "Explore"),
            NavigationTabInfo(HomeTab.PLAYLISTS, Icons.Default.QueueMusic, "Spaces"),
            NavigationTabInfo(HomeTab.EQUALIZER, Icons.Default.Equalizer, "Sculpt")
        )

        tabs.forEach { tabInfo ->
            val active = tabInfo.tab == currentTab
            
            NavigationBarItem(
                selected = active,
                onClick = { onTabSelected(tabInfo.tab) },
                icon = {
                    Icon(
                        imageVector = tabInfo.icon,
                        contentDescription = tabInfo.label,
                        tint = if (active) GlowBlue else CosmicGray
                    )
                },
                label = {
                    Text(
                        text = tabInfo.label,
                        color = if (active) StarWhite else CosmicGray,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0x1BFFFFFF) // subtle iOS glow capsule
                ),
                modifier = Modifier.testTag("nav_tab_${tabInfo.tab.name}")
            )
        }
    }
}

private data class NavigationTabInfo(
    val tab: HomeTab,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)
