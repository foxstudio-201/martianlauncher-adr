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

import com.foxstudio.martianlauncher.BuildKeys
import com.foxstudio.martianlauncher.game.account.Account
import com.foxstudio.martianlauncher.game.account.AccountType
import com.foxstudio.martianlauncher.game.account.AccountsManager
import com.foxstudio.martianlauncher.game.account.microsoft.MinecraftProfileException.ExceptionStatus.BLOCKED_IP
import com.foxstudio.martianlauncher.game.account.microsoft.MinecraftProfileException.ExceptionStatus.FREQUENT
import com.foxstudio.martianlauncher.game.account.microsoft.XboxLoginException.ExceptionStatus.BANNED
import com.foxstudio.martianlauncher.game.account.microsoft.XboxLoginException.ExceptionStatus.BLOCKED_REGION
import com.foxstudio.martianlauncher.game.account.microsoft.XboxLoginException.ExceptionStatus.NOT_ACCEPTED_SERVICE
import com.foxstudio.martianlauncher.game.account.microsoft.XboxLoginException.ExceptionStatus.REACHED_PLAYTIME_LIMIT
import com.foxstudio.martianlauncher.game.account.microsoft.XboxLoginException.ExceptionStatus.REQUIRES_PROOF_OF_AGE
import com.foxstudio.martianlauncher.game.account.microsoft.XboxLoginException.ExceptionStatus.RESTRICTED
import com.foxstudio.martianlauncher.game.account.microsoft.XboxLoginException.ExceptionStatus.UNDERAGE
import com.foxstudio.martianlauncher.game.account.microsoft.XboxLoginException.ExceptionStatus.UNREGISTERED
import com.foxstudio.martianlauncher.game.account.microsoft.models.DeviceCodeResponse
import com.foxstudio.martianlauncher.game.account.microsoft.models.MinecraftAuthResponse
import com.foxstudio.martianlauncher.game.account.microsoft.models.TokenResponse
import com.foxstudio.martianlauncher.game.account.microsoft.models.XBLProperties
import com.foxstudio.martianlauncher.game.account.microsoft.models.XBLRequest
import com.foxstudio.martianlauncher.game.account.microsoft.models.XSTSAuthResult
import com.foxstudio.martianlauncher.game.account.microsoft.models.XSTSProperties
import com.foxstudio.martianlauncher.game.account.microsoft.models.XSTSRequest
import com.foxstudio.martianlauncher.game.account.wardrobe.SkinModelType
import com.foxstudio.martianlauncher.game.account.yggdrasil.findUsing
import com.foxstudio.martianlauncher.game.account.yggdrasil.getPlayerProfile
import com.foxstudio.martianlauncher.game.account.yggdrasil.getSkinModel
import com.foxstudio.martianlauncher.path.GLOBAL_CLIENT
import com.foxstudio.martianlauncher.utils.logging.Logger
import com.foxstudio.martianlauncher.utils.network.httpPostJson
import com.foxstudio.martianlauncher.utils.network.safeBodyAsJson
import com.foxstudio.martianlauncher.utils.network.submitForm
import com.foxstudio.martianlauncher.utils.string.toUuidStr
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "MicrosoftAuth"

private val SCOPES = listOf("XboxLive.signin", "offline_access", "openid", "profile", "email")
private const val TENANT = "/consumers"

const val MICROSOFT_AUTH_URL = "https://login.microsoftonline.com"
const val LIVE_AUTH_URL = "https://login.live.com"
const val XBL_AUTH_URL = "https://user.auth.xboxlive.com"
const val XSTS_AUTH_URL = "https://xsts.auth.xboxlive.com"
const val MINECRAFT_SERVICES_URL = "https://api.minecraftservices.com"

