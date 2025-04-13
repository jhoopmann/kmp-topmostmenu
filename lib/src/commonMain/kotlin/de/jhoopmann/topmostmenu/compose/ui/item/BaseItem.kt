package de.jhoopmann.topmostmenu.compose.ui.item

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import de.jhoopmann.topmostmenu.compose.ui.scope.LocalLayoutScope
import de.jhoopmann.topmostmenu.compose.ui.scope.ProvideLayoutScope
import de.jhoopmann.topmostmenu.compose.ui.state.LocalMenuState
import de.jhoopmann.topmostmenu.compose.ui.state.MenuState

typealias Content = @Composable () -> Unit
typealias PrependContent = @Composable (Content) -> Unit
typealias AppendContent = @Composable (Content) -> Unit
typealias CenterContent = @Composable (Content) -> Unit
typealias ItemLayout = @Composable (ItemModifiers, PrependContent, AppendContent, CenterContent) -> Unit

val DefaultItemModifier: Modifier = Modifier.height(IntrinsicSize.Max).fillMaxWidth()

val DefaultItemContentPrependModifier: Modifier = Modifier.fillMaxHeight()
val DefaultItemContentAppendModifier: Modifier = Modifier.fillMaxHeight()
val DefaultItemContentCenterModifier: Modifier = Modifier.fillMaxHeight()

open class ItemContentModifiers(
    var prepend: Modifier = DefaultItemContentPrependModifier,
    var append: Modifier = DefaultItemContentAppendModifier,
    var center: Modifier = DefaultItemContentCenterModifier
)

open class ItemModifiers(
    var item: Modifier = DefaultItemModifier,
    var contents: ItemContentModifiers = ItemContentModifiers()
)

val DefaultItemLayout: ItemLayout = { modifiers, prepend, append, center ->
    Row(modifier = modifiers.item) {
        Row(
            modifier = modifiers.contents.prepend.wrapContentWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            ProvideLayoutScope(LocalLayoutScope.current?.apply { item = this@Row }) {
                prepend {}
            }
        }

        Row(
            modifier = modifiers.contents.center.wrapContentWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            ProvideLayoutScope(LocalLayoutScope.current?.apply { item = this@Row }) {
                center {}
            }
        }

        Row(
            modifier = modifiers.contents.append.weight(1.0f, fill = true),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            ProvideLayoutScope(LocalLayoutScope.current?.apply { item = this@Row }) {
                append {}
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BaseItem(
    modifiers: ItemModifiers = ItemModifiers(),
    layout: ItemLayout = DefaultItemLayout
) {
    val menuState: MenuState = LocalMenuState.current!!
    val topState: MenuState = menuState.topState

    layout(
        modifiers.apply {
            item = Modifier.onPointerEvent(PointerEventType.Enter, onEvent = { event ->
                if (!event.changes.first().isConsumed) {
                    topState.eventQueue.trySend {
                        if (synchronized(topState) {
                                if (!menuState.processing) {
                                    menuState.processing = true
                                    true
                                } else false
                            }
                        ) {
                            menuState.closeChildren()

                            synchronized(topState) {
                                menuState.processing = false
                            }
                        }
                    }
                }
            }).then(item)
        },
        { it() },
        { it() }
    ) { it() }
}