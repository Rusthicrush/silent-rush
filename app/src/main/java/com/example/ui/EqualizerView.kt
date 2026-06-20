package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playback.EqBand
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

@Composable
fun EqualizerView(viewModel: SilentRushViewModel, modifier: Modifier = Modifier) {
    val enabled by viewModel.eqEnabled.collectAsState()
    val bands by viewModel.eqBands.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .testTag("equalizer_view_column")
    ) {
        Column {
            Text(
                text = "Equalizer",
                color = StarWhite,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Sculpt the nocturnal sound landscape",
                color = CosmicGray,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Toggle Card
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = null,
                        tint = if (enabled) GlowBlue else CosmicGray,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Calibrate Decibels",
                            color = StarWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (enabled) "Nocturnal filter active" else "Unfiltered bypass mode",
                            color = CosmicGray,
                            fontSize = 12.sp
                        )
                    }
                }

                Switch(
                    checked = enabled,
                    onCheckedChange = { viewModel.toggleEqualizer() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = StarWhite,
                        checkedTrackColor = GlowBlue,
                        uncheckedThumbColor = CosmicGray,
                        uncheckedTrackColor = GlassSurface
                    ),
                    modifier = Modifier.testTag("eq_master_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Freq Curve representation (Dynamic Bezier Canvas!)
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                Text(
                    text = "Acoustic Reflection Curve",
                    color = CosmicGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (bands.isEmpty()) return@Canvas
                    
                    val w = size.width
                    val h = size.height
                    val centerY = h / 2f
                    
                    val path = Path()
                    val stepX = w / (bands.size - 1)
                    
                    bands.forEachIndexed { i, band ->
                        val ratio = (band.currentLevelMilliBels.toFloat() - band.minLevelMilliBels) / 
                                (band.maxLevelMilliBels - band.minLevelMilliBels) // 0.0 to 1.0
                        
                        // Map 0..1 ratio to centerY +/- 40dp limit
                        val mappedY = centerY - (ratio - 0.5f) * (h * 0.7f)
                        val x = i * stepX
                        
                        if (i == 0) {
                            path.moveTo(x, mappedY)
                        } else {
                            // Quadratic curve interpolation
                            val prevRatio = (bands[i-1].currentLevelMilliBels.toFloat() - bands[i-1].minLevelMilliBels) /
                                    (bands[i-1].maxLevelMilliBels - bands[i-1].minLevelMilliBels)
                            val prevY = centerY - (prevRatio - 0.5f) * (h * 0.7f)
                            val prevX = (i - 1) * stepX
                            
                            val controlX = (prevX + x) / 2f
                            path.quadraticTo(controlX, prevY, x, mappedY)
                        }
                    }

                    // Draw static centerY line
                    drawLine(
                        color = GlassBorder,
                        start = Offset(0f, centerY),
                        end = Offset(w, centerY),
                        strokeWidth = 1f
                    )

                    // Draw actual curve
                    drawPath(
                        path = path,
                        color = if (enabled) GlowBlue else CosmicGray.copy(alpha = 0.5f),
                        style = Stroke(width = 4f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Presets Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                PresetEqProfile("Flat", listOf(0, 0, 0, 0, 0)),
                PresetEqProfile("Chime Boost", listOf(200, 300, -200, 800, 900)),
                PresetEqProfile("Space Pad", listOf(600, 400, 100, -200, -300)),
                PresetEqProfile("Deep Sub", listOf(1000, 600, -100, -400, -800))
            ).forEach { profile ->
                GlassCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    onClick = {
                        if (enabled && bands.size == profile.levels.size) {
                            bands.forEachIndexed { idx, band ->
                                // Map levels (range -1000 to 1000 or similar based on center levels)
                                val milliBels = profile.levels[idx]
                                viewModel.setEqualizerBandLevel(band, milliBels)
                            }
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.name,
                            color = if (enabled) StarWhite else CosmicGray.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Audio bands sliders
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(bands) { band ->
                BandSliderRow(band = band, enabled = enabled, onLevelChange = { level ->
                    viewModel.setEqualizerBandLevel(band, level)
                })
            }
        }
    }
}

private data class PresetEqProfile(
    val name: String,
    val levels: List<Int> // mapped out of -1500 to 1500
)

@Composable
fun BandSliderRow(
    band: EqBand,
    enabled: Boolean,
    onLevelChange: (Int) -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = band.label,
                color = if (enabled) StarWhite else CosmicGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(60.dp)
            )

            Slider(
                value = band.currentLevelMilliBels.toFloat(),
                onValueChange = { onLevelChange(it.toInt()) },
                valueRange = band.minLevelMilliBels.toFloat()..band.maxLevelMilliBels.toFloat(),
                enabled = enabled,
                colors = SliderDefaults.colors(
                    thumbColor = if (enabled) GlowBlue else CosmicGray,
                    activeTrackColor = if (enabled) GlowCaelum else CosmicGray.copy(alpha = 0.5f),
                    inactiveTrackColor = GlassBorder
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("eq_slider_${band.id}")
            )

            Spacer(modifier = Modifier.width(12.dp))

            val displayDb = band.currentLevelMilliBels / 100
            Text(
                text = "${if (displayDb > 0) "+" else ""}$displayDb dB",
                color = if (enabled) GlowCaelum else CosmicGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(45.dp)
            )
        }
    }
}
