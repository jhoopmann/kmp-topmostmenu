package de.jhoopmann.topmostmenu.compose.ui.item

import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.jhoopmann.topmostmenu.compose.ui.scope.LocalItemScope
import de.jhoopmann.topmostmenu.compose.ui.scope.ProvideItemScope

val DefaultItemModifier: Modifier = Modifier.height(IntrinsicSize.Max).fillMaxWidth()
val DefaultItemContentPrependModifier: Modifier = Modifier.fillMaxHeight()
val DefaultItemContentAppendModifier: Modifier = Modifier.fillMaxHeight()
val DefaultItemContentCenterModifier: Modifier = Modifier.fillMaxHeight()
val DefaultItemLayout: ItemLayout = { modifiers, prepend, append, center ->
    Row(modifier = modifiers.item) {
        Row(
            modifier = modifiers.contents.prepend.wrapContentWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            ProvideItemScope(LocalItemScope.current?.apply { item = this }) {
                prepend {}
            }
        }

        Row(
            modifier = modifiers.contents.center.wrapContentWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            ProvideItemScope(LocalItemScope.current?.apply { item = this }) {
                center {}
            }
        }

        Row(
            modifier = modifiers.contents.append.weight(1.0f, fill = true),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            ProvideItemScope(LocalItemScope.current?.apply { item = this }) {
                append {}
            }
        }
    }
}

fun defaultItemContentModifiers(
    prepend: Modifier = DefaultItemContentPrependModifier,
    append: Modifier = DefaultItemContentAppendModifier,
    center: Modifier = DefaultItemContentCenterModifier
): ItemContentModifiers = ItemContentModifiers(prepend, append, center)

fun defaultItemModifiers(
    item: Modifier = DefaultItemModifier,
    contents: ItemContentModifiers = defaultItemContentModifiers()
): ItemModifiers = ItemModifiers(item, contents)
