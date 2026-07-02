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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.foxstudio.martianlauncher.R
import com.foxstudio.martianlauncher.game.account.AccountsManager
import com.foxstudio.martianlauncher.game.version.installed.Version
import com.foxstudio.martianlauncher.game.version.installed.VersionsManager
import com.foxstudio.martianlauncher.ui.base.BaseScreen
import com.foxstudio.martianlauncher.ui.components.BackgroundCard
import com.foxstudio.martianlauncher.ui.components.MarqueeText
import com.foxstudio.martianlauncher.ui.components.ScalingActionButton
import com.foxstudio.martianlauncher.ui.screens.NestedNavKey
import com.foxstudio.martianlauncher.ui.screens.NormalNavKey
import com.foxstudio.martianlauncher.ui.screens.TitledNavKey
import com.foxstudio.martianlauncher.ui.screens.content.elements.AccountAvatar
import com.foxstudio.martianlauncher.ui.screens.content.elements.PlayerFace
import com.foxstudio.martianlauncher.ui.screens.content.elements.CommonVersionInfoLayout
import com.foxstudio.martianlauncher.ui.screens.content.elements.VersionIconImage
import com.foxstudio.martianlauncher.ui.screens.content.elements.versionBackgroundRes
import androidx.compose.material3.Surface
import com.foxstudio.martianlauncher.ui.screens.content.versions.ModsManagerScreen
import com.foxstudio.martianlauncher.ui.screens.content.versions.SavesManagerScreen
import com.foxstudio.martianlauncher.ui.screens.content.versions.ResourcePackManageScreen
import com.foxstudio.martianlauncher.ui.screens.content.versions.ShadersManagerScreen
import com.foxstudio.martianlauncher.ui.screens.content.versions.ScreenshotsManagerScreen
import com.foxstudio.martianlauncher.ui.screens.content.versions.ServerListScreen
import com.foxstudio.martianlauncher.utils.animation.swapAnimateDpAsState
import com.foxstudio.martianlauncher.viewmodel.ScreenBackStackViewModel

