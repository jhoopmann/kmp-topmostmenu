package de.jhoopmann.menukit.compose.ui

import androidx.compose.runtime.Composable
import de.jhoopmann.menukit.compose.ui.item.OnEnter
import de.jhoopmann.menukit.compose.ui.item.OnGloballyPositioned

typealias MenuItemLayout = @Composable (OnGloballyPositioned, OnEnter) -> Unit
