package de.jhoopmann.menukit.compose.ui.item

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val DefaultIconItemModifier: Modifier = Modifier.size(34.dp)
    .padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 0.dp)
val DefaultIconLayout: IconLayout = { modifiers, painter, tint, text ->
    Icon(
        imageVector = painter,
        contentDescription = text,
        modifier = modifiers.icon,
        tint = tint
    )
}

fun defaultIconItemModifiers(
    icon: Modifier = DefaultIconItemModifier,
    text: Modifier = DefaultTextModifier,
    keyBadge: Modifier = DefaultKeyBadgeModifier,
    keyText: Modifier = DefaultKeyTextModifier,
    item: Modifier = DefaultItemModifier,
    contents: ItemContentModifiers = defaultItemContentModifiers()
): IconItemModifiers = IconItemModifiers(icon, text, keyBadge, keyText, item, contents)

@Composable
fun defaultIconTint(): Color = with(LocalContentColor.current) {
    LocalContentAlpha.current.let { alpha ->
        copy(alpha = alpha)
    }
}
