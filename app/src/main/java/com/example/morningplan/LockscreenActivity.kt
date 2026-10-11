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
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        setContent {
            LockscreenContent(onDismiss = { dismissLockscreen() })
        }
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

    var wakeUpDone by remember { mutableStateOf(prefs.getBoolean("wake_up", false)) }
    var washDone by remember { mutableStateOf(prefs.getBoolean("wash", false)) }
    var waterDone by remember { mutableStateOf(prefs.getBoolean("water", false)) }
    var walkDone by remember { mutableStateOf(prefs.getBoolean("walk", false)) }

    // Палитра по референсу Excalidraw / Gemini
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
            .padding(horizontal = 20.dp, vertical = 40.dp)
    ) {
        // Кнопка сброса отметок
        Text(
            text = "Сброс",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clickable {
                    wakeUpDone = false
                    washDone = false
                    waterDone = false
                    walkDone = false
                    prefs.edit().clear().apply()
                }
                .padding(8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Заголовок в стиле рукописного скетча
            Text(
                text = "План на утро",
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                color = outlineDark,
                modifier = Modifier.padding(bottom = 36.dp)
            )

            // Контейнер диаграммы
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(460.dp)
            ) {
                // Холст со стрелками
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // 1. Стрелка Проснуться -> Умыться (вертикально вниз)
                    if (wakeBottom != Offset.Zero && washTop != Offset.Zero) {
                        val verticalX = wakeBottom.x
                        drawDiagramArrow(
                            start = Offset(verticalX, wakeBottom.y),
                            end = Offset(verticalX, washTop.y),
                            color = arrowColor
                        )
                    }

                    // 2. Стрелка Умыться -> Попить водички (диагональ)
                    if (washRight != Offset.Zero && waterLeft != Offset.Zero) {
                        drawDiagramArrow(
                            start = washRight,
                            end = waterLeft,
                            color = arrowColor
                        )
                    }

                    // 3. Стрелка Умыться -> Выйти на улицу
                    if (washBottom != Offset.Zero && walkLeftTop != Offset.Zero) {
                        drawDiagramArrow(
                            start = washBottom,
                            end = walkLeftTop,
                            color = arrowColor
                        )
                    }

                    // 4. Стрелка Попить водички -> Выйти на улицу
                    if (waterBottom != Offset.Zero && walkRightTop != Offset.Zero) {
                        drawDiagramArrow(
                            start = waterBottom,
                            end = walkRightTop,
                            color = arrowColor
                        )
                    }
                }

                // 1. Блок: Проснуться
                DiagramTaskCard(
                    title = "Проснуться",
                    isDone = wakeUpDone,
                    defaultColor = boxYellow,
                    doneColor = doneGreen,
                    outlineColor = outlineDark,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 8.dp)
                        .onGloballyPositioned { coords ->
                            val pos = coords.positionInParent()
                            val size = coords.size
                            wakeBottom = Offset(pos.x + size.width / 2f, pos.y + size.height)
                        },
                    onClick = {
                        wakeUpDone = !wakeUpDone
                        prefs.edit().putBoolean("wake_up", wakeUpDone).apply()
                    }
                )

                // 2. Блок: Умыться
                DiagramTaskCard(
                    title = "Умыться",
                    isDone = washDone,
                    defaultColor = boxBlue,
                    doneColor = doneGreen,
                    outlineColor = outlineDark,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp, bottom = 40.dp)
                        .onGloballyPositioned { coords ->
                            val pos = coords.positionInParent()
                            val size = coords.size
                            washTop = Offset(pos.x + size.width / 2f, pos.y)
                            washRight = Offset(pos.x + size.width, pos.y + size.height * 0.45f)
                            washBottom = Offset(pos.x + size.width * 0.8f, pos.y + size.height)
                        },
                    onClick = {
                        washDone = !washDone
                        prefs.edit().putBoolean("wash", washDone).apply()
                    }
                )

                // Текст над стрелкой: "опционально"
                Text(
                    text = "опционально",
                    fontSize = 13.sp,
                    color = arrowColor,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = 10.dp, y = (-72).dp)
                        .rotate(-12f)
                )

                // 3. Блок: Попить водички
                DiagramTaskCard(
                    title = "Попить\nводички",
                    isDone = waterDone,
                    defaultColor = boxBlue,
                    doneColor = doneGreen,
                    outlineColor = outlineDark,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp, bottom = 60.dp)
                        .onGloballyPositioned { coords ->
                            val pos = coords.positionInParent()
                            val size = coords.size
                            waterLeft = Offset(pos.x, pos.y + size.height * 0.5f)
                            waterBottom = Offset(pos.x + size.width * 0.35f, pos.y + size.height)
                        },
                    onClick = {
                        waterDone = !waterDone
                        prefs.edit().putBoolean("water", waterDone).apply()
                    }
                )

                // 4. Блок: Выйти на улицу
                DiagramTaskCard(
                    title = "Выйти\nна улицу",
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
                        walkDone = !walkDone
                        prefs.edit().putBoolean("walk", walkDone).apply()
                    }
                )
            }
        }

        // Подсказка внизу
        Text(
            text = "Свайпните вверх для разблокировки",
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
