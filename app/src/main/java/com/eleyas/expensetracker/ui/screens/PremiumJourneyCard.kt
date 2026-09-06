package com.eleyas.expensetracker.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import com.eleyas.expensetracker.model.Transaction
import com.eleyas.expensetracker.util.AccountStorage
import com.eleyas.expensetracker.util.formatMoney
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.ceil

private const val JOURNEY_PREFS = "amar_hisab_journey"
private const val JOURNEY_DATE_PREFIX = "journey_date_"

private data class JourneyStats(
    val averageIncome: Double,
    val averageExpense: Double
)

private data class DebtInfo(
    val remainingText: String,
    val freeDateText: String
)


// =============================================================
// MAIN JOURNEY CARD
// =============================================================

@Composable
fun PremiumJourneyCard(
    currentUserId: String,
    transactions: List<Transaction>,
    loanRemaining: Double
) {

    val context = LocalContext.current

    var showPopup by remember {
        mutableStateOf(false)
    }

    var showDatePicker by remember {
        mutableStateOf(false)
    }

    val journeyPrefs = remember(currentUserId) {
        context.getSharedPreferences(
            JOURNEY_PREFS,
            Context.MODE_PRIVATE
        )
    }

    var journeyDate by remember(currentUserId) {
        mutableStateOf(
            journeyPrefs.getString(
                JOURNEY_DATE_PREFIX + currentUserId,
                null
            )
        )
    }


    // ---------------------------------------------------------
    // JOURNEY POPUP
    // ---------------------------------------------------------

    if (showPopup) {

        PremiumJourneyPopup(
            currentUserId = currentUserId,
            transactions = transactions,
            loanRemaining = loanRemaining,
            journeyDate = journeyDate,

            onDismiss = {
                showPopup = false
            },

            onEditDate = {
                showDatePicker = true
            }
        )
    }


    // ---------------------------------------------------------
    // DATE SELECTOR
    // ---------------------------------------------------------

    if (showDatePicker) {

        JourneyDatePicker(
            currentDate = journeyDate,

            onDismiss = {
                showDatePicker = false
            },

            onDateSelected = { newDate ->

                journeyDate = newDate

                journeyPrefs
                    .edit()
                    .putString(
                        JOURNEY_DATE_PREFIX + currentUserId,
                        newDate
                    )
                    .apply()

                showDatePicker = false
            }
        )
    }


    // =========================================================
    // HOME JOURNEY CARD
    // =========================================================

    Card(
        onClick = {
            showPopup = true
        },

        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(20.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF17211F)
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 14.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(46.dp),

                shape =
                    RoundedCornerShape(14.dp),

                color =
                    Color(0xFF0B5C43)
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        text = "🇲🇻",
                        fontSize = 23.sp
                    )
                }
            }

            Spacer(
                Modifier.width(12.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        "🇲🇻 মালদ্বীপ আমার যাত্রা",

                    color = Color.White,

                    fontSize = 16.sp,

                    fontWeight =
                        FontWeight.ExtraBold
                )

                Spacer(
                    Modifier.height(3.dp)
                )

                Text(
                    text =
                        if (journeyDate == null) {

                            "মালদ্বীপে আছেন • তারিখ সেট করুন"

                        } else {

                            val duration =
                                calculateElapsedDuration(
                                    journeyDate
                                )

                            if (duration != null) {
                                "মালদ্বীপে আছেন • $duration"
                            } else {
                                "মালদ্বীপে আছেন"
                            }
                        },

                    color =
                        Color.White.copy(
                            alpha = 0.55f
                        ),

                    fontSize = 11.sp
                )
            }

            Surface(
                modifier =
                    Modifier.size(34.dp),

                shape =
                    RoundedCornerShape(11.dp),

                color =
                    Color.White.copy(
                        alpha = 0.06f
                    )
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    Text(
                        text = "›",

                        color =
                            Color.White.copy(
                                alpha = 0.70f
                            ),

                        fontSize = 25.sp
                    )
                }
            }
        }
    }
}


// =============================================================
// PREMIUM JOURNEY POPUP
// =============================================================

