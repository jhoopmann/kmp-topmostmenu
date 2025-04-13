package de.jhoopmann.topmostmenu.compose.ui.state

import androidx.compose.runtime.*
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import de.jhoopmann.topmostmenu.compose.ui.item.KeyEventMatcher
import de.jhoopmann.topmostmenu.compose.ui.scope.MenuScope
import de.jhoopmann.topmostmenu.native.Platform
import de.jhoopmann.topmostmenu.native.platform
import de.jhoopmann.topmostwindow.awt.native.ApplicationHelper
import kotlinx.coroutines.channels.Channel
import java.awt.Point

fun WindowState.copy(
    position: WindowPosition = when (this.position) {
        is WindowPosition.PlatformDefault -> WindowPosition.PlatformDefault
        is WindowPosition.Aligned -> (this.position as WindowPosition.Aligned).copy()
        is WindowPosition.Absolute -> (this.position as WindowPosition.Absolute).copy()
    },
    placement: WindowPlacement = this.placement,
    size: DpSize = this.size.copy(),
    isMinimized: Boolean = this.isMinimized
): WindowState {
    return WindowState(
        position = position,
        placement = placement,
        size = size,
        isMinimized = isMinimized
    )
}

class MenuState(windowState: WindowState) {
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
    private val processingState: MutableState<Boolean> = mutableStateOf(false)
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

    suspend fun open(windowState: WindowState = this.windowState) {
        withFrameNanos { // set new windowState and wait for apply
            this@MenuState.windowState = windowState
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
        action: Boolean = false,
        exceptChild: MenuState? = null
    ) {
        closeChildren(exceptChild)

        if (exceptChild != null && hasDeepChild(exceptChild)) {
            return
        }

        withFrameNanos {
            window.isVisible = false
        }

        synchronized(this) {
            visible = false
        }

        scope.onClosed?.invoke(action)
    }

    suspend fun closeChildren(except: MenuState? = null) {
        children.forEach {
            it.close(false, except)
        }
    }

    private fun hasDeepChild(state: MenuState): Boolean {
        return (children.firstOrNull { it == state } ?: children.firstOrNull {
            it.hasDeepChild(state)
        }) != null
    }
}

val LocalMenuState: ProvidableCompositionLocal<MenuState?> = compositionLocalOf { null }

@Composable
fun ProvideMenuState(state: MenuState, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalMenuState provides state, content)
}
