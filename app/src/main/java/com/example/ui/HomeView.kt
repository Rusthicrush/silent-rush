package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Track
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun HomeView(
    viewModel: SilentRushViewModel,
    modifier: Modifier = Modifier
) {
    val allTracks by viewModel.allTracks.collectAsState()
    val favoriteTracks by viewModel.favoriteTracks.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val mostListened by viewModel.mostListened.collectAsState()
    val activeMood by viewModel.selectedMood.collectAsState()
    val moodTracks by viewModel.moodTracks.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_column"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Welcome Header
        item {
            HeaderSection(viewModel)
        }

        // Tonight's Special Mood Playlist
        item {
            TonightPlaylistBanner(allTracks, viewModel)
        }

        // Mood Categories Selector / Horizontal Rows
        item {
            MoodSelectorSection(activeMood, moodTracks, viewModel)
        }

        // Favorites Row
        item {
            FavoritesSection(favoriteTracks, viewModel)
        }

        // Recently Played
        item {
            HorizontalTrackRow(
                title = "Recently Played",
                tracks = recentlyPlayed,
                emptyTip = "Clear nights are ahead. Songs you listen to will collect here.",
                viewModel = viewModel
            )
        }

        // Most Listened
        item {
            HorizontalTrackRow(
                title = "Most Listened",
                tracks = mostListened,
                emptyTip = "Your personal frequencies will arrange themselves here with play count.",
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun HeaderSection(viewModel: SilentRushViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Silent Rush",
                    color = StarWhite,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Some songs are not heard, they are felt.",
                    color = CosmicGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Composer shortcut
            IconButton(
                onClick = { viewModel.setComposerDialogVisible(true) },
                modifier = Modifier
                    .clip(CircleShape)
                    .testTag("composer_entry_button"),
                colors = IconButtonDefaults.iconButtonColors(containerColor = GlassSurface)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Compose Custom track",
                    tint = LunarGold
                )
            }
        }
    }
}

@Composable
fun TonightPlaylistBanner(
    allTracks: List<Track>,
    viewModel: SilentRushViewModel
) {
    if (allTracks.isEmpty()) return
    
    // Play of Tonight's choice: Randomly chooses 3 songs to build a cozy custom queue
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .testTag("tonight_banner"),
        onClick = {
            if (allTracks.isNotEmpty()) {
                val list = allTracks.shuffled().take(3)
                viewModel.playPlaylist(list)
                viewModel.setNowPlayingExpanded(true)
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradient color background for aesthetic visual depth
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                // Background visual art lines
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "TONIGHT'S COMBUSTION",
                            color = LunarGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Midnight Stargazing Mix",
                            color = StarWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Curated ambient frequencies for quiet contemplation",
                            color = CosmicGray,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Mix",
                            tint = GlowBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${allTracks.size.coerceAtMost(5)} calm sessions",
                            color = GlowCaelum,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Floating play action button styled iOS-like
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = StarWhite,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 24.dp)
                    .size(48.dp)
            )
        }
    }
}

@Composable
fun MoodSelectorSection(
    activeMood: String,
    moodTracks: List<Track>,
    viewModel: SilentRushViewModel
) {
    val moods = listOf("All", "Peaceful", "Emotional", "Deep", "Focus", "Calm", "Sleep")
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Mood Collection",
            color = StarWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        // Mood Buttons scroll row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            moods.forEach { mood ->
                val selected = mood == activeMood
                GlassCard(
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = if (selected) GlowBlue.copy(alpha = 0.25f) else GlassSurface,
                    borderColor = if (selected) GlowBlue.copy(alpha = 0.5f) else GlassBorder,
                    borderWidth = 1.dp,
                    onClick = { viewModel.setMood(mood) }
                ) {
                    Text(
                        text = mood,
                        color = if (selected) StarWhite else CosmicGray,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Mood Tracks Horizontal Display
        if (moodTracks.isEmpty()) {
            Text(
                text = "No songs matching this mood yet.",
                color = CosmicGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(moodTracks) { track ->
                    TrackCard(track = track, onClick = {
                        viewModel.playTrack(track, moodTracks)
                        viewModel.setNowPlayingExpanded(true)
                    })
                }
            }
        }
    }
}

@Composable
fun FavoritesSection(
    favoriteTracks: List<Track>,
    viewModel: SilentRushViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Favorite Songs",
                color = StarWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (favoriteTracks.isNotEmpty()) {
                Text(
                    text = "${favoriteTracks.size} songs",
                    color = GlowBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (favoriteTracks.isEmpty()) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = CosmicGray.copy(alpha = 0.4f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your heart is quiet.",
                        color = StarWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Tap the star on any song to create your nighttime collection.",
                        color = CosmicGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 24.dp),
                        onTextLayout = {}
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(favoriteTracks) { track ->
                    TrackCard(track = track, onClick = {
                        viewModel.playTrack(track, favoriteTracks)
                        viewModel.setNowPlayingExpanded(true)
                    })
                }
            }
        }
    }
}

@Composable
fun HorizontalTrackRow(
    title: String,
    tracks: List<Track>,
    emptyTip: String,
    viewModel: SilentRushViewModel
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            color = StarWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        if (tracks.isEmpty()) {
            Text(
                text = emptyTip,
                color = CosmicGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(tracks) { track ->
                    TrackCard(track = track, onClick = {
                        viewModel.playTrack(track, tracks)
                        viewModel.setNowPlayingExpanded(true)
                    })
                }
            }
        }
    }
}

@Composable
fun TrackCard(
    track: Track,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
            .testTag("track_card_${track.id}"),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Elegant placeholder album art mimicking stylized cosmic structures
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                Surface(
                    color = when (track.mood) {
                        "Peaceful" -> Color(0x3B5B87CE)
                        "Deep" -> Color(0x4B3B2B7A)
                        "Calm" -> Color(0x4B165C58)
                        "Sleep" -> Color(0x4B150F2B)
                        else -> Color(0x3BA183E2)
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.MusicNote,
                            contentDescription = null,
                            tint = StarWhite.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                        // Tiny moon indicator on Sleep track
                        if (track.mood == "Sleep") {
                            Icon(
                                imageVector = Icons.Default.Brightness2,
                                contentDescription = null,
                                tint = LunarGold.copy(alpha = 0.8f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(14.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
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
}
