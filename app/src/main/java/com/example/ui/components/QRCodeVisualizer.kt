package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun QRCodeVisualizer(
    text: String,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    matrixColor: Color = Color(0xFF0F172A),
    backgroundColor: Color = Color.White
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size - 24.dp)) {
            val canvasWidth = this.size.width
            val gridCount = 21
            val cellSize = canvasWidth / gridCount

            // Background
            drawRect(color = backgroundColor)

            // Generate deterministic pseudo-QR grid based on text hash
            val hash = text.hashCode()
            val grid = Array(gridCount) { BooleanArray(gridCount) }

            // Standard finder patterns (top-left, top-right, bottom-left)
            fun drawFinder(startX: Int, startY: Int) {
                for (r in 0..6) {
                    for (c in 0..6) {
                        val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                        val isInnerSquare = r in 2..4 && c in 2..4
                        grid[startX + r][startY + c] = isBorder || isInnerSquare
                    }
                }
            }

            drawFinder(0, 0)
            drawFinder(0, gridCount - 7)
            drawFinder(gridCount - 7, 0)

            // Fill body modules
            for (r in 0 until gridCount) {
                for (c in 0 until gridCount) {
                    // Skip finder regions
                    val inTopLeft = r < 7 && c < 7
                    val inTopRight = r < 7 && c >= gridCount - 7
                    val inBottomLeft = r >= gridCount - 7 && c < 7

                    if (!inTopLeft && !inTopRight && !inBottomLeft) {
                        val pseudoSeed = abs((hash + r * 37 + c * 17) xor (r * c))
                        grid[r][c] = (pseudoSeed % 3 == 0) || (pseudoSeed % 7 == 1)
                    }
                }
            }

            // Draw grid cells
            for (r in 0 until gridCount) {
                for (c in 0 until gridCount) {
                    if (grid[r][c]) {
                        drawRoundRect(
                            color = matrixColor,
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize * 0.9f, cellSize * 0.9f),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }
                }
            }
        }
    }
}