@Composable
fun LauncherScreen(
    backStackViewModel: ScreenBackStackViewModel,
    navigateToVersions: (Version) -> Unit,
    onLaunchGame: (Version?) -> Unit,
    onOpenLink: (String) -> Unit,
    onQuickPlay: (Version, String) -> Unit,
    swapToDownload: () -> Unit,
    onSwapMoreInfo: (String, com.foxstudio.martianlauncher.game.download.assets.platform.Platform) -> Unit,
    eventViewModel: com.foxstudio.martianlauncher.viewmodel.EventViewModel,
    submitError: (com.foxstudio.martianlauncher.viewmodel.ErrorViewModel.ThrowableMessage) -> Unit,
) {
    BaseScreen(
        screenKey = NormalNavKey.LauncherMain,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->
        val toAccountManageScreen: () -> Unit = {
            backStackViewModel.mainScreen.navigateTo(
                screenKey = NormalNavKey.AccountManager(FirstLoginMenu.NONE)
            )
        }
        val toVersionManageScreen: () -> Unit = {
            backStackViewModel.mainScreen.removeAndNavigateTo(
                remove = NestedNavKey.VersionSettings::class,
                screenKey = NormalNavKey.VersionsManager
            )
        }
        val toVersionSettingsScreen: () -> Unit = {
            VersionsManager.currentVersion.value?.let { version ->
                navigateToVersions(version)
            }
        }
        val toMainScreen: () -> Unit = {
            backStackViewModel.mainScreen.clearWith(NormalNavKey.LauncherMain)
        }

        CompositionLocalProvider(
            LocalUriHandler provides object : UriHandler {
                override fun openUri(uri: String) {
                    onOpenLink(uri)
                }
            }
        ) {
            HomePageLayout(
                isVisible = isVisible,
                onLaunchGame = onLaunchGame,
                toAccountManageScreen = toAccountManageScreen,
                toVersionManageScreen = toVersionManageScreen,
                toVersionSettingsScreen = toVersionSettingsScreen,
                toMainScreen = toMainScreen,
                onQuickPlay = onQuickPlay,
                swapToDownload = swapToDownload,
                onSwapMoreInfo = onSwapMoreInfo,
                eventViewModel = eventViewModel,
                submitError = submitError,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HomePageLayout(
    isVisible: Boolean,
    onLaunchGame: (Version?) -> Unit,
    toAccountManageScreen: () -> Unit,
    toVersionManageScreen: () -> Unit,
    toVersionSettingsScreen: () -> Unit,
    toMainScreen: () -> Unit,
    onQuickPlay: (Version, String) -> Unit,
    swapToDownload: () -> Unit,
    onSwapMoreInfo: (String, com.foxstudio.martianlauncher.game.download.assets.platform.Platform) -> Unit,
    eventViewModel: com.foxstudio.martianlauncher.viewmodel.EventViewModel,
    submitError: (com.foxstudio.martianlauncher.viewmodel.ErrorViewModel.ThrowableMessage) -> Unit,
) {
    val version by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    val hasVersion = version?.isValid() == true
    var activeTab by remember { mutableStateOf<NormalNavKey.Versions>(NormalNavKey.Versions.ModsManager) }

    var panelExpanded by remember { mutableStateOf(false) }
    var dragAccumulator by remember { mutableStateOf(0f) }
    val DRAG_THRESHOLD = 50f

    val TAB_BAR_HEIGHT = 64.dp
    val density = LocalDensity.current

    // Đo height HeroCard
    var heroHeightDp by remember { mutableStateOf(256.dp) }
    var heroMeasured by remember { mutableStateOf(false) }

    // HeroCard shrink + fade khi panel mở
    val heroAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (panelExpanded) 0f else 1f,
        animationSpec = androidx.compose.animation.core.tween(280),
        label = "heroAlpha"
    )
    val animatedHeroHeightDp by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (panelExpanded) 0.dp else heroHeightDp,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
        ),
        label = "heroHeight"
    )

    // Sheet offset: 0 = hiện full content, sheetContentHeightPx = ẩn (chỉ thấy tab bar)
    var sheetContentHeightPx by remember { mutableStateOf(0) }
    val sheetOffsetPx by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (panelExpanded) 0f else sheetContentHeightPx.toFloat(),
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
        ),
        label = "sheetOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = { dragAccumulator = 0f },
                    onDragCancel = { dragAccumulator = 0f },
                    onVerticalDrag = { _, dragAmount ->
                        dragAccumulator += dragAmount
                        if (dragAccumulator < -DRAG_THRESHOLD && !panelExpanded) {
                            panelExpanded = true
                            dragAccumulator = 0f
                        } else if (dragAccumulator > DRAG_THRESHOLD && panelExpanded) {
                            panelExpanded = false
                            dragAccumulator = 0f
                        }
                    }
                )
            }
    ) {
        // HeroCard — shrink + fade khi panel mở
        Column(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
                if (!heroMeasured) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .alpha(0f)
                            .onGloballyPositioned {
                                val h = with(density) { it.size.height.toDp() }
                                if (h > 0.dp) { heroHeightDp = h; heroMeasured = true }
                            }
                    ) {
                        HomeHeroCard(
                            isVisible = isVisible,
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 12.dp, end = 12.dp, bottom = TAB_BAR_HEIGHT + 8.dp),
                            onLaunchGame = onLaunchGame,
                            toAccountManageScreen = toAccountManageScreen,
                            toVersionManageScreen = toVersionManageScreen,
                            toVersionSettingsScreen = toVersionSettingsScreen,
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(animatedHeroHeightDp)
                            .alpha(heroAlpha)
                            .clipToBounds()
                    ) {
                        HomeHeroCard(
                            isVisible = isVisible,
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 12.dp, end = 12.dp, bottom = TAB_BAR_HEIGHT + 8.dp),
                            onLaunchGame = onLaunchGame,
                            toAccountManageScreen = toAccountManageScreen,
                            toVersionManageScreen = toVersionManageScreen,
                            toVersionSettingsScreen = toVersionSettingsScreen,
                        )
                    }
                }
            }
        }

        // Sheet trượt lên từ dưới
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .align(Alignment.BottomCenter)
                .offset { IntOffset(0, sheetOffsetPx.toInt()) }
                .onGloballyPositioned {
                    val total = it.size.height
                    val tabPx = with(density) { TAB_BAR_HEIGHT.roundToPx() }
                    sheetContentHeightPx = (total - tabPx).coerceAtLeast(0)
                }
        ) {
            HomeTabBar(
                activeTab = activeTab,
                hasVersion = hasVersion,
                onTabSelected = { tab ->
                    activeTab = tab
                    panelExpanded = true
                },
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val currentVersion = version
                if (currentVersion != null && hasVersion) {
                    val fakeMainKey = remember(currentVersion) {
                        NestedNavKey.VersionSettings(currentVersion)
                    }
                    when (activeTab) {
                        NormalNavKey.Versions.ModsManager -> ModsManagerScreen(
                            mainScreenKey = fakeMainKey,
                            versionsScreenKey = NormalNavKey.Versions.ModsManager,
                            version = currentVersion,
                            backToMainScreen = toMainScreen,
                            swapToDownload = swapToDownload,
                            onSwapMoreInfo = onSwapMoreInfo,
                            eventViewModel = eventViewModel,
                            submitError = submitError,
                        )
                        NormalNavKey.Versions.SavesManager -> SavesManagerScreen(
                            mainScreenKey = fakeMainKey,
                            versionsScreenKey = NormalNavKey.Versions.SavesManager,
                            version = currentVersion,
                            onQuickPlay = onQuickPlay,
                            backToMainScreen = toMainScreen,
                            swapToDownload = swapToDownload,
                            submitError = submitError,
                        )
                        NormalNavKey.Versions.ResourcePackManager -> ResourcePackManageScreen(
                            mainScreenKey = fakeMainKey,
                            versionsScreenKey = NormalNavKey.Versions.ResourcePackManager,
                            version = currentVersion,
                            backToMainScreen = toMainScreen,
                            swapToDownload = swapToDownload,
                            submitError = submitError,
                        )
                        NormalNavKey.Versions.ShadersManager -> ShadersManagerScreen(
                            mainScreenKey = fakeMainKey,
                            versionsScreenKey = NormalNavKey.Versions.ShadersManager,
                            version = currentVersion,
                            backToMainScreen = toMainScreen,
                            swapToDownload = swapToDownload,
                            submitError = submitError,
                        )
                        NormalNavKey.Versions.ScreenshotsManager -> ScreenshotsManagerScreen(
                            mainScreenKey = fakeMainKey,
                            versionsScreenKey = NormalNavKey.Versions.ScreenshotsManager,
                            version = currentVersion,
                            backToMainScreen = toMainScreen,
                            submitError = submitError,
                        )
                        NormalNavKey.Versions.ServerList -> ServerListScreen(
                            mainScreenKey = fakeMainKey,
                            versionsScreenKey = NormalNavKey.Versions.ServerList,
                            version = currentVersion,
                            onQuickPlay = onQuickPlay,
                            backToMainScreen = toMainScreen,
                        )
                        else -> {}
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.versions_manage_no_versions),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } // Box content
        } // Column sheet
    } // Box fillMaxSize
}

