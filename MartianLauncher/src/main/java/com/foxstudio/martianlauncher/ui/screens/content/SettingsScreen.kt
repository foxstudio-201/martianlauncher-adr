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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.foxstudio.martianlauncher.R
import com.foxstudio.martianlauncher.ui.base.BaseScreen
import com.foxstudio.martianlauncher.ui.components.EdgeDirection
import com.foxstudio.martianlauncher.ui.components.fadeEdge
import com.foxstudio.martianlauncher.ui.screens.NestedNavKey
import com.foxstudio.martianlauncher.ui.screens.NormalNavKey
import com.foxstudio.martianlauncher.ui.screens.TitledNavKey
import com.foxstudio.martianlauncher.ui.screens.content.settings.AboutInfoScreen
import com.foxstudio.martianlauncher.ui.screens.content.settings.ControlManageScreen
import com.foxstudio.martianlauncher.ui.screens.content.settings.ControlSettingsScreen
import com.foxstudio.martianlauncher.ui.screens.content.settings.GameSettingsScreen
import com.foxstudio.martianlauncher.ui.screens.content.settings.GamepadSettingsScreen
import com.foxstudio.martianlauncher.ui.screens.content.settings.JavaManageScreen
import com.foxstudio.martianlauncher.ui.screens.content.settings.LauncherSettingsScreen
import com.foxstudio.martianlauncher.ui.screens.content.settings.RendererSettingsScreen
import com.foxstudio.martianlauncher.ui.screens.navigateOnce
import com.foxstudio.martianlauncher.ui.screens.onBack
import com.foxstudio.martianlauncher.ui.screens.rememberTransitionSpec
import com.foxstudio.martianlauncher.utils.animation.swapAnimateDpAsState
import com.foxstudio.martianlauncher.viewmodel.ErrorViewModel
import com.foxstudio.martianlauncher.viewmodel.EventViewModel
import com.foxstudio.martianlauncher.viewmodel.ScreenBackStackViewModel

