package de.jhoopmann.topmostmenu.compose.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowDecoration
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import de.jhoopmann.topmostmenu.compose.ui.scope.LayoutScope
import de.jhoopmann.topmostmenu.compose.ui.scope.MenuScope
import de.jhoopmann.topmostmenu.compose.ui.scope.ProvideLayoutScope
import de.jhoopmann.topmostmenu.compose.ui.state.LocalMenuState
import de.jhoopmann.topmostmenu.compose.ui.state.MenuState
import de.jhoopmann.topmostmenu.compose.ui.state.ProvideMenuState
import de.jhoopmann.topmostmenu.compose.ui.state.copy
import de.jhoopmann.topmostwindow.awt.ui.TopMostImpl
import de.jhoopmann.topmostwindow.awt.ui.TopMostOptions
import de.jhoopmann.topmostwindow.compose.ui.window.TopMostWindow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.internal.synchronized
import kotlinx.coroutines.launch
import java.awt.AWTEvent
import java.awt.MouseInfo
import java.awt.Toolkit
import java.awt.event.AWTEventListener
import java.awt.event.FocusEvent

class MenuModifiers(
    var menu: Modifier,
    var list: Modifier
)

typealias MenuLayout = @Composable (MenuState, MenuContent) -> Unit
typealias MenuContent = @Composable () -> Unit
typealias OnClosedEvent = (action: Boolean) -> Unit

val DefaultMenuShape: Shape = RoundedCornerShape(10.dp)
val DefaultLayoutPadding: PaddingValues = PaddingValues(8.dp)

fun defaultMenuModifier(shape: Shape): Modifier {
    return Modifier.wrapContentSize().shadow(4.dp, shape = shape)
}

fun defaultListModifier(): Modifier {
    return Modifier.width(IntrinsicSize.Max).wrapContentHeight()
}

fun defaultMenuModifiers(shape: Shape): MenuModifiers {
    return MenuModifiers(
        menu = defaultMenuModifier(shape),
        list = defaultListModifier()
    )
}

fun defaultWindowState(
    size: DpSize = DpSize.Unspecified,
    position: WindowPosition = WindowPosition.PlatformDefault,
): WindowState {
    return WindowState(size = size, position = position, placement = WindowPlacement.Floating)
}

@Composable
fun rememberDefaultMenuState(
    windowState: WindowState = defaultWindowState()
): MenuState = remember {
    MenuState(windowState = windowState)
}

val DefaultMenuWindowLayout: MenuLayout = { state, content ->
    with(state.scope) {
        Surface(shape = shape, modifier = Modifier.padding(layoutPadding).then(modifiers.menu)) {
            Column(modifier = modifiers.list) {
                ProvideLayoutScope(LayoutScope(this, null)) {
                    content()
                }
            }
        }
    }
}

private class FocusEventListener(
    private val topState: MenuState
) : AWTEventListener {
    @OptIn(InternalCoroutinesApi::class)
    override fun eventDispatched(event: AWTEvent?) {
        if (event is FocusEvent && event.id == FocusEvent.FOCUS_LOST) {
            if (!topState.treeVisibleInLocation(MouseInfo.getPointerInfo().location)) {
                if (synchronized(topState) {
                        if (!topState.processing) {
                            topState.processing = true
                            true
                        } else false
                    }
                ) {
                    topState.eventQueue.trySend {
                        if (!topState.treeFocused) {
                            topState.close(false)
                        }

                        synchronized(topState) {
                            topState.processing = false
                        }
                    }
                }
            }
        }
    }

    fun register() {
        Toolkit.getDefaultToolkit().addAWTEventListener(
            this, AWTEvent.FOCUS_EVENT_MASK or AWTEvent.WINDOW_EVENT_MASK
        )
    }

    fun unregister() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this)
    }
}

@OptIn(ExperimentalComposeUiApi::class, InternalCoroutinesApi::class)
@Composable
fun Menu(
    state: MenuState,
    shape: Shape = DefaultMenuShape,
    modifiers: MenuModifiers = defaultMenuModifiers(shape),
    actionAutoClose: Boolean = true,
    onClosed: OnClosedEvent? = null,
    layoutPadding: PaddingValues = DefaultLayoutPadding,
    layout: MenuLayout = DefaultMenuWindowLayout,
    content: MenuContent
) {
    val coroutineScope: CoroutineScope = rememberCoroutineScope()
    val parentState: MenuState? = LocalMenuState.current
    val topState: MenuState = parentState?.topState ?: state
    val topMostOptions = remember { TopMostOptions() }

    state.topState = topState

    remember {
        parentState?.children?.add(state)

        state.scope = MenuScope(
            initialWindowState = state.windowState.copy(),
            shape = shape,
            modifiers = modifiers,
            actionAutoClose = actionAutoClose,
            onClosed = onClosed,
            layoutPadding = layoutPadding,
            layout = layout
        )
    }

    TopMostWindow(
        visible = false,
        state = state.windowState,
        decoration = WindowDecoration.Undecorated(),
        transparent = true,
        topMost = true,
        sticky = true,
        skipTaskbar = true,
        resizable = false,
        focusable = true,
        onPreviewKeyEvent = { state.handleKeyEvent(it) },
        beforeInitialization = { _, _ ->
            if (parentState == null) {
                TopMostImpl.setPlatformOptionsBeforeInit(topMostOptions)
            }
        },
        afterInitialization = { _, _ ->
            if (parentState == null && state.treeInitialized) {
                TopMostImpl.setPlatformOptionsAfterInit(topMostOptions)
            }

            state.initialized = true
        },
        onCloseRequest = {
            synchronized(topState) {
                state.processing = true
            }

            topState.eventQueue.trySend {
                state.close()

                synchronized(topState) {
                    state.processing = false
                }
            }
        }
    ) {
        state.window = window

        ProvideMenuState(state = state) {
            layout(state, content)
        }
    }

    if (parentState == null) {
        val focusEventListener: FocusEventListener = remember {
            FocusEventListener(state)
        }
        var eventQueueJob: Job? by remember { mutableStateOf(null) }

        DisposableEffect(Unit) {
            eventQueueJob = coroutineScope.launch {
                for (block in state.eventQueue) {
                    block()
                }
            }
            focusEventListener.register()

            onDispose {
                focusEventListener.unregister()
                eventQueueJob?.cancel()
            }
        }
    }
}
