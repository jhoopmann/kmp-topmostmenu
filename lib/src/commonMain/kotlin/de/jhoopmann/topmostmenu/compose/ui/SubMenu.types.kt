package de.jhoopmann.topmostmenu.compose.ui

import androidx.compose.runtime.Composable
import de.jhoopmann.topmostmenu.compose.ui.item.OnEnter
import de.jhoopmann.topmostmenu.compose.ui.item.OnGloballyPositioned

typealias MenuItemLayout = @Composable (OnGloballyPositioned, OnEnter) -> Unit
