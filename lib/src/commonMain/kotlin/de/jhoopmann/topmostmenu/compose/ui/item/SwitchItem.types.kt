package de.jhoopmann.topmostmenu.compose.ui.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

class SwitchItemModifiers(
    var switch: Modifier,
    icon: Modifier,
    text: Modifier,
    keyBadge: Modifier,
    keyText: Modifier,
    item: Modifier,
    contents: ItemContentModifiers
) : IconItemModifiers(icon, text, keyBadge, keyText, item, contents)

typealias SwitchLayout = @Composable (SwitchItemModifiers, Boolean) -> Unit
