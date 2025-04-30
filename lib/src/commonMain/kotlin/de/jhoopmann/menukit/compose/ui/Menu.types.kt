package de.jhoopmann.menukit.compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.jhoopmann.menukit.compose.ui.state.MenuState

class MenuModifiers(
    var menu: Modifier,
    var list: Modifier
)

typealias MenuLayout = @Composable MenuState.(MenuContent) -> Unit
typealias MenuContent = @Composable MenuState.() -> Unit
typealias ClosedEvent = (propagate: Boolean) -> Unit
typealias MenuInitializedEvent = (MenuState) -> Unit