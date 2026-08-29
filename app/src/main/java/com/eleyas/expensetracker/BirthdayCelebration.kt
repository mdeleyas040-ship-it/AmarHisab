package com.eleyas.expensetracker

import android.app.DatePickerDialog
import android.content.SharedPreferences
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import kotlin.random.Random
import com.google.firebase.auth.FirebaseAuth

private const val BIRTHDAY_MONTH_KEY = "birthday_month"
private const val BIRTHDAY_DAY_KEY = "birthday_day"

fun getBirthday(
    prefs: SharedPreferences
): Pair<Int, Int>? {

    if (!prefs.contains(BIRTHDAY_MONTH_KEY) ||
        !prefs.contains(BIRTHDAY_DAY_KEY)
    ) {
        return null
    }

    val month =
        prefs.getInt(
            BIRTHDAY_MONTH_KEY,
            -1
        )

    val day =
        prefs.getInt(
            BIRTHDAY_DAY_KEY,
            -1
        )

    if (month !in 0..11 || day !in 1..31) {
        return null
    }

    return Pair(month, day)
}

fun saveBirthday(
    prefs: SharedPreferences,
    month: Int,
    day: Int
) {
    prefs.edit()
        .putInt(
            BIRTHDAY_MONTH_KEY,
            month
        )
        .putInt(
            BIRTHDAY_DAY_KEY,
            day
        )
        .apply()
}

private fun daysUntilBirthday(
    birthday: Pair<Int, Int>
): Int {

    val todayStart = Calendar.getInstance().apply {
        set(
            Calendar.HOUR_OF_DAY,
            0
        )
        set(
            Calendar.MINUTE,
            0
        )
        set(
            Calendar.SECOND,
            0
        )
        set(
            Calendar.MILLISECOND,
            0
        )
    }

    val birthdayDate =
        Calendar.getInstance().apply {
            set(
                Calendar.MONTH,
                birthday.first
            )
            set(
                Calendar.DAY_OF_MONTH,
                birthday.second
            )
            set(
                Calendar.HOUR_OF_DAY,
                0
            )
            set(
                Calendar.MINUTE,
                0
            )
            set(
                Calendar.SECOND,
                0
            )
            set(
                Calendar.MILLISECOND,
                0
            )
        }

    if (birthdayDate.before(todayStart)) {
        birthdayDate.add(
            Calendar.YEAR,
            1
        )
    }

    val difference =
        birthdayDate.timeInMillis -
                todayStart.timeInMillis

    return (
            difference /
                    (24L * 60L * 60L * 1000L)
            ).toInt()
}

private fun isBirthdayToday(
    birthday: Pair<Int, Int>
): Boolean {

    val today = Calendar.getInstance()

    return today.get(Calendar.MONTH) ==
            birthday.first &&
            today.get(Calendar.DAY_OF_MONTH) ==
            birthday.second
}

private fun formatBirthday(
    birthday: Pair<Int, Int>
): String {

    val calendar = Calendar.getInstance().apply {
        set(
            Calendar.MONTH,
            birthday.first
        )
        set(
            Calendar.DAY_OF_MONTH,
            birthday.second
        )
    }

    val month =
        calendar.getDisplayName(
            Calendar.MONTH,
            Calendar.LONG,
            java.util.Locale.ENGLISH
        ) ?: ""

    return String.format(
        "%02d %s",
        birthday.second,
        month
    )
}

@Composable
fun BirthdayCountdownCard(
    userId: String,
    currentBirthday: Pair<Int, Int>?,
    onBirthdaySet: (Pair<Int, Int>) -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val context = LocalContext.current
    val prefs = remember(userId) { AccountStorage.getPrefs(context, userId) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val today = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                saveBirthday(prefs, month, dayOfMonth)
                onBirthdaySet(Pair(month, dayOfMonth))
                showDatePicker = false
            },
            today.get(Calendar.YEAR),
            currentBirthday?.first ?: today.get(Calendar.MONTH),
            currentBirthday?.second ?: today.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnCancelListener { showDatePicker = false }
            show()
        }
    }

    if (currentBirthday == null) {
        BirthdaySetupCard(
            modifier = modifier,
            onSetBirthday = { showDatePicker = true },
            isCompact = isCompact
        )
        return
    }

    val days = daysUntilBirthday(currentBirthday)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(if (isCompact) 12.dp else 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🎂 Birthday",
                    color = Color.White,
                    fontSize = if (isCompact) 14.sp else 19.sp,
                    fontWeight = FontWeight.Bold
                )

                if (days == 0) {
                    Text(
                        text = "🎉 আজ তোমার জন্মদিন!",
                        color = Color(0xFF00E676),
                        fontSize = if (isCompact) 13.sp else 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$days দিন ",
                            color = Color(0xFF00E676),
                            fontSize = if (isCompact) 20.sp else 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "বাকি",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = if (isCompact) 11.sp else 14.sp,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }
            }

            IconButton(onClick = { showDatePicker = true }, modifier = Modifier.size(28.dp)) {
                Text(if (days == 0) "🎉" else "🎁", fontSize = if (isCompact) 24.sp else 40.sp)
            }
        }
    }
}

