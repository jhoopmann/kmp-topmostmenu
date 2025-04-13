package de.jhoopmann.topmostmenu.compose.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.WindowPosition
import de.jhoopmann.topmostmenu.compose.ui.item.OnEnter
import de.jhoopmann.topmostmenu.compose.ui.item.OnGloballyPositioned
import de.jhoopmann.topmostmenu.compose.ui.state.LocalMenuState
import de.jhoopmann.topmostmenu.compose.ui.state.MenuState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized

typealias MenuItemLayout = @Composable (OnGloballyPositioned, OnEnter) -> Unit

private fun MenuState.calculateItemScreenPosition(itemCoordinates: LayoutCoordinates, density: Density): DpOffset {
    return windowState.position.let { windowPosition ->
        itemCoordinates.positionInWindow().let { position ->
            with(density) {
                DpOffset(
                    x = windowPosition.x + position.x.toDp(),
                    y = windowPosition.y + position.y.toDp()
                )
            }
        }
    }
}

private fun MenuState.getPreferredRootSize(): DpSize {
    return with(window.contentPane) {
        paint(graphics)
        DpSize(preferredSize.width.dp, preferredSize.height.dp)
    }
}

private fun MenuState.calculatePositionDefault(menuItemPosition: DpOffset, menuItemSize: DpSize): DpOffset {
    return with(scope.layoutPadding) {
        DpOffset(
            x = (menuItemPosition.x + menuItemSize.width) - calculateLeftPadding(LayoutDirection.Ltr),
            y = menuItemPosition.y - calculateTopPadding()
        )
    }
}

private fun MenuState.positionLeftExceedsScreen(x: Dp, menuSize: DpSize): Boolean {
    return topState.window.graphicsConfiguration.bounds.let { screenBounds ->
        (x + menuSize.width) > (screenBounds.width.dp + screenBounds.x.dp)
    }
}

private fun MenuState.positionTopExceedsScreen(y: Dp, menuSize: DpSize): Boolean {
    return topState.window.graphicsConfiguration.bounds.let { screenBounds ->
        (y + menuSize.height) > (screenBounds.height.dp + screenBounds.y.dp)
    }
}

private fun MenuState.correctLeftForExceededScreen(x: Dp, menuItemSize: DpSize, menuSize: DpSize): Dp {
    return with(scope.layoutPadding) {
        ((x - menuItemSize.width) - menuSize.width) +
                calculateLeftPadding(LayoutDirection.Rtl) + calculateRightPadding(LayoutDirection.Rtl)
    }
}

private fun MenuState.correctTopForExceededScreen(y: Dp, menuItemSize: DpSize, menuSize: DpSize): Dp {
    return with(scope.layoutPadding) {
        ((y + menuItemSize.height) - menuSize.height) + calculateTopPadding() + calculateBottomPadding()
    }
}

private fun MenuState.calculatePosition(
    parentState: MenuState,
    menuItemCoordinates: LayoutCoordinates,
    density: Density
): DpOffset {
    return parentState.calculateItemScreenPosition(menuItemCoordinates, density).let { menuItemPosition ->
        with(density) {
            menuItemCoordinates.size.toSize().toDpSize().let { menuItemSize ->
                val menuSize: DpSize = getPreferredRootSize()

                calculatePositionDefault(menuItemPosition, menuItemSize).run {
                    copy(
                        x = if (positionLeftExceedsScreen(x, menuSize))
                            correctLeftForExceededScreen(x, menuItemSize, menuSize)
                        else x,
                        y = if (positionTopExceedsScreen(y, menuSize))
                            correctTopForExceededScreen(y, menuItemSize, menuSize)
                        else y
                    )
                }

            }
        }
    }
}

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
                    state?.scope?.initialWindowState?.position is WindowPosition.Absolute
                } ?: currentState.calculatePosition(
                    parentState,
                    menuItemCoordinates!!,
                    density,
                ).run { WindowPosition.Absolute(x = x, y = y) }

                val size: DpSize = currentState.windowState.size.takeIf {
                    state?.scope?.initialWindowState?.size?.isSpecified ?: false
                } ?: DpSize.Unspecified

                currentState.open(
                    windowState = currentState.windowState.apply {
                        this.position = position
                        this.size = size
                    }
                )

                synchronized(topState) {
                    currentState.processing = false
                }
            }
        }
    })
}
