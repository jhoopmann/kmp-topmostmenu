package de.jhoopmann.menukit.compose.ui.item

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates

open class MenuItemModifiers(
    var menuIcon: Modifier,
    icon: Modifier,
    text: Modifier,
    keyBadge: Modifier,
    keyText: Modifier,
    item: Modifier,
    contents: ItemContentModifiers
) : IconItemModifiers(icon, text, keyBadge, keyText, item, contents)

typealias OnGloballyPositioned = (LayoutCoordinates) -> Unit
typealias OnEnter = () -> Unit
