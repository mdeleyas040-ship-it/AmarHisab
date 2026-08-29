package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.Transaction
import com.eleyas.expensetracker.ui.components.TransactionCard
import com.eleyas.expensetracker.ui.theme.*
import com.eleyas.expensetracker.util.formatMoney
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    transactions: List<Transaction> = emptyList(),
    onDateSelected: (String) -> Unit = {}
) {
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {

        CalendarHeader(
            currentMonth = currentMonth,
            onPreviousMonth = {
                currentMonth = Calendar.getInstance().apply {
                    time = currentMonth.time
                    add(Calendar.MONTH, -1)
                }
            },
            onNextMonth = {
                currentMonth = Calendar.getInstance().apply {
                    time = currentMonth.time
                    add(Calendar.MONTH, 1)
                }
            }
        )

        CalendarGrid(
            currentMonth = currentMonth,
            transactions = transactions,
            selectedDate = selectedDate,
            onDateClick = { date ->
                selectedDate = date
                onDateSelected(date)
            }
        )

        if (selectedDate != null) {
            TransactionDetailsForDate(
                date = selectedDate ?: "",
                transactions = transactions
            )
        }
    }
}

@Composable
private fun CalendarHeader(
    currentMonth: Calendar,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("bn", "BD"))
    val monthName = monthFormat.format(currentMonth.time)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 14.dp,
                bottom = 10.dp
            )
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = "Calendar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "ক্যালেন্ডার",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = monthName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onPreviousMonth() },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Month",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onNextMonth() },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Month",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: Calendar,
    transactions: List<Transaction>,
    selectedDate: String?,
    onDateClick: (String) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, currentMonth.get(Calendar.YEAR))
        set(Calendar.MONTH, currentMonth.get(Calendar.MONTH))
        set(Calendar.DAY_OF_MONTH, 1)
    }

    val firstDayRaw = calendar.get(Calendar.DAY_OF_WEEK)
    val firstDayOfWeek = (firstDayRaw + 5) % 7

    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7

    val dayLabels = listOf(
        "সোম", "মঙ্গল", "বুধ", "বৃহ",
        "শুক্র", "শনি", "রবি"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 10.dp
            )
    ) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(10.dp)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    dayLabels.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier
                                .weight(1f)
                                .height(26.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                for (week in 0 until totalCells / 7) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {

                        for (dayOfWeek in 0..6) {

                            val cellIndex = week * 7 + dayOfWeek
                            val dayOfMonth =
                                cellIndex - firstDayOfWeek + 1

                            if (dayOfMonth in 1..daysInMonth) {

                                val dateCalendar =
                                    Calendar.getInstance().apply {
                                        set(
                                            Calendar.YEAR,
                                            currentMonth.get(Calendar.YEAR)
                                        )
                                        set(
                                            Calendar.MONTH,
                                            currentMonth.get(Calendar.MONTH)
                                        )
                                        set(
                                            Calendar.DAY_OF_MONTH,
                                            dayOfMonth
                                        )
                                    }

                                val dateString =
                                    dateFormat.format(dateCalendar.time)

                                val dayTransactions =
                                    transactions.filter {
                                        it.date == dateString
                                    }

                                val totalIncome =
                                    dayTransactions
                                        .filter { it.type == "income" }
                                        .sumOf { it.amount }

                                val totalExpense =
                                    dayTransactions
                                        .filter { it.type == "expense" }
                                        .sumOf { it.amount }

                                val totalHome =
                                    dayTransactions
                                        .filter { it.type == "home" }
                                        .sumOf { it.amount }

                                val totalHomeExpense =
                                    dayTransactions
                                        .filter { it.type == "home_expense" }
                                        .sumOf { it.amount }

                                val netAmount =
                                    totalIncome +
                                            totalHome -
                                            totalExpense -
                                            totalHomeExpense

                                CalendarDayCell(
                                    dayOfMonth = dayOfMonth,
                                    income = totalIncome,
                                    expense = totalExpense,
                                    home = totalHome,
                                    homeExpense = totalHomeExpense,
                                    netAmount = netAmount,
                                    isSelected = selectedDate == dateString,
                                    isToday = dateString ==
                                            dateFormat.format(
                                                Calendar.getInstance().time
                                            ),
                                    onClick = {
                                        onDateClick(dateString)
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                            } else {

                                Spacer(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(80.dp)
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
private fun CalendarDayCell(
    dayOfMonth: Int,
    income: Double,
    expense: Double,
    home: Double,
    homeExpense: Double,
    netAmount: Double,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val hasTransactions =
        income > 0 || expense > 0 || home > 0 || homeExpense > 0

    val backgroundColor = when {
        isSelected -> AccentGreen.copy(alpha = 0.14f)
        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.09f)
        hasTransactions -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    val borderColor = when {
        isSelected -> AccentGreen
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }

    val netLineColor = when {
        netAmount > 0 -> IncomeGreen
        netAmount < 0 -> ExpenseRed
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    }

    val cellShape = RoundedCornerShape(13.dp)

    Box(
        modifier = modifier
            .height(80.dp)
            .clip(cellShape)
            .background(backgroundColor)
            .border(
                width = if (isSelected || isToday) 1.5.dp else 1.dp,
                color = borderColor,
                shape = cellShape
            )
            .clickable(enabled = hasTransactions, onClick = onClick)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 3.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isToday -> MaterialTheme.colorScheme.primary
                            isSelected -> AccentGreen.copy(alpha = 0.22f)
                            else -> Color.Transparent
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayOfMonth.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = when {
                        isToday -> MaterialTheme.colorScheme.onPrimary
                        isSelected -> AccentGreen
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            if (hasTransactions) {

                Spacer(modifier = Modifier.height(3.dp))

                if (expense > 0) {
                    MiniAmountRow(
                        color = ExpenseRed,
                        sign = "-",
                        amount = expense
                    )
                }

                if (income > 0) {
                    MiniAmountRow(
                        color = IncomeGreen,
                        sign = "+",
                        amount = income
                    )
                }

                if (home > 0) {
                    MiniAmountRow(
                        color = Color(0xFF1976D2),
                        sign = "↓",
                        amount = home
                    )
                }

                if (homeExpense > 0) {
                    MiniAmountRow(
                        color = Color(0xFFFF9800),
                        sign = "↑",
                        amount = homeExpense
                    )
                }

            }
        }

        // Net-balance indicator strip at the bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(3.dp)
                .background(netLineColor)
        )
    }
}

@Composable
private fun MiniAmountRow(
    color: Color,
    sign: String,
    amount: Double
) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(color)
        )

        Spacer(modifier = Modifier.width(3.dp))

        Text(
            text = "$sign৳${compactAmount(amount)}",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1
        )
    }
}

private fun compactAmount(amount: Double): String {
    val abs = kotlin.math.abs(amount)
    return when {
        abs >= 1_000_000.0 ->
            String.format(Locale.US, "%.1fM", abs / 1_000_000.0)

        abs >= 1_000.0 ->
            String.format(Locale.US, "%.1fk", abs / 1_000.0)

        else ->
            String.format(Locale.US, "%.0f", abs)
    }
}

@Composable
private fun TransactionDetailsForDate(
    date: String,
    transactions: List<Transaction>
) {
    val dateTransactions =
        transactions.filter { it.date == date }

    if (dateTransactions.isEmpty()) return

    val totalIncome =
        dateTransactions
            .filter { it.type == "income" }
            .sumOf { it.amount }

    val totalExpense =
        dateTransactions
            .filter { it.type == "expense" }
            .sumOf { it.amount }

    val totalHome =
        dateTransactions
            .filter { it.type == "home" }
            .sumOf { it.amount }

    val totalHomeExpense =
        dateTransactions
            .filter { it.type == "home_expense" }
            .sumOf { it.amount }

    val netAmount =
        totalIncome + totalHome - totalExpense - totalHomeExpense

    val displayDate = try {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .parse(date)?.let {
                SimpleDateFormat("d MMMM yyyy", Locale("bn", "BD")).format(it)
            } ?: date
    } catch (_: Exception) {
        date
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 4.dp
            )
    ) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "সেদিনের লেনদেন",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = displayDate,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    if (totalIncome > 0) {
                        SummaryBox(
                            label = "আয়",
                            amount = totalIncome,
                            color = IncomeGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (totalExpense > 0) {
                        SummaryBox(
                            label = "খরচ",
                            amount = totalExpense,
                            color = ExpenseRed,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (totalHome > 0) {
                        SummaryBox(
                            label = "হোম জমা",
                            amount = totalHome,
                            color = Color(0xFF1976D2),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (totalHomeExpense > 0) {
                        SummaryBox(
                            label = "হোম খরচ",
                            amount = totalHomeExpense,
                            color = Color(0xFFFF9800),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = (if (netAmount >= 0) IncomeGreen else ExpenseRed)
                        .copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "দিনশেষ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (netAmount >= 0)
                                "+৳${formatMoney(netAmount)}"
                            else
                                "-৳${formatMoney(-netAmount)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (netAmount >= 0) IncomeGreen else ExpenseRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dateTransactions.forEach { transaction ->

                        TransactionCard(
                            transaction = transaction,
                            usdToBdt = 120.0,
                            usdToMvr = 15.0
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryBox(
    label: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {

    Surface(
        modifier = modifier.height(74.dp),
        shape = RoundedCornerShape(15.dp),
        color = color.copy(alpha = 0.10f)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "৳${"%.0f".format(amount)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
    }
}