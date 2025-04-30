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
import de.jhoopmann.topmostmenu.compose.ui.state.MenuState

@Composable
fun MenuState.SubMenu(
    state: MenuState,
    shape: Shape = scope.shape,
    modifiers: MenuModifiers = scope.modifiers,
    autoClose: Boolean = scope.actionAutoClose,
    onClosed: ClosedEvent? = null,
    layoutPadding: PaddingValues = scope.layoutPadding,
    layout: MenuLayout = scope.layout,
    menuItemLayout: MenuItemLayout,
    content: MenuContent
) {
    val density: Density = LocalDensity.current
    var menuItemCoordinates: LayoutCoordinates? by remember { mutableStateOf(null) }

    Menu(
        state = state,
        shape = shape,
        actionAutoClose = autoClose,
        onClosed = { propagate ->
            onClosed?.invoke(propagate)

            if (propagate) {
                close(propagate, focus = null)
            }
        },
        modifiers = modifiers,
        layoutPadding = layoutPadding,
        layout = layout,
        content = content
    )

    val open: () -> Unit = remember {
        {
            val newPosition: WindowPosition = state.position.takeIf {
                state.scope.initialPosition is WindowPosition.Absolute
            } ?: state.calculatePosition(
                this,
                menuItemCoordinates!!,
                density,
            ).run { WindowPosition.Absolute(x = x, y = y) }

            val newSize: DpSize = state.size.takeIf { state.scope.initialSize.isSpecified }
                ?: DpSize.Unspecified

            state.open(focus = state.window, position = newPosition, size = newSize)

            topState.closeChildren(focus = null, except = state)
        }
    }

    menuItemLayout({
        menuItemCoordinates = it
    }, {
        if (isVisible && !state.isVisible) {
            state.emitAction(open)
        }
    })
}
