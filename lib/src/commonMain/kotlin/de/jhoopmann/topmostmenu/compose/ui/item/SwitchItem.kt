package de.jhoopmann.topmostmenu.compose.ui.item

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

typealias SwitchLayout = @Composable (SwitchItemModifiers, Boolean) -> Unit

val DefaultSwitchLayout: SwitchLayout = { modifiers, checked ->
    Switch(
        modifier = modifiers.switch,
        checked = checked,
        onCheckedChange = null
    )
}

val DefaultSwitchLayoutModifier: Modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, end = 8.dp, start = 0.dp)

class SwitchItemModifiers(
    var switch: Modifier = DefaultSwitchLayoutModifier,
    icon: Modifier = DefaultIconItemModifier,
    text: Modifier = DefaultTextModifier,
    keyBadge: Modifier = DefaultKeyBadgeModifier,
    keyText: Modifier = Modifier,
    item: Modifier = DefaultItemModifier,
    contents: ItemContentModifiers = ItemContentModifiers()
) : IconItemModifiers(icon, text, keyBadge, keyText, item, contents)

@Composable
fun SwitchItem(
    checked: Boolean,
    switchLayout: SwitchLayout = DefaultSwitchLayout,
    iconPainter: ImageVector? = null,
    iconLayout: IconLayout = DefaultIconLayout,
    text: String = "",
    textStyle: TextStyle = rememberDefaultTextStyle(),
    textLayout: TextLayout = DefaultTextLayout,
    keyText: String = "",
    keyTextStyle: TextStyle = rememberDefaultKeyTextStyle(),
    keyBadgeColors: KeyBadgeColors = rememberDefaultKeyBadgeColor(),
    keyTextLayout: KeyTextLayout = DefaultKeyTextLayout,
    keyEventMatcher: KeyEventMatcher? = null,
    modifiers: SwitchItemModifiers = SwitchItemModifiers(),
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
                    it()

                    switchLayout(modifiers, checked)
                }
            },
            center
        )
    }
}
