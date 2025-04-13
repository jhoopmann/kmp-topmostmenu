package de.jhoopmann.topmostmenu.compose.ui.item

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val DefaultTextModifier: Modifier = Modifier.padding(8.dp)

open class TextItemModifiers(
    var text: Modifier = DefaultTextModifier,
    keyBadge: Modifier = DefaultKeyBadgeModifier,
    keyText: Modifier = Modifier,
    item: Modifier = DefaultItemModifier,
    contents: ItemContentModifiers = ItemContentModifiers()
) : ClickableItemModifiers(keyBadge, keyText, item, contents)

typealias TextLayout = @Composable (TextItemModifiers, TextStyle, String) -> Unit

val DefaultTextLayout: TextLayout = { modifiers, style, label ->
    Text(
        text = label,
        modifier = modifiers.text,
        style = style,
        maxLines = 1,
        overflow = TextOverflow.Visible
    )
}

@Composable
fun rememberDefaultTextStyle(): TextStyle = with(LocalTextStyle.current) {
    remember { this.copy(lineHeight = 0.sp) }
}

@Composable
fun TextItem(
    text: String = "",
    textStyle: TextStyle = rememberDefaultTextStyle(),
    textLayout: TextLayout = DefaultTextLayout,
    keyText: String = "",
    keyTextStyle: TextStyle = rememberDefaultKeyTextStyle(),
    keyBadgeColors: KeyBadgeColors = rememberDefaultKeyBadgeColor(),
    keyTextLayout: KeyTextLayout = DefaultKeyTextLayout,
    keyEventMatcher: KeyEventMatcher? = null,
    modifiers: TextItemModifiers = TextItemModifiers(),
    onClick: ItemOnClick? = null,
    layout: ItemLayout = DefaultItemLayout
) {
    ClickableItem(
        keyText = keyText,
        keyTextStyle = keyTextStyle,
        keyTextLayout = keyTextLayout,
        keyBadgeColors = keyBadgeColors,
        keyEventMatcher = keyEventMatcher,
        modifiers = modifiers,
        onClick = onClick
    ) { contentModifiers, prepend, append, center ->
        layout(
            contentModifiers,
            prepend,
            append
        ) {
            center {
                if (text.isNotEmpty()) {
                    textLayout(modifiers, textStyle, text)
                }

                it()
            }
        }
    }
}
