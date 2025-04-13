package de.jhoopmann.topmostmenu.compose.ui.state

import androidx.compose.runtime.*
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import de.jhoopmann.topmostmenu.compose.ui.item.KeyEventMatcher
import de.jhoopmann.topmostmenu.compose.ui.scope.MenuScope
import de.jhoopmann.topmostmenu.native.Platform
import de.jhoopmann.topmostmenu.native.platform
import de.jhoopmann.topmostwindow.awt.native.ApplicationHelper
import kotlinx.coroutines.channels.Channel
import java.awt.Point

class MenuState(windowState: WindowState) {
    private val processingState: MutableState<Boolean> = mutableStateOf(false)
    var windowState: WindowState by mutableStateOf(windowState)
    var visible: Boolean by mutableStateOf(false)
        private set
    var initialized: Boolean by mutableStateOf(false)
    val children: MutableList<MenuState> = mutableListOf()
    val keyEventListeners: MutableList<KeyEventMatcher> = mutableListOf()
    val eventQueue: Channel<suspend () -> Unit> = Channel(Channel.UNLIMITED)
    lateinit var topState: MenuState
    lateinit var scope: MenuScope
    lateinit var window: ComposeWindow
    val focused: Boolean
        get() = window.isFocused
    val treeInitialized: Boolean
        get() = initialized && children.all { it.treeInitialized }
    val treeFocused: Boolean
        get() = focused || children.any { it.treeFocused }
    var processing: Boolean
        get() = processingState.value || children.any { it.processing }
        set(value) {
            children.forEach { it.processing = value }

            processingState.value = value
        }

    fun treeVisibleInLocation(location: Point): Boolean {
        return visibleInLocation(location) ||
                children.any { it.treeVisibleInLocation(location) }
    }

    fun visibleInLocation(location: Point): Boolean {
        if (visible) {
            return with(windowState.position) {
                val size: DpSize = windowState.size
                val xMax: Float = x.value + size.width.value
                val yMax: Float = y.value + size.height.value

                (location.x >= x.value && location.x <= xMax) &&
                        (location.y >= y.value && location.y <= yMax)
            }
        }

        return false
    }

    fun handleKeyEvent(event: KeyEvent): Boolean {
        return keyEventListeners.any { it.invoke(event) } || children.any {
            it.handleKeyEvent(event)
        }
    }

    suspend fun open(
        position: WindowPosition = windowState.position,
        size: DpSize = windowState.size
    ) {
        withFrameNanos { // set position,size and wait for apply
            windowState = windowState.copy(position = position, size = size)
        }

        if (platform == Platform.MacOS && !ApplicationHelper.instance.isActive()) {
            ApplicationHelper.instance.activate()
        }

        withFrameNanos {
            window.isVisible = true
        }

        synchronized(this) {
            visible = true
        }
    }

    suspend fun close(
        byAction: Boolean = false,
        except: MenuState? = null
    ) {
        closeChildren(except)

        if (except != null && treeHasChild(except)) {
            return
        }

        withFrameNanos {
            window.isVisible = false
        }

        synchronized(this) {
            visible = false
        }

        scope.onClosed?.invoke(byAction)
    }

    suspend fun closeChildren(except: MenuState? = null) {
        children.forEach {
            it.close(false, except)
        }
    }

    private fun treeHasChild(state: MenuState): Boolean {
        return (children.any { it == state } || children.any { it.treeHasChild(state) })
    }
}
