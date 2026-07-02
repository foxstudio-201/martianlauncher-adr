package com.foxstudio.martianlauncher.ui.screens.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.foxstudio.martianlauncher.R
import com.foxstudio.martianlauncher.game.download.assets.platform.Platform

data class DownloadPlatformBarState(
    val platform: Platform,
    val enabled: Boolean,
    val onChange: (Platform) -> Unit
)

val LocalDownloadPlatformBar = compositionLocalOf<MutableState<DownloadPlatformBarState?>?> { null }

private data class PlatformTab(val platform: Platform, val icon: Int, val label: String)

private val PLATFORM_TABS = listOf(
    PlatformTab(Platform.MODRINTH, R.drawable.img_platform_modrinth, "Modrinth"),
    PlatformTab(Platform.CURSEFORGE, R.drawable.img_platform_curseforge, "CurseForge"),
)

@Composable
fun PlatformTabBar(
    modifier: Modifier = Modifier,
    state: DownloadPlatformBarState?
) {
    AnimatedVisibility(visible = state != null) {
        if (state == null) return@AnimatedVisibility
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PLATFORM_TABS.forEach { tab ->
                PlatformTabItem(
                    tab = tab,
                    selected = state.platform == tab.platform,
                    enabled = state.enabled || state.platform == tab.platform,
                    onClick = {
                        if (state.enabled && state.platform != tab.platform) {
                            state.onChange(tab.platform)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PlatformTabItem(
    tab: PlatformTab,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val accent = MaterialTheme.colorScheme.primary
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else 0.35f),
        color = if (selected) accent.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier
                    .size(18.dp)
                    .alpha(if (selected) 1f else 0.6f),
                painter = painterResource(tab.icon),
                contentDescription = tab.label
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = tab.label,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
