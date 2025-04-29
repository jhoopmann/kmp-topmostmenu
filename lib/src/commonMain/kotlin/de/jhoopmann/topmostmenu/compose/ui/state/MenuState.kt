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
import java.awt.Window
import java.lang.ref.WeakReference
import de.jhoopmann.topmostmenu.native.platform
import de.jhoopmann.topmostmenu.native.Platform

class MenuState(
    position: WindowPosition = WindowPosition(Alignment.Center),
    size: DpSize = DpSize.Unspecified
) {
    lateinit var scope: MenuScope
        internal set
    internal lateinit var topState: MenuState
    internal var parentState: MenuState? = null
    internal val children: MutableList<MenuState> = mutableListOf()
    internal val windowState: DialogState = DialogState(position = position, size = size)
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
    private var ownerWindowRef: WeakReference<Window?>? = null
    internal var ownerWindow: Window?
        get() = ownerWindowRef?.get()
        set(value) {
            ownerWindowRef?.clear()
            ownerWindowRef = value?.run { WeakReference(value) }
        }
    val menuActionState: MutableStateFlow<MenuAction?> = MutableStateFlow(null)

    var initialized: Boolean by mutableStateOf(false)
        internal set
    val initializedAll: Boolean
        get() = initialized && children.all { it.initializedAll }
    val focused: Boolean
        get() = window.isFocused || children.any { it.focused }
    var isVisible: Boolean by mutableStateOf(false)
        private set

    var size: DpSize
        get() = windowState.size
        set(value) = setWindowSize(value)
    var position: WindowPosition
        get() = windowState.position
        set(value) = setWindowPosition(value)


    fun emitOpen(
        position: WindowPosition = this.position,
        size: DpSize = this.size
    ) {
        emitAction {
            open(focus = true, position, size)
        }
    }

    fun emitClose() {
        emitAction {
            close(propagate = false, focus = true)
        }
    }

    fun handleKeyEvent(event: KeyEvent): Boolean {
        return topState.keyEventListeners.any { it.invoke(event) } || children.any {
            it.handleKeyEvent(event)
        }
    }

    internal fun emitAction(action: MenuAction) {
        topState.menuActionState.value = action
    }

    internal fun open(
        focus: Boolean,
        position: WindowPosition = this.position,
        size: DpSize = this.size
    ) {
        setWindowPosition(position)
        setWindowSize(size)

        if (focus && !window.hasFocus()) { // mac os specific
            requestDesktopForeground()
        }

        if (!composeTopMostImpl.isVisible) {
            composeTopMostImpl.isVisible = true
        }

        if (focus) {
            requestFocus(window)
        }

        closeChildren(false, except = null)

        isVisible = true
    }

    internal fun close(
        propagate: Boolean,
        focus: Boolean,
        except: MenuState? = null,
    ) {
        if (focus) {
            parentState?.window?.also { requestFocus(it) }
        }

        closeChildren(focus = false, except)

        if (except != null && (except == this || hasDeepChild(except))) {
            return
        }

        if (composeTopMostImpl.isVisible) {
            composeTopMostImpl.isVisible = false
        }

        isVisible = false
        scope.onClosed?.invoke(propagate)
    }

    internal fun closeChildren(focus: Boolean, except: MenuState? = null) {
        if (focus) {
            requestFocus(window)
        }

        children.forEach {
            it.close(propagate = false, focus = false, except)
        }
    }

    private fun hasDeepChild(state: MenuState): Boolean {
        return (children.any { it == state } || children.any { it.hasDeepChild(state) })
    }

    @OptIn(FlowPreview::class)
    internal fun launchActionCoroutine(coroutineScope: CoroutineScope): Job {
        return topState.menuActionState.debounce(16 * 2)
            .distinctUntilChanged()
            .filterNotNull()
            .onEach {
                it.invoke()
            }
            .flowOn(Dispatchers.Main)
            .launchIn(coroutineScope)
    }

    private fun setWindowSize(value: DpSize) {
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

    private fun setWindowPosition(value: WindowPosition) {
        if (value.isSpecified && value != this.position) {
            window.location = with(value) {
                Point(x.value.toInt(), y.value.toInt())
            }
        }
    }

    private fun requestFocus(window: Window) {
        window.takeUnless { it.hasFocus() }?.run {
            toFront()
            requestFocus()
        }
    }

    private fun requestDesktopForeground() {
        if (platform == Platform.MacOS) {
            if (!Desktop.getDesktop().isSupported(Desktop.Action.APP_REQUEST_FOREGROUND)) {
                throw RequestForegroundUnsupportedException()
            }

            Desktop.getDesktop().requestForeground(true)
        }
    }
}
