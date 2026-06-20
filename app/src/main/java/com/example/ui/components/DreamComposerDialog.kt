package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SilentRushViewModel
import com.example.ui.theme.*

@Composable
fun DreamComposerDialog(
    viewModel: SilentRushViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var album by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf("Calm") }

    val moods = listOf("Peaceful", "Emotional", "Deep", "Focus", "Calm", "Sleep")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MidnightBlue,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = LunarGold
                )
                Text(
                    text = "Acoustic Reflection Composer",
                    color = StarWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "This composer will physically synthesize and write a new, loopable, 30-second standard wav audio tracking sequence to your device repository.",
                    color = CosmicGray,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reflection Title (e.g. Midnight Mist)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite,
                        focusedBorderColor = GlowBlue,
                        unfocusedBorderColor = GlassBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("composer_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist / Composer") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite,
                        focusedBorderColor = GlowBlue,
                        unfocusedBorderColor = GlassBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("composer_artist_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = album,
                    onValueChange = { album = it },
                    label = { Text("Album Sphere Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = StarWhite,
                        unfocusedTextColor = StarWhite,
                        focusedBorderColor = GlowBlue,
                        unfocusedBorderColor = GlassBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("composer_album_input"),
                    singleLine = true
                )

                // Mood selector grid row
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Target Aura Mood", color = StarWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        moods.take(3).forEach { mood ->
                            val active = mood == selectedMood
                            GlassCard(
                                shape = RoundedCornerShape(12.dp),
                                backgroundColor = if (active) GlowBlue.copy(alpha = 0.2f) else GlassSurface,
                                borderColor = if (active) GlowBlue.copy(alpha = 0.5f) else GlassBorder,
                                onClick = { selectedMood = mood },
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    Text(text = mood, color = StarWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        moods.drop(3).forEach { mood ->
                            val active = mood == selectedMood
                            GlassCard(
                                shape = RoundedCornerShape(12.dp),
                                backgroundColor = if (active) GlowBlue.copy(alpha = 0.2f) else GlassSurface,
                                borderColor = if (active) GlowBlue.copy(alpha = 0.5f) else GlassBorder,
                                onClick = { selectedMood = mood },
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    Text(text = mood, color = StarWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && artist.isNotBlank() && album.isNotBlank()) {
                        viewModel.importLocalMockSong(title, artist, album, selectedMood)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GlowBlue),
                modifier = Modifier.testTag("compose_submit_button")
            ) {
                Text("Synthesize", color = StarWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Bypass", color = CosmicGray)
            }
        }
    )
}
