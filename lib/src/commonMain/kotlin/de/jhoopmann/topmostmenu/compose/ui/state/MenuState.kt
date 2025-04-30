package de.jhoopmann.topmostmenu.compose.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeDialog
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.window.DialogState
import androidx.compose.ui.window.WindowPosition
import de.jhoopmann.topmostmenu.compose.ui.item.KeyEventMatcher
import de.jhoopmann.topmostmenu.compose.ui.scope.MenuScope
import de.jhoopmann.topmostmenu.native.Platform
import de.jhoopmann.topmostmenu.native.platform
import de.jhoopmann.topmostwindow.compose.ui.awt.ComposeTopMostDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import java.awt.Desktop
import java.awt.Dimension
import java.awt.Point
import java.awt.Window
import java.lang.ref.WeakReference

class MenuState(
    position: WindowPosition = WindowPosition.PlatformDefault,
    size: DpSize = DpSize.Unspecified
) {
    lateinit var scope: MenuScope
        internal set
    internal lateinit var topState: MenuState
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
        private set(value) = setWindowSize(value)
    var position: WindowPosition
        get() = windowState.position
        private set(value) = setWindowPosition(value)


    fun emitOpen(
        position: WindowPosition = this.position,
        size: DpSize = this.size
    ) {
        emitAction {
            open(focus = window, position = position, size = size)
        }
    }

    fun emitClose() {
        emitAction {
            close(propagate = false, focus = null)
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
        position: WindowPosition = this.position,
        size: DpSize = this.size,
        focus: Window? = window
    ) {
        setWindowPosition(position)
        setWindowSize(size)

        requestDesktopForeground()

        if (!composeTopMostImpl.isVisible) {
            composeTopMostImpl.isVisible = true
        }

        focus?.toFrontRequestFocus()

        closeChildren(null, except = null)

        isVisible = true
    }

    internal fun close(
        propagate: Boolean,
        focus: Window? = null,
        except: MenuState? = null,
    ) {
        focus?.toFrontRequestFocus()

        closeChildren(focus = focus, except)

        if (except != null && (except == this || hasDeepChild(except))) {
            return
        }

        if (composeTopMostImpl.isVisible) {
            composeTopMostImpl.isVisible = false
        }

        isVisible = false
        scope.onClosed?.invoke(propagate)
    }

    internal fun closeChildren(focus: Window? = window, except: MenuState? = null) {
        focus?.toFront()

        children.forEach {
            it.close(propagate = false, focus = window, except)
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
            }.flowOn(Dispatchers.Main)
            .launchIn(coroutineScope)
    }

    private fun setWindowSize(value: DpSize) {
        value.takeIf { it != size }?.also {
            if (value.isSpecified) {
                window.size = Dimension(value.width.value.toInt(), value.height.value.toInt())
            } else {
                with(window) {
                    contentPane.size = getPreferredRootSize().run {
                        Dimension(width.value.toInt(), height.value.toInt())
                    }
                    rootPane.size = contentPane.size
                    pack()
                }
            }
        }
    }

    private fun setWindowPosition(value: WindowPosition) {
        value.takeIf { it != position }?.also {
            if (it.isSpecified) {
                window.location = with(value) {
                    Point(x.value.toInt(), y.value.toInt())
                }
            } else {
                windowState.position = value
            }
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

    private fun Window.toFrontRequestFocus() {
        takeUnless { it.hasFocus() }?.run {
            toFront()
            requestFocus()
        }
    }
}
