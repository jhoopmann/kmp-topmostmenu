package de.jhoopmann.topmostmenu.compose.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.window.WindowDecoration
import androidx.compose.ui.window.WindowPosition
import de.jhoopmann.topmostmenu.compose.ui.scope.MenuScope
import de.jhoopmann.topmostmenu.compose.ui.state.MenuState
import de.jhoopmann.topmostwindow.awt.ui.DefaultPlatformAfterInitialization
import de.jhoopmann.topmostwindow.awt.ui.DefaultPlatformBeforeInitialization
import de.jhoopmann.topmostwindow.awt.ui.TopMostOptions
import de.jhoopmann.topmostwindow.compose.ui.awt.ComposeTopMostDialog
import de.jhoopmann.topmostwindow.compose.ui.util.ComposeDialogHelper
import de.jhoopmann.topmostwindow.compose.ui.window.TopMostDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import java.awt.Dialog
import java.awt.Window

@Composable
fun Menu(
    state: MenuState,
    shape: Shape = DefaultMenuShape,
    modifiers: MenuModifiers = defaultMenuModifiers(shape),
    actionAutoClose: Boolean = true,
    onClosed: ClosedEvent? = null,
    onInitialized: MenuInitializedEvent? = null,
    layoutPadding: PaddingValues = DefaultLayoutPadding,
    layout: MenuLayout = { defaultMenuLayout(it) },
    content: MenuContent
) = Menu(
    shape,
    modifiers,
    actionAutoClose,
    onClosed,
    onInitialized,
    layoutPadding,
    layout,
    content,
    parentState = null,
    state,
)

@Composable
fun MenuState.Menu(
    state: MenuState,
    shape: Shape = DefaultMenuShape,
    modifiers: MenuModifiers = defaultMenuModifiers(shape),
    actionAutoClose: Boolean = true,
    onClosed: ClosedEvent? = null,
    onInitialized: MenuInitializedEvent? = null,
    layoutPadding: PaddingValues = DefaultLayoutPadding,
    layout: MenuLayout = { defaultMenuLayout(it) },
    content: MenuContent
) = Menu(
    shape,
    modifiers,
    actionAutoClose,
    onClosed,
    onInitialized,
    layoutPadding,
    layout,
    content,
    parentState = this,
    state,
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Menu(
    shape: Shape = DefaultMenuShape,
    modifiers: MenuModifiers,
    actionAutoClose: Boolean,
    onClosed: ClosedEvent?,
    onInitialized: MenuInitializedEvent?,
    layoutPadding: PaddingValues,
    layout: MenuLayout,
    content: MenuContent,
    parentState: MenuState?,
    state: MenuState
) {
    val localWindow: Window? = remember { ComposeDialogHelper.getLocalWindow() }.current

    remember {
        state.parentWindow = localWindow
        state.topState = parentState?.topState ?: state
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

    val topMostOptions: TopMostOptions = remember {
        TopMostOptions(
            topMost = state.topState.parentWindow == null,
            sticky = state.topState.parentWindow == null,
            skipTaskbar = true
        )
    }

    TopMostDialog(
        state = state.windowState,
        visible = false,
        topMost = topMostOptions.topMost,
        sticky = topMostOptions.sticky,
        skipTaskbar = topMostOptions.skipTaskbar,
        resizable = false,
        focusable = true,
        transparent = true,
        decoration = WindowDecoration.Undecorated(),
        owner = state.topState.parentWindow,
        modalityType = Dialog.ModalityType.MODELESS,
        onPreviewKeyEvent = {
            state.handleKeyEvent(it)
        },
        onCreate = {
            state.composeTopMostImpl = it as ComposeTopMostDialog
        },
        beforeInitialization = {
            if (parentState == null) {
                DefaultPlatformBeforeInitialization.invoke(this)
            }
        },
        afterInitialization = {
            state.initialized = true

            if (state.topState.initializedAll) {
                DefaultPlatformAfterInitialization.invoke(this)
            }

            onInitialized?.invoke(state)
        },
        onCloseRequest = {
            state.emitAction {
                if (state != state.topState) {
                    state.parentWindow?.toFront()
                    state.parentWindow?.requestFocus()
                }

                state.close()
            }
        },
        content = {
            layout.invoke(state, content)
        }
    )

    parentState?.apply {
        DisposableEffect(Unit) {
            children.add(state)

            onDispose {
                children.remove(state)
            }
        }
    } ?: run {
        val coroutineScope: CoroutineScope = rememberCoroutineScope()
        var eventQueueJob: Job? by remember { mutableStateOf(null) }
        val focusEventListener: FocusEventListener = remember { FocusEventListener(state) }

        DisposableEffect(state.initializedAll) {
            if (state.initializedAll) {
                focusEventListener.register()
                eventQueueJob = state.launchActionCoroutine(coroutineScope)
            }

            onDispose {
                if (eventQueueJob != null) {
                    eventQueueJob?.cancel()
                    eventQueueJob = null
                    focusEventListener.unregister()
                }
            }
        }
    }
}
