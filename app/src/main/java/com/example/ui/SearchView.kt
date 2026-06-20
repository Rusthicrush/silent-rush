package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Track
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun SearchView(viewModel: SilentRushViewModel, modifier: Modifier = Modifier) {
    val query by viewModel.searchQuery.collectAsState()
    val results by viewModel.searchedTracks.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .testTag("search_view_column")
    ) {
        Text(
            text = "Search",
            color = StarWhite,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Find offline tracks or compose new calmness",
            color = CosmicGray,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar with glassmorphism styling
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            backgroundColor = GlassSurface,
            borderColor = GlassBorder
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = CosmicGray,
                    modifier = Modifier.size(22.dp)
                )
                TextField(
                    value = query,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = "Search songs, artists, or moods...",
                            color = CosmicGray,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input_field"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite
                    ),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (results.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = CosmicGray.copy(alpha = 0.3f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Silences are quiet.",
                        color = StarWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "No tracks match your query. Try 'Rain' or 'Sola'.",
                        color = CosmicGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.setComposerDialogVisible(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = GlowBlue.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, GlowBlue.copy(alpha = 0.5f))
                    ) {
                        Text("Compose custom song", color = StarWhite)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(results) { track ->
                    SearchResultRow(track = track, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun SearchResultRow(track: Track, viewModel: SilentRushViewModel) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isCurrent = currentTrack?.id == track.id

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("search_result_${track.id}"),
        onClick = {
            viewModel.playTrack(track)
            viewModel.setNowPlayingExpanded(true)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon / Album art indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x1AFFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCurrent && isPlaying) Icons.Default.BarChart else Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = if (isCurrent) GlowBlue else StarWhite.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = if (isCurrent) GlowBlue else StarWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = "${track.artist}  •  ${track.mood}",
                    color = CosmicGray,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            // Star / Favorite Action Button
            IconButton(
                onClick = { viewModel.toggleFavorite(track) }
            ) {
                Icon(
                    imageVector = if (track.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (track.isFavorite) LunarGold else CosmicGray
                )
            }

            // Quick Play Button icon
            IconButton(
                onClick = {
                    viewModel.playTrack(track)
                    viewModel.setNowPlayingExpanded(true)
                }
            ) {
                Icon(
                    imageVector = if (isCurrent && isPlaying) Icons.Filled.PauseCircle else Icons.Filled.PlayCircle,
                    contentDescription = "Play",
                    tint = StarWhite
                )
            }
        }
    }
}
