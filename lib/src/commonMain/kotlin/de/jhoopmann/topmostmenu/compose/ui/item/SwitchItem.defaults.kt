package de.jhoopmann.topmostmenu.compose.ui.item

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Switch
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val DefaultSwitchLayout: SwitchLayout = { modifiers, checked ->
    Switch(
        modifier = modifiers.switch,
        checked = checked,
        onCheckedChange = null
    )
}
val DefaultSwitchLayoutModifier: Modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, end = 8.dp, start = 0.dp)

fun defaultSwitchItemModifiers(
    switch: Modifier = DefaultSwitchLayoutModifier,
    icon: Modifier = DefaultIconItemModifier,
    text: Modifier = DefaultTextModifier,
    keyBadge: Modifier = DefaultKeyBadgeModifier,
    keyText: Modifier = DefaultKeyTextModifier,
    item: Modifier = DefaultItemModifier,
    contents: ItemContentModifiers = defaultItemContentModifiers()
): SwitchItemModifiers = SwitchItemModifiers(switch, icon, text, keyBadge, keyText, item, contents)
