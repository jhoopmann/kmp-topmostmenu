package de.jhoopmann.topmostmenu.compose.ui.scope

inline fun <reified T> ItemScope.listAsOrNull(): T? = tryCast(list)
inline fun <reified T> ItemScope.itemAsOrNull(): T? = tryCast(item)

inline fun <reified T> ItemScope.tryCast(value: Any?): T? {
    return try {
        value as T?
    } catch (e: Exception) {
        null
    }
}