@Composable
private fun PremiumJourneyPopup(
    currentUserId: String,
    transactions: List<Transaction>,
    loanRemaining: Double,
    journeyDate: String?,
    onDismiss: () -> Unit,
    onEditDate: () -> Unit
) {

    val context = LocalContext.current

    val prefs = remember(currentUserId) {
        AccountStorage.getPrefs(
            context,
            currentUserId
        )
    }

    val usdToBdt =
        prefs
            .getFloat(
                "usd_to_bdt",
                0f
            )
            .toDouble()

    val usdToMvr =
        prefs
            .getFloat(
                "usd_to_mvr",
                0f
            )
            .toDouble()


    // ---------------------------------------------------------
    // LAST 3 MONTHS STATS
    // ---------------------------------------------------------

    val journeyStats = remember(
        transactions,
        usdToBdt,
        usdToMvr
    ) {

        calculateJourneyStats(
            transactions = transactions,
            usdToBdt = usdToBdt,
            usdToMvr = usdToMvr
        )
    }


    val monthlySaving =
        (
                journeyStats.averageIncome -
                        journeyStats.averageExpense
                )
            .coerceAtLeast(0.0)


    // ---------------------------------------------------------
    // DEBT INFORMATION
    // ---------------------------------------------------------

    val debtInfo = remember(
        loanRemaining,
        monthlySaving
    ) {

        calculateDebtInfo(
            loanRemaining = loanRemaining,
            monthlySaving = monthlySaving
        )
    }


    val journeyDuration =
        remember(journeyDate) {

            calculateElapsedDuration(
                journeyDate
            )
        }


    // =========================================================
    // POPUP
    // =========================================================

    Dialog(
        onDismissRequest = onDismiss
    ) {

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),

            shape =
                RoundedCornerShape(28.dp),

            color = Color.Transparent
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush =
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF1B2732),
                                    Color(0xFF111920),
                                    Color(0xFF0E151B)
                                )
                            ),

                        shape =
                            RoundedCornerShape(28.dp)
                    )
                    .padding(14.dp)
            ) {


                // =================================================
                // HEADER
                // =================================================

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Surface(
                        modifier =
                            Modifier.size(48.dp),

                        shape =
                            RoundedCornerShape(14.dp),

                        color =
                            Color(0xFF29394A)
                    ) {

                        Box(
                            contentAlignment =
                                Alignment.Center
                        ) {

                            Text(
                                text = "🇲🇻",
                                fontSize = 25.sp
                            )
                        }
                    }

                    Spacer(
                        Modifier.width(10.dp)
                    )

                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {

                        Text(
                            text =
                                "মালদ্বীপ আমার যাত্রা",

                            color = Color.White,

                            fontSize = 17.sp,

                            fontWeight =
                                FontWeight.ExtraBold
                        )

                        Text(
                            text =
                                "আপনার বর্তমান যাত্রার হিসাব",

                            color =
                                Color.White.copy(
                                    alpha = 0.42f
                                ),

                            fontSize = 9.sp
                        )
                    }

                    Surface(
                        modifier =
                            Modifier
                                .size(36.dp)
                                .clickable {
                                    onDismiss()
                                },

                        shape =
                            RoundedCornerShape(11.dp),

                        color =
                            Color.White.copy(
                                alpha = 0.07f
                            )
                    ) {

                        Box(
                            contentAlignment =
                                Alignment.Center
                        ) {

                            Text(
                                text = "×",

                                color =
                                    Color.White.copy(
                                        alpha = 0.75f
                                    ),

                                fontSize = 24.sp
                            )
                        }
                    }
                }


                Spacer(
                    Modifier.height(10.dp)
                )


                // =================================================
                // CURRENT LOCATION
                // =================================================

                Surface(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(18.dp),

                    color =
                        Color(0xFF0C1720)
                ) {

                    Row(
                        modifier =
                            Modifier.padding(
                                horizontal = 11.dp,
                                vertical = 9.dp
                            ),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Surface(
                            modifier =
                                Modifier.size(40.dp),

                            shape =
                                RoundedCornerShape(12.dp),

                            color =
                                Color(0xFF073D2D)
                        ) {

                            Box(
                                contentAlignment =
                                    Alignment.Center
                            ) {

                                Icon(
                                    imageVector =
                                        Icons.Default.LocationOn,

                                    contentDescription =
                                        null,

                                    tint =
                                        Color(0xFF20E993),

                                    modifier =
                                        Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(
                            Modifier.width(9.dp)
                        )

                        Column(
                            modifier =
                                Modifier.weight(1f)
                        ) {

                            Text(
                                text =
                                    "বর্তমান অবস্থান",

                                color =
                                    Color.White.copy(
                                        alpha = 0.38f
                                    ),

                                fontSize = 8.sp
                            )

                            Text(
                                text =
                                    "🇲🇻 মালদ্বীপে আছেন",

                                color = Color.White,

                                fontSize = 13.sp,

                                fontWeight =
                                    FontWeight.ExtraBold
                            )
                        }

                        if (journeyDuration != null) {

                            Text(
                                text =
                                    journeyDuration,

                                color =
                                    Color(0xFF26E993),

                                fontSize = 9.sp,

                                fontWeight =
                                    FontWeight.ExtraBold
                            )
                        }
                    }
                }


                Spacer(
                    Modifier.height(9.dp)
                )


                // =================================================
                // FINANCIAL TITLE
                // =================================================

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Text(
                        text = "আর্থিক চিত্র",

                        color = Color.White,

                        fontSize = 12.sp,

                        fontWeight =
                            FontWeight.ExtraBold
                    )

                    Spacer(
                        Modifier.width(6.dp)
                    )

                    Surface(
                        shape =
                            RoundedCornerShape(20.dp),

                        color =
                            Color(0xFF1B3B31)
                    ) {

                        Text(
                            text = "গত ৩ মাস",

                            modifier =
                                Modifier.padding(
                                    horizontal = 7.dp,
                                    vertical = 3.dp
                                ),

                            color =
                                Color(0xFF42EFA1),

                            fontSize = 7.sp,

                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }


                Spacer(
                    Modifier.height(6.dp)
                )


                // =================================================
                // FINANCIAL GRID - ROW 1
                // =================================================

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(7.dp)
                ) {

                    JourneyFinanceCard(
                        modifier =
                            Modifier.weight(1f),

                        icon =
                            Icons.Default.AccountBalanceWallet,

                        iconColor =
                            Color(0xFF5BA7FF),

                        title =
                            "গড় আয় / মাস",

                        value =
                            "৳ ${
                                formatMoney(
                                    journeyStats.averageIncome
                                )
                            }"
                    )

                    JourneyFinanceCard(
                        modifier =
                            Modifier.weight(1f),

                        icon =
                            Icons.Default.ShoppingCart,

                        iconColor =
                            Color(0xFFFFBD3B),

                        title =
                            "গড় খরচ / মাস",

                        value =
                            "৳ ${
                                formatMoney(
                                    journeyStats.averageExpense
                                )
                            }"
                    )
                }


                Spacer(
                    Modifier.height(7.dp)
                )


                // =================================================
                // FINANCIAL GRID - ROW 2
                // =================================================

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(7.dp)
                ) {

                    JourneyFinanceCard(
                        modifier =
                            Modifier.weight(1f),

                        icon =
                            Icons.Default.Savings,

                        iconColor =
                            Color(0xFF19E98B),

                        title =
                            "জমা যাবে / মাস",

                        value =
                            "৳ ${
                                formatMoney(
                                    monthlySaving
                                )
                            }"
                    )

                    JourneyFinanceCard(
                        modifier =
                            Modifier.weight(1f),

                        icon =
                            Icons.Default.CreditCard,

                        iconColor =
                            Color(0xFFFF5065),

                        title =
                            "মোট বাকি ঋণ",

                        value =
                            "৳ ${
                                formatMoney(
                                    loanRemaining
                                        .coerceAtLeast(0.0)
                                )
                            }"
                    )
                }


                Spacer(
                    Modifier.height(8.dp)
                )


                // =================================================
                // DEBT PROJECTION
                // =================================================

                Surface(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(18.dp),

                    color =
                        Color(0xFF171F28)
                ) {

                    Column(
                        modifier =
                            Modifier.padding(
                                horizontal = 11.dp,
                                vertical = 9.dp
                            )
                    ) {

                        Row(
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Surface(
                                modifier =
                                    Modifier.size(32.dp),

                                shape =
                                    RoundedCornerShape(9.dp),

                                color =
                                    Color(0xFF292A30)
                            ) {

                                Box(
                                    contentAlignment =
                                        Alignment.Center
                                ) {

                                    Icon(
                                        imageVector =
                                            Icons.Default.Timer,

                                        contentDescription =
                                            null,

                                        tint =
                                            Color(0xFFFFC84D),

                                        modifier =
                                            Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(
                                Modifier.width(8.dp)
                            )

                            Text(
                                text =
                                    "ঋণ পরিশোধের পরিকল্পনা",

                                color = Color.White,

                                fontSize = 10.sp,

                                fontWeight =
                                    FontWeight.ExtraBold
                            )
                        }


                        Spacer(
                            Modifier.height(7.dp)
                        )


                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.spacedBy(7.dp)
                        ) {

                            DebtMiniInfo(
                                modifier =
                                    Modifier.weight(1f),

                                title =
                                    "ঋণ শেষ হতে",

                                value =
                                    debtInfo.remainingText
                            )

                            DebtMiniInfo(
                                modifier =
                                    Modifier.weight(1f),

                                title =
                                    "সম্ভাব্য ঋণমুক্তি",

                                value =
                                    debtInfo.freeDateText
                            )
                        }
                    }
                }


                Spacer(
                    Modifier.height(8.dp)
                )


                // =================================================
                // JOURNEY DATE
                // =================================================

                Surface(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(18.dp),

                    color =
                        Color(0xFF1A2A38)
                ) {

                    Row(
                        modifier =
                            Modifier.padding(
                                horizontal = 11.dp,
                                vertical = 9.dp
                            ),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Surface(
                            modifier =
                                Modifier.size(40.dp),

                            shape =
                                RoundedCornerShape(12.dp),

                            color =
                                Color(0xFF132C45)
                        ) {

                            Box(
                                contentAlignment =
                                    Alignment.Center
                            ) {

                                Icon(
                                    imageVector =
                                        Icons.Default.CalendarMonth,

                                    contentDescription =
                                        null,

                                    tint =
                                        Color(0xFF69B2FF),

                                    modifier =
                                        Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(
                            Modifier.width(9.dp)
                        )

                        Column(
                            modifier =
                                Modifier.weight(1f)
                        ) {

                            Text(
                                text =
                                    "মালদ্বীপ আমার তারিখ",

                                color =
                                    Color.White.copy(
                                        alpha = 0.40f
                                    ),

                                fontSize = 8.sp
                            )

                            Text(
                                text =
                                    journeyDate?.let {
                                        formatDisplayDate(it)
                                    }
                                        ?: "তারিখ নির্ধারণ করুন",

                                color = Color.White,

                                fontSize = 13.sp,

                                fontWeight =
                                    FontWeight.ExtraBold
                            )
                        }
                    }
                }


                Spacer(
                    Modifier.height(7.dp)
                )


                // =================================================
                // INFORMATION
                // =================================================

                Surface(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(15.dp),

                    color =
                        Color(0xFF064B36)
                ) {

                    Row(
                        modifier =
                            Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 7.dp
                            ),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(
                            text = "💡",
                            fontSize = 15.sp
                        )

                        Spacer(
                            Modifier.width(6.dp)
                        )

                        Text(
                            text =
                                "আয় ও খরচ গত ৩ মাসের transaction থেকে অটো হিসাব করা হচ্ছে।",

                            color =
                                Color.White.copy(
                                    alpha = 0.68f
                                ),

                            fontSize = 8.sp,

                            lineHeight = 11.sp
                        )
                    }
                }


                Spacer(
                    Modifier.height(8.dp)
                )


                // =================================================
                // BUTTONS
                // =================================================

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    OutlinedButton(
                        onClick = onDismiss,

                        modifier =
                            Modifier
                                .weight(0.85f)
                                .height(48.dp),

                        shape =
                            RoundedCornerShape(16.dp),

                        border =
                            androidx.compose.foundation
                                .BorderStroke(
                                    1.dp,
                                    Color.White.copy(
                                        alpha = 0.12f
                                    )
                                ),

                        colors =
                            ButtonDefaults
                                .outlinedButtonColors(
                                    containerColor =
                                        Color.White.copy(
                                            alpha = 0.04f
                                        ),

                                    contentColor =
                                        Color.White
                                )
                    ) {

                        Text(
                            text = "বন্ধ",

                            fontSize = 12.sp,

                            fontWeight =
                                FontWeight.Bold
                        )
                    }


                    Button(
                        onClick = onEditDate,

                        modifier =
                            Modifier
                                .weight(1.15f)
                                .height(48.dp),

                        shape =
                            RoundedCornerShape(16.dp),

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    Color(0xFF18E985),

                                contentColor =
                                    Color(0xFF07130E)
                            ),

                        elevation =
                            ButtonDefaults.buttonElevation(
                                defaultElevation = 2.dp
                            )
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Edit,

                            contentDescription =
                                null,

                            modifier =
                                Modifier.size(16.dp)
                        )

                        Spacer(
                            Modifier.width(5.dp)
                        )

                        Text(
                            text =
                                "তারিখ পরিবর্তন",

                            fontSize = 11.sp,

                            fontWeight =
                                FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}


// =============================================================
// FINANCIAL CARD
// =============================================================

@Composable
private fun JourneyFinanceCard(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    value: String
) {

    Surface(
        modifier = modifier,

        shape =
            RoundedCornerShape(16.dp),

        color =
            Color(0xFF171F28)
    ) {

        Column(
            modifier =
                Modifier.padding(
                    horizontal = 9.dp,
                    vertical = 8.dp
                )
        ) {

            Surface(
                modifier =
                    Modifier.size(29.dp),

                shape =
                    RoundedCornerShape(9.dp),

                color =
                    iconColor.copy(
                        alpha = 0.10f
                    )
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector = icon,

                        contentDescription =
                            null,

                        tint = iconColor,

                        modifier =
                            Modifier.size(17.dp)
                    )
                }
            }


            Spacer(
                Modifier.height(5.dp)
            )


            Text(
                text = title,

                color =
                    Color.White.copy(
                        alpha = 0.45f
                    ),

                fontSize = 8.sp
            )


            Spacer(
                Modifier.height(2.dp)
            )


            Text(
                text = value,

                color = iconColor,

                fontSize = 11.sp,

                fontWeight =
                    FontWeight.ExtraBold
            )
        }
    }
}


// =============================================================
// DEBT MINI CARD
// =============================================================

@Composable
private fun DebtMiniInfo(
    modifier: Modifier,
    title: String,
    value: String
) {

    Surface(
        modifier = modifier,

        shape =
            RoundedCornerShape(13.dp),

        color =
            Color(0xFF10171E)
    ) {

        Column(
            modifier =
                Modifier.padding(
                    horizontal = 8.dp,
                    vertical = 7.dp
                )
        ) {

            Text(
                text = title,

                color =
                    Color.White.copy(
                        alpha = 0.38f
                    ),

                fontSize = 7.sp
            )


            Spacer(
                Modifier.height(3.dp)
            )


            Text(
                text = value,

                color = Color.White,

                fontSize = 9.sp,

                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}


// =============================================================
// EASY DATE SELECTOR
// =============================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JourneyDatePicker(
    currentDate: String?,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {

    val initialCalendar =
        remember(currentDate) {

            Calendar.getInstance().apply {

                if (!currentDate.isNullOrBlank()) {

                    try {

                        val date =
                            SimpleDateFormat(
                                "yyyy-MM-dd",
                                Locale.getDefault()
                            ).parse(
                                currentDate
                            )

                        if (date != null) {
                            time = date
                        }

                    } catch (_: Exception) {
                        // আজকের তারিখ থাকবে
                    }
                }
            }
        }


    var selectedDay by remember {
        mutableIntStateOf(
            initialCalendar.get(
                Calendar.DAY_OF_MONTH
            )
        )
    }


    var selectedMonth by remember {
        mutableIntStateOf(
            initialCalendar.get(
                Calendar.MONTH
            )
        )
    }


    var selectedYear by remember {
        mutableIntStateOf(
            initialCalendar.get(
                Calendar.YEAR
            )
        )
    }


    var showCalendar by remember {
        mutableStateOf(false)
    }


    // =========================================================
    // CALENDAR
    // =========================================================

    if (showCalendar) {

        val calendarState =
            rememberDatePickerState(
                initialSelectedDateMillis =
                    Calendar.getInstance().apply {

                        set(
                            selectedYear,
                            selectedMonth,
                            selectedDay,
                            0,
                            0,
                            0
                        )

                        set(
                            Calendar.MILLISECOND,
                            0
                        )

                    }.timeInMillis
            )


        DatePickerDialog(

            onDismissRequest = {
                showCalendar = false
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        calendarState
                            .selectedDateMillis
                            ?.let { millis ->

                                val calendar =
                                    Calendar.getInstance()

                                calendar.timeInMillis =
                                    millis

                                selectedDay =
                                    calendar.get(
                                        Calendar.DAY_OF_MONTH
                                    )

                                selectedMonth =
                                    calendar.get(
                                        Calendar.MONTH
                                    )

                                selectedYear =
                                    calendar.get(
                                        Calendar.YEAR
                                    )
                            }

                        showCalendar = false
                    }
                ) {

                    Text("ঠিক আছে")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        showCalendar = false
                    }
                ) {

                    Text("বাতিল")
                }
            }

        ) {

            DatePicker(
                state = calendarState
            )
        }
    }


    // =========================================================
    // EASY DATE DIALOG
    // =========================================================

    Dialog(
        onDismissRequest = onDismiss
    ) {

        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),

            shape =
                RoundedCornerShape(26.dp),

            color = Color.Transparent
        ) {

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            brush =
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF1B2732),
                                        Color(0xFF10181F)
                                    )
                                ),

                            shape =
                                RoundedCornerShape(26.dp)
                        )
                        .padding(17.dp)
            ) {


                // =================================================
                // HEADER
                // =================================================

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Surface(
                        modifier =
                            Modifier.size(45.dp),

                        shape =
                            RoundedCornerShape(13.dp),

                        color =
                            Color(0xFF132C45)
                    ) {

                        Box(
                            contentAlignment =
                                Alignment.Center
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.CalendarMonth,

                                contentDescription =
                                    null,

                                tint =
                                    Color(0xFF69B2FF),

                                modifier =
                                    Modifier.size(23.dp)
                            )
                        }
                    }


                    Spacer(
                        Modifier.width(10.dp)
                    )


                    Column(
                        modifier =
                            Modifier.weight(1f)
                    ) {

                        Text(
                            text =
                                "যাত্রার তারিখ",

                            color = Color.White,

                            fontSize = 17.sp,

                            fontWeight =
                                FontWeight.ExtraBold
                        )

                        Text(
                            text =
                                "সহজে আপনার তারিখ নির্বাচন করুন",

                            color =
                                Color.White.copy(
                                    alpha = 0.42f
                                ),

                            fontSize = 9.sp
                        )
                    }


                    TextButton(
                        onClick = onDismiss
                    ) {

                        Text(
                            text = "বাতিল",

                            color =
                                Color.White.copy(
                                    alpha = 0.65f
                                ),

                            fontSize = 10.sp
                        )
                    }
                }


                Spacer(
                    Modifier.height(13.dp)
                )


                // =================================================
                // SELECTED DATE
                // =================================================

                Surface(
                    modifier =
                        Modifier.fillMaxWidth(),

                    shape =
                        RoundedCornerShape(19.dp),

                    color =
                        Color(0xFF0B1720)
                ) {

                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 14.dp,
                                    vertical = 13.dp
                                ),

                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(
                            text =
                                "নির্বাচিত তারিখ",

                            color =
                                Color.White.copy(
                                    alpha = 0.38f
                                ),

                            fontSize = 8.sp
                        )


                        Spacer(
                            Modifier.height(4.dp)
                        )


                        Text(
                            text =
                                "${toBanglaNumber(selectedDay)} " +
                                        "${getBanglaMonth(selectedMonth)} " +
                                        "${toBanglaNumber(selectedYear)}",

                            color = Color.White,

                            fontSize = 20.sp,

                            fontWeight =
                                FontWeight.ExtraBold
                        )


                        Spacer(
                            Modifier.height(3.dp)
                        )


                        Text(
                            text =
                                String.format(
                                    Locale.getDefault(),
                                    "%02d/%02d/%04d",
                                    selectedDay,
                                    selectedMonth + 1,
                                    selectedYear
                                ),

                            color =
                                Color(0xFF69B2FF),

                            fontSize = 9.sp,

                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }


                Spacer(
                    Modifier.height(11.dp)
                )


                // =================================================
                // QUICK OPTIONS
                // =================================================

                Text(
                    text =
                        "দ্রুত নির্বাচন",

                    color = Color.White,

                    fontSize = 10.sp,

                    fontWeight =
                        FontWeight.Bold
                )


                Spacer(
                    Modifier.height(6.dp)
                )


                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(7.dp)
                ) {

                    DateQuickButton(
                        modifier =
                            Modifier.weight(1f),

                        text = "আজ",

                        onClick = {

                            val today =
                                Calendar.getInstance()

                            selectedDay =
                                today.get(
                                    Calendar.DAY_OF_MONTH
                                )

                            selectedMonth =
                                today.get(
                                    Calendar.MONTH
                                )

                            selectedYear =
                                today.get(
                                    Calendar.YEAR
                                )
                        }
                    )


                    DateQuickButton(
                        modifier =
                            Modifier.weight(1.5f),

                        text = "ক্যালেন্ডার",

                        onClick = {
                            showCalendar = true
                        }
                    )
                }


                Spacer(
                    Modifier.height(11.dp)
                )


                // =================================================
                // DAY / MONTH / YEAR
                // =================================================

                Text(
                    text =
                        "দিন • মাস • বছর",

                    color = Color.White,

                    fontSize = 10.sp,

                    fontWeight =
                        FontWeight.Bold
                )


                Spacer(
                    Modifier.height(6.dp)
                )


                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(6.dp)
                ) {

                    DateNumberSelector(
                        modifier =
                            Modifier.weight(0.8f),

                        label = "দিন",

                        value =
                            selectedDay,

                        min = 1,

                        max = 31,

                        onValueChange = {
                            selectedDay = it
                        }
                    )


                    DateNumberSelector(
                        modifier =
                            Modifier.weight(1.3f),

                        label = "মাস",

                        value =
                            selectedMonth,

                        min = 0,

                        max = 11,

                        displayValue = {
                            getBanglaMonth(it)
                        },

                        onValueChange = {
                            selectedMonth = it
                        }
                    )


                    DateNumberSelector(
                        modifier =
                            Modifier.weight(1f),

                        label = "বছর",

                        value =
                            selectedYear,

                        min = 2000,

                        max = 2100,

                        onValueChange = {
                            selectedYear = it
                        }
                    )
                }


                Spacer(
                    Modifier.height(12.dp)
                )


                // =================================================
                // SAVE
                // =================================================

                Button(

                    onClick = {

                        val calendar =
                            Calendar.getInstance()

                        calendar.set(
                            Calendar.YEAR,
                            selectedYear
                        )

                        calendar.set(
                            Calendar.MONTH,
                            selectedMonth
                        )

                        calendar.set(
                            Calendar.DAY_OF_MONTH,
                            1
                        )

                        val maxDay =
                            calendar.getActualMaximum(
                                Calendar.DAY_OF_MONTH
                            )

                        val validDay =
                            selectedDay.coerceIn(
                                1,
                                maxDay
                            )

                        selectedDay = validDay

                        calendar.set(
                            Calendar.DAY_OF_MONTH,
                            validDay
                        )

                        val result =
                            SimpleDateFormat(
                                "yyyy-MM-dd",
                                Locale.getDefault()
                            ).format(
                                calendar.time
                            )

                        onDateSelected(result)
                    },

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(50.dp),

                    shape =
                        RoundedCornerShape(16.dp),

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                Color(0xFF18E985),

                            contentColor =
                                Color(0xFF07130E)
                        )
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.CalendarMonth,

                        contentDescription =
                            null,

                        modifier =
                            Modifier.size(17.dp)
                    )


                    Spacer(
                        Modifier.width(6.dp)
                    )


                    Text(
                        text =
                            "তারিখ সংরক্ষণ করুন",

                        fontSize = 12.sp,

                        fontWeight =
                            FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}


