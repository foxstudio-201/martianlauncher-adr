package com.foxstudio.martianlauncher.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

data class SideBarItem(
    val icon: Int,
    val contentDescription: String,
    val selected: Boolean,
    val onClick: () -> Unit
)

@Composable
fun SideBar(
    modifier: Modifier = Modifier,
    topItems: List<SideBarItem>,
    accountItem: SideBarItem
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(72.dp)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        topItems.forEach { item ->
            SideBarButton(item)
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .alpha(0.4f)
        )
        Spacer(modifier = Modifier.height(6.dp))

        SideBarButton(accountItem)
    }
}

private val SideBarActiveColor = Color(0xFFF97316)

@Composable
private fun SideBarButton(item: SideBarItem) {
    val background = if (item.selected) SideBarActiveColor.copy(alpha = 0.15f) else Color.Transparent
    val tint = if (item.selected) SideBarActiveColor else MaterialTheme.colorScheme.onSurfaceVariant
    Icon(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .clickable(onClick = item.onClick)
            .padding(12.dp),
        painter = painterResource(item.icon),
        contentDescription = item.contentDescription,
        tint = tint
    )
}
