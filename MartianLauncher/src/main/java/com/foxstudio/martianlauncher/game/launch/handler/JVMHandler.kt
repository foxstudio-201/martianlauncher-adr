/*
 * Martian Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.foxstudio.martianlauncher.game.launch.handler

import android.graphics.Canvas
import android.graphics.Paint
import android.view.KeyEvent
import android.view.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withSave
import com.foxstudio.martianlauncher.bridge.ZLBridge
import com.foxstudio.martianlauncher.game.input.AWTInputEvent
import com.foxstudio.martianlauncher.game.launch.JvmLauncher
import com.foxstudio.martianlauncher.ui.control.input.TextInputMode
import com.foxstudio.martianlauncher.ui.screens.game.JVMScreen
import com.foxstudio.martianlauncher.ui.screens.game.elements.LogState
import com.foxstudio.martianlauncher.utils.logging.Logger
import com.foxstudio.martianlauncher.viewmodel.ErrorViewModel
import com.foxstudio.martianlauncher.viewmodel.EventViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "JVMHandler"

class JVMHandler(
    jvmLauncher: JvmLauncher,
    errorViewModel: ErrorViewModel,
    eventViewModel: EventViewModel,
    onExit: (code: Int) -> Unit
) : AbstractHandler(
    type = HandlerType.JVM,
    errorViewModel = errorViewModel,
    eventViewModel = eventViewModel,
    launcher = jvmLauncher,
    onExit = onExit
) {
    /**
     * 日志展示状态
     */
    private var logState by mutableStateOf(LogState.CLOSE)

    override suspend fun execute(
        surface: Surface,
        screenSize: IntSize,
        scope: CoroutineScope
    ) {
        val canvasWidth = screenSize.width
        val canvasHeight = screenSize.height

        scope.launch(Dispatchers.Default) {
            var canvas: Canvas?
            val rgbArrayBitmap = createBitmap(canvasWidth, canvasHeight)
            val paint = Paint()

            try {
                while (!mIsSurfaceDestroyed && surface.isValid) {
                    canvas = surface.lockCanvas(null)
                    canvas?.drawRGB(0, 0, 0)

                    ZLBridge.renderAWTScreenFrame()?.let { rgbArray ->
                        canvas?.withSave {
                            rgbArrayBitmap.setPixels(
                                rgbArray,
                                0,
                                canvasWidth,
                                0,
                                0,
                                canvasWidth,
                                canvasHeight
                            )
                            this.drawBitmap(rgbArrayBitmap, 0f, 0f, paint)
                        }
                    }

                    canvas?.let { surface.unlockCanvasAndPost(it) }
                }
            } catch (throwable: Throwable) {
                Logger.error(TAG, "An exception occurred while rendering the AWT frame.", throwable)
            } finally {
                rgbArrayBitmap.recycle()
                surface.release()
            }
        }
        super.execute(surface, screenSize, scope)
    }

    override fun onPause() {
    }

    override fun onResume() {
    }

    override fun onDestroy() {
    }

    override fun onGraphicOutput() {
    }

    override fun shouldIgnoreKeyEvent(event: KeyEvent): Boolean {
        return true
    }

    override fun sendMouseRight(isPressed: Boolean) {
        ZLBridge.sendMousePress(AWTInputEvent.BUTTON3_DOWN_MASK, isPressed)
    }

    @Composable
    override fun ComposableLayout(
        textInputMode: TextInputMode
    ) {
        JVMScreen(
            logState = logState,
            onLogStateChange = { logState = it },
            eventViewModel = eventViewModel
        )
    }
}