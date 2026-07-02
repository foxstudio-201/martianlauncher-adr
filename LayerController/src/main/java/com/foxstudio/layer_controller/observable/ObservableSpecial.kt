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

package com.foxstudio.layer_controller.observable

import com.foxstudio.layer_controller.layout.ControlLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ObservableSpecial(
    private val special: ControlLayout.Special
): Packable<ControlLayout.Special> {
    private val _joystickStyle = MutableStateFlow(
        special.joystickStyle?.let { style ->
            ObservableJoystickStyle(style)
        }
    )
    val joystickStyle = _joystickStyle.asStateFlow()

    /**
     * 设置摇杆独立样式
     */
    fun setJoystickStyle(joystickStyle: ObservableJoystickStyle?) {
        this._joystickStyle.update { joystickStyle }
    }

    override fun pack(): ControlLayout.Special {
        return ControlLayout.Special(
            joystickStyle = _joystickStyle.value?.pack()
        )
    }

    override fun isModified(): Boolean {
        return this.special.isModified(pack())
    }
}