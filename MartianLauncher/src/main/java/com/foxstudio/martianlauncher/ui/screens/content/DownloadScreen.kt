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

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.foxstudio.martianlauncher.R
import com.foxstudio.martianlauncher.game.download.assets.platform.PlatformClasses
import com.foxstudio.martianlauncher.ui.base.BaseScreen
import com.foxstudio.martianlauncher.ui.screens.NestedNavKey
import com.foxstudio.martianlauncher.ui.screens.NormalNavKey
import com.foxstudio.martianlauncher.ui.screens.TitledNavKey
import com.foxstudio.martianlauncher.ui.screens.content.download.DownloadModPackScreen
import com.foxstudio.martianlauncher.ui.screens.content.download.DownloadModScreen
import com.foxstudio.martianlauncher.ui.screens.content.download.DownloadResourcePackScreen
import com.foxstudio.martianlauncher.ui.screens.content.download.DownloadSavesScreen
import com.foxstudio.martianlauncher.ui.screens.content.download.DownloadShadersScreen
import com.foxstudio.martianlauncher.ui.screens.content.download.assets.search.SearchIdScreen
import com.foxstudio.martianlauncher.ui.screens.content.elements.CategoryIconColored
import com.foxstudio.martianlauncher.ui.screens.content.elements.CategoryItem
import com.foxstudio.martianlauncher.ui.screens.navigateOnce
import com.foxstudio.martianlauncher.ui.screens.onBack
import com.foxstudio.martianlauncher.ui.screens.rememberTransitionSpec
import com.foxstudio.martianlauncher.utils.animation.swapAnimateDpAsState
import com.foxstudio.martianlauncher.viewmodel.ErrorViewModel
import com.foxstudio.martianlauncher.viewmodel.EventViewModel
import com.foxstudio.martianlauncher.viewmodel.ModpackImportViewModel
import com.foxstudio.martianlauncher.viewmodel.ScreenBackStackViewModel

/**
 * 导航至DownloadScreen
 */
fun ScreenBackStackViewModel.navigateToDownload(targetScreen: TitledNavKey? = null) {
    downloadScreen.clearWith(targetScreen ?: downloadModPackScreen)
    mainScreen.removeAndNavigateTo(
        removes = clearBeforeNavKeys,
        screenKey = downloadScreen,
        useClassEquality = true
    )
}

fun ScreenBackStackViewModel.navigateToProfile() {
    mainScreen.removeAndNavigateTo(
        removes = clearBeforeNavKeys,
        screenKey = downloadGameScreen,
        useClassEquality = true
    )
}

