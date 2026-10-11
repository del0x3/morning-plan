package com.example.morningplan

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class LockscreenActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            LockscreenContent(onDismiss = { dismissLockscreen() })
        }
    }

    override fun onResume() {
        super.onResume()
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun dismissLockscreen() {
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            keyguardManager.requestDismissKeyguard(this, null)
        }
        finish()
    }
}

@Composable
fun LockscreenContent(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val prefs = remember { context.getSharedPreferences("morning_plan_prefs", Context.MODE_PRIVATE) }

    var wakeUpDone by remember { mutableStateOf(prefs.getBoolean("wake_up", false)) }
    var washDone by remember { mutableStateOf(prefs.getBoolean("wash", false)) }
    var waterDone by remember { mutableStateOf(prefs.getBoolean("water", false)) }
    var walkDone by remember { mutableStateOf(prefs.getBoolean("walk", false)) }

    // Authentic Excalidraw font
    val kalamFamily = remember {
        try {
            FontFamily(Font(R.font.kalam, FontWeight.Bold))
        } catch (e: Exception) {
            FontFamily.SansSerif
        }
    }

    // Palette exactly matching the reference Excalidraw artwork
    val bgWarm = Color(0xFFFAF3E8) // Soft warm sketchbook background
    val boxYellow = Color(0xFFFDE483) // Hand-drawn pastel yellow
    val boxBlue = Color(0xFFA5D4FE)   // Hand-drawn sky blue
    val boxGreen = Color(0xFFB1F19F)  // Hand-drawn pastel green
    val doneGreen = Color(0xFF8BE77E) // Vibrant completed green
    val inkDark = Color(0xFF231F20)   // Rich Excalidraw ink stroke
    val arrowColor = Color(0xFF6B655E) // Graphite/ink arrow color

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgWarm)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -25) {
                        onDismiss()
                    }
                }
            }
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main title matching Excalidraw marker handwriting
            Text(
                text = "Morning Routine",
                fontSize = 44.sp,
                fontFamily = kalamFamily,
                fontWeight = FontWeight.Bold,
                color = inkDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 36.dp)
            )

            // Exact mathematical layout for the diagram
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp)
            ) {
                val totalWidth = maxWidth
                val density = LocalDensity.current

                // Card Dimensions
                val card1Width = 140.dp
                val card1Height = 72.dp

                val card2Width = 140.dp
                val card2Height = 74.dp

                val card3Width = 125.dp
                val card3Height = 74.dp

                val card4Width = 148.dp
                val card4Height = 76.dp

                // Exact horizontal positions
                val leftMargin = 18.dp
                val card1X = leftMargin
                val card2X = leftMargin
                val card1CenterX = card1X + card1Width / 2
                val card2CenterX = card2X + card2Width / 2 // Identical X center!

                val rightMargin = 18.dp
                val card3X = totalWidth - rightMargin - card3Width
                val card3CenterX = card3X + card3Width / 2

                // Card 4: PERFECTLY CENTERED between Card 2 and Card 3
                val card4CenterX = (card2CenterX + card3CenterX) / 2
                val card4X = card4CenterX - card4Width / 2

                // Vertical positions
                val card1Y = 0.dp
                val card2Y = 140.dp
                val card3Y = 100.dp // Slightly higher than Card 2, exactly like reference
                val card4Y = 320.dp

                // Precise Arrow anchors in Dp
                // 1. Wake Up -> Wash Up (100% straight vertical)
                val arrow1Start = Offset(
                    with(density) { card1CenterX.toPx() },
                    with(density) { (card1Y + card1Height).toPx() }
                )
                val arrow1End = Offset(
                    with(density) { card2CenterX.toPx() },
                    with(density) { card2Y.toPx() }
                )

                // 2. Wash Up -> Drink Water (Angled upwards)
                val arrow2Start = Offset(
                    with(density) { (card2X + card2Width).toPx() },
                    with(density) { (card2Y + 28.dp).toPx() }
                )
                val arrow2End = Offset(
                    with(density) { card3X.toPx() },
                    with(density) { (card3Y + card3Height - 24.dp).toPx() }
                )

                // 3. Wash Up -> Step Outside (Left diagonal)
                val arrow3Start = Offset(
                    with(density) { (card2X + card2Width - 22.dp).toPx() },
                    with(density) { (card2Y + card2Height).toPx() }
                )
                val arrow3End = Offset(
                    with(density) { (card4CenterX - 32.dp).toPx() },
                    with(density) { card4Y.toPx() }
                )

                // 4. Drink Water -> Step Outside (Right diagonal - 100% symmetric to Left diagonal)
                val arrow4Start = Offset(
                    with(density) { (card3X + 22.dp).toPx() },
                    with(density) { (card3Y + card3Height).toPx() }
                )
                val arrow4End = Offset(
                    with(density) { (card4CenterX + 32.dp).toPx() },
                    with(density) { card4Y.toPx() }
                )

                // Canvas rendering Excalidraw-style sketchy arrows
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawExcalidrawArrow(start = arrow1Start, end = arrow1End, color = arrowColor)
                    drawExcalidrawArrow(start = arrow2Start, end = arrow2End, color = arrowColor)
                    drawExcalidrawArrow(start = arrow3Start, end = arrow3End, color = arrowColor)
                    drawExcalidrawArrow(start = arrow4Start, end = arrow4End, color = arrowColor)
                }

                // "optional" hand-drawn label above Arrow 2
                val optX = (card2X + card2Width + card3X) / 2 - 34.dp
                val optY = (card2Y + card3Y) / 2 - 18.dp
                Text(
                    text = "optional",
                    fontSize = 14.sp,
                    fontFamily = kalamFamily,
                    color = arrowColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .offset(x = optX, y = optY)
                        .rotate(-15f)
                )

                // 1. Wake Up Card
                ExcalidrawCard(
                    title = "Wake Up",
                    isDone = wakeUpDone,
                    defaultColor = boxYellow,
                    doneColor = doneGreen,
                    fontFamily = kalamFamily,
                    inkColor = inkDark,
                    width = card1Width,
                    height = card1Height,
                    modifier = Modifier.offset(x = card1X, y = card1Y),
                    onCompleted = {
                        wakeUpDone = true
                        prefs.edit().putBoolean("wake_up", true).apply()
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    },
                    onReset = {
                        wakeUpDone = false
                        prefs.edit().remove("wake_up").apply()
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    }
                )

                // 2. Wash Up Card
                ExcalidrawCard(
                    title = "Wash Up",
                    isDone = washDone,
                    defaultColor = boxBlue,
                    doneColor = doneGreen,
                    fontFamily = kalamFamily,
                    inkColor = inkDark,
                    width = card2Width,
                    height = card2Height,
                    modifier = Modifier.offset(x = card2X, y = card2Y),
                    onCompleted = {
                        washDone = true
                        prefs.edit().putBoolean("wash", true).apply()
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    },
                    onReset = {
                        washDone = false
                        prefs.edit().remove("wash").apply()
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    }
                )

                // 3. Drink Water Card
                ExcalidrawCard(
                    title = "Drink\nWater",
                    isDone = waterDone,
                    defaultColor = boxBlue,
                    doneColor = doneGreen,
                    fontFamily = kalamFamily,
                    inkColor = inkDark,
                    width = card3Width,
                    height = card3Height,
                    modifier = Modifier.offset(x = card3X, y = card3Y),
                    onCompleted = {
                        waterDone = true
                        prefs.edit().putBoolean("water", true).apply()
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    },
                    onReset = {
                        waterDone = false
                        prefs.edit().remove("water").apply()
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    }
                )

                // 4. Step Outside Card
                ExcalidrawCard(
                    title = "Step\nOutside",
                    isDone = walkDone,
                    defaultColor = boxGreen,
                    doneColor = doneGreen,
                    fontFamily = kalamFamily,
                    inkColor = inkDark,
                    width = card4Width,
                    height = card4Height,
                    modifier = Modifier.offset(x = card4X, y = card4Y),
                    onCompleted = {
                        walkDone = true
                        prefs.edit().putBoolean("walk", true).apply()
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    },
                    onReset = {
                        walkDone = false
                        prefs.edit().remove("walk").apply()
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    }
                )
            }
        }

        // Bottom hint matching hand-drawn minimalist aesthetic
        Text(
            text = "Swipe up to unlock",
            fontSize = 15.sp,
            fontFamily = kalamFamily,
            color = Color(0xFF888279),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .clickable { onDismiss() }
        )
    }
}

