package de.jhoopmann.topmostmenu.compose.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.window.WindowDecoration
import androidx.compose.ui.window.WindowPosition
import de.jhoopmann.topmostmenu.compose.ui.scope.MenuScope
import de.jhoopmann.topmostmenu.compose.ui.state.LocalMenuState
import de.jhoopmann.topmostmenu.compose.ui.state.MenuState
import de.jhoopmann.topmostmenu.compose.ui.state.ProvideMenuState
import de.jhoopmann.topmostwindow.awt.ui.TopMostImpl
import de.jhoopmann.topmostwindow.awt.ui.TopMostOptions
import de.jhoopmann.topmostwindow.compose.ui.window.TopMostWindow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.internal.synchronized
import kotlinx.coroutines.launch

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
    val topMostOptions: TopMostOptions = remember { TopMostOptions() }

    state.topState = topState

    remember {
        parentState?.children?.add(state)

        state.scope = MenuScope(
            initialPosition = when (val position: WindowPosition = state.position) {
                is WindowPosition.PlatformDefault -> position
                is WindowPosition.Absolute -> position.copy()
                is WindowPosition.Aligned -> position.copy()
            },
            initialSize = state.size.copy(),
            shape = shape,
            modifiers = modifiers,
            actionAutoClose = actionAutoClose,
            onClosed = onClosed,
            layoutPadding = layoutPadding,
            layout = layout
        )
    }

    TopMostWindow(
        state = state.windowState,
        visible = false,
        topMost = true,
        sticky = true,
        skipTaskbar = true,
        resizable = false,
        focusable = true,
        transparent = true,
        decoration = WindowDecoration.Undecorated(),
        onPreviewKeyEvent = { state.handleKeyEvent(it) },
        onCreate = {
            state.composeWindow = it.composeWindow
            state.composeTopMostWindow = it
        },
        onBeforeInitialization = { _, _ ->
            if (parentState == null) {
                TopMostImpl.setPlatformOptionsBeforeInit(topMostOptions)
            }
        },
        onAfterInitialization = { _, _ ->
            if (parentState == null && state.allInitialized) {
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
        ProvideMenuState(state = state) {
            layout(state, content)
        }
    }

    if (parentState == null) {
        val focusEventListener: FocusEventListener = remember { FocusEventListener(state) }
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
