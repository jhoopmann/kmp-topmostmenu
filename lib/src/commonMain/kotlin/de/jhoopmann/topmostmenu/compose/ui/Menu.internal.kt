package de.jhoopmann.topmostmenu.compose.ui

import de.jhoopmann.topmostmenu.compose.ui.state.MenuState
import kotlinx.coroutines.delay
import java.awt.AWTEvent
import java.awt.MouseInfo
import java.awt.Toolkit
import java.awt.event.AWTEventListener
import java.awt.event.FocusEvent

internal class FocusEventListener(
    private val menuState: MenuState
) : AWTEventListener {
    override fun eventDispatched(event: AWTEvent?) {
        if (event is FocusEvent && event.id == FocusEvent.FOCUS_LOST) {
            if (!menuState.anyVisibleInLocation(MouseInfo.getPointerInfo().location)) {
                menuState.emitAction {
                    delay(16 * 2)

                    if (!menuState.focusedAny) {
                        menuState.close()
                    }
                }
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