@Composable
private fun HomeTabBar(
    activeTab: NormalNavKey.Versions,
    hasVersion: Boolean,
    onTabSelected: (NormalNavKey.Versions) -> Unit,
) {
    val activeColor = Color(0xFFF97316)
    val inactiveColor = MaterialTheme.colorScheme.onSurface
    val disabledColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

    val tabs = listOf(
        Triple(R.drawable.ic_duo_extension, R.string.mods_manage, NormalNavKey.Versions.ModsManager),
        Triple(R.drawable.ic_duo_public, R.string.saves_manage, NormalNavKey.Versions.SavesManager),
        Triple(R.drawable.ic_duo_format_paint, R.string.resource_pack_manage, NormalNavKey.Versions.ResourcePackManager),
        Triple(R.drawable.ic_duo_lightbulb, R.string.shader_pack_manage, NormalNavKey.Versions.ShadersManager),
        Triple(R.drawable.ic_duo_photo_library, R.string.screenshots_manage, NormalNavKey.Versions.ScreenshotsManager),
        Triple(R.drawable.ic_duo_dns, R.string.servers_list, NormalNavKey.Versions.ServerList),
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            tabs.forEach { (icon, desc, tab) ->
                val isActive = activeTab == tab
                val tint = when {
                    !hasVersion -> disabledColor
                    isActive -> activeColor
                    else -> inactiveColor
                }
                Column(
                    modifier = Modifier
                        .alpha(if (hasVersion) 1f else 0.4f)
                        .clip(MaterialTheme.shapes.small)
                        .combinedClickable(
                            enabled = hasVersion,
                            role = Role.Tab,
                            onClick = { onTabSelected(tab) }
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Icon(
                        modifier = Modifier.size(22.dp),
                        painter = painterResource(icon),
                        contentDescription = stringResource(desc),
                        tint = tint,
                    )
                    Text(
                        text = stringResource(desc),
                        style = MaterialTheme.typography.labelSmall,
                        color = tint,
                        maxLines = 1,
                    )
                    // Active indicator
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .width(24.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(if (isActive && hasVersion) activeColor else Color.Transparent)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HomeHeroCard(
    isVisible: Boolean,
    onLaunchGame: (Version?) -> Unit,
    toAccountManageScreen: () -> Unit,
    toVersionManageScreen: () -> Unit,
    toVersionSettingsScreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val yOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible
    )

    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()
    val accounts by AccountsManager.accountsFlow.collectAsStateWithLifecycle()
    val version by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    val isRefreshing by VersionsManager.isRefreshing.collectAsStateWithLifecycle()

    val bgRes = versionBackgroundRes(version?.getVersionInfo()?.minecraftVersion)

    Box(
        modifier = modifier
            .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
            .clip(MaterialTheme.shapes.extraLarge)
    ) {
        Image(
            painter = painterResource(bgRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.70f)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Giữa (2/3): Launch button + tên profile, căn giữa HeroCard
            Box(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = version?.getVersionName() ?: stringResource(R.string.versions_manage_no_versions),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                    )
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.extraLarge)
                            .background(Color.Black.copy(alpha = 0.35f))
                            .combinedClickable(role = Role.Button, onClick = { onLaunchGame(null) })
                            .padding(horizontal = 36.dp, vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                modifier = Modifier.size(30.dp),
                                painter = painterResource(R.drawable.ic_duo_play),
                                contentDescription = null,
                                tint = Color.White
                            )
                            Text(
                                text = stringResource(R.string.main_launch_game),
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Bên phải (1/3): version + account + settings xếp dọc
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                val hasVersion = version?.isValid() == true
                val activeColor = Color(0xFFF97316)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Version
                    var showList by remember { mutableStateOf(false) }
                    Box {
                        BackgroundCard(shape = MaterialTheme.shapes.large) {
                            VersionManagerLayout(
                                isRefreshing = isRefreshing,
                                version = version,
                                modifier = Modifier.fillMaxWidth(),
                                swapToVersionManage = toVersionManageScreen,
                                openListMenu = { showList = true }
                            )
                        }
                        DropdownMenu(
                            expanded = showList,
                            onDismissRequest = { showList = false },
                            modifier = Modifier.width(240.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            VersionsManager.versions.forEach { version0 ->
                                DropdownMenuItem(
                                    text = { CommonVersionInfoLayout(modifier = Modifier.fillMaxWidth(), version = version0, iconSize = 28.dp) },
                                    onClick = { if (version != version0) VersionsManager.saveVersion(version0); showList = false }
                                )
                            }
                        }
                    }

                    // Account
                    var showAccounts by remember { mutableStateOf(false) }
                    var accountRowCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.large)
                                .background(Color.Black.copy(alpha = 0.30f))
                                .onGloballyPositioned { accountRowCoords = it }
                                .clickable(role = Role.Button) { showAccounts = true }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val currentAccount = account
                            if (currentAccount != null) {
                                PlayerFace(account = currentAccount, avatarSize = 24.dp)
                            } else {
                                Icon(modifier = Modifier.size(24.dp), painter = painterResource(R.drawable.ic_add), contentDescription = null, tint = Color.White)
                            }
                            Text(
                                modifier = Modifier.weight(1f).basicMarquee(iterations = Int.MAX_VALUE),
                                text = account?.username ?: stringResource(R.string.account_no_account),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                maxLines = 1
                            )
                            Icon(modifier = Modifier.size(14.dp), painter = painterResource(R.drawable.ic_arrow_drop_down_rounded), contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
                        }
                        val rowBounds = accountRowCoords?.boundsInParent()
                        val rowWidth = rowBounds?.width ?: 0f
                        val dropdownWidth = 200.dp
                        DropdownMenu(
                            expanded = showAccounts,
                            onDismissRequest = { showAccounts = false },
                            modifier = Modifier.width(dropdownWidth),
                            shape = MaterialTheme.shapes.large,
                            offset = DpOffset(x = with(LocalDensity.current) { rowWidth.toDp() } - dropdownWidth, y = 4.dp)
                        ) {
                            accounts.forEach { account0 ->
                                DropdownMenuItem(
                                    text = { Text(text = account0.username, style = MaterialTheme.typography.labelMedium, maxLines = 1) },
                                    onClick = { AccountsManager.setCurrentAccount(account0); showAccounts = false }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text(text = stringResource(R.string.account_add_new_account)) },
                                onClick = { showAccounts = false; toAccountManageScreen() }
                            )
                        }
                    }

                    // Settings
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (hasVersion) 1f else 0.4f)
                            .clip(MaterialTheme.shapes.large)
                            .combinedClickable(enabled = hasVersion, role = Role.Button, onClick = toVersionSettingsScreen)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource(R.drawable.ic_duo_settings),
                            contentDescription = null,
                            tint = if (hasVersion) activeColor else Color.White.copy(alpha = 0.35f),
                        )
                        Text(
                            text = stringResource(R.string.versions_manage_settings),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (hasVersion) activeColor else Color.White.copy(alpha = 0.35f),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RightMenuContent(
    modifier: Modifier = Modifier,
    onLaunchGame: (Version?) -> Unit,
    toAccountManageScreen: () -> Unit,
    toVersionManageScreen: () -> Unit,
    toVersionSettingsScreen: () -> Unit,
    launchButton: @Composable (
        innerModifier: Modifier,
        onClick: () -> Unit,
        text: @Composable RowScope.() -> Unit
    ) -> Unit,
) {
    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()
    val version by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    val isRefreshing by VersionsManager.isRefreshing.collectAsStateWithLifecycle()

    ConstraintLayout(
        modifier = modifier
    ) {
        val (accountAvatar, versionManagerLayout, launchButton) = createRefs()

        AccountAvatar(
            modifier = Modifier
                .constrainAs(accountAvatar) {
                    top.linkTo(parent.top)
                    bottom.linkTo(launchButton.top, margin = 32.dp)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            account = account,
            onClick = toAccountManageScreen
        )

        var showList by remember { mutableStateOf(false) }
        var versionManagerRow by remember { mutableStateOf<LayoutCoordinates?>(null) }
        Box(
            modifier = Modifier.constrainAs(versionManagerLayout) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(launchButton.top)
            },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .onGloballyPositioned { coordinates ->
                            versionManagerRow = coordinates
                        }
                ) {
                    VersionManagerLayout(
                        isRefreshing = isRefreshing,
                        version = version,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        swapToVersionManage = toVersionManageScreen,
                        openListMenu = { showList = true },
                    )
                }
                version?.takeIf { !isRefreshing && it.isValid() }?.let {
                    IconButton(
                        modifier = Modifier.padding(end = 8.dp),
                        onClick = toVersionSettingsScreen
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings_filled),
                            contentDescription = stringResource(R.string.versions_manage_settings)
                        )
                    }
                }
            }

            val menuAnchor = versionManagerRow
            val menuAnchorBounds = menuAnchor?.boundsInParent()
            val menuAnchorX = menuAnchorBounds?.left ?: 0f
            val menuAnchorHeight = menuAnchorBounds?.height ?: 0f

            DropdownMenu(
                expanded = showList && menuAnchor != null,
                onDismissRequest = { showList = false },
                modifier = Modifier.width(260.dp),
                offset = DpOffset(
                    x = with(LocalDensity.current) { menuAnchorX.toDp() },
                    y = with(LocalDensity.current) { (-menuAnchorHeight).toDp() } - 8.dp
                ),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                VersionsManager.versions.forEach { version0 ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CommonVersionInfoLayout(
                                    modifier = Modifier.weight(1f),
                                    version = version0,
                                    iconSize = 28.dp
                                )
                                IconButton(
                                    onClick = {
                                        onLaunchGame(version0)
                                        showList = false
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_play_arrow_filled),
                                        contentDescription = stringResource(R.string.main_launch_game),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        onClick = {
                            if (version == version0) return@DropdownMenuItem
                            VersionsManager.saveVersion(version0)
                            showList = false
                        }
                    )
                }
            }
        }

        launchButton(
            Modifier
                .fillMaxWidth()
                .constrainAs(launchButton) {
                    bottom.linkTo(parent.bottom, margin = 8.dp)
                }
                .padding(PaddingValues(horizontal = 12.dp)),
            {
                onLaunchGame(null)
            },
            {
                MarqueeText(text = stringResource(R.string.main_launch_game))
            }
        )
    }
}

@Composable
private fun RightMenu(
    isVisible: Boolean,
    onLaunchGame: (Version?) -> Unit,
    modifier: Modifier = Modifier,
    toAccountManageScreen: () -> Unit = {},
    toVersionManageScreen: () -> Unit = {},
    toVersionSettingsScreen: () -> Unit = {}
) {
    val xOffset by swapAnimateDpAsState(
        targetValue = 40.dp,
        swapIn = isVisible,
        isHorizontal = true
    )

    BackgroundCard(
        modifier = modifier.offset { IntOffset(x = xOffset.roundToPx(), y = 0) },
        shape = MaterialTheme.shapes.extraLarge
    ) {
        RightMenuContent(
            modifier = Modifier.fillMaxSize(),
            onLaunchGame = onLaunchGame,
            toAccountManageScreen = toAccountManageScreen,
            toVersionManageScreen = toVersionManageScreen,
            toVersionSettingsScreen = toVersionSettingsScreen
        ) { innerModifier, onClick, text ->
            ScalingActionButton(
                modifier = innerModifier,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                onClick = onClick,
                content = text
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun VersionManagerLayout(
    isRefreshing: Boolean,
    version: Version?,
    swapToVersionManage: () -> Unit,
    openListMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.large)
            .combinedClickable(
                role = Role.Button,
                onClick = swapToVersionManage,
                onLongClick = {
                    if (version != null) openListMenu()
                }
            )
            .padding(PaddingValues(all = 8.dp))
    ) {
        if (isRefreshing) {
            Box(modifier = Modifier.fillMaxWidth()) {
                LoadingIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center)
                )
            }
        } else {
            VersionIconImage(
                version = version,
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.width(8.dp))

            if (version == null) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .basicMarquee(iterations = Int.MAX_VALUE),
                    text = stringResource(R.string.versions_manage_no_versions),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                ) {
                    Text(
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                        text = version.getVersionName(),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                    if (version.isValid()) {
                        Text(
                            modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                            text = version.getVersionSummary(),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}