/**
 * 使用不同的身份验证类型异步验证用户，并检索其 Minecraft 帐户信息
 * 函数通过执行一系列步骤来编排身份验证过程，具体取决于提供的 [authType]。
 *
 * 支持刷新现有访问令牌或使用提供的访问令牌。然后，继续使用 Xbox Live （XBL）、Xbox 安全令牌服务 （XSTS） 进行身份验证，最后访问 Minecraft。
 *
 * 支持验证用户是否拥有游戏，然后创建 [Account] 对象。
 *
 * @param statusUpdate 验证执行到哪个步骤，通过这个进行回调更新
 */
suspend fun microsoftAuthAsync(
    authType: AuthType,
    refreshToken: String,
    accessToken: String = "NULL",
    context: CoroutineContext,
    statusUpdate: (AsyncStatus) -> Unit,
): Account = coroutineScope {
    val (finalAccessToken, newRefreshToken) = when (authType) {
        AuthType.Refresh -> refreshAccessToken(refreshToken, statusUpdate, context)
        else -> Pair(accessToken, refreshToken)
    }

    val xblToken = authenticateXBL(finalAccessToken, statusUpdate)
    val xstsToken = authenticateXSTS(xblToken.first, xblToken.second, statusUpdate, context)
    val authResponse = authenticateMinecraft(xstsToken, statusUpdate, context)
    verifyGameOwnership(authResponse.accessToken, statusUpdate)

    return@coroutineScope createAccount(authResponse, newRefreshToken, xblToken.second, statusUpdate)
}

private suspend fun refreshAccessToken(
    refreshToken: String,
    update: (AsyncStatus) -> Unit,
    context: CoroutineContext
): Pair<String, String> {
    update(AsyncStatus.GETTING_ACCESS_TOKEN)

    return withRetry {
        val response = submitForm<JsonObject>(
            url = "$LIVE_AUTH_URL/oauth20_token.srf",
            parameters = Parameters.build {
                //刷新令牌必须使用与登录时相同的客户端 ID（见 MicrosoftOAuth）
                append("client_id", MicrosoftOAuth.CLIENT_ID)
                append("refresh_token", refreshToken)
                append("grant_type", "refresh_token")
                append("scope", MicrosoftOAuth.SCOPE)
            },
            context = context
        )
        Pair(
            response["access_token"].text(),
            response["refresh_token"]?.jsonPrimitive?.content ?: refreshToken
        )
    }
}

private suspend fun authenticateXBL(accessToken: String, update: (AsyncStatus) -> Unit): Pair<String, String> {
    update(AsyncStatus.GETTING_XBL_TOKEN)
    val requestBody = XBLRequest(
        properties = XBLProperties(
            authMethod = "RPS",
            siteName = "user.auth.xboxlive.com",
            rpsTicket = "d=$accessToken"
        ),
        relyingParty = "http://auth.xboxlive.com",
        tokenType = "JWT"
    )

    return withRetry {
        val response = GLOBAL_CLIENT.post("$XBL_AUTH_URL/user/authenticate") {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }.safeBodyAsJson<JsonObject>()

        //提取uhs
        val uhs = response["DisplayClaims"]?.jsonObject
            ?.get("xui")?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.get("uhs")?.jsonPrimitive
            ?.content ?: throw Exception("Missing uhs in XBL response")

        Pair(response["Token"].text(), uhs)
    }
}

