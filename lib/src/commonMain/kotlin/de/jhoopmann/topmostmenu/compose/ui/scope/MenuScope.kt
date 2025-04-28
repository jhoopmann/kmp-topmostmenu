package de.jhoopmann.topmostmenu.compose.ui.scope

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.WindowPosition
import de.jhoopmann.topmostmenu.compose.ui.ClosedEvent
import de.jhoopmann.topmostmenu.compose.ui.MenuLayout
import de.jhoopmann.topmostmenu.compose.ui.MenuModifiers

data class MenuScope internal constructor(
    val initialPosition: WindowPosition,
    val initialSize: DpSize,
    val shape: Shape,
    val modifiers: MenuModifiers,
    val actionAutoClose: Boolean,
    val onClosed: ClosedEvent?,
    val layoutPadding: PaddingValues,
    val layout: MenuLayout
)
