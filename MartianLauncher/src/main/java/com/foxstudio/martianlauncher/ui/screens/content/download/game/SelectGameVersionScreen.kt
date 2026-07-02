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

package com.foxstudio.martianlauncher.ui.screens.content.download.game

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.scrollbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.foxstudio.martianlauncher.R
import com.foxstudio.martianlauncher.game.versioninfo.MinecraftVersion
import com.foxstudio.martianlauncher.game.versioninfo.MinecraftVersions
import com.foxstudio.martianlauncher.game.versioninfo.models.isType
import com.foxstudio.martianlauncher.setting.AllSettings
import com.foxstudio.martianlauncher.ui.base.BaseScreen
import com.foxstudio.martianlauncher.ui.components.CheckChip
import com.foxstudio.martianlauncher.ui.components.EdgeDirection
import com.foxstudio.martianlauncher.ui.components.LittleTextLabel
import com.foxstudio.martianlauncher.ui.components.ScalingLabel
import com.foxstudio.martianlauncher.ui.components.SimpleTextInputField
import com.foxstudio.martianlauncher.ui.components.fadeEdge
import com.foxstudio.martianlauncher.ui.screens.NestedNavKey
import com.foxstudio.martianlauncher.ui.screens.NormalNavKey
import com.foxstudio.martianlauncher.ui.screens.TitledNavKey
import com.foxstudio.martianlauncher.ui.screens.content.elements.backgroundGlass
import com.foxstudio.martianlauncher.ui.screens.content.elements.majorVersionKey
import com.foxstudio.martianlauncher.ui.screens.content.elements.versionBackgroundRes
import com.foxstudio.martianlauncher.ui.screens.content.elements.versionUpdateName
import com.foxstudio.martianlauncher.ui.theme.cardColor
import com.foxstudio.martianlauncher.ui.theme.onCardColor
import com.foxstudio.martianlauncher.utils.animation.getAnimateTween
import com.foxstudio.martianlauncher.utils.animation.swapAnimateDpAsState
import com.foxstudio.martianlauncher.utils.classes.Quadruple
import com.foxstudio.martianlauncher.utils.formatDate
import com.foxstudio.martianlauncher.utils.logging.Logger
import com.foxstudio.martianlauncher.utils.network.toLocal
import com.foxstudio.martianlauncher.utils.string.isEmptyOrBlank
import com.foxstudio.martianlauncher.viewmodel.EventViewModel
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException

private const val TAG = "SelectGameVersion"

/** 版本列表加载状态 */
private sealed interface VersionState {
    /** 加载中 */
    data object Loading : VersionState
    /** 加载完成 */
    data class None(val versions: List<MinecraftVersion>) : VersionState
    /** 加载出现异常 */
    data class Failure(val message: Int, val args: Array<Any>? = null) : VersionState {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Failure

            if (message != other.message) return false
            if (args != null) {
                if (other.args == null) return false
                if (!args.contentEquals(other.args)) return false
            } else if (other.args != null) return false

            return true
        }

        override fun hashCode(): Int {
            var result = message
            result = 31 * result + (args?.contentHashCode() ?: 0)
            return result
        }
    }
}

private enum class VersionTypeFilter {
    RELEASE, SNAPSHOT, APRIL_FOOLS, OLD
}

/**
 * 版本过滤条件（单选类型 + 名称搜索）
 * @param type 当前选中的版本类型（仅显示该类型）
 * @param id 搜索并过滤版本ID
 */
private data class VersionFilter(
    val type: VersionTypeFilter = VersionTypeFilter.RELEASE,
    val id: String = ""
)

private class VersionsViewModel: ViewModel() {
    var versionState by mutableStateOf<VersionState>(VersionState.Loading)
        private set

    //简易版本类型过滤器
    var versionFilter by mutableStateOf(VersionFilter())
        private set

    fun filterWith(filter: VersionFilter) {
        versionFilter = filter
        viewModelScope.launch {
            val allVersions = MinecraftVersions.allVersions.value
            versionState = VersionState.None(
                versions = allVersions.filterVersions(versionFilter)
            )
        }
    }

    fun refresh(forceReload: Boolean = false) {
        viewModelScope.launch {
            versionState = VersionState.Loading
            versionState = runCatching {
                MinecraftVersions.refreshVersions(forceReload)
                val allVersions = MinecraftVersions.allVersions.value
                VersionState.None(allVersions.filterVersions(versionFilter))
            }.getOrElse { e ->
                Logger.warning(TAG, "Failed to get version manifest!", e)
                val message: Pair<Int, Array<Any>?> = when(e) {
                    is HttpRequestTimeoutException -> R.string.error_timeout to null
                    is UnknownHostException, is UnresolvedAddressException -> R.string.error_network_unreachable to null
                    is ConnectException -> R.string.error_connection_failed to null
                    is ResponseException -> e.toLocal()
                    else -> {
                        Logger.error(TAG, "An unknown exception was caught!", e)
                        val errorMessage = e.localizedMessage ?: e.message ?: e::class.qualifiedName ?: "Unknown error"
                        R.string.empty_holder to arrayOf(errorMessage)
                    }
                }
                VersionState.Failure(message.first, message.second)
            }
        }
    }

