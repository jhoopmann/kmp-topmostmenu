package de.jhoopmann.topmostmenu.compose.ui.state

import androidx.compose.ui.layout.LayoutCoordinates

enum class HoverTargetOperation {
    CLOSE,
    OPEN
}

internal data class MenuHoverAction(
    val state: MenuState,
    val operation: HoverTargetOperation,
    val parentState: MenuState? = null,
    val menuItemCoordinates: LayoutCoordinates? = null
)
