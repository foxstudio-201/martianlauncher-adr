package com.foxstudio.colorpicker.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 竖条指示器
 * @param currentColor 当前颜色，函数内部会取反色显示
 * @param xPos 横向位置
 * @param height 竖条高度
 * @param width 竖条宽度
 * @param overshoot 让指示器上下各超出的距离
 */
internal fun DrawScope.drawVerticalIndicator(
    currentColor: Color,
    xPos: Float,
    height: Float,
    width: Dp = 2.dp,
    overshoot: Dp = 2.dp
) {
    //计算反色
    val invertedColor = invertColor(currentColor)
    val overshootPx = overshoot.toPx()

    drawLine(
        color = invertedColor,
        start = Offset(xPos, -overshootPx),
        end = Offset(xPos, height + overshootPx),
        strokeWidth = width.toPx()
    )
}

/**
 * 圆圈指示器
 * @param currentColor 当前颜色，函数内部会取反色显示
 * @param pressOffset 按下位置
 * @param radius 半径
 */
internal fun DrawScope.drawCenterIndicator(
    currentColor: Color,
    pressOffset: Offset,
    radius: Dp = 6.dp,
    width: Dp = 2.dp
) {
    //计算反色
    val invertedColor = invertColor(currentColor)

    drawCircle(
        color = invertedColor,
        radius = radius.toPx(),
        center = pressOffset,
        style = Stroke(width = width.toPx())
    )
}

/**
 * 计算反色，red, green, blue取反
 * 忽略alpha，默认255
 */
private fun invertColor(color: Color): Color {
    val r = 255 - (color.red * 255).toInt()
    val g = 255 - (color.green * 255).toInt()
    val b = 255 - (color.blue * 255).toInt()
    return Color(r, g, b, 255)
}