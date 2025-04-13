package de.jhoopmann.topmostmenu.compose.ui.scope

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
