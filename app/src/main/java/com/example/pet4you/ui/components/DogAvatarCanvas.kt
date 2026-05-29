package com.example.pet4you.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.pet4you.data.model.*

@Composable
fun DogAvatarCanvas(
    avatar: DogAvatar,
    modifier: Modifier = Modifier,
    animated: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dogAvatar")

    val tailAngle by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue  =  15f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tailWag"
    )

    val blinkProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                0f at 0
                0f at 2700
                1f at 2850
                0f at 3000
            }
        ),
        label = "blink"
    )

    val effectiveTail  = if (animated) tailAngle    else 0f
    val effectiveBlink = if (animated) blinkProgress else 0f

    val fur  = avatar.furColor.toColor()
    val dark = Color(
        red   = (fur.red   * 0.68f).coerceIn(0f, 1f),
        green = (fur.green * 0.68f).coerceIn(0f, 1f),
        blue  = (fur.blue  * 0.68f).coerceIn(0f, 1f)
    )

    Canvas(modifier = modifier) {
        val s  = size.minDimension
        val cx = size.width  / 2f
        val cy = size.height / 2f

        drawEars(avatar.earType, fur, dark, cx, cy, s)
        drawBody(avatar.bodyShape, fur, cx, cy, s)
        drawTail(avatar.tailType, dark, cx, cy, s, effectiveTail)
        drawSnout(fur, dark, cx, cy, s)
        drawEyes(avatar.eyeType, cx, cy, s, effectiveBlink)
        drawAccessory(avatar.accessory, dark, cx, cy, s)
        if (avatar.furColor == FurColor.SPOTTED) drawSpots(dark, cx, cy, s)
    }
}

private fun DrawScope.drawEars(type: EarType, fur: Color, dark: Color, cx: Float, cy: Float, s: Float) {
    when (type) {
        EarType.FLOPPY -> {
            listOf(-1f, 1f).forEach { side ->
                drawCircle(dark, radius = s * 0.17f, center = Offset(cx + side * s * 0.32f, cy + s * 0.10f))
                drawCircle(Color(0xFFFFB3BA), radius = s * 0.10f, center = Offset(cx + side * s * 0.32f, cy + s * 0.10f))
            }
        }
        EarType.POINTY -> {
            listOf(-1f, 1f).forEach { side ->
                val path = Path().apply {
                    moveTo(cx + side * s * 0.22f, cy - s * 0.45f)
                    lineTo(cx + side * s * 0.38f, cy - s * 0.22f)
                    lineTo(cx + side * s * 0.10f, cy - s * 0.22f)
                    close()
                }
                drawPath(path, dark)
                val inner = Path().apply {
                    moveTo(cx + side * s * 0.22f, cy - s * 0.39f)
                    lineTo(cx + side * s * 0.33f, cy - s * 0.25f)
                    lineTo(cx + side * s * 0.13f, cy - s * 0.25f)
                    close()
                }
                drawPath(inner, Color(0xFFFFB3BA))
            }
        }
        EarType.ROUND -> {
            listOf(-1f, 1f).forEach { side ->
                drawCircle(dark, radius = s * 0.14f, center = Offset(cx + side * s * 0.30f, cy - s * 0.30f))
                drawCircle(Color(0xFFFFB3BA), radius = s * 0.08f, center = Offset(cx + side * s * 0.30f, cy - s * 0.30f))
            }
        }
    }
}

private fun DrawScope.drawBody(shape: BodyShape, fur: Color, cx: Float, cy: Float, s: Float) {
    when (shape) {
        BodyShape.ROUND  -> drawCircle(fur, radius = s * 0.38f, center = Offset(cx, cy))
        BodyShape.STOCKY -> drawOval(fur,
            topLeft = Offset(cx - s * 0.43f, cy - s * 0.30f),
            size    = Size(s * 0.86f, s * 0.60f))
        BodyShape.SLIM   -> drawOval(fur,
            topLeft = Offset(cx - s * 0.27f, cy - s * 0.42f),
            size    = Size(s * 0.54f, s * 0.84f))
    }
}

private fun DrawScope.drawTail(type: TailType, dark: Color, cx: Float, cy: Float, s: Float, angle: Float) {
    val pivot = Offset(cx + s * 0.36f, cy + s * 0.10f)
    rotate(angle, pivot = pivot) {
        when (type) {
            TailType.STRAIGHT -> drawOval(dark,
                topLeft = Offset(cx + s * 0.34f, cy + s * 0.02f),
                size    = Size(s * 0.14f, s * 0.30f))
            TailType.CURLY -> {
                val path = Path().apply {
                    moveTo(cx + s * 0.36f, cy + s * 0.10f)
                    cubicTo(
                        cx + s * 0.55f, cy + s * 0.10f,
                        cx + s * 0.60f, cy - s * 0.15f,
                        cx + s * 0.42f, cy - s * 0.22f
                    )
                }
                drawPath(path, dark, style = Stroke(width = s * 0.07f, cap = StrokeCap.Round))
            }
            TailType.STUB -> drawOval(dark,
                topLeft = Offset(cx + s * 0.34f, cy + s * 0.05f),
                size    = Size(s * 0.12f, s * 0.14f))
        }
    }
}

