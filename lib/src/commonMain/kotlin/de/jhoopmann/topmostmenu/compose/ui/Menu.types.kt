package de.jhoopmann.topmostmenu.compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.jhoopmann.topmostmenu.compose.ui.state.MenuState

class MenuModifiers(
    var menu: Modifier,
    var list: Modifier
)

typealias MenuLayout = @Composable MenuState.(MenuContent) -> Unit
typealias MenuContent = @Composable MenuState.() -> Unit
typealias ClosedEvent = (action: Boolean) -> Unit
typealias MenuInitializedEvent = (MenuState) -> Unit