    init {
        //初始化后，刷新版本列表
        refresh()
    }

    override fun onCleared() {
        viewModelScope.cancel()
    }
}

@Composable
fun SelectGameVersionScreen(
    mainScreenKey: TitledNavKey?,
    downloadScreenKey: TitledNavKey?,
    downloadGameScreenKey: TitledNavKey?,
    eventViewModel: EventViewModel,
    onVersionSelect: (String) -> Unit = {}
) {
    val viewModel = viewModel(
        key = NormalNavKey.DownloadGame.SelectGameVersion.toString()
    ) {
        VersionsViewModel()
    }

    BaseScreen(
        levels1 = listOf(
            Pair(NestedNavKey.DownloadGame::class.java, mainScreenKey)
        ),
        Triple(NormalNavKey.DownloadGame.SelectGameVersion, downloadGameScreenKey, false)
    ) { isVisible ->
        val yOffset by swapAnimateDpAsState(
            targetValue = (-40).dp,
            swapIn = isVisible
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
        ) {
            when (val state = viewModel.versionState) {
                is VersionState.Loading -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LinearWavyProgressIndicator(
                            modifier = Modifier.width(168.dp),
                            wavelength = 32.dp
                        )
                    }
                }

                is VersionState.Failure -> {
                    Box(Modifier.fillMaxSize()) {
                        val message = if (state.args != null) {
                            stringResource(state.message, *state.args)
                        } else {
                            stringResource(state.message)
                        }

                        ScalingLabel(
                            modifier = Modifier.align(Alignment.Center),
                            text = stringResource(R.string.download_game_failed_to_get_versions, message),
                            onClick = {
                                viewModel.refresh(true)
                            }
                        )
                    }
                }

                is VersionState.None -> {
                    Column {
                        VersionHeader(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            versionFilter = viewModel.versionFilter,
                            onVersionFilterChange = { viewModel.filterWith(it) },
                            itemContainerColor = cardColor(),
                            itemContentColor = onCardColor(),
                            onRefreshClick = {
                                viewModel.refresh(true)
                            }
                        )

                        VersionGroupGrid(
                            modifier = Modifier.weight(1f),
                            versions = state.versions,
                            onVersionSelect = onVersionSelect,
                            openLink = { url ->
                                eventViewModel.sendEvent(EventViewModel.Event.OpenLink(url))
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 简易过滤器，过滤特定类型的版本
 */
private fun List<MinecraftVersion>.filterVersions(
    versionFilter: VersionFilter
) = this.filter { version ->
    version.isType(
        release = versionFilter.type == VersionTypeFilter.RELEASE,
        snapshot = versionFilter.type == VersionTypeFilter.SNAPSHOT,
        aprilFools = versionFilter.type == VersionTypeFilter.APRIL_FOOLS,
        old = versionFilter.type == VersionTypeFilter.OLD
    )
}.filter { version ->
    //Fix：单独过滤版本名称
    val versionId = versionFilter.id
    versionId.isEmptyOrBlank() || version.version.id.contains(versionId)
}

@Composable
private fun VersionHeader(
    modifier: Modifier = Modifier,
    versionFilter: VersionFilter,
    onVersionFilterChange: (VersionFilter) -> Unit,
    itemContainerColor: Color,
    itemContentColor: Color,
    onRefreshClick: () -> Unit = {}
) {
    Column(modifier = modifier) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fadeEdge(
                            state = scrollState,
                            direction = EdgeDirection.Horizontal
                        )
                        .widthIn(max = this@BoxWithConstraints.maxWidth / 5 * 3) //3/5
                        .horizontalScroll(scrollState),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    //版本筛选条件（单选）
                    VersionTypeItem(
                        selected = versionFilter.type == VersionTypeFilter.RELEASE,
                        onClick = {
                            onVersionFilterChange(versionFilter.copy(type = VersionTypeFilter.RELEASE))
                        },
                        text = stringResource(R.string.download_game_type_release)
                    )
                    VersionTypeItem(
                        selected = versionFilter.type == VersionTypeFilter.SNAPSHOT,
                        onClick = {
                            onVersionFilterChange(versionFilter.copy(type = VersionTypeFilter.SNAPSHOT))
                        },
                        text = stringResource(R.string.download_game_type_snapshot)
                    )
                    VersionTypeItem(
                        selected = versionFilter.type == VersionTypeFilter.APRIL_FOOLS,
                        onClick = {
                            onVersionFilterChange(versionFilter.copy(type = VersionTypeFilter.APRIL_FOOLS))
                        },
                        text = stringResource(R.string.download_game_type_april_fools)
                    )
                    VersionTypeItem(
                        selected = versionFilter.type == VersionTypeFilter.OLD,
                        onClick = {
                            onVersionFilterChange(versionFilter.copy(type = VersionTypeFilter.OLD))
                        },
                        text = stringResource(R.string.download_game_type_old)
                    )
                }

                //搜索、刷新
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SimpleTextInputField(
                        modifier = Modifier.weight(1f),
                        value = versionFilter.id,
                        onValueChange = { onVersionFilterChange(versionFilter.copy(id = it)) },
                        color = itemContainerColor,
                        contentColor = itemContentColor,
                        singleLine = true,
                        hint = {
                            Text(
                                text = stringResource(R.string.generic_search),
                                style = TextStyle(color = itemContentColor).copy(fontSize = 12.sp)
                            )
                        }
                    )

                    IconButton(
                        onClick = onRefreshClick
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_refresh),
                            contentDescription = stringResource(R.string.generic_refresh)
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun VersionTypeItem(
    selected: Boolean,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CheckChip(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        label = {
            Text(text)
        },
    )
}

/** 按游戏版本聚合后的版本分组 */
private data class VersionGroup(
    val key: String,
    val bgRes: Int,
    val versions: List<MinecraftVersion>
)

private fun MinecraftVersion.typeLabel(context: Context): String = when (type) {
    MinecraftVersion.Type.Release -> context.getString(R.string.download_game_type_release)
    MinecraftVersion.Type.Snapshot -> context.getString(R.string.download_game_type_snapshot)
    MinecraftVersion.Type.AprilFools -> context.getString(R.string.download_game_type_april_fools)
    MinecraftVersion.Type.OldBeta -> context.getString(R.string.download_game_type_old_beta)
    MinecraftVersion.Type.OldAlpha -> context.getString(R.string.download_game_type_old_alpha)
    else -> context.getString(R.string.generic_unknown)
}

private val NUMERIC_MAJOR = Regex("""^\d+(\.\d+)?$""")

/**
 * 把版本列表按游戏大版本（1.20、1.21…、26…）聚合成分组，
 * 无法解析出大版本的（部分快照/远古版）则按版本类型聚合。
 */
private fun List<MinecraftVersion>.groupByGameVersion(context: Context): List<VersionGroup> {
    return this
        .groupBy { version ->
            val major = majorVersionKey(version.version.id)
            if (major != null && NUMERIC_MAJOR.matches(major)) {
                major to versionBackgroundRes(major)
            } else {
                version.typeLabel(context) to versionBackgroundRes(null)
            }
        }
        .map { (keyAndBg, versions) ->
            VersionGroup(
                key = keyAndBg.first,
                bgRes = keyAndBg.second,
                versions = versions.sortedByDescending { it.version.releaseTime }
            )
        }
        .sortedByDescending { group -> group.versions.firstOrNull()?.version?.releaseTime ?: "" }
}

@Composable
private fun VersionGroupGrid(
    modifier: Modifier = Modifier,
    versions: List<MinecraftVersion>,
    onVersionSelect: (String) -> Unit,
    openLink: (url: String) -> Unit
) {
    val context = LocalContext.current
    val groups = remember(versions) { versions.groupByGameVersion(context) }
    val rows = remember(groups) { groups.chunked(2) }

    var expandedKey by rememberSaveable { mutableStateOf<String?>(null) }

    val scrollState = rememberLazyListState()
    LazyColumn(
        modifier = modifier.scrollbar(
            state = scrollState.scrollIndicatorState,
            orientation = Orientation.Vertical,
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        state = scrollState,
    ) {
        rows.forEachIndexed { rowIndex, rowGroups ->
            item(key = "g-row-$rowIndex") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowGroups.forEach { group ->
                        VersionGroupCard(
                            modifier = Modifier.weight(1f),
                            group = group,
                            expanded = expandedKey == group.key,
                            onClick = {
                                expandedKey = if (expandedKey == group.key) null else group.key
                            }
                        )
                    }
                    if (rowGroups.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item(key = "g-exp-$rowIndex") {
                rowGroups.forEach { group ->
                    AnimatedVisibility(
                        visible = expandedKey == group.key,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            val columns = (this.maxWidth / 180.dp).toInt().coerceIn(2, 4)
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                group.versions.chunked(columns).forEach { rowVersions ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(IntrinsicSize.Max),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        rowVersions.forEach { version ->
                                            VersionItemLayout(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight(),
                                                version = version,
                                                onClick = {
                                                    onVersionSelect(version.version.id)
                                                },
                                                onAccessWiki = { wikiUrl ->
                                                    openLink(wikiUrl)
                                                },
                                            )
                                        }
                                        repeat(columns - rowVersions.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VersionGroupCard(
    modifier: Modifier = Modifier,
    group: VersionGroup,
    expanded: Boolean,
    onClick: () -> Unit,
    shape: Shape = MaterialTheme.shapes.large
) {
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "groupArrowRotation"
    )
    Surface(
        modifier = modifier,
        onClick = onClick,
        shape = shape,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .height(100.dp)
                .clip(shape)
        ) {
            Image(
                painter = painterResource(group.bgRes),
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
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = group.key,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1
                    )
                    versionUpdateName(group.key)?.let { updateName ->
                        Text(
                            text = updateName,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1
                        )
                    }
                    Text(
                        text = stringResource(
                            R.string.download_game_group_version_count,
                            group.versions.size
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
                Icon(
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(arrowRotation),
                    painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun VersionItemLayout(
    modifier: Modifier = Modifier,
    version: MinecraftVersion,
    onClick: () -> Unit = {},
    onAccessWiki: (String) -> Unit = {},
    shape: Shape = MaterialTheme.shapes.large,
    influencedByBackground: Boolean = true,
    color: Color = cardColor(influencedByBackground),
    contentColor: Color = onCardColor(),
    blur: Int = AllSettings.backgroundBlur.state,
) {
    val scale = remember { Animatable(initialValue = 0.95f) }
    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
    }

    val (icon, versionType, wikiUrl, summary) = getVersionComponents(version)

    Surface(
        modifier = modifier.graphicsLayer(scaleY = scale.value, scaleX = scale.value),
        onClick = onClick,
        shape = shape,
        color = color,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier
                .clip(shape = shape)
                .backgroundGlass(blur, color, influencedByBackground)
                .padding(all = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let { versionIcon ->
                Image(
                    modifier = Modifier.size(32.dp),
                    painter = versionIcon,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = version.version.id,
                        style = MaterialTheme.typography.labelLarge
                    )

                    LittleTextLabel(
                        text = versionType
                    )
                }

                summary?.let { text ->
                    Text(
                        modifier = Modifier.alpha(0.7f),
                        text = text,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Text(
                    modifier = Modifier.alpha(0.7f),
                    text = formatDate(
                        input = version.version.releaseTime,
                        pattern = stringResource(R.string.date_format)
                    ),
                    style = MaterialTheme.typography.labelMedium
                )
            }

            wikiUrl?.let { url ->
                IconButton(
                    modifier = Modifier.size(32.dp),
                    onClick = { onAccessWiki(url) }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_link),
                        contentDescription = "Wiki"
                    )
                }
            }
        }
    }
}

@Composable
private fun getVersionComponents(
    version: MinecraftVersion
): Quadruple<Painter?, String, String?, String?> {
    val vmVer = version.version
    val summary = version.summary?.let { stringResource(it) }
    val urlSuffix = version.urlSuffix ?: vmVer.id

    return when (version.type) {
        MinecraftVersion.Type.Release -> {
            Quadruple(
                painterResource(R.drawable.img_minecraft),
                stringResource(R.string.download_game_type_release),
                stringResource(R.string.url_wiki_minecraft_game_release, urlSuffix),
                summary
            )
        }
        MinecraftVersion.Type.Snapshot -> {
            Quadruple(
                painterResource(R.drawable.img_command_block),
                stringResource(R.string.download_game_type_snapshot),
                stringResource(R.string.url_wiki_minecraft_game_snapshot, urlSuffix),
                summary
            )
        }
        MinecraftVersion.Type.AprilFools -> {
            Quadruple(
                painterResource(R.drawable.img_diamond_block),
                stringResource(R.string.download_game_type_april_fools),
                stringResource(R.string.url_wiki_minecraft_game_snapshot, urlSuffix),
                summary
            )
        }
        MinecraftVersion.Type.OldBeta -> {
            Quadruple(
                painterResource(R.drawable.img_old_cobblestone),
                stringResource(R.string.download_game_type_old_beta),
                null,
                summary
            )
        }
        MinecraftVersion.Type.OldAlpha -> {
            Quadruple(
                painterResource(R.drawable.img_old_grass_block),
                stringResource(R.string.download_game_type_old_alpha),
                null,
                summary
            )
        }
        else -> {
            Quadruple(
                null,
                stringResource(R.string.generic_unknown),
                null,
                version.summary?.let { stringResource(it) }
            )
        }
    }
}