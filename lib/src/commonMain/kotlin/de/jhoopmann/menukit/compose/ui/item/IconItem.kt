package de.jhoopmann.menukit.compose.ui.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import de.jhoopmann.menukit.compose.ui.state.MenuState
import java.awt.Window

@Composable
fun MenuState.IconItem(
    iconPainter: ImageVector? = null,
    iconTint: Color = defaultIconTint(),
    iconLayout: IconLayout = DefaultIconLayout,
    text: String = "",
    textStyle: TextStyle = defaultTextStyle(),
    textLayout: TextLayout = DefaultTextLayout,
    keyText: String = "",
    keyTextStyle: TextStyle = defaultKeyTextStyle(),
    keyBadgeColors: KeyBadgeColors = defaultKeyBadgeColors(),
    keyTextLayout: KeyTextLayout = DefaultKeyTextLayout,
    keyEventMatcher: KeyEventMatcher? = null,
    modifiers: IconItemModifiers = defaultIconItemModifiers(),
    onClick: ItemOnClick? = null,
    layout: ItemLayout = DefaultItemLayout
) {
    TextItem(
        text = text,
        textStyle = textStyle,
        textLayout = textLayout,
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
            { it ->
                prepend {
                    iconPainter?.let { painter ->
                        iconLayout(modifiers, painter, iconTint, text)
                    }

                    it.invoke()
                }
            },
            append,
            center
        )
    }
}
