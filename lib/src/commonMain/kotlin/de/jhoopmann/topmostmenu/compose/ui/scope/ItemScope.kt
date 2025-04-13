package de.jhoopmann.topmostmenu.compose.ui.scope

import androidx.compose.runtime.*

class ItemScope(var list: Any? = null, var item: Any? = null) {
    inline fun <reified T> listAsOrNull(): T? = tryCast(list)
    inline fun <reified T> itemAsOrNull(): T? = tryCast(item)

    inline fun <reified T> tryCast(value: Any?): T? {
        return try {
            value as T?
        } catch (e: Exception) {
            null
        }
    }
}

val LocalItemScope: ProvidableCompositionLocal<ItemScope?> = compositionLocalOf { null }

@Composable
fun ProvideItemScope(scope: ItemScope?, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalItemScope provides scope, content)
}
