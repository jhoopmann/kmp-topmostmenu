package de.jhoopmann.topmostmenu.compose.ui.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

open class IconItemModifiers(
    var icon: Modifier,
    text: Modifier,
    keyBadge: Modifier,
    keyText: Modifier,
    item: Modifier,
    contents: ItemContentModifiers
) : TextItemModifiers(text, keyBadge, keyText, item, contents)

typealias IconLayout = @Composable (IconItemModifiers, ImageVector, Color, String) -> Unit
