package de.jhoopmann.topmostmenu.compose.ui.item

import androidx.annotation.FloatRange
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultBlendMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.jhoopmann.topmostmenu.compose.ui.state.MenuState

@Composable
fun  MenuState.Separator(
    color: Color = Color.LightGray,
    width: Dp = 2.dp,
    cap: StrokeCap = Stroke.DefaultCap,
    pathEffect: PathEffect? = null,
    @FloatRange(from = 0.0, to = 1.0) alpha: Float = 1.0f,
    colorFilter: ColorFilter? = null,
    blendMode: BlendMode = DefaultBlendMode,
    modifier: Modifier = DefaultSeparatorModifier
) {
    Box(modifier = modifier.drawBehind {
        drawLine(
            color = color,
            start = Offset(0.0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = width.value,
            cap = cap,
            pathEffect = pathEffect,
            alpha = alpha,
            colorFilter = colorFilter,
            blendMode = blendMode
        )
    })
}

