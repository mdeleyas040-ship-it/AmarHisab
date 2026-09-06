package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.util.formatMoney
import java.time.LocalDate
import java.time.temporal.ChronoUnit

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
                    top = 24.dp,
                    bottom = 24.dp
                )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {

                // মোট ব্যালেন্স
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
                            modifier = Modifier.height(7.dp)
                        )

                        Text(
                            text = "৳ ${formatMoney(balance)}",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(
                            modifier = Modifier.height(3.dp)
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
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "ব্যালেন্স",
                                tint = Color(0xFF00E878),
                                modifier = Modifier.size(29.dp)
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                // আয় / খরচ / বাড়িতে
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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

                // Birthday — Balance Card-এর ভিতরে
                if (birthday != null && birthdayDays != null) {

                    Spacer(
                        modifier = Modifier.height(22.dp)
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
                                    vertical = 15.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Column {

                                Text(
                                    text = "🎂 জন্মদিন",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(
                                    modifier = Modifier.height(2.dp)
                                )

                                Row(
                                    verticalAlignment = Alignment.Bottom
                                ) {

                                    Text(
                                        text = "$birthdayDays দিন",
                                        color = Color(0xFF00E878),
                                        fontSize = 25.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )

                                    Spacer(
                                        modifier = Modifier.width(6.dp)
                                    )

                                    Text(
                                        text = "বাকি",
                                        color = Color.White.copy(alpha = 0.72f),
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(
                                            bottom = 4.dp
                                        )
                                    )
                                }
                            }

                            Text(
                                text = "🎁",
                                fontSize = 34.sp
                            )
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
        modifier = modifier.height(78.dp),
        shape = RoundedCornerShape(18.dp),
        color = backgroundColor.copy(alpha = 0.78f)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 13.dp,
                vertical = 10.dp
            ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = title,
                color = Color.White.copy(alpha = 0.68f),
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

    val today = LocalDate.now()

    val month = birthday.first
    val day = birthday.second

    val currentYearBirthday = try {
        LocalDate.of(
            today.year,
            month,
            day
        )
    } catch (_: Exception) {
        return null
    }

    val nextBirthday =
        if (!currentYearBirthday.isBefore(today)) {
            currentYearBirthday
        } else {
            try {
                LocalDate.of(
                    today.year + 1,
                    month,
                    day
                )
            } catch (_: Exception) {
                return null
            }
        }

    return ChronoUnit.DAYS.between(
        today,
        nextBirthday
    )
}