@Composable
fun DownloadScreen(
    key: NestedNavKey.Download,
    backScreenViewModel: ScreenBackStackViewModel,
    modpackImportViewModel: ModpackImportViewModel,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    BaseScreen(
        screenKey = key,
        currentKey = backScreenViewModel.mainScreen.currentKey,
        useClassEquality = true
    ) { isVisible: Boolean ->
        val platformBar = remember { mutableStateOf<DownloadPlatformBarState?>(null) }

        LaunchedEffect(backScreenViewModel.downloadScreen.currentKey) {
            platformBar.value = null
        }

        Column(modifier = Modifier.fillMaxSize()) {
            PlatformTabBar(
                modifier = Modifier.fillMaxWidth(),
                state = platformBar.value
            )

            CategoryPills(
                modifier = Modifier.fillMaxWidth(),
                isVisible = isVisible,
                backStack = key.backStack,
                backScreenViewModel = backScreenViewModel,
            )

            CompositionLocalProvider(LocalDownloadPlatformBar provides platformBar) {
                NavigationUI(
                    key = key,
                    backScreenViewModel = backScreenViewModel,
                    eventViewModel = eventViewModel,
                    modpackImportViewModel = modpackImportViewModel,
                    submitError = submitError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CategoryPills(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    backStack: NavBackStack<TitledNavKey>,
    backScreenViewModel: ScreenBackStackViewModel,
) {
    val downloadsList = listOf(
        CategoryItem(backScreenViewModel.downloadModPackScreen, { CategoryIconColored(R.drawable.ic_cat_modpack, R.string.download_category_modpack) }, R.string.download_category_modpack),
        CategoryItem(backScreenViewModel.downloadModScreen, { CategoryIconColored(R.drawable.ic_cat_mod, R.string.download_category_mod) }, R.string.download_category_mod),
        CategoryItem(backScreenViewModel.downloadResourcePackScreen, { CategoryIconColored(R.drawable.ic_cat_resourcepack, R.string.download_category_resource_pack) }, R.string.download_category_resource_pack),
        CategoryItem(backScreenViewModel.downloadSavesScreen, { CategoryIconColored(R.drawable.ic_cat_saves, R.string.download_category_saves) }, R.string.download_category_saves),
        CategoryItem(backScreenViewModel.downloadShadersScreen, { CategoryIconColored(R.drawable.ic_cat_shaders, R.string.download_category_shaders) }, R.string.download_category_shaders),
        CategoryItem(NormalNavKey.SearchId, { CategoryIconColored(R.drawable.ic_cat_searchid, R.string.download_category_by_id) }, R.string.download_category_by_id),
    )

    val yOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible
    )

    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        downloadsList.forEach { item ->
            val selected = backScreenViewModel.downloadScreen.currentKey == item.key
            FilterChip(
                selected = selected,
                onClick = {
                    backStack.navigateOnce(item.key)
                },
                leadingIcon = { item.icon() },
                label = {
                    Text(
                        text = stringResource(item.textRes),
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
        }
    }
}

@Composable
private fun NavigationUI(
    key: NestedNavKey.Download,
    backScreenViewModel: ScreenBackStackViewModel,
    eventViewModel: EventViewModel,
    modpackImportViewModel: ModpackImportViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    val backStack = key.backStack
    val stackTopKey = backStack.lastOrNull()
    LaunchedEffect(stackTopKey) {
        backScreenViewModel.downloadScreen.currentKey = stackTopKey
    }

    if (backStack.isNotEmpty()) {
        NavDisplay(
            backStack = backStack,
            modifier = modifier,
            onBack = {
                onBack(backStack)
            },
            transitionSpec = rememberTransitionSpec(),
            popTransitionSpec = rememberTransitionSpec(),
            entryProvider = entryProvider {
                entry<NestedNavKey.DownloadModPack> { key ->
                    DownloadModPackScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadModPackScreenKey = backScreenViewModel.downloadModPackScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadModPackScreen.currentKey = newKey
                        },
                        eventViewModel = eventViewModel,
                        importerViewModel = modpackImportViewModel
                    )
                }
                entry<NestedNavKey.DownloadMod> { key ->
                    DownloadModScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadModScreenKey = backScreenViewModel.downloadModScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadModScreen.currentKey = newKey
                        },
                        submitError = submitError,
                        eventViewModel = eventViewModel
                    )
                }
                entry<NestedNavKey.DownloadResourcePack> { key ->
                    DownloadResourcePackScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadResourcePackScreenKey = backScreenViewModel.downloadResourcePackScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadResourcePackScreen.currentKey = newKey
                        },
                        submitError = submitError,
                        eventViewModel = eventViewModel
                    )
                }
                entry<NestedNavKey.DownloadSaves> { key ->
                    DownloadSavesScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadSavesScreenKey = backScreenViewModel.downloadSavesScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadSavesScreen.currentKey = newKey
                        },
                        submitError = submitError,
                        eventViewModel = eventViewModel
                    )
                }
                entry<NestedNavKey.DownloadShaders> { key ->
                    DownloadShadersScreen(
                        key = key,
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        downloadShadersScreenKey = backScreenViewModel.downloadShadersScreen.currentKey,
                        onCurrentKeyChange = { newKey ->
                            backScreenViewModel.downloadShadersScreen.currentKey = newKey
                        },
                        submitError = submitError,
                        eventViewModel = eventViewModel
                    )
                }
                entry<NormalNavKey.SearchId> {
                    SearchIdScreen(
                        mainScreenKey = backScreenViewModel.mainScreen.currentKey,
                        downloadScreenKey = backScreenViewModel.downloadScreen.currentKey,
                        swapToDownload = { platform, classes, projectId, iconUrl ->
                            val backStack = when (classes) {
                                PlatformClasses.MOD -> backScreenViewModel.downloadModScreen
                                PlatformClasses.MOD_PACK -> backScreenViewModel.downloadModPackScreen
                                PlatformClasses.RESOURCE_PACK -> backScreenViewModel.downloadResourcePackScreen
                                PlatformClasses.SAVES -> backScreenViewModel.downloadSavesScreen
                                PlatformClasses.SHADERS -> backScreenViewModel.downloadShadersScreen
                            }
                            backScreenViewModel.navigateToDownload(
                                targetScreen = backStack.apply {
                                    navigateTo(
                                        NormalNavKey.DownloadAssets(
                                            platform = platform,
                                            projectId = projectId,
                                            classes = PlatformClasses.MOD,
                                            iconUrl = iconUrl
                                        )
                                    )
                                }
                            )
                        },
                        openLink = { link ->
                            eventViewModel.sendEvent(EventViewModel.Event.OpenLink(link))
                        }
                    )
                }
            }
        )
    } else {
        Box(modifier)
    }
}