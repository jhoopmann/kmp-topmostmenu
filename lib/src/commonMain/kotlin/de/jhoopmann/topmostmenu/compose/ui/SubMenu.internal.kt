package de.jhoopmann.topmostmenu.compose.ui

import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.*
import de.jhoopmann.topmostmenu.compose.ui.state.MenuState
import de.jhoopmann.topmostmenu.compose.ui.state.getPreferredRootSize

internal fun MenuState.calculateItemScreenPosition(
    itemCoordinates: LayoutCoordinates,
    density: Density
): DpOffset {
    return position.let { windowPosition ->
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

internal fun MenuState.calculatePositionDefault(menuItemPosition: DpOffset, menuItemSize: DpSize): DpOffset {
    return with(scope.layoutPadding) {
        DpOffset(
            x = (menuItemPosition.x + menuItemSize.width) - calculateLeftPadding(LayoutDirection.Ltr),
            y = menuItemPosition.y - calculateTopPadding()
        )
    }
}

internal fun MenuState.positionLeftExceedsScreen(x: Dp, menuSize: DpSize): Boolean {
    return topState.window.graphicsConfiguration.bounds.let { screenBounds ->
        (x + menuSize.width) > (screenBounds.width.dp + screenBounds.x.dp)
    }
}

internal fun MenuState.positionTopExceedsScreen(y: Dp, menuSize: DpSize): Boolean {
    return topState.window.graphicsConfiguration.bounds.let { screenBounds ->
        (y + menuSize.height) > (screenBounds.height.dp + screenBounds.y.dp)
    }
}

internal fun MenuState.correctLeftForExceededScreen(x: Dp, menuItemSize: DpSize, menuSize: DpSize): Dp {
    return with(scope.layoutPadding) {
        ((x - menuItemSize.width) - menuSize.width) +
                calculateLeftPadding(LayoutDirection.Rtl) + calculateRightPadding(LayoutDirection.Rtl)
    }
}

internal fun MenuState.correctTopForExceededScreen(y: Dp, menuItemSize: DpSize, menuSize: DpSize): Dp {
    return with(scope.layoutPadding) {
        ((y + menuItemSize.height) - menuSize.height) + calculateTopPadding() + calculateBottomPadding()
    }
}

internal fun MenuState.calculatePosition(
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
