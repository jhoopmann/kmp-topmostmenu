package de.jhoopmann.menukit.compose.ui.scope

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

val LocalItemScope: ProvidableCompositionLocal<ItemScope?> = compositionLocalOf { null }

@Composable
fun ProvideItemScope(scope: ItemScope?, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalItemScope provides scope, content)
}
