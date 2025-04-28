package de.jhoopmann.topmostmenu.compose.ui.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

open class TextItemModifiers(
    var text: Modifier,
    keyBadge: Modifier,
    keyText: Modifier,
    item: Modifier,
    contents: ItemContentModifiers
) : ClickableItemModifiers(keyBadge, keyText, item, contents)

typealias TextLayout = @Composable (TextItemModifiers, TextStyle, String) -> Unit
