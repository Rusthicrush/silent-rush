package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Track
import com.example.ui.SilentRushViewModel
import com.example.ui.theme.*

@Composable
fun MiniPlayer(
    viewModel: SilentRushViewModel,
    modifier: Modifier = Modifier
) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.playbackProgress.collectAsState()

    if (currentTrack == null) return

    val track = currentTrack!!

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 12.dp)
            .clickable { viewModel.setNowPlayingExpanded(true) }
            .testTag("collapsed_mini_player"),
        shape = RoundedCornerShape(12.dp),
        backgroundColor = MidnightBlue.copy(alpha = 0.95f),
        borderColor = GlassBorder,
        borderWidth = 1.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Horizontal micro progress meter at the top of the miniplayer
            val progressRatio = if (track.durationMs > 0) {
                progress.toFloat() / track.durationMs
            } else 0f
            
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressRatio)
                    .height(2.dp)
                    .background(GlowBlue)
                    .align(Alignment.TopStart)
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side metadata
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small spinning vinyl record
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x1BFFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(16.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = when (track.mood) {
                                "Peaceful" -> Color(0x755B87CE)
                                "Deep" -> Color(0x753B2B7A)
                                "Calm" -> Color(0x75165C58)
                                "Sleep" -> Color(0x75150F2B)
                                else -> Color(0x758E7CFF)
                            }
                        ) {}
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = track.title,
                            color = StarWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            color = CosmicGray,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Controls Right
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier.testTag("mini_player_play_pause")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = StarWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
