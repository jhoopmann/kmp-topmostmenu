package de.jhoopmann.topmostmenu.compose.ui.item

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

val DefaultMenuItemIconModifier: Modifier = Modifier.size(34.dp)
    .padding(top = 8.dp, bottom = 8.dp, end = 8.dp, start = 0.dp)
val DefaultMenuItemIconPainter: ImageVector = Icons.Default.ChevronRight
val DefaultMenuIconLayout: IconLayout = { modifiers, painter, tint, text ->
    Icon(
        imageVector = painter,
        contentDescription = text,
        modifier = (modifiers as MenuItemModifiers).menuIcon,
        tint = tint
    )
}

fun defaultMenuItemModifiers(
    menuIcon: Modifier = DefaultMenuItemIconModifier,
    icon: Modifier = DefaultIconItemModifier,
    text: Modifier = DefaultTextModifier,
    keyBadge: Modifier = DefaultKeyBadgeModifier,
    keyText: Modifier = DefaultKeyTextModifier,
    item: Modifier = DefaultItemModifier,
    contents: ItemContentModifiers = defaultItemContentModifiers()
): MenuItemModifiers = MenuItemModifiers(menuIcon, icon, text, keyBadge, keyText, item, contents)
