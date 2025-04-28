package de.jhoopmann.topmostmenu.compose.ui.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import de.jhoopmann.topmostmenu.compose.ui.state.MenuState
import java.awt.Window

@Composable
fun MenuState.TextItem(
    text: String = "",
    textStyle: TextStyle = defaultTextStyle(),
    textLayout: TextLayout = DefaultTextLayout,
    keyText: String = "",
    keyTextStyle: TextStyle = defaultKeyTextStyle(),
    keyBadgeColors: KeyBadgeColors = defaultKeyBadgeColors(),
    keyTextLayout: KeyTextLayout = DefaultKeyTextLayout,
    keyEventMatcher: KeyEventMatcher? = null,
    modifiers: TextItemModifiers = defaultTextItemModifiers(),
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

                it.invoke()
            }
        }
    }
}