private fun DrawScope.drawSnout(fur: Color, dark: Color, cx: Float, cy: Float, s: Float) {
    val snoutColor = Color(
        red   = (fur.red   * 1.15f).coerceIn(0f, 1f),
        green = (fur.green * 1.15f).coerceIn(0f, 1f),
        blue  = (fur.blue  * 1.15f).coerceIn(0f, 1f)
    )
    drawCircle(snoutColor, radius = s * 0.17f, center = Offset(cx, cy + s * 0.12f))
    drawOval(dark,
        topLeft = Offset(cx - s * 0.045f, cy + s * 0.06f),
        size    = Size(s * 0.09f, s * 0.06f))
    val mouth = Path().apply {
        moveTo(cx - s * 0.06f, cy + s * 0.15f)
        quadraticTo(cx, cy + s * 0.20f, cx + s * 0.06f, cy + s * 0.15f)
    }
    drawPath(mouth, dark, style = Stroke(width = s * 0.025f, cap = StrokeCap.Round))
}

private fun DrawScope.drawEyes(type: EyeType, cx: Float, cy: Float, s: Float, blink: Float) {
    listOf(-1f, 1f).forEach { side ->
        val ex = cx + side * s * 0.13f
        val ey = cy - s * 0.06f
        when (type) {
            EyeType.NORMAL -> {
                drawCircle(Color.White, radius = s * 0.085f, center = Offset(ex, ey))
                drawCircle(Color(0xFF1A1A1A), radius = s * 0.055f * (1f - blink), center = Offset(ex, ey))
            }
            EyeType.BIG -> {
                drawCircle(Color.White, radius = s * 0.110f, center = Offset(ex, ey))
                drawCircle(Color(0xFF1A1A1A), radius = s * 0.075f * (1f - blink), center = Offset(ex, ey))
                drawCircle(Color.White, radius = s * 0.022f, center = Offset(ex + s * 0.03f, ey - s * 0.03f))
            }
            EyeType.SLEEPY -> {
                drawCircle(Color.White, radius = s * 0.085f, center = Offset(ex, ey))
                val eyeH = s * 0.085f * (1f - blink * 0.8f)
                drawOval(Color(0xFF1A1A1A),
                    topLeft = Offset(ex - s * 0.055f, ey),
                    size    = Size(s * 0.11f, eyeH))
            }
        }
    }
}

private fun DrawScope.drawAccessory(acc: Accessory, dark: Color, cx: Float, cy: Float, s: Float) {
    when (acc) {
        Accessory.NONE -> Unit
        Accessory.COLLAR -> drawRect(
            color   = Color(0xFFE53935),
            topLeft = Offset(cx - s * 0.28f, cy + s * 0.22f),
            size    = Size(s * 0.56f, s * 0.08f)
        )
        Accessory.BOW -> {
            listOf(-1f, 1f).forEach { side ->
                val path = Path().apply {
                    moveTo(cx,           cy - s * 0.43f)
                    lineTo(cx + side * s * 0.18f, cy - s * 0.50f)
                    lineTo(cx + side * s * 0.18f, cy - s * 0.36f)
                    close()
                }
                drawPath(path, Color(0xFFE91E63))
            }
            drawCircle(Color(0xFFE91E63), radius = s * 0.04f, center = Offset(cx, cy - s * 0.43f))
        }
        Accessory.HAT -> {
            drawRect(Color(0xFF212121),
                topLeft = Offset(cx - s * 0.22f, cy - s * 0.50f),
                size    = Size(s * 0.44f, s * 0.22f))
            drawOval(Color(0xFF212121),
                topLeft = Offset(cx - s * 0.30f, cy - s * 0.32f),
                size    = Size(s * 0.60f, s * 0.10f))
        }
        Accessory.BANDANA -> {
            val path = Path().apply {
                moveTo(cx - s * 0.20f, cy + s * 0.18f)
                lineTo(cx + s * 0.20f, cy + s * 0.18f)
                lineTo(cx,             cy + s * 0.32f)
                close()
            }
            drawPath(path, Color(0xFF1565C0))
        }
    }
}

private fun DrawScope.drawSpots(dark: Color, cx: Float, cy: Float, s: Float) {
    val spotColor = dark.copy(alpha = 0.35f)
    drawCircle(spotColor, radius = s * 0.07f, center = Offset(cx - s * 0.18f, cy - s * 0.15f))
    drawCircle(spotColor, radius = s * 0.05f, center = Offset(cx + s * 0.20f, cy - s * 0.05f))
    drawCircle(spotColor, radius = s * 0.06f, center = Offset(cx + s * 0.05f, cy + s * 0.20f))
    drawCircle(spotColor, radius = s * 0.04f, center = Offset(cx - s * 0.22f, cy + s * 0.12f))
}
