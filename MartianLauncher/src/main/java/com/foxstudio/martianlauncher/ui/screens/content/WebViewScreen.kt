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

package com.foxstudio.martianlauncher.ui.screens.content

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.foxstudio.martianlauncher.game.account.microsoft.MicrosoftLoginSession
import com.foxstudio.martianlauncher.game.account.microsoft.MicrosoftOAuth
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation3.runtime.NavBackStack
import com.foxstudio.martianlauncher.ui.base.BaseScreen
import com.foxstudio.martianlauncher.ui.components.MarqueeText
import com.foxstudio.martianlauncher.ui.screens.NormalNavKey
import com.foxstudio.martianlauncher.ui.screens.TitledNavKey
import com.foxstudio.martianlauncher.ui.screens.navigateTo
import com.foxstudio.martianlauncher.utils.string.isNotEmptyOrBlank
import com.foxstudio.martianlauncher.viewmodel.EventViewModel
import com.foxstudio.martianlauncher.viewmodel.ScreenBackStackViewModel
import org.apache.commons.io.FileUtils

/**
 * 导航至WebViewScreen并访问特定网址
 */
fun NavBackStack<TitledNavKey>.navigateToWeb(webUrl: String) = this.navigateTo(
    screenKey = NormalNavKey.WebScreen(webUrl),
    useClassEquality = true
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
    key: NormalNavKey.WebScreen,
    backStackViewModel: ScreenBackStackViewModel,
    eventViewModel: EventViewModel
) {
    BaseScreen(
        screenKey = key,
        currentKey = backStackViewModel.mainScreen.currentKey,
        useClassEquality = true
    ) {
        var webUrl by remember {
            mutableStateOf(key.url)
        }

        val urlAvailable = remember(webUrl) {
            webUrl.isNotEmptyOrBlank() && webUrl != "about:blank"
        }

        val context = LocalContext.current
        var isWebLoading by rememberSaveable { mutableStateOf(true) }

        val webViewHolder = remember {
            mutableStateOf<WebView?>(null)
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.surface)
            ) {
                AnimatedVisibility(
                    visible = isWebLoading
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    )
                }

                //网址，可供用户复制
                AnimatedVisibility(
                    visible = webUrl.isNotEmptyOrBlank()
                ) {
                    MarqueeText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .clickable(enabled = urlAvailable) {
                                eventViewModel.sendEvent(EventViewModel.Event.OpenLink(webUrl))
                            },
                        text = webUrl,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        WebView(context).apply {
                            webViewClient = object : WebViewClient() {
                                /**
                                 * 拦截微软登录完成后的重定向地址，取出授权码（code）交给登录任务。
                                 * 仅当地址命中重定向地址时才处理，普通网页浏览不受影响。
                                 */
                                private fun handleOAuthRedirect(url: String?): Boolean {
                                    if (url == null || !MicrosoftOAuth.isRedirectUrl(url)) return false
                                    val error = MicrosoftOAuth.extractError(url)
                                    val code = MicrosoftOAuth.extractCode(url)
                                    when {
                                        error != null -> MicrosoftLoginSession.deliverError(Exception(error))
                                        code != null -> MicrosoftLoginSession.deliverCode(code)
                                        else -> return false
                                    }
                                    return true
                                }

                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    if (handleOAuthRedirect(request?.url?.toString())) return true
                                    return super.shouldOverrideUrlLoading(view, request)
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    webUrl = url ?: ""
                                    isWebLoading = false
                                }

                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    if (handleOAuthRedirect(url)) {
                                        view?.stopLoading()
                                        return
                                    }
                                    super.onPageStarted(view, url, favicon)
                                    webUrl = url ?: ""
                                    isWebLoading = true
                                }
                            }

                            settings.javaScriptEnabled = true
                            settings.cacheMode = WebSettings.LOAD_NO_CACHE
                            loadUrl(key.url)
                            webViewHolder.value = this
                        }
                    },
                    update = {
                        //不在此处重复加载 url
                    }
                )
            }

            DisposableEffect(Unit) {
                onDispose {
                    //若离开网页时登录尚未完成，则视为用户取消登录（已完成时为空操作）
                    MicrosoftLoginSession.cancel()
                    webViewHolder.value?.apply {
                        stopLoading()
                        loadUrl("about:blank")
                        clearHistory()
                        removeAllViews()
                        destroy()
                    }
                    webViewHolder.value = null

                    val webCache = context.getDir("webview", 0)
                    FileUtils.deleteQuietly(webCache)
                    CookieManager.getInstance().removeAllCookies(null)
                }
            }
        }
    }
}