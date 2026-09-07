package com.eleyas.expensetracker.ui.screens

import android.app.DatePickerDialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.util.formatMoney
import java.util.Calendar

@Composable
fun PremiumHomeHeader(
    balance: Double,
    totalIncome: Double,
    totalExpense: Double,
    totalHome: Double,
    currentUserId: String,
    birthday: Pair<Int, Int>?,
    onBirthdayChange: (Pair<Int, Int>?) -> Unit,
    onSearchClick: () -> Unit = {}
) {

    val birthdayDays = rememberBirthdayDays(birthday)

    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 12.dp
        )
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF18211E),
                            Color(0xFF111613),
                            Color(0xFF0D100F)
                        )
                    )
                )
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 20.dp,
                    bottom = 20.dp
                )
        ) {

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {

                // =========================
                // মোট ব্যালেন্স
                // =========================

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {

                    Column {

                        Text(
                            text = "মোট ব্যালেন্স",
                            color = Color(0xFF35D98A),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(
                            modifier = Modifier.height(5.dp)
                        )

                        Text(
                            text = "৳ ${formatMoney(balance)}",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(
                            modifier = Modifier.height(2.dp)
                        )

                        Text(
                            text = "বাংলাদেশি টাকা (BDT)",
                            color = Color.White.copy(alpha = 0.42f),
                            fontSize = 10.sp
                        )
                    }

                    Surface(
                        modifier = Modifier.size(54.dp),
                        shape = RoundedCornerShape(17.dp),
                        color = Color(0xFF0E6F4C).copy(alpha = 0.42f)
                    ) {

                        Box(
                            contentAlignment = Alignment.Center
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.AccountBalanceWallet,
                                contentDescription = "ব্যালেন্স",
                                tint = Color(0xFF00E878),
                                modifier = Modifier.size(29.dp)
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                // =========================
                // আয় / খরচ / বাড়িতে
                // =========================

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    PremiumHeaderStat(
                        modifier = Modifier.weight(1f),
                        title = "আয়",
                        amount = totalIncome,
                        amountColor = Color(0xFF18D879),
                        backgroundColor = Color(0xFF073E2B)
                    )

                    PremiumHeaderStat(
                        modifier = Modifier.weight(1f),
                        title = "খরচ",
                        amount = totalExpense,
                        amountColor = Color(0xFFFF334A),
                        backgroundColor = Color(0xFF401720)
                    )

                    PremiumHeaderStat(
                        modifier = Modifier.weight(1f),
                        title = "বাড়িতে",
                        amount = totalHome,
                        amountColor = Color(0xFF159BFF),
                        backgroundColor = Color(0xFF102C48)
                    )
                }

                // =========================
                // Birthday
                // =========================

                if (birthday != null && birthdayDays != null) {

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF292B30)
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 18.dp,
                                    vertical = 12.dp
                                ),
                            verticalAlignment =
                                Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {

                            Column {

                                Text(
                                    text = "🎂 জন্মদিন",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(
                                    modifier = Modifier.height(1.dp)
                                )

                                Row(
                                    verticalAlignment =
                                        Alignment.Bottom
                                ) {

                                    Text(
                                        text = "$birthdayDays দিন",
                                        color =
                                            Color(0xFF00E878),
                                        fontSize = 25.sp,
                                        fontWeight =
                                            FontWeight.ExtraBold
                                    )

                                    Spacer(
                                        modifier = Modifier.width(6.dp)
                                    )

                                    Text(
                                        text = "বাকি",
                                        color =
                                            Color.White.copy(
                                                alpha = 0.72f
                                            ),
                                        fontSize = 13.sp,
                                        modifier =
                                            Modifier.padding(
                                                bottom = 4.dp
                                            )
                                    )
                                }
                            }

                            // =========================
                            // Gift Button
                            // =========================

                            IconButton(
                                onClick = {

                                    val today =
                                        Calendar.getInstance()

                                    DatePickerDialog(
                                        context,

                                        { _, _, month, dayOfMonth ->

                                            onBirthdayChange(
                                                Pair(
                                                    month,
                                                    dayOfMonth
                                                )
                                            )
                                        },

                                        today.get(
                                            Calendar.YEAR
                                        ),

                                        birthday.first,

                                        birthday.second

                                    ).show()
                                },

                                modifier = Modifier.size(52.dp)
                            ) {

                                Icon(
                                    imageVector =
                                        Icons.Default.Redeem,

                                    contentDescription =
                                        "জন্মদিন পরিবর্তন করুন",

                                    tint =
                                        Color.White,

                                    modifier =
                                        Modifier.size(34.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumHeaderStat(
    modifier: Modifier,
    title: String,
    amount: Double,
    amountColor: Color,
    backgroundColor: Color
) {

    Surface(
        modifier = modifier.height(70.dp),
        shape = RoundedCornerShape(18.dp),
        color = backgroundColor.copy(alpha = 0.78f)
    ) {

        Column(
            modifier = Modifier.padding(
                horizontal = 13.dp,
                vertical = 8.dp
            ),
            verticalArrangement =
                Arrangement.SpaceBetween
        ) {

            Text(
                text = title,
                color =
                    Color.White.copy(alpha = 0.68f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "৳${formatMoney(amount)}",
                color = amountColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }
}

private fun rememberBirthdayDays(
    birthday: Pair<Int, Int>?
): Long? {

    if (birthday == null) return null

    val today = Calendar.getInstance()

    // আজকের সময় বাদ দিয়ে শুধু তারিখ রাখছি
    today.set(
        Calendar.HOUR_OF_DAY,
        12
    )
    today.set(
        Calendar.MINUTE,
        0
    )
    today.set(
        Calendar.SECOND,
        0
    )
    today.set(
        Calendar.MILLISECOND,
        0
    )

    val birthdayMonth = birthday.first
    val birthdayDay = birthday.second

    // প্রথমে এই বছরের birthday
    val nextBirthday = Calendar.getInstance()

    nextBirthday.set(
        Calendar.YEAR,
        today.get(Calendar.YEAR)
    )
    nextBirthday.set(
        Calendar.MONTH,
        birthdayMonth
    )
    nextBirthday.set(
        Calendar.DAY_OF_MONTH,
        birthdayDay
    )
    nextBirthday.set(
        Calendar.HOUR_OF_DAY,
        12
    )
    nextBirthday.set(
        Calendar.MINUTE,
        0
    )
    nextBirthday.set(
        Calendar.SECOND,
        0
    )
    nextBirthday.set(
        Calendar.MILLISECOND,
        0
    )

    // যদি এ বছরের birthday চলে যায়,
    // তাহলে পরের বছরের birthday
    if (nextBirthday.before(today)) {
        nextBirthday.add(
            Calendar.YEAR,
            1
        )
    }

    // সঠিক day difference বের করা
    var days = 0L

    val counter = today.clone() as Calendar

    while (counter.before(nextBirthday)) {
        counter.add(
            Calendar.DAY_OF_MONTH,
            1
        )
        days++
    }

    return days
}