
package de.jhoopmann.topmostmenu.compose.ui
import de.jhoopmann.topmostmenu.compose.ui.state.MenuState
import kotlinx.coroutines.delay
import java.awt.AWTEvent
import java.awt.MouseInfo
import java.awt.Toolkit
import java.awt.event.AWTEventListener
import java.awt.event.FocusEvent
import java.awt.event.WindowEvent

internal class FocusEventListener(
    private val menuState: MenuState
) : AWTEventListener {
    override fun eventDispatched(event: AWTEvent?) {
        if (event is FocusEvent && event.id == FocusEvent.FOCUS_LOST) {
            menuState.emitAction {
                delay(16 * 4)

                if (!menuState.topState.focusedAny) {
                    menuState.topState.close()
                }
            }
        }
    }

    fun register() {
        Toolkit.getDefaultToolkit().addAWTEventListener(this, java.awt.AWTEvent.FOCUS_EVENT_MASK)
    }

    fun unregister() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this)
    }
}
