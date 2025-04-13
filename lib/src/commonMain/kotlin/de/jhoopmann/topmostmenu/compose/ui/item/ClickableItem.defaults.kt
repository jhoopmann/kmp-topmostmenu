package de.jhoopmann.topmostmenu.compose.ui.item

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Badge
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val DefaultKeyBadgeModifier: Modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, end = 8.dp, start = 0.dp)
val DefaultKeyTextModifier: Modifier = Modifier
val DefaultKeyTextLayout: KeyTextLayout = { modifiers, text, textStyle, colors ->
    Badge(modifier = modifiers.keyBadge, backgroundColor = colors.background, contentColor = colors.content) {
        Text(
            modifier = modifiers.keyText,
            text = text,
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Visible
        )
    }
}

fun defaultClickableItemModifiers(
    keyBadge: Modifier = DefaultKeyBadgeModifier,
    keyText: Modifier = DefaultKeyTextModifier,
    item: Modifier = DefaultItemModifier,
    contents: ItemContentModifiers = defaultItemContentModifiers()
): ClickableItemModifiers = ClickableItemModifiers(keyBadge, keyText, item, contents)

@Composable
fun defaultKeyTextStyle(): TextStyle = with(LocalTextStyle.current) {
    return copy(lineHeight = 0.sp, fontSize = this.fontSize.div(1.2f))
}

@Composable
fun defaultKeyBadgeColors(
    background: Color = MaterialTheme.colors.primary,
    content: Color = MaterialTheme.colors.onPrimary
): KeyBadgeColors = KeyBadgeColors(background, content)
