package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.PlaylistPlay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Playlist
import com.example.data.model.Track
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun PlaylistView(viewModel: SilentRushViewModel, modifier: Modifier = Modifier) {
    val playlists by viewModel.playlists.collectAsState()
    val activePlaylist by viewModel.selectedPlaylist.collectAsState()
    val playlistTracks by viewModel.playlistTracks.collectAsState()
    val allTracks by viewModel.allTracks.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showAddTrackDialog by remember { mutableStateOf(false) }

    if (activePlaylist == null) {
        // Playlists List Screen
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .testTag("playlists_view_column")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Playlists",
                        color = StarWhite,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your custom structured moods",
                        color = CosmicGray,
                        fontSize = 13.sp
                    )
                }

                IconButton(
                    onClick = { showCreateDialog = true },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = GlassSurface),
                    modifier = Modifier.testTag("create_playlist_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Playlist",
                        tint = StarWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (playlists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.PlaylistPlay,
                            contentDescription = null,
                            tint = CosmicGray.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No custom spaces yet.",
                            color = StarWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Create your custom midnight spaces combining sleep and calm rain files.",
                            color = CosmicGray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(playlists) { p ->
                        PlaylistRowItem(playlist = p, onClick = {
                            viewModel.selectPlaylist(p)
                        }, onDelete = {
                            viewModel.deletePlaylist(p)
                        })
                    }
                }
            }
        }
    } else {
        // Detailed Playlist Tracks Screen
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .testTag("playlist_details_view")
        ) {
            // Header with Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { viewModel.selectPlaylist(null) },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = GlassSurface)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = StarWhite
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showAddTrackDialog = true },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = GlassSurface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LibraryAdd,
                            contentDescription = "Add Tracks",
                            tint = GlowBlue
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.deletePlaylist(activePlaylist!!)
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = GlassSurface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Playlist",
                            tint = Color.Red.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Playlist info Banner
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = activePlaylist!!.name,
                        color = StarWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (activePlaylist!!.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activePlaylist!!.description,
                            color = CosmicGray,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.playPlaylist(playlistTracks) },
                        colors = ButtonDefaults.buttonColors(containerColor = GlowBlue.copy(alpha = 0.3f)),
                        border = BorderStroke(1.dp, GlowBlue.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = playlistTracks.isNotEmpty()
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = StarWhite)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Play Space Session", color = StarWhite)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (playlistTracks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.LibraryMusic,
                            contentDescription = null,
                            tint = CosmicGray.copy(alpha = 0.3f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "This space is empty.",
                            color = StarWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap the '+' icon above to fill it with custom serene recordings.",
                            color = CosmicGray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(playlistTracks) { track ->
                        PlaylistTrackRowItem(
                            track = track,
                            onPlay = {
                                viewModel.playTrack(track, playlistTracks)
                                viewModel.setNowPlayingExpanded(true)
                            },
                            onRemove = {
                                viewModel.removeTrackFromPlaylist(activePlaylist!!.id, track.id)
                            }
                        )
                    }
                }
            }
        }
    }

    // CREATE PLAYLIST DIALOG
    if (showCreateDialog) {
        var playlistName by remember { mutableStateOf("") }
        var playlistDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = MidnightBlue,
            title = {
                Text(
                    text = "A New Reflection Space",
                    color = StarWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        label = { Text("Space Name (e.g. Rainy Moods)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlowBlue,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = StarWhite,
                            unfocusedTextColor = StarWhite
                        ),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = playlistDesc,
                        onValueChange = { playlistDesc = it },
                        label = { Text("Reflections / Description") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlowBlue,
                            unfocusedBorderColor = GlassBorder,
                            focusedTextColor = StarWhite,
                            unfocusedTextColor = StarWhite
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (playlistName.isNotBlank()) {
                            viewModel.createPlaylist(playlistName, playlistDesc)
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowBlue)
                ) {
                    Text("Form", color = StarWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = CosmicGray)
                }
            }
        )
    }

    // ADD TRACK TO PLAYLIST DIALOG
    if (showAddTrackDialog && activePlaylist != null) {
        AlertDialog(
            onDismissRequest = { showAddTrackDialog = false },
            containerColor = MidnightBlue,
            title = {
                Text(
                    text = "Incorporate Calming Tracks",
                    color = StarWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Box(modifier = Modifier.heightIn(max = 300.dp)) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(allTracks) { track ->
                            val alreadyIn = playlistTracks.any { it.id == track.id }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (!alreadyIn) {
                                            viewModel.addTrackToPlaylist(activePlaylist!!.id, track.id)
                                        } else {
                                            viewModel.removeTrackFromPlaylist(activePlaylist!!.id, track.id)
                                        }
                                    }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = track.title, color = StarWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(text = track.artist, color = CosmicGray, fontSize = 12.sp)
                                }
                                Icon(
                                    imageVector = if (alreadyIn) Icons.Default.CheckCircle else Icons.Default.AddCircleOutline,
                                    contentDescription = null,
                                    tint = if (alreadyIn) GlowCaelum else CosmicGray
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAddTrackDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowBlue)
                ) {
                    Text("Done", color = StarWhite)
                }
            }
        )
    }
}

@Composable
fun PlaylistRowItem(
    playlist: Playlist,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("playlist_row_${playlist.id}"),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Color Art preset ID indicator
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (playlist.imagePresetId) {
                                1 -> Color(0x3B3B2B7A)
                                2 -> Color(0x3B165C58)
                                3 -> Color(0x3B150F2B)
                                else -> Color(0x3B5B87CE)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QueueMusic,
                        tint = StarWhite.copy(alpha = 0.8f),
                        contentDescription = null
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = playlist.name,
                        color = StarWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (playlist.description.isNotEmpty()) {
                        Text(
                            text = playlist.description,
                            color = CosmicGray,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = CosmicGray
            )
        }
    }
}

@Composable
fun PlaylistTrackRowItem(
    track: Track,
    onPlay: () -> Unit,
    onRemove: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onPlay
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    tint = CosmicGray,
                    modifier = Modifier.size(20.dp),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = track.title,
                        color = StarWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = track.artist,
                        color = CosmicGray,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.RemoveCircleOutline,
                    contentDescription = "Remove Track",
                    tint = Color.Red.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
