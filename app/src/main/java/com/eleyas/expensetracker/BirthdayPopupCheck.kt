package com.eleyas.expensetracker

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.util.AccountStorage
import java.util.Calendar
import kotlin.random.Random

private fun isBirthdayToday(
    birthday: Pair<Int, Int>
): Boolean {
    val today = Calendar.getInstance()

    return today.get(Calendar.MONTH) == birthday.first &&
            today.get(Calendar.DAY_OF_MONTH) == birthday.second
}

@Composable
fun BirthdayPopupCheck(
    userId: String,
    birthday: Pair<Int, Int>?
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember(userId) {
        AccountStorage.getPrefs(context, userId)
    }

    val currentYear =
        Calendar.getInstance().get(Calendar.YEAR)

    val lastShownYear =
        prefs.getInt(
            "last_birthday_celebration_year",
            -1
        )

    var showBirthday by remember(
        userId,
        birthday
    ) {
        mutableStateOf(
            birthday != null &&
                    isBirthdayToday(birthday) &&
                    lastShownYear < currentYear
        )
    }

    if (showBirthday) {
        BirthdayCelebrationModal(
            onDismiss = {
                prefs.edit()
                    .putInt(
                        "last_birthday_celebration_year",
                        currentYear
                    )
                    .apply()

                showBirthday = false
            }
        )
    }
}

@Composable
fun BirthdayCelebrationModal(
    onDismiss: () -> Unit
) {
    val infiniteTransition =
        rememberInfiniteTransition(
            label = "birthday_animation"
        )

    val scale by
    infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(
                    durationMillis = 900,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
        label = "cake_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(alpha = 0.80f)
            ),
        contentAlignment = Alignment.Center
    ) {
        BirthdayConfetti(
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .background(
                    color = Color(0xFF123C3A),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(
                    horizontal = 24.dp,
                    vertical = 30.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎂",
                fontSize = 60.sp,
                modifier = Modifier.scale(scale)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "শুভ জন্মদিন! 🎉",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text =
                    "তোমার জন্মদিনটি আনন্দ, শান্তি ও সুন্দর মুহূর্তে ভরে উঠুক।",
                color = Color.White.copy(alpha = 0.90f),
                fontSize = 17.sp,
                lineHeight = 26.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(22.dp))

            Text(
                text = "🎁  ✨  🎂  ✨  🎁",
                fontSize = 30.sp
            )

            Spacer(Modifier.height(25.dp))

            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00E676)
                )
            ) {
                Text(
                    text = "ধন্যবাদ ❤️",
                    color = Color.Black,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BirthdayConfetti(
    modifier: Modifier
) {
    val pieces = remember {
        List(45) {
            ConfettiPiece(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextInt(
                    5,
                    12
                ).toFloat(),
                rotation =
                    Random.nextFloat() * 360f,
                color = listOf(
                    Color(0xFF00E676),
                    Color(0xFFFFD54F),
                    Color(0xFFFF4081),
                    Color(0xFF40C4FF),
                    Color(0xFFFF6D00)
                ).random()
            )
        }
    }

    val infiniteTransition =
        rememberInfiniteTransition(
            label = "confetti_animation"
        )

    val movement by
    infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(
                    durationMillis = 3000
                ),
                repeatMode = RepeatMode.Restart
            ),
        label = "confetti_movement"
    )

    Canvas(modifier = modifier) {
        pieces.forEach { piece ->
            val yPosition =
                ((piece.y + movement) % 1f) *
                        size.height

            rotate(
                degrees = piece.rotation,
                pivot = androidx.compose.ui.geometry.Offset(
                    x = piece.x * size.width,
                    y = yPosition
                )
            ) {
                drawCircle(
                    color = piece.color,
                    radius = piece.size,
                    center =
                        androidx.compose.ui.geometry.Offset(
                            x = piece.x * size.width,
                            y = yPosition
                        )
                )
            }
        }
    }
}

private data class ConfettiPiece(
    val x: Float,
    val y: Float,
    val size: Float,
    val rotation: Float,
    val color: Color
)
