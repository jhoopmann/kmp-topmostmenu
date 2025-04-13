package de.jhoopmann.topmostmenu.compose.ui.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Badge
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.jhoopmann.topmostmenu.compose.ui.state.LocalMenuState
import de.jhoopmann.topmostmenu.compose.ui.state.MenuState
import kotlinx.coroutines.delay

data class KeyBadgeColors(val background: Color, val content: Color)
data class ItemClickEvent(var result: Boolean = true)
typealias ItemOnClick = (ItemClickEvent) -> Unit
typealias KeyTextLayout = @Composable (modifiers: ClickableItemModifiers, text: String, textStyle: TextStyle, colors: KeyBadgeColors) -> Unit
typealias KeyEventMatcher = (KeyEvent) -> Boolean

val DefaultKeyBadgeModifier: Modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, end = 8.dp, start = 0.dp)

open class ClickableItemModifiers(
    var keyBadge: Modifier = DefaultKeyBadgeModifier,
    var keyText: Modifier = Modifier,
    item: Modifier = DefaultItemModifier,
    contents: ItemContentModifiers = ItemContentModifiers()
) : ItemModifiers(item, contents)

val DefaultKeyTextLayout: KeyTextLayout = { modifiers, text, textStyle, colors ->
    Badge(modifier = modifiers.keyBadge, backgroundColor = colors.background, contentColor = colors.content) {
        Text(
            modifier = modifiers.keyText,
            text = text,
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Visible
        )
    }
}

@Composable
fun rememberDefaultKeyTextStyle(): TextStyle = with(LocalTextStyle.current) {
    remember { this.copy(lineHeight = 0.sp, fontSize = this.fontSize.div(1.2f)) }
}

@Composable
fun rememberDefaultKeyBadgeColor(
    background: Color = MaterialTheme.colors.primary,
    content: Color = MaterialTheme.colors.onPrimary
): KeyBadgeColors {
    return KeyBadgeColors(background, content)
}

@Composable
fun ClickableItem(
    keyText: String = "",
    keyTextStyle: TextStyle = rememberDefaultKeyTextStyle(),
    keyBadgeColors: KeyBadgeColors = rememberDefaultKeyBadgeColor(),
    keyTextLayout: KeyTextLayout = DefaultKeyTextLayout,
    keyEventMatcher: KeyEventMatcher? = null,
    modifiers: ClickableItemModifiers = ClickableItemModifiers(),
    onClick: ItemOnClick? = null,
    layout: ItemLayout = DefaultItemLayout
) {
    val menuState: MenuState = LocalMenuState.current!!
    val topState: MenuState = menuState.topState

    val onClickWithClose: () -> Boolean = remember {
        {
            val event = ItemClickEvent()
            onClick!!.invoke(event)

            if (event.result) {
                menuState.takeIf { it.scope.actionAutoClose }?.run {
                    synchronized(topState) {
                        topState.processing = true
                    }

                    topState.eventQueue.trySend {
                        delay(300)
                        menuState.close(true)

                        synchronized(topState) {
                            topState.processing = false
                        }
                    }
                }
            }

            event.result
        }
    }

    remember {
        keyEventMatcher?.run {
            menuState.keyEventListeners.add { event ->
                invoke(event) && onClickWithClose.invoke()
            }
        }
    }

    BaseItem(
        modifiers = modifiers.apply {
            item = onClick?.let { click ->
                item.clickable(
                    enabled = true,
                    onClick = { onClickWithClose.invoke() }
                )
            } ?: item
        }
    ) { contentModifiers, prepend, append, center ->
        layout(
            contentModifiers,
            prepend,
            { it ->
                append {
                    it()

                    if (keyEventMatcher != null) {
                        keyTextLayout(modifiers, keyText, keyTextStyle, keyBadgeColors)
                    }
                }
            },
            center
        )
    }
}