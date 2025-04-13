package de.jhoopmann.topmostmenu.compose.ui.scope

import androidx.compose.runtime.*

class LayoutScope(var list: Any? = null, var item: Any? = null) {
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

val LocalLayoutScope: ProvidableCompositionLocal<LayoutScope?> = compositionLocalOf { null }

@Composable
fun ProvideLayoutScope(scope: LayoutScope?, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutScope provides scope, content)
}
