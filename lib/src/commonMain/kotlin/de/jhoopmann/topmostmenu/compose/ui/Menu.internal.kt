package de.jhoopmann.topmostmenu.compose.ui

import de.jhoopmann.topmostmenu.compose.ui.state.MenuState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.jetbrains.skiko.MainUIDispatcher
import java.awt.AWTEvent
import java.awt.Toolkit
import java.awt.event.AWTEventListener
import java.awt.event.FocusEvent

internal class FocusEventListener : AWTEventListener {
    private val flow: MutableStateFlow<AWTEvent?> = MutableStateFlow(null)

    override fun eventDispatched(event: AWTEvent?) {
        if (event is FocusEvent && event.id == FocusEvent.FOCUS_LOST) {
            flow.value = event
        }
    }

    fun register() {
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.FOCUS_EVENT_MASK)
    }

    fun unregister() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this)
    }

    @OptIn(FlowPreview::class)
    fun launchEventsCoroutine(menuState: MenuState, coroutineScope: CoroutineScope): Job {
        return flow.debounce(16 * 4)
            .filterNotNull()
            .onEach {
                if (!menuState.focused) {
                    menuState.emitAction {
                        menuState.close(focus = null, propagate = false)
                    }
                }
            }.flowOn(Dispatchers.Main)
            .launchIn(coroutineScope)
    }
}
