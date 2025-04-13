package de.jhoopmann.topmostmenu.compose.ui.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import de.jhoopmann.topmostmenu.compose.ui.state.LocalMenuState
import de.jhoopmann.topmostmenu.compose.ui.state.MenuState

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BaseItem(
    modifiers: ItemModifiers = defaultItemModifiers(),
    layout: ItemLayout = DefaultItemLayout
) {
    val menuState: MenuState = LocalMenuState.current!!
    val topState: MenuState = menuState.topState

    layout(
        modifiers.apply {
            item = Modifier.onPointerEvent(PointerEventType.Enter, onEvent = { event ->
                if (!event.changes.first().isConsumed) {
                    topState.eventQueue.trySend {
                        if (synchronized(topState) {
                                if (!menuState.processing) {
                                    menuState.processing = true
                                    true
                                } else false
                            }
                        ) {
                            menuState.closeChildren()

                            synchronized(topState) {
                                menuState.processing = false
                            }
                        }
                    }
                }
            }).then(item)
        },
        { it() },
        { it() }
    ) { it() }
}