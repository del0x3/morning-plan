package com.example.morningplan

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    val prefs = remember { context.getSharedPreferences("morning_plan_prefs", Context.MODE_PRIVATE) }
    val coroutineScope = rememberCoroutineScope()

    var isEnglish by remember { mutableStateOf(prefs.getBoolean("is_english", true)) }

    var wakeUpDone by remember { mutableStateOf(prefs.getBoolean("wake_up", false)) }
    var washDone by remember { mutableStateOf(prefs.getBoolean("wash", false)) }
    var waterDone by remember { mutableStateOf(prefs.getBoolean("water", false)) }
    var walkDone by remember { mutableStateOf(prefs.getBoolean("walk", false)) }

    // State for holding reset button for 3 seconds
    var resetHoldSeconds by remember { mutableIntStateOf(0) }
    var isResetting by remember { mutableStateOf(false) }

    val bgWarm = Color(0xFFFBF4E9)
    val boxYellow = Color(0xFFFFE788)
    val boxBlue = Color(0xFFA6D4FF)
    val boxGreen = Color(0xFFB4EFA6)
    val doneGreen = Color(0xFF8CE67E)
    val outlineDark = Color(0xFF1E1B18)
    val arrowColor = Color(0xFF5A544D)

    var wakeBottom by remember { mutableStateOf(Offset.Zero) }
    var washTop by remember { mutableStateOf(Offset.Zero) }
    var washRight by remember { mutableStateOf(Offset.Zero) }
    var washBottom by remember { mutableStateOf(Offset.Zero) }
    var waterLeft by remember { mutableStateOf(Offset.Zero) }
    var waterBottom by remember { mutableStateOf(Offset.Zero) }
    var walkLeftTop by remember { mutableStateOf(Offset.Zero) }
    var walkRightTop by remember { mutableStateOf(Offset.Zero) }

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
            .padding(horizontal = 20.dp, vertical = 44.dp)
    ) {
        // Top navigation bar: Language and 3s Hold Reset
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEnglish) "EN / ru" else "RU / en",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray,
                modifier = Modifier
                    .clickable {
                        isEnglish = !isEnglish
                        prefs.edit().putBoolean("is_english", isEnglish).apply()
                    }
                    .padding(8.dp)
            )

            // Hold-to-reset 3-second button
            val resetText = when {
                isResetting && resetHoldSeconds > 0 -> if (isEnglish) "Hold (${resetHoldSeconds}s)..." else "Держи (${resetHoldSeconds}с)..."
                else -> if (isEnglish) "Hold 3s to reset" else "Зажми 3с для сброса"
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isResetting) Color(0x22FF4D4F) else Color.Transparent)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isResetting = true
                                resetHoldSeconds = 3
                                var completed = false
                                val job = coroutineScope.launch {
                                    for (i in 3 downTo 1) {
                                        resetHoldSeconds = i
                                        delay(1000)
                                    }
                                    completed = true
                                    wakeUpDone = false
                                    washDone = false
                                    waterDone = false
                                    walkDone = false
                                    prefs.edit().remove("wake_up").remove("wash").remove("water").remove("walk").apply()
                                    isResetting = false
                                    resetHoldSeconds = 0
                                }
                                tryAwaitRelease()
                                job.cancel()
                                isResetting = false
                                resetHoldSeconds = 0
                            }
                        )
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = resetText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isResetting) Color(0xFFD32F2F) else Color.Gray
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main title
            Text(
                text = if (isEnglish) "Morning Routine" else "План на утро",
                fontSize = if (isEnglish) 36.sp else 40.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                color = outlineDark,
                modifier = Modifier.padding(bottom = 34.dp)
            )

            // Diagram box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(460.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // 1. Wake Up -> Wash Up
                    if (wakeBottom != Offset.Zero && washTop != Offset.Zero) {
                        val verticalX = wakeBottom.x
                        drawDiagramArrow(
                            start = Offset(verticalX, wakeBottom.y),
                            end = Offset(verticalX, washTop.y),
                            color = arrowColor
                        )
                    }

                    // 2. Wash Up -> Drink Water
                    if (washRight != Offset.Zero && waterLeft != Offset.Zero) {
                        drawDiagramArrow(
                            start = washRight,
                            end = waterLeft,
                            color = arrowColor
                        )
                    }

                    // 3. Wash Up -> Step Outside
                    if (washBottom != Offset.Zero && walkLeftTop != Offset.Zero) {
                        drawDiagramArrow(
                            start = washBottom,
                            end = walkLeftTop,
                            color = arrowColor
                        )
                    }

                    // 4. Drink Water -> Step Outside
                    if (waterBottom != Offset.Zero && walkRightTop != Offset.Zero) {
                        drawDiagramArrow(
                            start = waterBottom,
                            end = walkRightTop,
                            color = arrowColor
                        )
                    }
                }

                // 1. Wake Up
                DiagramTaskCard(
                    title = if (isEnglish) "Wake Up" else "Проснуться",
                    isDone = wakeUpDone,
                    defaultColor = boxYellow,
                    doneColor = doneGreen,
                    outlineColor = outlineDark,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 18.dp, top = 8.dp)
                        .onGloballyPositioned { coords ->
                            val pos = coords.positionInParent()
                            val size = coords.size
                            wakeBottom = Offset(pos.x + size.width / 2f, pos.y + size.height)
                        },
                    onClick = {
                        if (!wakeUpDone) {
                            wakeUpDone = true
                            prefs.edit().putBoolean("wake_up", true).apply()
                        }
                    }
                )

                // 2. Wash Up
                DiagramTaskCard(
                    title = if (isEnglish) "Wash Up" else "Умыться",
                    isDone = washDone,
                    defaultColor = boxBlue,
                    doneColor = doneGreen,
                    outlineColor = outlineDark,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 18.dp, bottom = 40.dp)
                        .onGloballyPositioned { coords ->
                            val pos = coords.positionInParent()
                            val size = coords.size
                            washTop = Offset(pos.x + size.width / 2f, pos.y)
                            washRight = Offset(pos.x + size.width, pos.y + size.height * 0.45f)
                            washBottom = Offset(pos.x + size.width * 0.8f, pos.y + size.height)
                        },
                    onClick = {
                        if (!washDone) {
                            washDone = true
                            prefs.edit().putBoolean("wash", true).apply()
                        }
                    }
                )

                // Optional text
                Text(
                    text = if (isEnglish) "optional" else "опционально",
                    fontSize = 13.sp,
                    color = arrowColor,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = 10.dp, y = (-72).dp)
                        .rotate(-12f)
                )

                // 3. Drink Water
                DiagramTaskCard(
                    title = if (isEnglish) "Drink\nWater" else "Попить\nводички",
                    isDone = waterDone,
                    defaultColor = boxBlue,
                    doneColor = doneGreen,
                    outlineColor = outlineDark,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp, bottom = 60.dp)
                        .onGloballyPositioned { coords ->
                            val pos = coords.positionInParent()
                            val size = coords.size
                            waterLeft = Offset(pos.x, pos.y + size.height * 0.5f)
                            waterBottom = Offset(pos.x + size.width * 0.35f, pos.y + size.height)
                        },
                    onClick = {
                        if (!waterDone) {
                            waterDone = true
                            prefs.edit().putBoolean("water", true).apply()
                        }
                    }
                )

                // 4. Step Outside (clickable and toggles to checkmark)
                DiagramTaskCard(
                    title = if (isEnglish) "Step\nOutside" else "Выйти\nна улицу",
                    isDone = walkDone,
                    defaultColor = boxGreen,
                    doneColor = doneGreen,
                    outlineColor = outlineDark,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .onGloballyPositioned { coords ->
                            val pos = coords.positionInParent()
                            val size = coords.size
                            walkLeftTop = Offset(pos.x + size.width * 0.35f, pos.y)
                            walkRightTop = Offset(pos.x + size.width * 0.65f, pos.y)
                        },
                    onClick = {
                        if (!walkDone) {
                            walkDone = true
                            prefs.edit().putBoolean("walk", true).apply()
                        }
                    }
                )
            }
        }

        // Bottom hint
        Text(
            text = if (isEnglish) "Swipe up to unlock" else "Свайпните вверх для разблокировки",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .clickable { onDismiss() }
        )
    }
}

@Composable
fun DiagramTaskCard(
    title: String,
    isDone: Boolean,
    defaultColor: Color,
    doneColor: Color,
    outlineColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isDone) doneColor else defaultColor)
            .border(2.6.dp, outlineColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = outlineColor,
                textAlign = TextAlign.Center,
                lineHeight = 23.sp
            )
            if (isDone) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "✓",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = outlineColor
                )
            }
        }
    }
}

fun DrawScope.drawDiagramArrow(
    start: Offset,
    end: Offset,
    color: Color,
    strokeWidth: Float = 5.5f
) {
    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )

    val angle = atan2((end.y - start.y).toDouble(), (end.x - start.x).toDouble())
    val headLength = 24f
    val headAngle = 0.42

    val p1 = Offset(
        (end.x - headLength * cos(angle - headAngle)).toFloat(),
        (end.y - headLength * sin(angle - headAngle)).toFloat()
    )
    val p2 = Offset(
        (end.x - headLength * cos(angle + headAngle)).toFloat(),
        (end.y - headLength * sin(angle + headAngle)).toFloat()
    )

    drawLine(
        color = color,
        start = end,
        end = p1,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = end,
        end = p2,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}
