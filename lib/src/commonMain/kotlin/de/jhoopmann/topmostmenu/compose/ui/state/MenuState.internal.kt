package de.jhoopmann.topmostmenu.compose.ui.state

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState

internal fun MenuState.getPreferredRootSize(): DpSize {
    return with(window.contentPane) {
        paint(graphics)
        DpSize(preferredSize.width.dp, preferredSize.height.dp)
    }
}

fun WindowState.copy(
    position: WindowPosition = when (this.position) {
        is WindowPosition.PlatformDefault -> WindowPosition.PlatformDefault
        is WindowPosition.Aligned -> (this.position as WindowPosition.Aligned).copy()
        is WindowPosition.Absolute -> (this.position as WindowPosition.Absolute).copy()
    },
    placement: WindowPlacement = this.placement,
    size: DpSize = this.size.copy(),
    isMinimized: Boolean = this.isMinimized
): WindowState = WindowState(
    position = position,
    placement = placement,
    size = size,
    isMinimized = isMinimized
)
