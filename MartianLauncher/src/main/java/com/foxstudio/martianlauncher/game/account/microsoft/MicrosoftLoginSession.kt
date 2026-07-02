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

package com.foxstudio.martianlauncher.game.account.microsoft

import kotlinx.coroutines.CompletableDeferred

/**
 * 在网页登录界面（WebView）与后台登录任务之间传递微软授权码（code）。
 *
 * 登录任务调用 [start] 开启一次会话并 await 返回的 Deferred；网页拦截到重定向地址后，
 * 通过 [deliverCode] / [deliverError] 交付结果；若用户中途关闭网页，则由界面调用 [cancel]。
 */
object MicrosoftLoginSession {
    @Volatile
    private var deferred: CompletableDeferred<String?>? = null

    /** 开始一次登录会话，返回用于等待授权码的 Deferred（null 表示用户取消） */
    fun start(): CompletableDeferred<String?> =
        CompletableDeferred<String?>().also { deferred = it }

    /** 网页重定向成功，交付授权码 */
    fun deliverCode(code: String) {
        deferred?.complete(code)
        deferred = null
    }

    /** 网页登录返回了错误 */
    fun deliverError(error: Throwable) {
        deferred?.completeExceptionally(error)
        deferred = null
    }

    /** 用户取消（关闭登录网页） */
    fun cancel() {
        deferred?.complete(null)
        deferred = null
    }
}