private suspend fun authenticateXSTS(
    xblToken: String,
    uhs: String,
    update: (AsyncStatus) -> Unit,
    context: CoroutineContext
): XSTSAuthResult {
    update(AsyncStatus.GETTING_XSTS_TOKEN)

    return withRetry {
        val response = httpPostJson<JsonObject>(
            url = "$XSTS_AUTH_URL/xsts/authorize",
            body = XSTSRequest(
                properties = XSTSProperties(
                    sandboxId = "RETAIL",
                    userTokens = listOf(xblToken)
                ),
                relyingParty = "rp://api.minecraftservices.com/",
                tokenType = "JWT"
            ),
            context = context
        )

        when (response["XErr"].text()) {
            //Reference : https://github.com/PrismarineJS/prismarine-auth/blob/1aef6e1/src/common/Constants.js#L50-L59
            "2148916227" -> throw XboxLoginException(BANNED)
            "2148916229" -> throw XboxLoginException(RESTRICTED)
            "2148916233" -> throw XboxLoginException(UNREGISTERED)
            "2148916234" -> throw XboxLoginException(NOT_ACCEPTED_SERVICE)
            "2148916235" -> throw XboxLoginException(BLOCKED_REGION)
            "2148916236" -> throw XboxLoginException(REQUIRES_PROOF_OF_AGE)
            "2148916237" -> throw XboxLoginException(REACHED_PLAYTIME_LIMIT)
            "2148916238" -> throw XboxLoginException(UNDERAGE)
        }

        XSTSAuthResult(token = response["Token"].text(), uhs = uhs)
    }
}

private suspend fun authenticateMinecraft(
    xstsResult: XSTSAuthResult,
    update: (AsyncStatus) -> Unit,
    context: CoroutineContext
): MinecraftAuthResponse {
    update(AsyncStatus.AUTHENTICATE_MINECRAFT)

    return withRetry {
        runCatching {
            httpPostJson<MinecraftAuthResponse>(
                url = "$MINECRAFT_SERVICES_URL/authentication/login_with_xbox",
                body = mapOf("identityToken" to "XBL3.0 x=${xstsResult.uhs};${xstsResult.token}"),
                context = context
            )
        }.onFailure { e ->
            if (e is ResponseException) {
                when (e.response.status.value) {
                    429 -> throw MinecraftProfileException(FREQUENT)
                    403 -> throw MinecraftProfileException(BLOCKED_IP)
                }
            }
        }.getOrThrow()
    }
}

private suspend fun verifyGameOwnership(accessToken: String, update: (AsyncStatus) -> Unit) {
    update(AsyncStatus.VERIFY_GAME_OWNERSHIP)
    withRetry {
        val response = GLOBAL_CLIENT.get("$MINECRAFT_SERVICES_URL/entitlements/mcstore") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        if (response.safeBodyAsJson<JsonObject>()["items"]?.jsonArray?.isEmpty() != false) {
            throw NotPurchasedMinecraftException()
        }
    }
}

private suspend fun createAccount(
    authResponse: MinecraftAuthResponse,
    refreshToken: String,
    uhs: String,
    statusUpdate: (AsyncStatus) -> Unit
): Account {
    statusUpdate(AsyncStatus.GETTING_PLAYER_PROFILE)

    val profile = getPlayerProfile(
        apiUrl = MINECRAFT_SERVICES_URL,
        accessToken = authResponse.accessToken
    )

    val profileId = profile.id
    //避免同一个账号反复添加
    val account = AccountsManager.loadFromProfileID(profileId, AccountType.MICROSOFT.tag) ?: Account()

    return account.apply {
        this.username = profile.name
        this.accessToken = authResponse.accessToken
        this.expiresAt = System.currentTimeMillis() + authResponse.expiresIn * 1000
        this.accountType = AccountType.MICROSOFT.tag
        this.clientToken = BuildKeys.LAUNCHER_NAME.toUuidStr().replace("-", "")
        this.profileId = profileId
        this.refreshToken = refreshToken.ifEmpty { "None" }
        this.xUid = uhs
        this.skinModelType = profile.skins.findUsing()?.getSkinModel() ?: SkinModelType.NONE
    }
}

private fun JsonElement?.text() = this?.jsonPrimitive?.content.orEmpty()

private suspend fun <T> withRetry(
    maxRetries: Int = 3,
    initialDelay: Long = 1000,
    maxDelay: Long = 10_000,
    block: suspend () -> T
): T = com.foxstudio.martianlauncher.utils.network.withRetry(
    "MicrosoftAuthenticator", maxRetries, initialDelay, maxDelay, block
)