// =============================================================
// QUICK BUTTON
// =============================================================

@Composable
private fun DateQuickButton(
    modifier: Modifier,
    text: String,
    onClick: () -> Unit
) {

    Surface(
        modifier =
            modifier
                .height(42.dp)
                .clickable {
                    onClick()
                },

        shape =
            RoundedCornerShape(13.dp),

        color =
            Color(0xFF182630)
    ) {

        Box(
            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text = text,

                color = Color.White,

                fontSize = 10.sp,

                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}


// =============================================================
// NUMBER SELECTOR
// =============================================================

@Composable
private fun DateNumberSelector(
    modifier: Modifier,
    label: String,
    value: Int,
    min: Int,
    max: Int,
    displayValue: ((Int) -> String)? = null,
    onValueChange: (Int) -> Unit
) {

    Surface(
        modifier = modifier,

        shape =
            RoundedCornerShape(15.dp),

        color =
            Color(0xFF18232C)
    ) {

        Column(
            modifier =
                Modifier.padding(
                    horizontal = 6.dp,
                    vertical = 7.dp
                ),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(
                text = label,

                color =
                    Color.White.copy(
                        alpha = 0.38f
                    ),

                fontSize = 7.sp
            )


            Spacer(
                Modifier.height(3.dp)
            )


            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically,

                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Surface(
                    modifier =
                        Modifier
                            .size(27.dp)
                            .clickable {

                                if (value > min) {
                                    onValueChange(
                                        value - 1
                                    )
                                }
                            },

                    shape =
                        RoundedCornerShape(8.dp),

                    color =
                        Color.White.copy(
                            alpha = 0.06f
                        )
                ) {

                    Box(
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text = "−",

                            color = Color.White,

                            fontSize = 15.sp
                        )
                    }
                }


                Text(
                    text =
                        displayValue?.invoke(value)
                            ?: toBanglaNumber(value),

                    color = Color.White,

                    fontSize = 11.sp,

                    fontWeight =
                        FontWeight.ExtraBold
                )


                Surface(
                    modifier =
                        Modifier
                            .size(27.dp)
                            .clickable {

                                if (value < max) {
                                    onValueChange(
                                        value + 1
                                    )
                                }
                            },

                    shape =
                        RoundedCornerShape(8.dp),

                    color =
                        Color.White.copy(
                            alpha = 0.06f
                        )
                ) {

                    Box(
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(
                            text = "+",

                            color = Color.White,

                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}


// =============================================================
// JOURNEY STATS
// =============================================================

private fun calculateJourneyStats(
    transactions: List<Transaction>,
    usdToBdt: Double,
    usdToMvr: Double
): JourneyStats {

    val threeMonthsAgo =
        Calendar.getInstance().apply {
            add(
                Calendar.MONTH,
                -3
            )
        }


    val dateFormat =
        SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        )


    fun convertToBdt(
        amount: Double,
        currency: String
    ): Double {

        return when (
            currency.uppercase(
                Locale.getDefault()
            )
        ) {

            "BDT" ->
                amount

            "USD" ->
                if (usdToBdt > 0) {
                    amount * usdToBdt
                } else {
                    0.0
                }

            "MVR" ->
                if (
                    usdToMvr > 0 &&
                    usdToBdt > 0
                ) {

                    amount *
                            (
                                    usdToBdt /
                                            usdToMvr
                                    )

                } else {
                    0.0
                }

            else ->
                0.0
        }
    }


    val recentTransactions =
        transactions.filter { transaction ->

            if (
                transaction.type != "income" &&
                transaction.type != "expense"
            ) {
                return@filter false
            }


            try {

                val date =
                    dateFormat.parse(
                        transaction.date
                    )
                        ?: return@filter false


                !date.before(
                    threeMonthsAgo.time
                )

            } catch (_: Exception) {

                false
            }
        }


    val income =
        recentTransactions
            .filter {
                it.type == "income"
            }
            .sumOf {

                convertToBdt(
                    it.amount,
                    it.currency
                )
            }


    val expense =
        recentTransactions
            .filter {
                it.type == "expense"
            }
            .sumOf {

                convertToBdt(
                    it.amount,
                    it.currency
                )
            }


    return JourneyStats(
        averageIncome =
            income / 3.0,

        averageExpense =
            expense / 3.0
    )
}


// =============================================================
// DEBT CALCULATION
// =============================================================

private fun calculateDebtInfo(
    loanRemaining: Double,
    monthlySaving: Double
): DebtInfo {

    if (loanRemaining <= 0.0) {

        return DebtInfo(
            remainingText =
                "ঋণ নেই",

            freeDateText =
                "ঋণমুক্ত"
        )
    }


    if (monthlySaving <= 0.0) {

        return DebtInfo(
            remainingText =
                "হিসাব করা যাচ্ছে না",

            freeDateText =
                "হিসাব করা যাচ্ছে না"
        )
    }


    val months =
        ceil(
            loanRemaining /
                    monthlySaving
        ).toInt()


    val days =
        months * 30


    val freeDate =
        Calendar.getInstance().apply {

            add(
                Calendar.MONTH,
                months
            )
        }


    val dateText =
        SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        ).format(
            freeDate.time
        )


    return DebtInfo(
        remainingText =
            "${toBanglaNumber(days)} দিন",

        freeDateText =
            dateText
    )
}


// =============================================================
// JOURNEY DURATION
// =============================================================

private fun calculateElapsedDuration(
    journeyDate: String?
): String? {

    if (
        journeyDate.isNullOrBlank()
    ) {
        return null
    }


    return try {

        val dateFormat =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            )


        val startDate =
            dateFormat.parse(
                journeyDate
            )
                ?: return null


        val start =
            Calendar.getInstance().apply {

                time = startDate

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


        val today =
            Calendar.getInstance().apply {

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


        if (start.after(today)) {

            return "০ বছর ০ মাস ০ দিন"
        }


        var years =
            today.get(
                Calendar.YEAR
            ) -
                    start.get(
                        Calendar.YEAR
                    )


        var months =
            today.get(
                Calendar.MONTH
            ) -
                    start.get(
                        Calendar.MONTH
                    )


        var days =
            today.get(
                Calendar.DAY_OF_MONTH
            ) -
                    start.get(
                        Calendar.DAY_OF_MONTH
                    )


        if (days < 0) {

            months--


            val previousMonth =
                today.clone() as Calendar


            previousMonth.add(
                Calendar.MONTH,
                -1
            )


            days +=
                previousMonth.getActualMaximum(
                    Calendar.DAY_OF_MONTH
                )
        }


        if (months < 0) {

            years--

            months += 12
        }


        "${
            toBanglaNumber(years)
        } বছর ${
            toBanglaNumber(months)
        } মাস ${
            toBanglaNumber(days)
        } দিন"

    } catch (_: Exception) {

        null
    }
}


// =============================================================
// BANGLA NUMBER
// =============================================================

private fun toBanglaNumber(
    number: Int
): String {

    val english =
        "0123456789"

    val bangla =
        "০১২৩৪৫৬৭৮৯"


    return number
        .toString()
        .map { char ->

            val index =
                english.indexOf(char)

            if (index >= 0) {
                bangla[index]
            } else {
                char
            }
        }
        .joinToString("")
}


// =============================================================
// BANGLA MONTH
// =============================================================

private fun getBanglaMonth(
    month: Int
): String {

    return when (month) {

        0 ->
            "জানুয়ারি"

        1 ->
            "ফেব্রুয়ারি"

        2 ->
            "মার্চ"

        3 ->
            "এপ্রিল"

        4 ->
            "মে"

        5 ->
            "জুন"

        6 ->
            "জুলাই"

        7 ->
            "আগস্ট"

        8 ->
            "সেপ্টেম্বর"

        9 ->
            "অক্টোবর"

        10 ->
            "নভেম্বর"

        11 ->
            "ডিসেম্বর"

        else ->
            ""
    }
}


// =============================================================
// DISPLAY DATE
// =============================================================

private fun formatDisplayDate(
    storageDate: String
): String {

    return try {

        val date =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).parse(
                storageDate
            )


        if (date == null) {

            storageDate

        } else {

            SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            ).format(
                date
            )
        }

    } catch (_: Exception) {

        storageDate
    }
}