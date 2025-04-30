package de.jhoopmann.menukit.compose.ui.state

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

internal fun MenuState.getPreferredRootSize(): DpSize {
    return with(window.contentPane) {
        paint(graphics)
        DpSize(preferredSize.width.dp, preferredSize.height.dp)
    }
}

class RequestForegroundUnsupportedException :
    Exception("Desktop.Action.APP_REQUEST_FOREGROUND not supported by platform")
