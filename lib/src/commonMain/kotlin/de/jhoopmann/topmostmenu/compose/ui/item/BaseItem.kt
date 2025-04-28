package de.jhoopmann.topmostmenu.compose.ui.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import de.jhoopmann.topmostmenu.compose.ui.state.MenuState

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MenuState.BaseItem(
    modifiers: ItemModifiers = defaultItemModifiers(),
    layout: ItemLayout = DefaultItemLayout
) {
    layout(
        modifiers.apply {
            item = Modifier.onPointerEvent(PointerEventType.Enter, onEvent = { event ->
                if (!event.changes.first().isConsumed) {
                    emitAction {
                        window.toFront()
                        window.requestFocus()

                        closeChildren()
                    }
                }
            }).then(item)
        },
        { it.invoke() },
        { it.invoke() }
    ) { it.invoke() }

}