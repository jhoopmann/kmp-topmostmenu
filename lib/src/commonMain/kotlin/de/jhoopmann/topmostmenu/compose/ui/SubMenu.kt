package de.jhoopmann.topmostmenu.compose.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.window.WindowPosition
import de.jhoopmann.topmostmenu.compose.ui.state.LocalMenuState
import de.jhoopmann.topmostmenu.compose.ui.state.MenuState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized

@OptIn(InternalCoroutinesApi::class)
@Composable
fun SubMenu(
    state: MenuState? = null,
    shape: Shape? = null,
    modifiers: MenuModifiers? = null,
    autoClose: Boolean? = null,
    onClosed: OnClosedEvent? = null,
    layoutPadding: PaddingValues? = null,
    layout: MenuLayout? = null,
    menuItemLayout: MenuItemLayout,
    content: MenuContent
) {
    val density: Density = LocalDensity.current
    val parentState: MenuState = LocalMenuState.current!!
    val currentState: MenuState = state ?: rememberDefaultMenuState()
    val topState: MenuState = parentState.topState
    var menuItemCoordinates: LayoutCoordinates? by remember { mutableStateOf(null) }

    Menu(
        state = currentState,
        shape = shape ?: parentState.scope.shape,
        actionAutoClose = autoClose ?: parentState.scope.actionAutoClose,
        onClosed = {
            onClosed?.invoke(it)

            if (it) {
                synchronized(topState) {
                    currentState.processing = true
                }

                topState.eventQueue.trySend {
                    parentState.close(it)

                    synchronized(topState) {
                        currentState.processing = false
                    }
                }
            }
        },
        modifiers = modifiers ?: parentState.scope.modifiers,
        layoutPadding = layoutPadding ?: parentState.scope.layoutPadding,
        layout = layout ?: parentState.scope.layout,
        content = content
    )

    menuItemLayout({
        menuItemCoordinates = it
    }, {
        if (!synchronized(currentState) { currentState.visible } && synchronized(topState) {
                if (!currentState.processing) {
                    currentState.processing = true
                    true
                } else false
            }
        ) {
            topState.eventQueue.trySend {
                topState.closeChildren(currentState)

                val position: WindowPosition = currentState.windowState.position.takeIf {
                    state?.scope?.initialPosition is WindowPosition.Absolute
                } ?: currentState.calculatePosition(
                    parentState,
                    menuItemCoordinates!!,
                    density,
                ).run { WindowPosition.Absolute(x = x, y = y) }

                println(currentState.windowState.size)

                val size: DpSize = currentState.windowState.size.takeIf {
                    state?.scope?.initialSize?.isSpecified ?: false
                } ?: DpSize.Unspecified

                currentState.open(position = position, size = size)

                synchronized(topState) {
                    currentState.processing = false
                }
            }
        }
    })
}