@Composable
fun SettingsScreen(
    key: NestedNavKey.Settings,
    backStackViewModel: ScreenBackStackViewModel,
    openLicenseScreen: (raw: Int) -> Unit,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    BaseScreen(
        screenKey = key,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->

        Column(modifier = Modifier.fillMaxSize()) {
            TabMenu(
                modifier = Modifier.fillMaxWidth(),
                isVisible = isVisible,
                settingsScreenKey = backStackViewModel.settingsScreen.currentKey,
                navigateTo = { settingKey ->
                    key.backStack.navigateOnce(settingKey)
                }
            )
            NavigationUI(
                key = key,
                mainScreenKey = backStackViewModel.mainScreen.currentKey,
                settingsScreenKey = backStackViewModel.settingsScreen.currentKey,
                onCurrentKeyChange = { newKey ->
                    backStackViewModel.settingsScreen.currentKey = newKey
                },
                openLicenseScreen = openLicenseScreen,
                eventViewModel = eventViewModel,
                submitError = submitError,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

private data class SettingsTab(
    val key: TitledNavKey,
    @DrawableRes val icon: Int,
    @StringRes val textRes: Int,
    val division: Boolean = false
)

private val settingItems = listOf(
    SettingsTab(NormalNavKey.Settings.Renderer, R.drawable.ic_set_renderer, R.string.settings_tab_renderer),
    SettingsTab(NormalNavKey.Settings.Game, R.drawable.ic_set_game, R.string.settings_tab_game),
    SettingsTab(NormalNavKey.Settings.Control, R.drawable.ic_set_control, R.string.settings_tab_control),
    SettingsTab(NormalNavKey.Settings.Gamepad, R.drawable.ic_set_gamepad, R.string.settings_tab_gamepad),
    SettingsTab(NormalNavKey.Settings.Launcher, R.drawable.ic_set_launcher, R.string.settings_tab_launcher),
    SettingsTab(NormalNavKey.Settings.JavaManager, R.drawable.ic_set_java, R.string.settings_tab_java_manage, division = true),
    SettingsTab(NormalNavKey.Settings.ControlManager, R.drawable.ic_set_control_manage, R.string.settings_tab_control_manage),
    SettingsTab(NormalNavKey.Settings.AboutInfo, R.drawable.ic_set_about, R.string.settings_tab_info_about, division = true)
)

private val SettingsActiveColor = Color(0xFFF97316)

@Composable
private fun TabMenu(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    settingsScreenKey: TitledNavKey?,
    navigateTo: (TitledNavKey) -> Unit
) {
    val yOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible
    )

    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .fadeEdge(state = scrollState, direction = EdgeDirection.Horizontal)
            .horizontalScroll(scrollState)
            .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        settingItems.forEach { item ->
            if (item.division) {
                VerticalDivider(
                    modifier = Modifier
                        .height(28.dp)
                        .padding(horizontal = 4.dp)
                        .alpha(0.4f),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            SettingsTabItem(
                item = item,
                selected = settingsScreenKey == item.key,
                onClick = { navigateTo(item.key) }
            )
        }
    }
}

@Composable
private fun SettingsTabItem(
    item: SettingsTab,
    selected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (selected) SettingsActiveColor else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(item.icon),
            contentDescription = stringResource(item.textRes),
            tint = contentColor
        )
        Text(
            text = stringResource(item.textRes),
            maxLines = 1,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
    }
}

@Composable
private fun NavigationUI(
    key: NestedNavKey.Settings,
    mainScreenKey: TitledNavKey?,
    settingsScreenKey: TitledNavKey?,
    onCurrentKeyChange: (TitledNavKey?) -> Unit,
    openLicenseScreen: (raw: Int) -> Unit,
    eventViewModel: EventViewModel,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    val backStack = key.backStack
    val stackTopKey = backStack.lastOrNull()
    LaunchedEffect(stackTopKey) {
        onCurrentKeyChange(stackTopKey)
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
                entry<NormalNavKey.Settings.Renderer> {
                    RendererSettingsScreen(key, settingsScreenKey, mainScreenKey, eventViewModel)
                }
                entry<NormalNavKey.Settings.Game> {
                    GameSettingsScreen(key, settingsScreenKey, mainScreenKey, eventViewModel)
                }
                entry<NormalNavKey.Settings.Control> {
                    ControlSettingsScreen(key, settingsScreenKey, mainScreenKey, eventViewModel, submitError)
                }
                entry<NormalNavKey.Settings.Gamepad> {
                    GamepadSettingsScreen(key, settingsScreenKey, mainScreenKey)
                }
                entry<NormalNavKey.Settings.Launcher> {
                    LauncherSettingsScreen(
                        key = key,
                        settingsScreenKey = settingsScreenKey,
                        mainScreenKey = mainScreenKey,
                        eventViewModel = eventViewModel,
                        submitError = submitError,
                    )
                }
                entry<NormalNavKey.Settings.JavaManager> {
                    JavaManageScreen(key, settingsScreenKey, mainScreenKey, submitError)
                }
                entry<NormalNavKey.Settings.ControlManager> {
                    ControlManageScreen(key, settingsScreenKey, mainScreenKey, eventViewModel, submitError)
                }
                entry<NormalNavKey.Settings.AboutInfo> {
                    AboutInfoScreen(
                        key = key,
                        settingsScreenKey = settingsScreenKey,
                        mainScreenKey = mainScreenKey,
                        checkUpdate = {
                            eventViewModel.sendEvent(EventViewModel.Event.CheckUpdate)
                        },
                        openLicense = openLicenseScreen,
                        openLink = { url ->
                            eventViewModel.sendEvent(EventViewModel.Event.OpenLink(url))
                        }
                    )
                }
            }
        )
    } else {
        Box(modifier)
    }
}