@Composable
private fun BirthdaySetupCard(
    modifier: Modifier = Modifier,
    onSetBirthday: () -> Unit,
    isCompact: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(if (isCompact) 12.dp else 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🎂 Birthday",
                    color = Color.White,
                    fontSize = if (isCompact) 14.sp else 19.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "সেট করুন",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = if (isCompact) 11.sp else 14.sp
                )
            }
            Button(
                onClick = onSetBirthday,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Set", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BirthdayPopupCheck(
    userId: String, 
    birthday: Pair<Int, Int>?
) {
    val context = LocalContext.current
    val prefs = remember(userId) { AccountStorage.getPrefs(context, userId) }

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val lastShownYear = prefs.getInt("last_birthday_celebration_year", -1)

    var showBirthday by remember(userId, birthday) {
        mutableStateOf(
            birthday != null && 
            isBirthdayToday(birthday) && 
            lastShownYear < currentYear
        )
    }

    if (showBirthday) {
        BirthdayCelebrationModal(
            onDismiss = {
                prefs.edit().putInt("last_birthday_celebration_year", currentYear).apply()
                showBirthday = false
            }
        )
    }
}

@Composable
fun BirthdayCelebrationCheck(
    userId: String,
    birthday: Pair<Int, Int>?,
    onBirthdaySet: (Pair<Int, Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    BirthdayCountdownCard(
        userId = userId,
        currentBirthday = birthday,
        onBirthdaySet = onBirthdaySet,
        modifier = modifier
    )

    BirthdayPopupCheck(
        userId = userId,
        birthday = birthday
    )
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
                animation =
                    tween(
                        durationMillis = 900,
                        easing =
                            FastOutSlowInEasing
                    ),
                repeatMode =
                    RepeatMode.Reverse
            ),
        label = "cake_scale"
    )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(
                        alpha = 0.80f
                    )
                ),
        contentAlignment =
            Alignment.Center
    ) {

        BirthdayConfetti(
            modifier =
                Modifier.fillMaxSize()
        )

        Column(
            modifier =
                Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .background(
                        color =
                            Color(0xFF123C3A),
                        shape =
                            RoundedCornerShape(28.dp)
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
                modifier =
                    Modifier.scale(scale)
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            Text(
                text =
                    "শুভ জন্মদিন! 🎉",
                color =
                    Color.White,
                fontSize = 30.sp,
                fontWeight =
                    FontWeight.Bold,
                textAlign =
                    TextAlign.Center
            )

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Text(
                text =
                    "আজ তোমার বিশেষ দিন।\n" +
                            "Amar Hisab-এর পক্ষ থেকে রইল\n" +
                            "অনেক শুভকামনা! 💚",
                color =
                    Color.White.copy(
                        alpha = 0.9f
                    ),
                fontSize = 17.sp,
                lineHeight = 26.sp,
                textAlign =
                    TextAlign.Center
            )

            Spacer(
                modifier =
                    Modifier.height(22.dp)
            )

            Text(
                text =
                    "🎁  ✨  🎂  ✨  🎁",
                fontSize = 30.sp
            )

            Spacer(
                modifier =
                    Modifier.height(25.dp)
            )

            Button(
                onClick =
                    onDismiss,
                shape =
                    RoundedCornerShape(18.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            Color(0xFF00E676)
                    )
            ) {

                Text(
                    text =
                        "ধন্যবাদ ❤️",
                    color =
                        Color.Black,
                    fontSize = 17.sp,
                    fontWeight =
                        FontWeight.Bold
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
                x =
                    Random.nextFloat(),
                y =
                    Random.nextFloat(),
                size =
                    Random.nextInt(
                        5,
                        12
                    ).toFloat(),
                rotation =
                    Random.nextFloat() * 360f,
                color =
                    listOf(
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
            label =
                "confetti_animation"
        )

    val movement by
    infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = 3000
                    ),
                repeatMode =
                    RepeatMode.Restart
            ),
        label =
            "confetti_movement"
    )

    Canvas(
        modifier = modifier
    ) {

        pieces.forEach { piece ->

            val yPosition =
                (
                        (piece.y + movement) % 1f
                        ) * size.height

            rotate(
                degrees =
                    piece.rotation,
                pivot =
                    androidx.compose.ui.geometry
                        .Offset(
                            x =
                                piece.x *
                                        size.width,
                            y =
                                yPosition
                        )
            ) {

                drawCircle(
                    color =
                        piece.color,
                    radius =
                        piece.size,
                    center =
                        androidx.compose.ui.geometry
                            .Offset(
                                x =
                                    piece.x *
                                            size.width,
                                y =
                                    yPosition
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