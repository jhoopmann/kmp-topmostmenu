package de.jhoopmann.topmostmenu.compose.ui.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.text.TextStyle

open class ClickableItemModifiers(
    var keyBadge: Modifier,
    var keyText: Modifier,
    item: Modifier,
    contents: ItemContentModifiers
) : ItemModifiers(item, contents)

data class KeyBadgeColors(val background: Color, val content: Color)

data class ItemClickEvent(var result: Boolean = true)

typealias ItemOnClick = (ItemClickEvent) -> Unit
typealias KeyTextLayout = @Composable (modifiers: ClickableItemModifiers, text: String, textStyle: TextStyle, colors: KeyBadgeColors) -> Unit
typealias KeyEventMatcher = (KeyEvent) -> Boolean
