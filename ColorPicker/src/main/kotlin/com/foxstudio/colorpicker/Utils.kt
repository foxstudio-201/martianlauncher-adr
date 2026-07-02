package com.foxstudio.colorpicker

import androidx.compose.ui.graphics.Color

/**
 * 在颜色渐变中，根据当前X坐标获取对应的颜色
 */
internal fun List<Color>.getGradientColorAtPosition(x: Float, widthPx: Float): Color {
    if (this.isEmpty()) return Color.Black
    if (x <= 0f) return this.first()
    if (x >= widthPx) return this.last()

    val fraction = x / widthPx
    val totalSegments = this.size - 1
    val segmentFraction = fraction * totalSegments
    val segmentIndex = segmentFraction.toInt()
    val t = segmentFraction - segmentIndex

    val startColor = this[segmentIndex]
    val endColor = this[segmentIndex + 1]

    return Color(
        red = startColor.red + (endColor.red - startColor.red) * t,
        green = startColor.green + (endColor.green - startColor.green) * t,
        blue = startColor.blue + (endColor.blue - startColor.blue) * t,
        alpha = startColor.alpha + (endColor.alpha - startColor.alpha) * t
    )
}
