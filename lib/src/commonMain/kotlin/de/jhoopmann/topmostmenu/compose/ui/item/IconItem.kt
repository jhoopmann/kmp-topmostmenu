package de.jhoopmann.topmostmenu.compose.ui.item

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

typealias IconLayout = @Composable (IconItemModifiers, ImageVector, Color, String) -> Unit

val DefaultIconItemModifier: Modifier =
    Modifier.size(34.dp).padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 0.dp)

val DefaultIconLayout: IconLayout = { modifiers, painter, tint, text ->
    Icon(
        imageVector = painter,
        contentDescription = text,
        modifier = modifiers.icon,
        tint = tint
    )
}

open class IconItemModifiers(
    var icon: Modifier = DefaultIconItemModifier,
    text: Modifier = DefaultTextModifier,
    keyBadge: Modifier = DefaultKeyBadgeModifier,
    keyText: Modifier = Modifier,
    item: Modifier = DefaultItemModifier,
    contents: ItemContentModifiers = ItemContentModifiers()
) : TextItemModifiers(text, keyBadge, keyText, item, contents)

@Composable
fun rememberDefaultIconTint(): Color = with(LocalContentColor.current) {
    LocalContentAlpha.current.let { alpha ->
        remember { copy(alpha = alpha) }
    }
}


@Composable
fun IconItem(
    iconPainter: ImageVector? = null,
    iconTint: Color = rememberDefaultIconTint(),
    iconLayout: IconLayout = DefaultIconLayout,
    text: String = "",
    textStyle: TextStyle = rememberDefaultTextStyle(),
    textLayout: TextLayout = DefaultTextLayout,
    keyText: String = "",
    keyTextStyle: TextStyle = rememberDefaultKeyTextStyle(),
    keyBadgeColors: KeyBadgeColors = rememberDefaultKeyBadgeColor(),
    keyTextLayout: KeyTextLayout = DefaultKeyTextLayout,
    keyEventMatcher: KeyEventMatcher? = null,
    modifiers: IconItemModifiers = IconItemModifiers(),
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

                    it()
                }
            },
            append,
            center
        )
    }
}
