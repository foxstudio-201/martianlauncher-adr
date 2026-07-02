package com.foxstudio.colorpicker.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.foxstudio.colorpicker.ColorPickerController

/**
 * 颜色选择方形面板，用于选择饱和度与明度
 * @param controller [ColorPickerController] 的实例，用于控制和响应颜色变化
 * @param onChangeFinished 结束拖动后的回调
 */
@Composable
fun ColorSquarePicker(
    controller: ColorPickerController,
    modifier: Modifier = Modifier,
    onChangeFinished: () -> Unit = {}
) {
    var pressOffset by remember { mutableStateOf(Offset.Zero) }
    var widthPx by remember { mutableFloatStateOf(0f) }
    var heightPx by remember { mutableFloatStateOf(0f) }

    val hue by remember(controller.hue) {
        derivedStateOf { controller.hue }
    }

    val currentColor by controller.color

    LaunchedEffect(controller.saturation, controller.value, widthPx, heightPx) {
        if (widthPx > 0f && heightPx > 0f) {
            pressOffset = satValToOffset(controller.saturation, controller.value, widthPx, heightPx)
        }
    }

    Canvas(
        modifier = modifier
            .squarePicker(
                widthPx = widthPx,
                heightPx = heightPx,
                onOffsetChanged = { offset ->
                    pressOffset = offset
                    val (s, v) = pointToSatVal(offset.x, offset.y, widthPx, heightPx)
                    controller.setSaturation(s)
                    controller.setValue(v)
                },
                onSizeChanged = { size ->
                    widthPx = size.width.toFloat()
                    heightPx = size.height.toFloat()
                },
                onChangeFinished = onChangeFinished
            )
    ) {
        //饱和度-明度色板
        val hueColor = Color.hsv(hue, 1f, 1f)

        //横向饱和度渐变：白 -> 当前色调
        val satBrush = Brush.horizontalGradient(
            0f to Color.White,
            1f to hueColor
        )

        //纵向明度渐变：透明 -> 黑色
        val valBrush = Brush.verticalGradient(
            0f to Color.Transparent,
            1f to Color.Black
        )

        //饱和度
        drawRect(
            brush = satBrush,
            size = size
        )

        //明度叠加
        drawRect(
            brush = valBrush,
            size = size,
            blendMode = BlendMode.Multiply
        )

        drawCenterIndicator(
            currentColor = currentColor,
            pressOffset = pressOffset
        )
    }
}

/**
 * 将触摸点坐标转换为饱和度与明度
 */
private fun pointToSatVal(x: Float, y: Float, widthPx: Float, heightPx: Float): Pair<Float, Float> {
    val sat = (x / widthPx).coerceIn(0f, 1f)
    val value = (1f - y / heightPx).coerceIn(0f, 1f)
    return sat to value
}

/**
 * 将饱和度与明度转换为坐标
 */
private fun satValToOffset(s: Float, v: Float, widthPx: Float, heightPx: Float): Offset {
    val x = (s * widthPx).coerceIn(0f, widthPx)
    val y = ((1f - v) * heightPx).coerceIn(0f, heightPx)
    return Offset(x, y)
}