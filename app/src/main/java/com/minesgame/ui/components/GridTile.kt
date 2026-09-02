package com.minesgame.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.minesgame.data.engine.MinesEngine
import com.minesgame.data.model.Tile
import com.minesgame.data.model.TileState
import com.minesgame.ui.theme.GemEnd
import com.minesgame.ui.theme.GemStart
import com.minesgame.ui.theme.Green
import com.minesgame.ui.theme.Red
import com.minesgame.ui.theme.Tile as TileColor
import com.minesgame.ui.theme.TileBorder
import com.minesgame.ui.theme.TileHover
import com.minesgame.ui.theme.TileMine
import com.minesgame.ui.theme.TileSafe
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun GemIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val path = Path().apply {
            moveTo(cx, h * 0.03f)
            lineTo(w * 0.94f, h * 0.30f)
            lineTo(w * 0.62f, h * 0.44f)
            lineTo(cx, h * 0.92f)
            lineTo(w * 0.38f, h * 0.44f)
            lineTo(w * 0.06f, h * 0.30f)
            close()
        }
        drawPath(
            path = path,
            brush = Brush.linearGradient(
                colors = listOf(GemStart, GemEnd),
                start = Offset(0f, 0f),
                end = Offset(w, h),
            ),
        )
        drawLine(
            color = Color.White.copy(alpha = 0.7f),
            start = Offset(cx, h * 0.03f),
            end = Offset(w * 0.38f, h * 0.44f),
            strokeWidth = w * 0.03f,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
fun MineIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val c = Offset(w / 2f, h / 2f)
        val r = min(w, h) * 0.34f

        val spike = w * 0.05f
        val spikeLength = w * 0.16f
        for (i in 0 until 8) {
            val angle = (i * Math.PI / 4).toFloat()
            val dx = cos(angle)
            val dy = sin(angle)
            drawLine(
                color = Red,
                start = Offset(c.x + dx * (r - spike), c.y + dy * (r - spike)),
                end = Offset(c.x + dx * (r + spikeLength), c.y + dy * (r + spikeLength)),
                strokeWidth = spike,
                cap = StrokeCap.Round,
            )
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Red, Color(0xFF4A0D0D)),
                center = c,
                radius = r,
            ),
            radius = r,
            center = c,
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.35f),
            radius = r * 0.28f,
            center = Offset(c.x - r * 0.28f, c.y - r * 0.28f),
        )
    }
}

@Composable
fun TileView(
    tile: Tile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 1.03f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tileScale",
    )
    val reveal by animateFloatAsState(
        targetValue = if (tile.state == TileState.SAFE || tile.state == TileState.MINE) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "tileReveal",
    )

    val background = when (tile.state) {
        TileState.HIDDEN -> TileColor
        TileState.REVEALING -> TileHover
        TileState.SAFE -> TileSafe
        TileState.MINE -> TileMine
    }
    val borderColor = when (tile.state) {
        TileState.SAFE -> Green
        TileState.MINE -> Red
        else -> TileBorder
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(3.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = tile.state == TileState.HIDDEN,
                onClickLabel = "Reveal tile ${tile.index}",
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .graphicsLayer {
                    scaleX = reveal
                    scaleY = reveal
                },
        ) {
            when (tile.state) {
                TileState.SAFE -> GemIcon(Modifier.fillMaxSize())
                TileState.MINE -> MineIcon(Modifier.fillMaxSize())
                else -> Unit
            }
        }
    }
}

@Composable
fun MinesGrid(
    tiles: List<Tile>,
    onTileClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayTiles = if (tiles.isEmpty()) {
        List(MinesEngine.TILES) { Tile(index = it, isMine = false) }
    } else {
        tiles
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        for (row in 0 until MinesEngine.GRID_SIZE) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                for (col in 0 until MinesEngine.GRID_SIZE) {
                    val index = row * MinesEngine.GRID_SIZE + col
                    TileView(
                        tile = displayTiles[index],
                        onClick = { onTileClick(index) },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                    )
                }
            }
        }
    }
}
