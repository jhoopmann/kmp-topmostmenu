package de.jhoopmann.topmostmenu.compose.ui.item

import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val DefaultTextModifier: Modifier = Modifier.padding(8.dp)
val DefaultTextLayout: TextLayout = { modifiers, style, label ->
    Text(
        text = label,
        modifier = modifiers.text,
        style = style,
        maxLines = 1,
        overflow = TextOverflow.Visible
    )
}

fun defaultTextItemModifiers(
    text: Modifier = DefaultTextModifier,
    keyBadge: Modifier = DefaultKeyBadgeModifier,
    keyText: Modifier = DefaultKeyTextModifier,
    item: Modifier = DefaultItemModifier,
    contents: ItemContentModifiers = defaultItemContentModifiers()
): TextItemModifiers = TextItemModifiers(text, keyBadge, keyText, item, contents)

@Composable
fun defaultTextStyle(): TextStyle = with(LocalTextStyle.current) {
    copy(lineHeight = 0.sp)
}
