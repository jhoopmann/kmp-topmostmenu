package de.jhoopmann.topmostmenu.compose.ui.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

typealias OnGloballyPositioned = (LayoutCoordinates) -> Unit
typealias OnEnter = () -> Unit

val DefaultMenuItemIconModifier: Modifier =
    Modifier.size(34.dp).padding(top = 8.dp, bottom = 8.dp, end = 8.dp, start = 0.dp)
val DefaultMenuItemIconPainter: ImageVector = Icons.Default.ChevronRight

val DefaultMenuIconLayout: IconLayout = { modifiers, painter, tint, text ->
    Icon(
        imageVector = painter,
        contentDescription = text,
        modifier = (modifiers as MenuItemModifiers).menuIcon,
        tint = tint
    )
}

class MenuItemModifiers(
    var menuIcon: Modifier = DefaultMenuItemIconModifier,
    icon: Modifier = DefaultIconItemModifier,
    text: Modifier = DefaultTextModifier,
    keyBadge: Modifier = DefaultKeyBadgeModifier,
    keyText: Modifier = Modifier,
    item: Modifier = DefaultItemModifier,
    contents: ItemContentModifiers = ItemContentModifiers()
) : IconItemModifiers(icon, text, keyBadge, keyText, item, contents)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MenuItem(
    menuIconPainter: ImageVector? = DefaultMenuItemIconPainter,
    menuIconTint: Color = rememberDefaultIconTint(),
    menuIconLayout: IconLayout = DefaultMenuIconLayout,
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
    modifiers: MenuItemModifiers = MenuItemModifiers(),
    onGloballyPositioned: OnGloballyPositioned,
    onEnter: OnEnter,
    layout: ItemLayout = DefaultItemLayout,
) {
    var layoutCoordinates: LayoutCoordinates? by remember { mutableStateOf(null) }

    IconItem(
        iconPainter = iconPainter,
        iconTint = iconTint,
        iconLayout = iconLayout,
        text = text,
        textStyle = textStyle,
        textLayout = textLayout,
        keyText = keyText,
        keyTextStyle = keyTextStyle,
        keyBadgeColors = keyBadgeColors,
        keyTextLayout = keyTextLayout,
        keyEventMatcher = keyEventMatcher,
        modifiers = modifiers.apply {
            item = modifiers.item.onGloballyPositioned {
                layoutCoordinates = it
                onGloballyPositioned(it)
            }.clickable(enabled = true, onClick = { onEnter.invoke() })
                .onPointerEvent(eventType = PointerEventType.Enter, onEvent = { event ->
                    event.changes.first().consume()
                    onEnter.invoke()
                })
        },
    ) { contentModifiers, prepend, append, center ->
        layout(
            contentModifiers,
            prepend,
            {
                append() {
                    it()

                    menuIconPainter?.let { painter ->
                        menuIconLayout(modifiers, painter, menuIconTint, text)
                    }
                }
            },
            center
        )
    }
}
