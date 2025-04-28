package de.jhoopmann.topmostmenu.compose.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.awt.ComposeDialog
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.window.DialogState
import androidx.compose.ui.window.WindowPosition
import de.jhoopmann.topmostmenu.compose.ui.item.KeyEventMatcher
import de.jhoopmann.topmostmenu.compose.ui.scope.MenuScope
import de.jhoopmann.topmostwindow.compose.ui.awt.ComposeTopMostDialog
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.awt.Desktop
import java.awt.Dimension
import java.awt.Point
import java.lang.ref.WeakReference

class MenuState(
    position: WindowPosition = WindowPosition(Alignment.Center),
    size: DpSize = DpSize.Unspecified
) {
    lateinit var scope: MenuScope
        internal set
    lateinit var topState: MenuState
        internal set
    internal val windowState: DialogState = DialogState(position = position, size = size)
    internal val children: MutableList<MenuState> = mutableListOf()
    internal val keyEventListeners: MutableList<KeyEventMatcher> = mutableListOf()
    private var composeTopMostImplRef: WeakReference<ComposeTopMostDialog>? = null
    internal var composeTopMostImpl: ComposeTopMostDialog
        get() = composeTopMostImplRef?.get()!!
        set(value) {
            composeTopMostImplRef?.clear()
            composeTopMostImplRef = WeakReference(value)
        }
    internal val window: ComposeDialog
        get() = composeTopMostImpl.window

    internal val focusedAny: Boolean
        get() = window.isFocused || children.any { it.focusedAny }
    private val menuActionState: MutableStateFlow<MenuAction?> = MutableStateFlow(null)

    var size: DpSize
        get() = windowState.size
        set(value) {
            if (value.isSpecified && value != this.size) {
                window.size = Dimension(value.width.value.toInt(), value.height.value.toInt())
            } else if (value.isUnspecified) {
                with(window) {
                    contentPane.size = getPreferredRootSize().run {
                        Dimension(width.value.toInt(), height.value.toInt())
                    }
                    rootPane.size = contentPane.size
                    pack()
                }
            }
        }
    var position: WindowPosition
        get() = windowState.position
        set(value) {
            if (value.isSpecified && value != this.position) {
                window.location = with(value) {
                    Point(x.value.toInt(), y.value.toInt())
                }
            }
        }
    var initialized: Boolean by mutableStateOf(false)
        internal set
    val initializedAll: Boolean
        get() = initialized && children.all { it.initializedAll }
    var isVisible: Boolean by mutableStateOf(false)
        private set

    fun emitOpen(
        position: WindowPosition = this.position,
        size: DpSize = this.size
    ) {
        emitAction { open(position, size) }
    }

    fun emitClose(byAction: Boolean = false) {
        emitAction { close(byAction) }
    }

    fun emitCloseChildren() {
        emitAction { closeChildren() }
    }

    fun handleKeyEvent(event: KeyEvent): Boolean {
        return topState.keyEventListeners.any { it.invoke(event) } || children.any {
            it.handleKeyEvent(event)
        }
    }

    @OptIn(FlowPreview::class)
    internal fun launchActionCoroutine(coroutineScope: CoroutineScope): Job {
        return topState.menuActionState.debounce(16 * 2)
            .distinctUntilChanged()
            .filterNotNull()
            .onEach { it.invoke() }
            .flowOn(Dispatchers.Main)
            .launchIn(coroutineScope)
    }

    internal fun emitAction(action: MenuAction) {
        topState.menuActionState.value = action
    }

    internal fun open(
        position: WindowPosition = this.position,
        size: DpSize = this.size
    ) {
        this.position = position
        this.size = size

        requestDesktopForeground()

        if (!composeTopMostImpl.isVisible) {
            composeTopMostImpl.isVisible = true
        }

        window.toFront()
        window.requestFocus()

        isVisible = true
    }

    internal fun requestDesktopForeground() {
        if (!Desktop.getDesktop().isSupported(Desktop.Action.APP_REQUEST_FOREGROUND)) {
            throw RequestForegroundUnsupportedException()
        }

        Desktop.getDesktop().requestForeground(true)
    }

    internal fun close(
        byAction: Boolean = false,
        except: MenuState? = null
    ) {
        closeChildren(except)

        if (except != null && (except == this || hasDeepChild(except))) {
            return
        }

        if (composeTopMostImpl.isVisible) {
            composeTopMostImpl.isVisible = false
        }

        isVisible = false
        scope.onClosed?.invoke(byAction)
    }

    internal fun closeChildren(except: MenuState? = null) {
        children.forEach {
            it.close(false, except)
        }
    }

    internal fun anyVisibleInLocation(location: Point): Boolean {
        return visibleInLocation(location) ||
                children.any { it.anyVisibleInLocation(location) }
    }

    private fun hasDeepChild(state: MenuState): Boolean {
        return (children.any { it == state } || children.any { it.hasDeepChild(state) })
    }

    private fun visibleInLocation(location: Point): Boolean {
        return isVisible && with(position) {
            val size: DpSize = windowState.size
            val xMax: Float = x.value + size.width.value
            val yMax: Float = y.value + size.height.value

            (location.x >= x.value && location.x <= xMax) &&
                    (location.y >= y.value && location.y <= yMax)
        }
    }
}
