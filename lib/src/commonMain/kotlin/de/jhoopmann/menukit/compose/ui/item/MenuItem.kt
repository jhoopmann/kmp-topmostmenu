package de.jhoopmann.menukit.compose.ui.item

import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import de.jhoopmann.menukit.compose.ui.state.MenuState

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MenuState.MenuItem(
    menuIconPainter: ImageVector? = DefaultMenuItemIconPainter,
    menuIconTint: Color = defaultIconTint(),
    menuIconLayout: IconLayout = DefaultMenuIconLayout,
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
    modifiers: MenuItemModifiers = defaultMenuItemModifiers(),
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
                .onPointerEvent(eventType = PointerEventType.Move, onEvent = { event ->
                    event.changes.first().consume() // disable for BaseItem

                    onEnter.invoke()
                })
        },
    ) { contentModifiers, prepend, append, center ->
        layout(
            contentModifiers,
            prepend,
            {
                append() {
                    it.invoke()

                    menuIconPainter?.let { painter ->
                        menuIconLayout(modifiers, painter, menuIconTint, text)
                    }
                }
            },
            center
        )
    }
}
