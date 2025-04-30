package de.jhoopmann.menukit.compose.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.window.WindowDecoration
import androidx.compose.ui.window.WindowPosition
import de.jhoopmann.menukit.compose.ui.scope.MenuScope
import de.jhoopmann.menukit.compose.ui.state.MenuState
import de.jhoopmann.stickywindow.awt.ui.DefaultPlatformAfterInitialization
import de.jhoopmann.stickywindow.awt.ui.DefaultPlatformBeforeInitialization
import de.jhoopmann.stickywindow.awt.ui.TopMostOptions
import de.jhoopmann.stickywindow.compose.ui.awt.ComposeTopMostDialog
import de.jhoopmann.stickywindow.compose.ui.util.ComposeDialogHelper
import de.jhoopmann.stickywindow.compose.ui.window.TopMostDialog
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
    remember {
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

    val owner: Window? = ComposeDialogHelper.getLocalWindow().current.takeIf { parentState == null }
        ?: parentState?.topState?.window?.owner
    val topMostOptions: TopMostOptions = remember {
        TopMostOptions(
            topMost = owner == null,
            sticky = owner == null,
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
        owner = owner,
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
                state.close(propagate = false, focus = parentState?.window ?: state.window.owner)
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
        var actionsJob: Job? by remember { mutableStateOf(null) }
        var focusEventsJob: Job? by remember { mutableStateOf(null) }
        val focusEventListener: FocusEventListener = remember { FocusEventListener() }

        DisposableEffect(state.initializedAll, state) {
            if (state.initializedAll) {
                focusEventListener.register()
                focusEventsJob = focusEventListener.launchEventsCoroutine(state, coroutineScope)
                actionsJob = state.launchActionCoroutine(coroutineScope)
            }

            onDispose {
                if (actionsJob != null) {
                    focusEventListener.unregister()
                    actionsJob?.cancel()
                    focusEventsJob?.cancel()
                    actionsJob = null
                }
            }
        }
    }
}
