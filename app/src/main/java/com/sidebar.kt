package com

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

// Geçici boş data class - sidebar kaldırılıyor
data class SidebarItem(
    val icon: ImageVector,
    val label: String, 
    val route: String
)

// Geçici boş composable - sidebar kaldırılıyor
@Composable
fun NeonSidebar(
    items: List<SidebarItem>,
    selectedRoute: String,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    onToggleExpand: () -> Unit = {}
) {
    // Boş implementasyon - sidebar kaldırıldı
}

// Geçici helper function
@Composable
fun sidebarContentPadding(isExpanded: Boolean): Modifier {
    return Modifier
}