package de.jhoopmann.topmostmenu.compose.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

val LocalMenuState: ProvidableCompositionLocal<MenuState?> = compositionLocalOf { null }

@Composable
fun ProvideMenuState(state: MenuState, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalMenuState provides state, content)
}
