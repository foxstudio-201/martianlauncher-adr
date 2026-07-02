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
import androidx.compose.ui.graphics.Brush
import com.foxstudio.colorpicker.ColorPickerController
import com.foxstudio.colorpicker.getGradientColorAtPosition

/**
 * 透明度条拾色器
 * @param controller [ColorPickerController] 的实例，用于控制和响应透明度变化
 * @param onChangeFinished 当滚动条结束拖动后的回调
 */
@Composable
fun AlphaBarPicker(
    controller: ColorPickerController,
    modifier: Modifier = Modifier,
    onChangeFinished: () -> Unit = {}
) {
    var pressOffset by remember { mutableStateOf(Offset.Zero) }
    var widthPx by remember { mutableFloatStateOf(0f) }
    var heightPx by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(controller.alpha, widthPx) {
        if (widthPx > 0f) {
            pressOffset = Offset(alphaToX(controller.alpha, widthPx), heightPx / 2f)
        }
    }

    //当前颜色
    val currentColor by controller.color

    //透明 -> 当前颜色
    val colors by remember(currentColor) {
        derivedStateOf {
            listOf(
                currentColor.copy(alpha = 0f),
                currentColor.copy(alpha = 1f)
            )
        }
    }

    Canvas(
        modifier = modifier
            .progressBar(
                widthPx = widthPx,
                heightPx = heightPx,
                onOffsetChanged = { offset ->
                    pressOffset = offset
                    controller.setAlpha(xToAlpha(offset.x, widthPx))
                },
                onSizeChanged = { size ->
                    widthPx = size.width.toFloat()
                    heightPx = size.height.toFloat()
                },
                onChangeFinished = onChangeFinished
            )
    ) {
        transparentCheckerBackground(
            width = size.width,
            height = size.height
        )

        drawRect(
            brush = Brush.horizontalGradient(colors = colors),
            size = size
        )

        val indicatorColor = colors.getGradientColorAtPosition(
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

private fun alphaToX(alpha: Float, widthPx: Float) =
    (alpha * widthPx).coerceIn(0f, widthPx)

private fun xToAlpha(x: Float, widthPx: Float) =
    (x / widthPx).coerceIn(0f, 1f)