@Composable
fun ExcalidrawCard(
    title: String,
    isDone: Boolean,
    defaultColor: Color,
    doneColor: Color,
    fontFamily: FontFamily,
    inkColor: Color,
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
    onCompleted: () -> Unit,
    onReset: () -> Unit
) {
    var isPressing by remember { mutableStateOf(false) }

    // Smooth hold-to-fill animation
    val fillProgress by animateFloatAsState(
        targetValue = if (isDone) 1f else if (isPressing) 1f else 0f,
        animationSpec = if (isPressing && !isDone) {
            tween(durationMillis = 650, easing = LinearEasing)
        } else {
            spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
        },
        label = "fillProgress"
    )

    // Checkmark scale bounce animation
    val checkmarkScale by animateFloatAsState(
        targetValue = if (isDone) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium),
        label = "checkmarkScale"
    )

    LaunchedEffect(fillProgress, isPressing) {
        if (fillProgress >= 0.98f && isPressing && !isDone) {
            isPressing = false
            onCompleted()
        }
    }

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .pointerInput(isDone) {
                detectTapGestures(
                    onPress = {
                        if (!isDone) {
                            isPressing = true
                            tryAwaitRelease()
                            isPressing = false
                        }
                    },
                    onLongPress = {
                        if (isDone) {
                            onReset()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Custom Canvas drawing organic hand-drawn card background and sketch border
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Base organic path
            val cardPath = Path().apply {
                val r = 18f
                // Top edge
                moveTo(r, 0f)
                quadraticBezierTo(w * 0.5f, -1.5f, w - r, 0f)
                quadraticBezierTo(w, 0f, w, r)
                // Right edge
                quadraticBezierTo(w + 1.2f, h * 0.5f, w, h - r)
                quadraticBezierTo(w, h, w - r, h)
                // Bottom edge
                quadraticBezierTo(w * 0.5f, h + 1.5f, r, h)
                quadraticBezierTo(0f, h, 0f, h - r)
                // Left edge
                quadraticBezierTo(-1.2f, h * 0.5f, 0f, r)
                quadraticBezierTo(0f, 0f, r, 0f)
                close()
            }

            // Fill default pastel color
            drawPath(path = cardPath, color = defaultColor)

            // Hold-to-fill animated green progress layer
            if (fillProgress > 0f) {
                drawPath(
                    path = cardPath,
                    color = doneColor.copy(alpha = if (isDone) 1f else fillProgress)
                )
            }

            // Hand-drawn sketch border (Rough.js style double-curved ink stroke)
            drawPath(
                path = cardPath,
                color = inkColor,
                style = Stroke(
                    width = 3.2f.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // Card Text and Checkmark
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontFamily = fontFamily,
                fontWeight = FontWeight.Bold,
                color = inkColor,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            if (checkmarkScale > 0.05f) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "✓",
                    fontSize = 24.sp,
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    color = inkColor,
                    modifier = Modifier.scale(checkmarkScale)
                )
            }
        }
    }
}

/**
 * Draws an authentic Excalidraw hand-drawn arrow with subtle organic curvature
 * and classic hand-drawn barb strokes.
 */
fun DrawScope.drawExcalidrawArrow(
    start: Offset,
    end: Offset,
    color: Color,
    strokeWidthDp: Float = 3.8f
) {
    val strokePx = strokeWidthDp.dp.toPx()

    val dx = end.x - start.x
    val dy = end.y - start.y
    val dist = kotlin.math.sqrt(dx * dx + dy * dy)
    if (dist < 5f) return

    // Offset end slightly so arrow head sits right outside target
    val angle = atan2(dy.toDouble(), dx.toDouble())
    val targetEnd = Offset(
        (end.x - 4f * cos(angle)).toFloat(),
        (end.y - 4f * sin(angle)).toFloat()
    )

    // Hand-drawn arrow shaft with slight organic curve
    val shaftPath = Path().apply {
        moveTo(start.x, start.y)
        // Midpoint slight organic arch
        val midX = (start.x + targetEnd.x) / 2f
        val midY = (start.y + targetEnd.y) / 2f
        val perpX = -sin(angle).toFloat() * 1.5f
        val perpY = cos(angle).toFloat() * 1.5f
        quadraticBezierTo(midX + perpX, midY + perpY, targetEnd.x, targetEnd.y)
    }

    drawPath(
        path = shaftPath,
        color = color,
        style = Stroke(
            width = strokePx,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Hand-drawn arrowhead barbs
    val headLength = 22f
    val headAngle = 0.44 // ~25 degrees

    val p1 = Offset(
        (targetEnd.x - headLength * cos(angle - headAngle)).toFloat(),
        (targetEnd.y - headLength * sin(angle - headAngle)).toFloat()
    )
    val p2 = Offset(
        (targetEnd.x - headLength * cos(angle + headAngle)).toFloat(),
        (targetEnd.y - headLength * sin(angle + headAngle)).toFloat()
    )

    drawLine(
        color = color,
        start = targetEnd,
        end = p1,
        strokeWidth = strokePx,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = targetEnd,
        end = p2,
        strokeWidth = strokePx,
        cap = StrokeCap.Round
    )
}
