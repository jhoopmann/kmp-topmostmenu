package de.jhoopmann.topmostmenu.compose.ui.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import de.jhoopmann.topmostmenu.compose.ui.state.MenuState
import java.awt.Window

@Composable
fun MenuState.SwitchItem(
    checked: Boolean,
    switchLayout: SwitchLayout = DefaultSwitchLayout,
    iconPainter: ImageVector? = null,
    iconLayout: IconLayout = DefaultIconLayout,
    text: String = "",
    textStyle: TextStyle = defaultTextStyle(),
    textLayout: TextLayout = DefaultTextLayout,
    keyText: String = "",
    keyTextStyle: TextStyle = defaultKeyTextStyle(),
    keyBadgeColors: KeyBadgeColors = defaultKeyBadgeColors(),
    keyTextLayout: KeyTextLayout = DefaultKeyTextLayout,
    keyEventMatcher: KeyEventMatcher? = null,
    modifiers: SwitchItemModifiers = defaultSwitchItemModifiers(),
    onClick: ItemOnClick? = null,
    layout: ItemLayout = DefaultItemLayout
) {
    IconItem(
        iconPainter = iconPainter,
        iconLayout = iconLayout,
        text = text,
        textStyle = textStyle,
        textLayout = textLayout,
        keyText = keyText,
        keyTextStyle = keyTextStyle,
        keyBadgeColors = keyBadgeColors,
        keyTextLayout = keyTextLayout,
        keyEventMatcher = keyEventMatcher,
        modifiers = modifiers,
        onClick = onClick
    ) { contentModifiers, prepend, append, center ->
        layout(
            contentModifiers,
            prepend,
            {
                append {
                    it.invoke()

                    switchLayout(modifiers, checked)
                }
            },
            center
        )
    }
}
