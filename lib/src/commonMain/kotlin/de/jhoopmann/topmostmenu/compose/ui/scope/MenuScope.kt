package de.jhoopmann.topmostmenu.compose.ui.scope

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.window.WindowState
import de.jhoopmann.topmostmenu.compose.ui.MenuLayout
import de.jhoopmann.topmostmenu.compose.ui.MenuModifiers
import de.jhoopmann.topmostmenu.compose.ui.OnClosedEvent

data class MenuScope(
    val initialWindowState: WindowState,
    val shape: Shape,
    val modifiers: MenuModifiers,
    val actionAutoClose: Boolean,
    val onClosed: OnClosedEvent?,
    val layoutPadding: PaddingValues,
    val layout: MenuLayout
)
