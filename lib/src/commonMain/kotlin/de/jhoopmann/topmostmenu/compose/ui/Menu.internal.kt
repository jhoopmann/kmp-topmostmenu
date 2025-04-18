package de.jhoopmann.topmostmenu.compose.ui

import de.jhoopmann.topmostmenu.compose.ui.state.MenuState
import java.awt.AWTEvent
import java.awt.MouseInfo
import java.awt.Toolkit
import java.awt.event.AWTEventListener
import java.awt.event.FocusEvent

internal class FocusEventListener(
    private val topState: MenuState
) : AWTEventListener {
    override fun eventDispatched(event: AWTEvent?) {
        if (event is FocusEvent && event.id == FocusEvent.FOCUS_LOST) {
            if (!topState.anyVisibleInLocation(MouseInfo.getPointerInfo().location)) {        
                topState.close()
            }
        }
    }

    fun register() {
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.FOCUS_EVENT_MASK)
    }

    fun unregister() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this)
    }
}
