package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LyricLine
import com.example.data.model.Track
import com.example.playback.RepeatMode
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NowPlayingView(
    viewModel: SilentRushViewModel,
    modifier: Modifier = Modifier
) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.playbackProgress.collectAsState()
    val queue by viewModel.queue.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val shuffleEnabled by viewModel.shuffleEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val sleepTimerSec by viewModel.sleepTimerRemainingSec.collectAsState()

    // Sub-sections INSIDE now-playing for neat screen utilization
    var selectedSubTab by remember { mutableStateOf(NowPlayingTab.PLAYER) }

    if (currentTrack == null) return

    val track = currentTrack!!

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MidnightBlue.copy(alpha = 0.98f),
                        DeepBlack
                    )
                )
            )
            .testTag("now_playing_expanded_sheet")
    ) {
        // Star Particle Background behind Now Playing too for deep visual vibe
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Drawing 10 static subtle stars in background
                val starPositions = listOf(
                    0.2f to 0.15f, 0.8f to 0.12f, 0.15f to 0.45f,
                    0.45f to 0.35f, 0.75f to 0.5f, 0.3f to 0.7f,
                    0.85f to 0.75f
                )
                starPositions.forEach { (x, y) ->
                    drawCircle(
                        color = Color.White.copy(alpha = 0.25f),
                        radius = 2.5f,
                        center = androidx.compose.ui.geometry.Offset(x * size.width, y * size.height)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Navigation header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.setNowPlayingExpanded(false) },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = GlassSurface)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = StarWhite
                    )
                }

                // Title & Subtitle
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NOW PLAYING",
                        color = CosmicGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = track.album,
                        color = StarWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Sleep Timer icon / dialog toggle button
                var showSleepTimerMenu by remember { mutableStateOf(false) }
                IconButton(
                    onClick = { showSleepTimerMenu = !showSleepTimerMenu },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (sleepTimerSec > 0) GlowBlue.copy(alpha = 0.25f) else GlassSurface
                    )
                ) {
                    Icon(
                        imageVector = if (sleepTimerSec > 0) Icons.Default.Timer else Icons.Outline.Timer,
                        contentDescription = "Sleep Timer",
                        tint = if (sleepTimerSec > 0) LunarGold else StarWhite
                    )
                }

                // Sleep timer micro menus
                if (showSleepTimerMenu) {
                    SleepTimerPopup(sleepTimerSec, onSelect = { mins ->
                        viewModel.startSleepTimer(mins)
                        showSleepTimerMenu = false
                    }, onCancel = {
                        viewModel.cancelSleepTimer()
                        showSleepTimerMenu = false
                    })
                }
            }

            // Sub Tab Selectors: Player, Lyrics, Queue
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(
                    NowPlayingTab.PLAYER to "Vibe",
                    NowPlayingTab.LYRICS to "Lyrics",
                    NowPlayingTab.QUEUE to "Queue"
                ).forEach { (tab, label) ->
                    val selected = selectedSubTab == tab
                    Text(
                        text = label,
                        color = if (selected) GlowBlue else CosmicGray,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable { selectedSubTab = tab }
                            .padding(vertical = 4.dp, horizontal = 12.dp)
                            .testTag("now_playing_tab_${tab.name}")
                    )
                }
            }

            // Center Dynamic Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = selectedSubTab,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) with fadeOut(animationSpec = tween(220))
                    }
                ) { tab ->
                    when (tab) {
                        NowPlayingTab.PLAYER -> VibeTabContent(track, isPlaying)
                        NowPlayingTab.LYRICS -> LyricsTabContent(track, progress, viewModel)
                        NowPlayingTab.QUEUE -> QueueTabContent(queue, currentIndex, viewModel)
                    }
                }
            }

            // Bottom Audio Panel: Track Info, Progress, Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Song Metadata
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            color = StarWhite,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            color = CosmicGray,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }

                    // Favorite Button
                    IconButton(
                        onClick = { viewModel.toggleFavorite(track) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(GlassSurface)
                    ) {
                        Icon(
                            imageVector = if (track.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (track.isFavorite) LunarGold else StarWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Progress Bar Slider
                Column {
                    Slider(
                        value = progress.toFloat(),
                        onValueChange = { viewModel.seekTo(it.toLong()) },
                        valueRange = 0f..track.durationMs.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = GlowBlue,
                            activeTrackColor = GlowCaelum,
                            inactiveTrackColor = GlassBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("playback_progress_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(progress),
                            color = CosmicGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatTime(track.durationMs),
                            color = CosmicGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Control Deck Controls
                ControlDeck(
                    isPlaying = isPlaying,
                    shuffleEnabled = shuffleEnabled,
                    repeatMode = repeatMode,
                    onPlayPause = { viewModel.togglePlayPause() },
                    onPrev = { viewModel.playPrevious() },
                    onNext = { viewModel.playNext() },
                    onToggleShuffle = { viewModel.toggleShuffle() },
                    onCycleRepeat = { viewModel.cycleRepeatMode() }
                )
            }
        }
    }
}

enum class NowPlayingTab {
    PLAYER, LYRICS, QUEUE
}

@Composable
fun VibeTabContent(track: Track, isPlaying: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        
        // Artwork rotate animation
        val rotationAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(15000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        val sizePulse by animateFloatAsState(
            targetValue = if (isPlaying) 1.05f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )

        // Glow Background
        Box(
            modifier = Modifier
                .size((210 * sizePulse).dp)
                .rotate(if (isPlaying) rotationAngle else 0f)
                .shadow(
                    elevation = 28.dp,
                    shape = CircleShape,
                    ambientColor = GlowBlue,
                    spotColor = GlowBlue
                ),
            contentAlignment = Alignment.Center
        ) {
            // Elegant Cosmic vinyl background
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = DeepBlack,
                border = BorderStroke(3.dp, GlassBorder)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Styled vinyl records grooves
                    Canvas(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                        drawCircle(color = GlassBorder, radius = size.minDimension / 2f, style = Stroke(width = 1f))
                        drawCircle(color = GlassBorder, radius = size.minDimension / 3f, style = Stroke(width = 1f))
                        drawCircle(color = GlassBorder, radius = size.minDimension / 4f, style = Stroke(width = 1.5f))
                    }

                    // Core track symbol
                    Surface(
                        modifier = Modifier.size(60.dp),
                        shape = CircleShape,
                        color = when (track.mood) {
                            "Peaceful" -> Color(0x755B87CE)
                            "Deep" -> Color(0x753B2B7A)
                            "Calm" -> Color(0x75165C58)
                            "Sleep" -> Color(0x75150F2B)
                            else -> Color(0x758E7CFF)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = StarWhite,
                            modifier = Modifier
                                .padding(14.dp)
                                .size(28.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Fully Simulated/Animated Audio Waveform Canvas!
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            val wavePhase by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 2 * Math.PI.toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val midY = h / 2f
                
                val path1 = Path()
                val path2 = Path()

                // Drawing double sine wave overlay
                for (x in 0..w.toInt() step 5) {
                    val angle1 = (x.toFloat() / w) * 3 * Math.PI.toFloat() + wavePhase
                    val angle2 = (x.toFloat() / w) * 5 * Math.PI.toFloat() - wavePhase + 1.0f
                    
                    val amp1 = if (isPlaying) 15f else 1.5f
                    val amp2 = if (isPlaying) 10f else 1f
                    
                    val y1 = midY + sin(angle1) * amp1
                    val y2 = midY + sin(angle2) * amp2

                    if (x == 0) {
                        path1.moveTo(0f, y1)
                        path2.moveTo(0f, y2)
                    } else {
                        path1.lineTo(x.toFloat(), y1)
                        path2.lineTo(x.toFloat(), y2)
                    }
                }

                drawPath(path1, color = GlowBlue.copy(alpha = 0.8f), style = Stroke(width = 3f))
                drawPath(path2, color = GlowCaelum.copy(alpha = 0.5f), style = Stroke(width = 1.5f))
            }
        }
    }
}

@Composable
fun LyricsTabContent(
    track: Track,
    currentProgressMs: Long,
    viewModel: SilentRushViewModel
) {
    val lyrics = remember(track) { track.getParsedLyrics() }
    val activeIndex by viewModel.activeLyricIndex.collectAsState()
    val listState = rememberLazyListState()

    // Smooth auto centering as active index changes
    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0) {
            // Centers the item nicely in the viewport
            listState.animateScrollToItem(activeIndex, scrollOffset = -220)
        }
    }

    if (lyrics.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No lyrics timings available for this serene recording.",
                color = CosmicGray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 120.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            itemsIndexed(lyrics) { idx, line ->
                val isActive = idx == activeIndex
                val scale by animateFloatAsState(if (isActive) 1.08f else 0.95f)
                val alpha by animateFloatAsState(if (isActive) 1.0f else 0.4f)

                Text(
                    text = line.text,
                    color = if (isActive) LunarGold else StarWhite,
                    fontSize = if (isActive) 20.sp else 16.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .rotate(0f)
                        .clickable {
                            // Touch interaction seeking directly to the lyric line segment!
                            viewModel.seekTo(line.timestampMs)
                        }
                        .padding(horizontal = 12.dp)
                        .testTag("lyric_line_$idx"),
                    style = LocalTextStyle.current.copy(
                        drawStyle = null // Avoid visual complications
                    )
                )
            }
        }
    }
}

@Composable
fun QueueTabContent(
    queue: List<Track>,
    currentIndex: Int,
    viewModel: SilentRushViewModel
) {
    Column(modifier = Modifier.fillMaxSize().padding(vertical = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "UPCOMING QUEUE",
                color = CosmicGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "${queue.size} songs",
                color = GlowBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (queue.isEmpty() || currentIndex == -1) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Queue is empty.", color = CosmicGray)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Sliced queue starting from current offset to end
                val upcoming = queue.slice(currentIndex until queue.size)
                
                itemsIndexed(upcoming) { i, track ->
                    val isFirst = i == 0
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = if (isFirst) GlowBlue.copy(alpha = 0.15f) else GlassSurface,
                        borderColor = if (isFirst) GlowBlue.copy(alpha = 0.3f) else GlassBorder,
                        onClick = {
                            // Adjust index to actual queue position
                            viewModel.playTrack(track, queue)
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = if (isFirst) Icons.Default.VolumeUp else Icons.Default.MusicNote,
                                    tint = if (isFirst) GlowCaelum else CosmicGray,
                                    modifier = Modifier.size(18.dp),
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = track.title,
                                        color = if (isFirst) GlowCaelum else StarWhite,
                                        fontSize = 13.sp,
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

                            if (isFirst) {
                                Text(
                                    text = "Playing",
                                    color = GlowBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
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
fun ControlDeck(
    isPlaying: Boolean,
    shuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    onPlayPause: () -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Shuffle Button
        IconButton(onClick = onToggleShuffle) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                tint = if (shuffleEnabled) GlowBlue else CosmicGray,
                modifier = Modifier.size(20.dp)
            )
        }

        // Previous Button
        IconButton(onClick = onPrev) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Previous",
                tint = StarWhite,
                modifier = Modifier.size(30.dp)
            )
        }

        // Play/Pause Floating glass action button
        IconButton(
            onClick = onPlayPause,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .shadow(elevation = 12.dp, shape = CircleShape, clip = false)
                .testTag("expanded_play_pause_button"),
            colors = IconButtonDefaults.iconButtonColors(containerColor = GlowBlue.copy(alpha = 0.25f))
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause : Icons.Filled.PlayArrow,
                contentDescription = "Play/Pause",
                tint = StarWhite,
                modifier = Modifier.size(34.dp)
            )
        }

        // Next Button
        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next",
                tint = StarWhite,
                modifier = Modifier.size(30.dp)
            )
        }

        // Repeat Button cycling repeat styles
        IconButton(onClick = onCycleRepeat) {
            val icon = when (repeatMode) {
                RepeatMode.ONE -> Icons.Default.RepeatOne
                else -> Icons.Default.Repeat
            }
            val tint = when (repeatMode) {
                RepeatMode.NONE -> CosmicGray
                else -> GlowBlue
            }
            Icon(
                imageVector = icon,
                contentDescription = "Repeat",
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SleepTimerPopup(
    currentRemainingSec: Int,
    onSelect: (Int) -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(10.dp)
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = MidnightBlue),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Sleep Timer",
                color = StarWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            if (currentRemainingSec > 0) {
                Text(
                    text = "Active: ${currentRemainingSec / 60}m ${currentRemainingSec % 60}s left",
                    color = LunarGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = StarWhite, fontSize = 11.sp)
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(1, 15, 30, 60).forEach { mins ->
                        Button(
                            onClick = { onSelect(mins) },
                            colors = ButtonDefaults.buttonColors(containerColor = GlassSurface),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.width(55.dp)
                        ) {
                            Text("${mins}m", color = StarWhite, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val secTotal = ms / 1000
    val min = secTotal / 60
    val sec = secTotal % 60
    return String.format("%02d:%02d", min, sec)
}
