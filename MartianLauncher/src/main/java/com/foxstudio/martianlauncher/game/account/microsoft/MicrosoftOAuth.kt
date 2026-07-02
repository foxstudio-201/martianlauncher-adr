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

import android.net.Uri
import com.foxstudio.martianlauncher.game.account.microsoft.models.TokenResponse
import com.foxstudio.martianlauncher.utils.network.submitForm
import io.ktor.http.Parameters
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlin.coroutines.CoroutineContext

/**
 * Microsoft OAuth2 授权码登录流程（参考 VoxelXClient 的网页登录方式）。
 *
 * 直接在内置 WebView 中打开微软登录页面，用户完成登录后，微软会重定向到
 * [REDIRECT_URI] 并在地址中附带授权码（code）。我们拦截该地址、取出 code，
 * 再用 code 换取访问令牌与刷新令牌。
 *
 * 相比设备代码（device code）流程，这种方式不需要让用户手动复制、输入验证码，
 * 登录体验与桌面端启动器一致。
 */
object MicrosoftOAuth {
    /** 旧版 MSA 公共客户端 ID，无需 Azure 应用注册即可用于 Xbox/Minecraft 登录 */
    const val CLIENT_ID = "00000000402b5328"
    const val REDIRECT_URI = "https://login.live.com/oauth20_desktop.srf"
    const val SCOPE = "XboxLive.signin offline_access"

    private const val AUTH_URL = "https://login.live.com/oauth20_authorize.srf"
    private const val TOKEN_URL = "https://login.live.com/oauth20_token.srf"

    /** 构造微软登录页面地址，prompt=select_account 让用户每次都能选择账号 */
    fun authorizeUrl(): String =
        "$AUTH_URL?client_id=$CLIENT_ID" +
            "&response_type=code" +
            "&redirect_uri=${Uri.encode(REDIRECT_URI)}" +
            "&scope=${Uri.encode(SCOPE)}" +
            "&prompt=select_account"

    /** 判断某个地址是否为登录完成后的重定向地址 */
    fun isRedirectUrl(url: String): Boolean = url.startsWith(REDIRECT_URI)

    /** 从重定向地址中取出授权码 */
    fun extractCode(url: String): String? =
        runCatching { Uri.parse(url).getQueryParameter("code") }.getOrNull()

    /** 从重定向地址中取出错误信息（若登录失败或被拒绝） */
    fun extractError(url: String): String? = runCatching {
        val uri = Uri.parse(url)
        uri.getQueryParameter("error_description") ?: uri.getQueryParameter("error")
    }.getOrNull()

    /** 用授权码换取访问令牌与刷新令牌 */
    suspend fun exchangeAuthCode(code: String, context: CoroutineContext): TokenResponse {
        val response: JsonObject = submitForm(
            url = TOKEN_URL,
            parameters = Parameters.build {
                append("client_id", CLIENT_ID)
                append("code", code)
                append("grant_type", "authorization_code")
                append("redirect_uri", REDIRECT_URI)
                append("scope", SCOPE)
            },
            context = context
        )
        return TokenResponse(
            expiresIn = response["expires_in"]?.jsonPrimitive?.int ?: 0,
            accessToken = response["access_token"]?.jsonPrimitive?.content.orEmpty(),
            refreshToken = response["refresh_token"]?.jsonPrimitive?.content.orEmpty()
        )
    }
}
