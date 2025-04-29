package de.jhoopmann.topmostmenu.compose.ui.item

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    val close: () -> Unit = remember {
        {
            closeChildren(focus = true)
        }
    }

    layout(
        modifiers.apply {
            item = Modifier.onPointerEvent(PointerEventType.Move, onEvent = { event ->
                if (!event.changes.first().isConsumed) {
                    emitAction(close)
                }
            }).then(item)
        },
        { it.invoke() },
        { it.invoke() }
    ) { it.invoke() }

}