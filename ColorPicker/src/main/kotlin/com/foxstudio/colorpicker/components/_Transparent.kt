package com.foxstudio.colorpicker.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect

/**
 * 绘制透明棋盘格背景。
 * @param width 宽度
 * @param height 高度
 * @param gridSize 网格大小 Px
 */
internal fun DrawScope.transparentCheckerBackground(
    width: Float,
    height: Float,
    gridSize: Float = 20f
) {
    val numCols = (width / gridSize).toInt() + 1
    val numRows = (height / gridSize).toInt() + 1

    clipRect(0f, 0f, width, height) {
        for (row in 0 until numRows) {
            for (col in 0 until numCols) {
                val isGray = (row + col) % 2 == 0
                drawRect(
                    color = if (isGray) Color(0xFFE0E0E0) else Color.White,
                    topLeft = Offset(col * gridSize, row * gridSize),
                    size = Size(gridSize, gridSize)
                )
            }
        }
    }
}