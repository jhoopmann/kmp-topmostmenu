package de.jhoopmann.topmostmenu.compose.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import de.jhoopmann.topmostmenu.compose.ui.scope.ItemScope
import de.jhoopmann.topmostmenu.compose.ui.scope.ProvideItemScope
import de.jhoopmann.topmostmenu.compose.ui.state.MenuState

val DefaultMenuShape: Shape = RoundedCornerShape(10.dp)
val DefaultLayoutPadding: PaddingValues = PaddingValues(8.dp)
val DefaultListModifier: Modifier = Modifier.width(IntrinsicSize.Max).wrapContentHeight()
val DefaultSize: DpSize = DpSize.Unspecified
val DefaultPosition: WindowPosition = WindowPosition.PlatformDefault
val DefaultMenuWindowLayout: MenuLayout = { state, content ->
    with(state.scope) {
        Surface(shape = shape, modifier = Modifier.padding(layoutPadding).then(modifiers.menu)) {
            Column(modifier = modifiers.list) {
                ProvideItemScope(ItemScope(this, null)) {
                    content()
                }
            }
        }
    }
}

fun defaultMenuModifier(shape: Shape): Modifier = Modifier.wrapContentSize().shadow(4.dp, shape = shape)
fun defaultMenuModifiers(shape: Shape): MenuModifiers = MenuModifiers(defaultMenuModifier(shape), DefaultListModifier)

@Composable
fun rememberDefaultMenuState(
    position: WindowPosition = DefaultPosition,
    size: DpSize = DefaultSize
): MenuState = remember {
    MenuState(position, size)
}
