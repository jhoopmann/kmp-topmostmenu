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

@Composable
fun SubMenu(
    state: MenuState? = null,
    shape: Shape? = null,
    modifiers: MenuModifiers? = null,
    autoClose: Boolean? = null,
    onClosed: ClosedEvent? = null,
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
                parentState.close(it)
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
        currentState.emitAction {
            if (currentState.isVisible) {
                return@emitAction
            }

            topState.closeChildren(currentState)

            val newPosition: WindowPosition = currentState.position.takeIf {
                currentState.scope.initialPosition is WindowPosition.Absolute
            } ?: currentState.calculatePosition(
                parentState,
                menuItemCoordinates!!,
                density,
            ).run { WindowPosition.Absolute(x = x, y = y) }

            val newSize: DpSize = currentState.size.takeIf { currentState.scope.initialSize.isSpecified }
                ?: DpSize.Unspecified

            currentState.open(position = newPosition, size = newSize)
        }
    })
}
