package de.jhoopmann.topmostmenu.compose.ui.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

open class ItemContentModifiers(
    var prepend: Modifier,
    var append: Modifier,
    var center: Modifier
)

open class ItemModifiers(
    var item: Modifier,
    var contents: ItemContentModifiers
)

typealias Content = @Composable () -> Unit
typealias PrependContent = @Composable (Content) -> Unit
typealias AppendContent = @Composable (Content) -> Unit
typealias CenterContent = @Composable (Content) -> Unit
typealias ItemLayout = @Composable (ItemModifiers, PrependContent, AppendContent, CenterContent) -> Unit
