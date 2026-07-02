package com.foxstudio.colorpicker.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.foxstudio.colorpicker.ColorPickerController
import com.foxstudio.colorpicker.getGradientColorAtPosition

/**
 * 色相条拾色器
 * @param controller [ColorPickerController] 的实例，用于控制和响应色相变化
 * @param onChangeFinished 当滚动条结束拖动后的回调
 */
@Composable
fun HueBarPicker(
    controller: ColorPickerController,
    modifier: Modifier = Modifier,
    onChangeFinished: () -> Unit = {}
) {
    var pressOffset by remember { mutableStateOf(Offset.Zero) }
    var widthPx by remember { mutableFloatStateOf(0f) }
    var heightPx by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(controller.hue, widthPx) {
        if (widthPx > 0f) {
            pressOffset = Offset(hueToX(controller.hue, widthPx), heightPx / 2f)
        }
    }

    //构造色相渐变颜色（分段实现 0~360°）
    val hueColors = remember {
        buildList {
            for (i in 0..6) {
                add(Color.hsv(i * 60f, 1f, 1f))
            }
        }
    }

    Canvas(
        modifier = modifier
            .progressBar(
                widthPx = widthPx,
                heightPx = heightPx,
                onOffsetChanged = { offset ->
                    pressOffset = offset
                    controller.setHue(xToHue(offset.x, widthPx))
                },
                onSizeChanged = { size ->
                    widthPx = size.width.toFloat()
                    heightPx = size.height.toFloat()
                },
                onChangeFinished = onChangeFinished
            )
    ) {
        //绘制色相条
        drawRect(
            brush = Brush.horizontalGradient(colors = hueColors),
            size = size
        )

        val indicatorColor = hueColors.getGradientColorAtPosition(
            x = pressOffset.x,
            widthPx = widthPx
        )

        drawVerticalIndicator(
            currentColor = indicatorColor,
            xPos = pressOffset.x,
            height = size.height
        )
    }
}

private fun xToHue(x: Float, widthPx: Float): Float =
    (x / widthPx * 360f).coerceIn(0f, 360f)

private fun hueToX(hue: Float, widthPx: Float): Float =
    (hue / 360f * widthPx).coerceIn(0f, widthPx)