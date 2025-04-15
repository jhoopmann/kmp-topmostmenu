package de.jhoopmann.topmostmenu.compose.ui.state

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

internal fun MenuState.getPreferredRootSize(): DpSize {
    return with(composeWindow.contentPane) {
        paint(graphics)
        DpSize(preferredSize.width.dp, preferredSize.height.dp)
    }
}
