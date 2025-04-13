package de.jhoopmann.topmostmenu.compose.ui.item

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import de.jhoopmann.topmostmenu.compose.ui.state.LocalMenuState
import de.jhoopmann.topmostmenu.compose.ui.state.MenuState
import kotlinx.coroutines.delay

@Composable
fun ClickableItem(
    keyText: String = "",
    keyTextStyle: TextStyle = defaultKeyTextStyle(),
    keyBadgeColors: KeyBadgeColors = defaultKeyBadgeColors(),
    keyTextLayout: KeyTextLayout = DefaultKeyTextLayout,
    keyEventMatcher: KeyEventMatcher? = null,
    modifiers: ClickableItemModifiers = defaultClickableItemModifiers(),
    onClick: ItemOnClick? = null,
    layout: ItemLayout = DefaultItemLayout
) {
    val menuState: MenuState = LocalMenuState.current!!
    val topState: MenuState = menuState.topState

    val onClickWithClose: () -> Boolean = remember {
        {
            val event = ItemClickEvent()
            onClick!!.invoke(event)

            if (event.result) {
                menuState.takeIf { it.scope.actionAutoClose }?.run {
                    synchronized(topState) {
                        topState.processing = true
                    }

                    topState.eventQueue.trySend {
                        delay(300)
                        menuState.close(true)

                        synchronized(topState) {
                            topState.processing = false
                        }
                    }
                }
            }

            event.result
        }
    }

    remember {
        keyEventMatcher?.run {
            menuState.keyEventListeners.add { event ->
                invoke(event) && onClickWithClose.invoke()
            }
        }
    }

    BaseItem(
        modifiers = modifiers.apply {
            item = onClick?.let { click ->
                item.clickable(
                    enabled = true,
                    onClick = { onClickWithClose.invoke() }
                )
            } ?: item
        }
    ) { contentModifiers, prepend, append, center ->
        layout(
            contentModifiers,
            prepend,
            { it ->
                append {
                    it.invoke()

                    if (keyEventMatcher != null) {
                        keyTextLayout(modifiers, keyText, keyTextStyle, keyBadgeColors)
                    }
                }
            },
            center
        )
    }
}
