package de.jhoopmann.topmostmenu.compose.ui.state

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import de.jhoopmann.topmostmenu.compose.ui.item.KeyEventMatcher
import de.jhoopmann.topmostmenu.compose.ui.scope.MenuScope
import de.jhoopmann.topmostmenu.native.Platform
import de.jhoopmann.topmostmenu.native.platform
import de.jhoopmann.topmostwindow.awt.native.ApplicationHelper
import de.jhoopmann.topmostwindow.compose.ui.awt.ComposeTopMostWindow
import kotlinx.coroutines.channels.Channel
import java.awt.Dimension
import java.awt.Point

class MenuState(
    position: WindowPosition = WindowPosition.PlatformDefault,
    size: DpSize = DpSize.Unspecified,
) {
    lateinit var topState: MenuState
    lateinit var scope: MenuScope
    lateinit var composeWindow: ComposeWindow
    lateinit var composeTopMostWindow: ComposeTopMostWindow
    internal val windowState: WindowState = WindowState(
        position = position,
        size = size,
        placement = WindowPlacement.Floating
    )
    internal val eventQueue: Channel<suspend () -> Unit> = Channel(Channel.UNLIMITED)
    internal val keyEventListeners: MutableList<KeyEventMatcher> = mutableListOf()
    internal val children: MutableList<MenuState> = mutableListOf()
    private val processingState: MutableState<Boolean> = mutableStateOf(false)
    internal var processing: Boolean
        get() = processingState.value || children.any { it.processing }
        set(value) {
            children.forEach { it.processing = value }

            processingState.value = value
        }
    internal val anyFocused: Boolean
        get() = composeWindow.isFocused || children.any { it.anyFocused }

    var size: DpSize
        get() = windowState.size
        set(value) {
            if (value.isSpecified && value != this.size) {
                composeWindow.size = Dimension(value.width.value.toInt(), value.height.value.toInt())
            } else if (value.isUnspecified) {
                composeWindow.contentPane.size = getPreferredRootSize().run {
                    Dimension(width.value.toInt(), height.value.toInt())
                }
                composeWindow.rootPane.size = composeWindow.contentPane.size
                composeWindow.pack()
            }
        }
    var position: WindowPosition
        get() = windowState.position
        set(value) {
            if (value.isSpecified && value != this.position) {
                composeWindow.location = with(value) {
                    Point(x.value.toInt(), y.value.toInt())
                }
            }
        }
    var initialized: Boolean by mutableStateOf(false)
        internal set
    val allInitialized: Boolean
        get() = initialized && children.all { it.allInitialized }
    var visible: Boolean by mutableStateOf(false)
        private set

    fun queuedOpen(
        position: WindowPosition = this.position,
        size: DpSize = this.size
    ) {
        eventQueue.trySend {
            open(position, size)
        }
    }

    fun queuedClose(
        byAction: Boolean = false,
        except: MenuState? = null
    ) {
        eventQueue.trySend {
            close(byAction, except)
        }
    }

    fun handleKeyEvent(event: KeyEvent): Boolean {
        return keyEventListeners.any { it.invoke(event) } || children.any {
            it.handleKeyEvent(event)
        }
    }

    internal fun anyVisibleInLocation(location: Point): Boolean {
        return visibleInLocation(location) ||
                children.any { it.anyVisibleInLocation(location) }
    }

    internal fun visibleInLocation(location: Point): Boolean {
        return visible && with(position) {
            val size: DpSize = windowState.size
            val xMax: Float = x.value + size.width.value
            val yMax: Float = y.value + size.height.value

            (location.x >= x.value && location.x <= xMax) &&
                    (location.y >= y.value && location.y <= yMax)
        }
    }

    internal fun open(
        position: WindowPosition = this.position,
        size: DpSize = this.size
    ) {
        this.size = size
        this.position = position

        if (platform == Platform.MacOS && !ApplicationHelper.instance.isActive()) {
            ApplicationHelper.instance.activate()
        }

        composeTopMostWindow.setVisible(true)

        synchronized(this) {
            visible = true
        }
    }

    internal fun close(
        byAction: Boolean = false,
        except: MenuState? = null
    ) {
        closeChildren(except)

        if (except != null && hasDeepChild(except)) {
            return
        }

        composeTopMostWindow.setVisible(false)

        synchronized(this) {
            visible = false
        }

        scope.onClosed?.invoke(byAction)
    }

    internal fun closeChildren(except: MenuState? = null) {
        children.forEach {
            it.close(false, except)
        }
    }

    private fun hasDeepChild(state: MenuState): Boolean {
        return (children.any { it == state } || children.any { it.hasDeepChild(state) })
    }
}
