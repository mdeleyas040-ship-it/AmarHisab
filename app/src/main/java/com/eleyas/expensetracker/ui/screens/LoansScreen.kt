package com.eleyas.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.*
import com.eleyas.expensetracker.ui.components.*
import com.eleyas.expensetracker.ui.theme.*
import com.eleyas.expensetracker.util.displayLoanDate
import com.eleyas.expensetracker.util.formatMoney
private val TealGreen = Color(0xFF16A085)

@Composable
fun LoansScreen(
    modifier: Modifier,
    loans: List<LoanAccount>,
    loanPayments: List<LoanPayment>,
    lendings: List<LendingAccount>,
    lendingReturns: List<LendingReturn>,
    onAddLoan: () -> Unit,
    onAddLoanPayment: (LoanAccount) -> Unit,
    onEditLoan: (LoanAccount) -> Unit,
    onEditBorrowing: (LoanAccount, LoanBorrowing) -> Unit,
    onDeleteBorrowing: (LoanAccount, LoanBorrowing) -> Unit,
    onAddLending: () -> Unit,
    onAddLendingReturn: (LendingAccount) -> Unit,
    onEditLending: (LendingAccount) -> Unit = {},
    onDeleteLending: (LendingAccount) -> Unit = {},
    loanInterestTerms: List<LoanInterestTerms>,
    onShowCalculator: () -> Unit = {},
    onShareLoan: (LoanAccount, Boolean) -> Unit = { _, _ -> },
    onShareLending: (LendingAccount, Boolean) -> Unit = { _, _ -> },
    searchQuery: String = ""
) {

    // =========================================================
    // SCREEN STATE
    // =========================================================

    var showLending by remember {
        mutableStateOf(false)
    }

    var shareOptionsLoan by remember {
        mutableStateOf<LoanAccount?>(null)
    }

    var shareOptionsLending by remember {
        mutableStateOf<LendingAccount?>(null)
    }

    var showPaymentHistoryLoan by remember {
        mutableStateOf<LoanAccount?>(null)
    }

    var expandedLoanId by remember {
        mutableStateOf<Long?>(null)
    }

    var expandedLendingId by remember {
        mutableStateOf<Long?>(null)
    }

    // =========================================================
    // LOAN SHARE DIALOG
    // =========================================================

    if (shareOptionsLoan != null) {

        AlertDialog(
            onDismissRequest = {
                shareOptionsLoan = null
            },
            title = {
                Text("স্টেটমেন্ট শেয়ার করুন")
            },
            text = {
                Text(
                    "${shareOptionsLoan!!.name} ঋণের স্টেটমেন্ট " +
                            "কিভাবে শেয়ার করতে চান?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onShareLoan(
                            shareOptionsLoan!!,
                            true
                        )
                        shareOptionsLoan = null
                    }
                ) {
                    Text("PDF ফাইল")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onShareLoan(
                            shareOptionsLoan!!,
                            false
                        )
                        shareOptionsLoan = null
                    }
                ) {
                    Text("মেসেজ (Text)")
                }
            }
        )
    }

    // =========================================================
    // LENDING SHARE DIALOG
    // =========================================================


    // =========================================================
    // LOAN PAYMENT HISTORY
    // =========================================================

    if (showPaymentHistoryLoan != null) {

        val historyLoan = showPaymentHistoryLoan!!

        val paymentHistory =
            loanPayments.filter {
                it.loanId == historyLoan.id
            }

        AlertDialog(
            onDismissRequest = {
                showPaymentHistoryLoan = null
            },
            title = {
                Text("পরিশোধের তথ্য")
            },
            text = {

                Column {

                    if (paymentHistory.isEmpty()) {

                        Text(
                            "কোনো পরিশোধের তথ্য নেই"
                        )

                    } else {

                        paymentHistory.forEach { payment ->

                            Text(
                                "✓ ${
                                    displayLoanDate(
                                        payment.date
                                    )
                                } — ৳${
                                    formatMoney(
                                        payment.amount
                                    )
                                } পরিশোধ করা হয়েছে"
                            )

                            Spacer(
                                Modifier.height(6.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPaymentHistoryLoan = null
                    }
                ) {
                    Text("ঠিক আছে")
                }
            }
        )
    }

    // =========================================================
    // FILTERED LOANS
    // =========================================================

    val filteredLoans =
        loans.filter { loan ->

            loan.name.contains(
                searchQuery,
                ignoreCase = true
            ) ||
                    loan.note.contains(
                        searchQuery,
                        ignoreCase = true
                    )
        }

    // =========================================================
    // LOAN TOTALS
    // =========================================================

    val totalBorrowed =
        loans.sumOf {
            it.principal
        }

    val totalInterest =
        loans.sumOf { loan ->

            loanInterestTerms
                .firstOrNull {
                    it.loanId == loan.id
                }
                ?.totalInterest
                ?: 0.0
        }

    val totalPaid =
        loanPayments.sumOf {
            it.amount
        }

    val totalRemaining =
        (
                totalBorrowed +
                        totalInterest -
                        totalPaid
                )
            .coerceAtLeast(0.0)

    // =========================================================
    // LENDING TOTALS
    // =========================================================

    val totalLent =
        lendings.sumOf {
            it.amount
        }

    val totalReturned =
        lendingReturns.sumOf {
            it.amount
        }

    val totalReceivable =
        (
                totalLent -
                        totalReturned
                )
            .coerceAtLeast(0.0)

    // =========================================================
    // MAIN SCREEN
    // =========================================================

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.background
            ),
        contentPadding = PaddingValues(
            start = 24.dp,
            end = 24.dp,
            top = 14.dp,
            bottom = 100.dp
        ),
        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {

        // =====================================================
        // PREMIUM BALANCE DASHBOARD
        // =====================================================

        item {

            PremiumBalanceCard(
                isLending = showLending,
                totalBorrowed = totalBorrowed,
                totalPaid = totalPaid,
                totalRemaining = totalRemaining,
                totalLent = totalLent,
                totalReturned = totalReturned,
                totalReceivable = totalReceivable
            )
        }

        // =====================================================
        // LOAN / LENDING SWITCH
        // =====================================================

        item {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                PremiumTabButton(
                    modifier =
                        Modifier.weight(1f),
                    selected =
                        !showLending,
                    icon =
                        Icons.Default.CreditCard,
                    text =
                        "আমার ঋণ",
                    color =
                        Green,
                    onClick = {
                        showLending = false
                    }
                )

                PremiumTabButton(
                    modifier =
                        Modifier.weight(1f),
                    selected =
                        showLending,
                    icon =
                        Icons.Default.Handshake,
                    text =
                        "ধার দিয়েছি",
                    color =
                        TealGreen,
                    onClick = {
                        showLending = true
                    }
                )
            }
        }

        // =====================================================
        // LOAN MODE
        // =====================================================

        if (!showLending) {

            // -------------------------------------------------
            // LOAN SUMMARY
            // -------------------------------------------------

            item {

                PremiumThreeSummary(
                    firstTitle = "মোট নেওয়া",
                    firstAmount = totalBorrowed,
                    firstIcon = Icons.Default.CreditCard,
                    firstColor = Blue,

                    secondTitle = "পরিশোধ",
                    secondAmount = totalPaid,
                    secondIcon = Icons.Default.CheckCircle,
                    secondColor = IncomeGreen,

                    thirdTitle = "বাকি ঋণ",
                    thirdAmount = totalRemaining,
                    thirdIcon = Icons.Default.Error,
                    thirdColor = ExpenseRed
                )
            }

            // -------------------------------------------------
            // ADD LOAN
            // -------------------------------------------------

            item {

                Button(
                    onClick = onAddLoan,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                    shape =
                        RoundedCornerShape(17.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = Green
                        )
                ) {

                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier =
                            Modifier.size(23.dp)
                    )

                    Spacer(
                        Modifier.width(8.dp)
                    )

                    Text(
                        "ঋণ যোগ করুন",
                        fontSize = 16.sp,
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }

            // -------------------------------------------------
            // LOAN TITLE
            // -------------------------------------------------

            item {

                Text(
                    "আমার নেওয়া ঋণ",
                    fontSize = 21.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )
            }

            // -------------------------------------------------
            // LOAN LIST
            // -------------------------------------------------

            if (filteredLoans.isEmpty()) {

                item {

                    EmptyFinanceCard(
                        icon =
                            Icons.Default.CreditCard,
                        title =
                            "এখনও কোনো ঋণ যোগ করা হয়নি",
                        subtitle =
                            "ঋণের হিসাব এখানে দেখা যাবে"
                    )
                }

            } else {

                items(
                    filteredLoans,
                    key = {
                        it.id
                    }
                ) { loan ->

                    LoanPremiumCard(
                        loan = loan,
                        loanPayments = loanPayments,
                        loanInterestTerms =
                            loanInterestTerms,
                        expanded =
                            expandedLoanId ==
                                    loan.id,
                        onExpand = {

                            expandedLoanId =
                                if (
                                    expandedLoanId ==
                                    loan.id
                                ) {
                                    null
                                } else {
                                    loan.id
                                }
                        },
                        onShare = {
                            shareOptionsLoan =
                                loan
                        },
                        onEdit = {
                            onEditLoan(
                                loan
                            )
                        },
                        onPayment = {

                            val paid =
                                loanPayments
                                    .filter {
                                        it.loanId ==
                                                loan.id
                                    }
                                    .sumOf {
                                        it.amount
                                    }

                            val interest =
                                loanInterestTerms
                                    .firstOrNull {
                                        it.loanId ==
                                                loan.id
                                    }
                                    ?.totalInterest
                                    ?: 0.0

                            val remaining =
                                (
                                        loan.principal +
                                                interest -
                                                paid
                                        )
                                    .coerceAtLeast(
                                        0.0
                                    )

                            if (
                                remaining >
                                0.0
                            ) {
                                onAddLoanPayment(
                                    loan
                                )
                            } else {
                                showPaymentHistoryLoan =
                                    loan
                            }
                        },
                        onEditBorrowing =
                            onEditBorrowing,
                        onDeleteBorrowing =
                            onDeleteBorrowing
                    )
                }
            }

        } else {

            // =================================================
            // LENDING MODE
            // =================================================

            // -------------------------------------------------
            // LENDING SUMMARY
            // -------------------------------------------------

            item {

                PremiumThreeSummary(
                    firstTitle = "মোট ধার",
                    firstAmount = totalLent,
                    firstIcon = Icons.Default.Handshake,
                    firstColor = Blue,

                    secondTitle = "ফেরত পেলাম",
                    secondAmount = totalReturned,
                    secondIcon = Icons.Default.Paid,
                    secondColor = IncomeGreen,

                    thirdTitle = "পাওনা আছে",
                    thirdAmount = totalReceivable,
                    thirdIcon = Icons.Default.Schedule,
                    thirdColor = ExpenseRed
                )
            }

            // -------------------------------------------------
            // ADD LENDING
            // -------------------------------------------------

            item {

                Button(
                    onClick = onAddLending,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                    shape =
                        RoundedCornerShape(17.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                TealGreen
                        )
                ) {

                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier =
                            Modifier.size(23.dp)
                    )

                    Spacer(
                        Modifier.width(8.dp)
                    )

                    Text(
                        "ধার দিন",
                        fontSize = 16.sp,
                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }

            // -------------------------------------------------
            // LENDING TITLE
            // -------------------------------------------------

            item {

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically,
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Column {

                        Text(
                            "আমি যাদের ধার দিয়েছি",
                            fontSize = 21.sp,
                            fontWeight =
                                FontWeight.ExtraBold
                        )

                        Text(
                            "পাওনা ও ফেরতের বিস্তারিত হিসাব",
                            fontSize = 12.sp,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }

                    Surface(
                        shape =
                            RoundedCornerShape(13.dp),
                        color =
                            TealGreen.copy(
                                alpha = 0.10f
                            )
                    ) {

                        Text(
                            "${lendings.size} টি",
                            modifier =
                                Modifier.padding(
                                    horizontal = 13.dp,
                                    vertical = 8.dp
                                ),
                            color =
                                TealGreen,
                            fontSize = 12.sp,
                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }

            // -------------------------------------------------
            // LENDING LIST
            // -------------------------------------------------

            if (lendings.isEmpty()) {

                item {

                    EmptyFinanceCard(
                        icon =
                            Icons.Default.Handshake,
                        title =
                            "এখনও কাউকে ধার দেওয়ার হিসাব নেই",
                        subtitle =
                            "ধার দেওয়ার হিসাব এখানে দেখা যাবে"
                    )
                }

            } else {

                items(
                    lendings,
                    key = {
                        it.id
                    }
                ) { lending ->

                    LendingPremiumCard(
                        lending = lending,
                        lendingReturns =
                            lendingReturns,
                        expanded =
                            expandedLendingId ==
                                    lending.id,
                        onExpand = {

                            expandedLendingId =
                                if (
                                    expandedLendingId ==
                                    lending.id
                                ) {
                                    null
                                } else {
                                    lending.id
                                }
                        },
                        onShare = {
                            shareOptionsLending =
                                lending
                        },
                        onEdit = {
                            onEditLending(
                                lending
                            )
                        },
                        onDelete = {
                            onDeleteLending(
                                lending
                            )
                        },
                        onAddReturn = {
                            onAddLendingReturn(
                                lending
                            )
                        }
                    )
                }
            }
        }
    }
}


// =====================================================================
// PREMIUM BALANCE CARD
// =====================================================================

@Composable
private fun PremiumBalanceCard(
    isLending: Boolean,
    totalBorrowed: Double,
    totalPaid: Double,
    totalRemaining: Double,
    totalLent: Double,
    totalReturned: Double,
    totalReceivable: Double
) {

    val primaryColor =
        if (isLending) {
            TealGreen
        } else {
            Green
        }

    val balance =
        if (isLending) {
            totalReceivable
        } else {
            totalRemaining
        }

    val total =
        if (isLending) {
            totalLent
        } else {
            totalBorrowed
        }

    val returned =
        if (isLending) {
            totalReturned
        } else {
            totalPaid
        }

    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(28.dp),
        color =
            primaryColor,
        shadowElevation = 7.dp
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Surface(
                        shape =
                            CircleShape,
                        color =
                            Color.White.copy(
                                alpha = 0.13f
                            ),
                        modifier =
                            Modifier.size(58.dp)
                    ) {

                        Box(
                            contentAlignment =
                                Alignment.Center
                        ) {

                            Icon(
                                if (isLending) {
                                    Icons.Default.Handshake
                                } else {
                                    Icons.Default.CreditCard
                                },
                                contentDescription = null,
                                tint =
                                    Color.White,
                                modifier =
                                    Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(
                        Modifier.width(12.dp)
                    )

                    Column {

                        Text(
                            if (isLending) {
                                "LENDING BALANCE"
                            } else {
                                "LOAN BALANCE"
                            },
                            color =
                                Color.White.copy(
                                    alpha = 0.75f
                                ),
                            fontSize = 12.sp,
                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            Modifier.height(3.dp)
                        )

                        Text(
                            if (isLending) {
                                "বর্তমান পাওনা"
                            } else {
                                "বর্তমান বাকি ঋণ"
                            },
                            color =
                                Color.White,
                            fontSize = 18.sp,
                            fontWeight =
                                FontWeight.ExtraBold
                        )
                    }
                }

                Surface(
                    shape =
                        RoundedCornerShape(15.dp),
                    color =
                        Color.White.copy(
                            alpha = 0.10f
                        )
                ) {

                    Text(
                        if (isLending) {
                            "ধার"
                        } else {
                            "ঋণ"
                        },
                        color =
                            Color.White,
                        fontSize = 12.sp,
                        fontWeight =
                            FontWeight.Bold,
                        modifier =
                            Modifier.padding(
                                horizontal = 15.dp,
                                vertical = 11.dp
                            )
                    )
                }
            }

            Spacer(
                Modifier.height(22.dp)
            )

            Text(
                "৳${formatMoney(balance)}",
                color =
                    Color.White,
                fontSize = 38.sp,
                fontWeight =
                    FontWeight.ExtraBold
            )

            Spacer(
                Modifier.height(20.dp)
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(9.dp)
            ) {

                PremiumBalanceMiniCard(
                    modifier =
                        Modifier.weight(1f),
                    icon =
                        if (isLending) {
                            Icons.Default.AccountBalance
                        } else {
                            Icons.Default.AccountBalance
                        },
                    title =
                        if (isLending) {
                            "মোট ধার"
                        } else {
                            "মোট নেওয়া"
                        },
                    amount =
                        total
                )

                PremiumBalanceMiniCard(
                    modifier =
                        Modifier.weight(1f),
                    icon =
                        Icons.Default.CheckCircle,
                    title =
                        if (isLending) {
                            "ফেরত"
                        } else {
                            "পরিশোধ"
                        },
                    amount =
                        returned
                )

                PremiumBalanceMiniCard(
                    modifier =
                        Modifier.weight(1f),
                    icon =
                        Icons.Default.Schedule,
                    title =
                        if (isLending) {
                            "পাওনা"
                        } else {
                            "বাকি"
                        },
                    amount =
                        balance
                )
            }
        }
    }
}


// =====================================================================
// BALANCE MINI CARD
// =====================================================================

@Composable
private fun PremiumBalanceMiniCard(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    amount: Double
) {

    Surface(
        modifier = modifier,
        shape =
            RoundedCornerShape(17.dp),
        color =
            Color.White.copy(
                alpha = 0.10f
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 12.dp
                )
        ) {

            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Icon(
                    icon,
                    contentDescription = null,
                    tint =
                        Color.White.copy(
                            alpha = 0.85f
                        ),
                    modifier =
                        Modifier.size(17.dp)
                )

                Spacer(
                    Modifier.width(5.dp)
                )

                Text(
                    title,
                    color =
                        Color.White.copy(
                            alpha = 0.72f
                        ),
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }

            Spacer(
                Modifier.height(7.dp)
            )

            Text(
                "৳${formatMoney(amount)}",
                color =
                    Color.White,
                fontSize = 13.sp,
                fontWeight =
                    FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }
}


// =====================================================================
// PREMIUM TAB BUTTON
// =====================================================================

@Composable
private fun PremiumTabButton(
    modifier: Modifier,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color,
    onClick: () -> Unit
) {

    if (selected) {

        Button(
            onClick = onClick,
            modifier =
                modifier.height(58.dp),
            shape =
                RoundedCornerShape(17.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = color
                ),
            contentPadding =
                PaddingValues(
                    horizontal = 8.dp
                )
        ) {

            Icon(
                icon,
                contentDescription = null,
                modifier =
                    Modifier.size(22.dp)
            )

            Spacer(
                Modifier.width(7.dp)
            )

            Text(
                text,
                fontSize = 15.sp,
                fontWeight =
                    FontWeight.Bold,
                maxLines = 1
            )
        }

    } else {

        OutlinedButton(
            onClick = onClick,
            modifier =
                modifier.height(58.dp),
            shape =
                RoundedCornerShape(17.dp),
            colors =
                ButtonDefaults.outlinedButtonColors(
                    containerColor =
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant
                            .copy(alpha = 0.55f),
                    contentColor =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                ),
            border = null,
            contentPadding =
                PaddingValues(
                    horizontal = 8.dp
                )
        ) {

            Icon(
                icon,
                contentDescription = null,
                modifier =
                    Modifier.size(22.dp)
            )

            Spacer(
                Modifier.width(7.dp)
            )

            Text(
                text,
                fontSize = 15.sp,
                fontWeight =
                    FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}


// =====================================================================
// THREE SUMMARY
// =====================================================================

@Composable
private fun PremiumThreeSummary(
    firstTitle: String,
    firstAmount: Double,
    firstIcon: androidx.compose.ui.graphics.vector.ImageVector,
    firstColor: Color,
    secondTitle: String,
    secondAmount: Double,
    secondIcon: androidx.compose.ui.graphics.vector.ImageVector,
    secondColor: Color,
    thirdTitle: String,
    thirdAmount: Double,
    thirdIcon: androidx.compose.ui.graphics.vector.ImageVector,
    thirdColor: Color
) {

    Row(
        modifier =
            Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {

        PremiumSummaryItem(
            modifier =
                Modifier.weight(1f),
            title =
                firstTitle,
            amount =
                firstAmount,
            icon =
                firstIcon,
            color =
                firstColor
        )

        PremiumSummaryItem(
            modifier =
                Modifier.weight(1f),
            title =
                secondTitle,
            amount =
                secondAmount,
            icon =
                secondIcon,
            color =
                secondColor
        )

        PremiumSummaryItem(
            modifier =
                Modifier.weight(1f),
            title =
                thirdTitle,
            amount =
                thirdAmount,
            icon =
                thirdIcon,
            color =
                thirdColor
        )
    }
}


// =====================================================================
// SUMMARY ITEM
// =====================================================================

@Composable
private fun PremiumSummaryItem(
    modifier: Modifier,
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {

    Column(
        modifier =
            modifier
                .padding(
                    vertical = 3.dp
                )
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier =
                    Modifier.size(21.dp)
            )

            Spacer(
                Modifier.width(6.dp)
            )

            Text(
                title,
                color = color,
                fontSize = 12.sp,
                maxLines = 1
            )
        }

        Spacer(
            Modifier.height(8.dp)
        )

        Text(
            "৳${formatMoney(amount)}",
            color = color,
            fontSize = 18.sp,
            fontWeight =
                FontWeight.ExtraBold,
            maxLines = 1
        )

        Spacer(
            Modifier.height(3.dp)
        )

        Text(
            "BDT",
            fontSize = 11.sp,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant
        )
    }
}


// =====================================================================
// EMPTY CARD
// =====================================================================

@Composable
private fun EmptyFinanceCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {

    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(18.dp),
        color =
            MaterialTheme
                .colorScheme
                .surface
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Icon(
                icon,
                contentDescription = null,
                tint =
                    MaterialTheme
                        .colorScheme
                        .primary,
                modifier =
                    Modifier.size(42.dp)
            )

            Spacer(
                Modifier.height(10.dp)
            )

            Text(
                title,
                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                Modifier.height(3.dp)
            )

            Text(
                subtitle,
                fontSize = 12.sp,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}


// =====================================================================
// LOAN PREMIUM CARD
// =====================================================================

@Composable
private fun LoanPremiumCard(
    loan: LoanAccount,
    loanPayments: List<LoanPayment>,
    loanInterestTerms: List<LoanInterestTerms>,
    expanded: Boolean,
    onExpand: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onPayment: () -> Unit,
    onEditBorrowing:
        (LoanAccount, LoanBorrowing) -> Unit,
    onDeleteBorrowing:
        (LoanAccount, LoanBorrowing) -> Unit
) {

    val paid =
        loanPayments
            .filter {
                it.loanId == loan.id
            }
            .sumOf {
                it.amount
            }

    val interest =
        loanInterestTerms
            .firstOrNull {
                it.loanId == loan.id
            }
            ?.totalInterest
            ?: 0.0

    val totalPayable =
        loan.principal +
                interest

    val remaining =
        (
                totalPayable -
                        paid
                )
            .coerceAtLeast(0.0)

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(19.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surface
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            ),
        onClick = onExpand
    ) {

        Column(
            modifier =
                Modifier.padding(18.dp)
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {

                    Text(
                        loan.name.ifBlank {
                            "ঋণ"
                        },
                        fontSize = 18.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )

                    if (
                        loan.note.isNotBlank()
                    ) {

                        Text(
                            loan.note,
                            fontSize = 12.sp,
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                        )
                    }

                    Text(
                        if (
                            loan.sourceType ==
                            "bank"
                        ) {
                            "🏦 ব্যাংক ঋণ"
                        } else {
                            "👤 ব্যক্তিগত ঋণ"
                        },
                        fontSize = 11.sp,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }

                Column(
                    horizontalAlignment =
                        Alignment.End
                ) {

                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        IconButton(
                            onClick = onShare,
                            modifier =
                                Modifier.size(32.dp)
                        ) {

                            Icon(
                                Icons.Default.Share,
                                contentDescription = null,
                                tint =
                                    MaterialTheme
                                        .colorScheme
                                        .primary,
                                modifier =
                                    Modifier.size(18.dp)
                            )
                        }

                        Text(
                            "৳${
                                formatMoney(
                                    if (remaining <= 0.0) {
                                        paid
                                    } else {
                                        remaining
                                    }
                                )
                            }",
                            fontSize = 17.sp,
                            fontWeight =
                                FontWeight.ExtraBold,
                            color =
                                if (remaining <= 0.0) {
                                    IncomeGreen
                                } else {
                                    ExpenseRed
                                }
                        )
                    }

                    if (remaining <= 0.0) {

                        // Fully paid: show only the paid status.
                        Text(
                            "পরিশোধিত",
                            fontSize = 12.sp,
                            fontWeight =
                                FontWeight.Bold,
                            color =
                                IncomeGreen
                        )

                    } else {

                        // Outstanding: show only the remaining status.
                        // The paid amount remains available in the expanded
                        // payment details / payment history.
                        Text(
                            "বাকি আছে",
                            fontSize = 12.sp,
                            fontWeight =
                                FontWeight.Bold,
                            color =
                                ExpenseRed
                        )
                    }
                }
            }

            if (expanded) {

                Spacer(
                    Modifier.height(14.dp)
                )

                HorizontalDivider()

                Spacer(
                    Modifier.height(14.dp)
                )

                LoanInfoRow(
                    "মোট ঋণ",
                    loan.principal
                )

                LoanInfoRow(
                    "মোট সুদ",
                    interest
                )

                LoanInfoRow(
                    "মোট পরিশোধযোগ্য",
                    totalPayable
                )

                LoanInfoRow(
                    "পরিশোধিত",
                    paid
                )

                LoanInfoRow(
                    "বাকি",
                    remaining
                )

                if (
                    loan.monthlyInstallment > 0
                ) {

                    LoanInfoRow(
                        "মাসিক কিস্তি",
                        loan.monthlyInstallment
                    )
                }

                LoanInfoRowText(
                    "শুরু",
                    displayLoanDate(
                        loan.startDate
                    )
                )

                if (
                    loan.dueDate != null
                ) {

                    LoanInfoRowText(
                        "পরিশোধের তারিখ",
                        displayLoanDate(
                            loan.dueDate
                        )
                    )
                }

                if (
                    loan.borrowings.isNotEmpty()
                ) {

                    Spacer(
                        Modifier.height(12.dp)
                    )

                    Text(
                        "নেওয়ার History",
                        fontSize = 14.sp,
                        fontWeight =
                            FontWeight.Bold
                    )

                    loan.borrowings
                        .sortedByDescending {
                            it.date
                        }
                        .forEachIndexed {
                                index,
                                borrowing ->

                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            vertical =
                                                4.dp
                                        )
                            ) {

                                Text(
                                    "${
                                        index + 1
                                    }. ৳${
                                        formatMoney(
                                            borrowing.amount
                                        )
                                    } — ${
                                        displayLoanDate(
                                            borrowing.date
                                        )
                                    }",
                                    fontSize = 12.sp,
                                    fontWeight =
                                        FontWeight.Medium
                                )

                                if (
                                    borrowing.note
                                        .isNotBlank()
                                ) {

                                    Text(
                                        borrowing.note,
                                        fontSize = 11.sp,
                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .onSurfaceVariant
                                    )
                                }

                                Row(
                                    modifier =
                                        Modifier.fillMaxWidth(),
                                    horizontalArrangement =
                                        Arrangement.End
                                ) {

                                    IconButton(
                                        onClick = {
                                            onEditBorrowing(
                                                loan,
                                                borrowing
                                            )
                                        },
                                        modifier =
                                            Modifier.size(
                                                32.dp
                                            )
                                    ) {

                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = null,
                                            modifier =
                                                Modifier.size(
                                                    16.dp
                                                )
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            onDeleteBorrowing(
                                                loan,
                                                borrowing
                                            )
                                        },
                                        modifier =
                                            Modifier.size(
                                                32.dp
                                            )
                                    ) {

                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            tint =
                                                ExpenseRed,
                                            modifier =
                                                Modifier.size(
                                                    16.dp
                                                )
                                        )
                                    }
                                }
                            }
                        }
                }

                Spacer(
                    Modifier.height(12.dp)
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    OutlinedButton(
                        onClick = onEdit,
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(48.dp),
                        shape =
                            RoundedCornerShape(12.dp)
                    ) {

                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier =
                                Modifier.size(18.dp)
                        )

                        Spacer(
                            Modifier.width(5.dp)
                        )

                        Text("Edit")
                    }

                    Button(
                        onClick = onPayment,
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(48.dp),
                        shape =
                            RoundedCornerShape(12.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    Green
                            )
                    ) {

                        Icon(
                            if (
                                remaining > 0.0
                            ) {
                                Icons.Default.Add
                            } else {
                                Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            modifier =
                                Modifier.size(18.dp)
                        )

                        Spacer(
                            Modifier.width(5.dp)
                        )

                        Text(
                            if (
                                remaining > 0.0
                            ) {
                                "পরিশোধ"
                            } else {
                                "পরিশোধিত"
                            }
                        )
                    }
                }
            }
        }
    }
}


// =====================================================================
// LENDING PREMIUM CARD
// =====================================================================

@Composable
private fun LendingPremiumCard(
    lending: LendingAccount,
    lendingReturns: List<LendingReturn>,
    expanded: Boolean,
    onExpand: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddReturn: () -> Unit
) {

    val returned =
        lendingReturns
            .filter {
                it.lendingId ==
                        lending.id
            }
            .sumOf {
                it.amount
            }

    val remaining =
        (
                lending.amount -
                        returned
                )
            .coerceAtLeast(0.0)

    val progress =
        if (
            lending.amount > 0.0
        ) {

            (
                    returned /
                            lending.amount
                    )
                .coerceIn(
                    0.0,
                    1.0
                )
                .toFloat()

        } else {
            0f
        }

    val status =
        when {

            remaining <= 0.0 ->
                "পুরো ফেরত"

            returned > 0.0 ->
                "আংশিক ফেরত"

            else ->
                "পাওনা আছে"
        }

    val statusIcon =
        when {

            remaining <= 0.0 ->
                Icons.Default.CheckCircle

            returned > 0.0 ->
                Icons.Default.Sync

            else ->
                Icons.Default.AccessTime
        }

    Card(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            RoundedCornerShape(21.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surface
            ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            ),
        onClick = onExpand
    ) {

        Column(
            modifier =
                Modifier.padding(18.dp)
        ) {

            // -----------------------------------------------------
            // HEADER
            // -----------------------------------------------------

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Surface(
                    shape =
                        CircleShape,
                    color =
                        TealGreen.copy(
                            alpha = 0.12f
                        ),
                    modifier =
                        Modifier.size(56.dp)
                ) {

                    Box(
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint =
                                TealGreen,
                            modifier =
                                Modifier.size(30.dp)
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
                        lending.person.ifBlank {
                            "ব্যক্তি"
                        },
                        fontSize = 19.sp,
                        fontWeight =
                            FontWeight.ExtraBold
                    )

                    Text(
                        "ধার দেওয়া: ${
                            displayLoanDate(
                                lending.date
                            )
                        }",
                        fontSize = 12.sp,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }

                Column(
                    horizontalAlignment =
                        Alignment.End
                ) {

                    Text(
                        "৳${formatMoney(remaining)}",
                        fontSize = 19.sp,
                        fontWeight =
                            FontWeight.ExtraBold,
                        color =
                            if (
                                remaining > 0.0
                            ) {
                                TealGreen
                            } else {
                                IncomeGreen
                            }
                    )

                    Text(
                        if (
                            remaining > 0.0
                        ) {
                            "পাওনা"
                        } else {
                            "ফেরত পেয়েছি"
                        },
                        fontSize = 11.sp,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )
                }
            }

            Spacer(
                Modifier.height(14.dp)
            )

            // -----------------------------------------------------
            // STATUS + ARROW
            // -----------------------------------------------------

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween,
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Surface(
                    shape =
                        RoundedCornerShape(12.dp),
                    color =
                        TealGreen.copy(
                            alpha = 0.10f
                        )
                ) {

                    Row(
                        modifier =
                            Modifier.padding(
                                horizontal = 12.dp,
                                vertical = 9.dp
                            ),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Icon(
                            statusIcon,
                            contentDescription = null,
                            tint =
                                TealGreen,
                            modifier =
                                Modifier.size(19.dp)
                        )

                        Spacer(
                            Modifier.width(7.dp)
                        )

                        Text(
                            status,
                            fontSize = 12.sp,
                            fontWeight =
                                FontWeight.Bold,
                            color =
                                TealGreen
                        )
                    }
                }

                Icon(
                    if (expanded) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                    contentDescription = null,
                    modifier =
                        Modifier.size(28.dp),
                    tint =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }

            Spacer(
                Modifier.height(12.dp)
            )

            // -----------------------------------------------------
            // PROGRESS
            // -----------------------------------------------------

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Text(
                    "ফেরত: ৳${formatMoney(returned)}",
                    fontSize = 12.sp,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )

                Text(
                    "মোট: ৳${formatMoney(lending.amount)}",
                    fontSize = 12.sp,
                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                Modifier.height(7.dp)
            )

            LinearProgressIndicator(
                progress = {
                    progress
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                color =
                    TealGreen,
                trackColor =
                    TealGreen.copy(
                        alpha = 0.10f
                    )
            )

            // -----------------------------------------------------
            // EXPANDED
            // -----------------------------------------------------

            if (expanded) {

                Spacer(
                    Modifier.height(16.dp)
                )

                HorizontalDivider(
                    color =
                        MaterialTheme
                            .colorScheme
                            .outlineVariant
                )

                Spacer(
                    Modifier.height(16.dp)
                )

                // -------------------------------------------------
                // SUMMARY CARDS
                // -------------------------------------------------

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    LendingSummaryCard(
                        modifier =
                            Modifier.weight(1f),
                        title =
                            "মোট ধার",
                        amount =
                            lending.amount
                    )

                    LendingSummaryCard(
                        modifier =
                            Modifier.weight(1f),
                        title =
                            "ফেরত",
                        amount =
                            returned
                    )

                    LendingSummaryCard(
                        modifier =
                            Modifier.weight(1f),
                        title =
                            "বাকি",
                        amount =
                            remaining
                    )
                }

                // -------------------------------------------------
                // NOTE
                // -------------------------------------------------

                if (
                    lending.note.isNotBlank()
                ) {

                    Spacer(
                        Modifier.height(14.dp)
                    )

                    Surface(
                        modifier =
                            Modifier.fillMaxWidth(),
                        shape =
                            RoundedCornerShape(16.dp),
                        color =
                            MaterialTheme
                                .colorScheme
                                .surfaceVariant
                                .copy(
                                    alpha = 0.55f
                                )
                    ) {

                        Row(
                            modifier =
                                Modifier.padding(
                                    15.dp
                                ),
                            verticalAlignment =
                                Alignment.Top
                        ) {

                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint =
                                    TealGreen,
                                modifier =
                                    Modifier.size(23.dp)
                            )

                            Spacer(
                                Modifier.width(10.dp)
                            )

                            Column {

                                Text(
                                    "নোট",
                                    fontSize = 13.sp,
                                    fontWeight =
                                        FontWeight.Bold
                                )

                                Spacer(
                                    Modifier.height(4.dp)
                                )

                                Text(
                                    lending.note,
                                    fontSize = 13.sp,
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // -------------------------------------------------
                // DUE DATE
                // -------------------------------------------------

                if (
                    lending.dueDate != null &&
                    remaining > 0.0
                ) {

                    Spacer(
                        Modifier.height(12.dp)
                    )

                    Surface(
                        modifier =
                            Modifier.fillMaxWidth(),
                        shape =
                            RoundedCornerShape(14.dp),
                        color =
                            ExpenseRed.copy(
                                alpha = 0.08f
                            )
                    ) {

                        Row(
                            modifier =
                                Modifier.padding(
                                    14.dp
                                ),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Icon(
                                Icons.Default.Event,
                                contentDescription = null,
                                tint =
                                    ExpenseRed,
                                modifier =
                                    Modifier.size(22.dp)
                            )

                            Spacer(
                                Modifier.width(10.dp)
                            )

                            Column {

                                Text(
                                    "ফেরত পাওয়ার তারিখ",
                                    fontSize = 11.sp,
                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .onSurfaceVariant
                                )

                                Text(
                                    displayLoanDate(
                                        lending.dueDate
                                    ),
                                    fontSize = 13.sp,
                                    fontWeight =
                                        FontWeight.Bold,
                                    color =
                                        ExpenseRed
                                )
                            }
                        }
                    }
                }

                Spacer(
                    Modifier.height(18.dp)
                )

                // -------------------------------------------------
                // HISTORY
                // -------------------------------------------------

                Text(
                    "ফেরত পাওয়ার ইতিহাস",
                    fontSize = 16.sp,
                    fontWeight =
                        FontWeight.ExtraBold
                )

                Spacer(
                    Modifier.height(8.dp)
                )

                val history =
                    lendingReturns
                        .filter {
                            it.lendingId ==
                                    lending.id
                        }
                        .sortedByDescending {
                            it.date
                        }

                if (
                    history.isEmpty()
                ) {

                    Text(
                        "এখনও কোনো টাকা ফেরত পাওয়া যায়নি।",
                        fontSize = 12.sp,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                    )

                } else {

                    history.forEachIndexed {
                            index,
                            item ->

                        Surface(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        vertical = 4.dp
                                    ),
                            shape =
                                RoundedCornerShape(
                                    16.dp
                                ),
                            color =
                                TealGreen.copy(
                                    alpha = 0.06f
                                )
                        ) {

                            Row(
                                modifier =
                                    Modifier.padding(
                                        12.dp
                                    ),
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {

                                Surface(
                                    shape =
                                        CircleShape,
                                    color =
                                        TealGreen.copy(
                                            alpha = 0.10f
                                        ),
                                    modifier =
                                        Modifier.size(
                                            38.dp
                                        )
                                ) {

                                    Box(
                                        contentAlignment =
                                            Alignment.Center
                                    ) {

                                        Text(
                                            "${index + 1}",
                                            color =
                                                TealGreen,
                                            fontWeight =
                                                FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(
                                    Modifier.width(12.dp)
                                )

                                Column(
                                    modifier =
                                        Modifier.weight(
                                            1f
                                        )
                                ) {

                                    Text(
                                        "৳${
                                            formatMoney(
                                                item.amount
                                            )
                                        } ফেরত",
                                        fontSize = 14.sp,
                                        fontWeight =
                                            FontWeight.Bold
                                    )

                                    Text(
                                        displayLoanDate(
                                            item.date
                                        ),
                                        fontSize = 11.sp,
                                        color =
                                            MaterialTheme
                                                .colorScheme
                                                .onSurfaceVariant
                                    )

                                    if (
                                        item.note
                                            .isNotBlank()
                                    ) {

                                        Text(
                                            item.note,
                                            fontSize = 11.sp,
                                            color =
                                                MaterialTheme
                                                    .colorScheme
                                                    .onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(
                    Modifier.height(16.dp)
                )

                // -------------------------------------------------
                // ACTION BUTTONS
                // -------------------------------------------------

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {

                    OutlinedButton(
                        onClick = onShare,
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(54.dp),
                        shape =
                            RoundedCornerShape(15.dp),
                        contentPadding =
                            PaddingValues(
                                horizontal = 5.dp,
                                vertical = 4.dp
                            )
                    ) {

                        Icon(
                            Icons.Default.Share,
                            contentDescription = null,
                            modifier =
                                Modifier.size(19.dp)
                        )

                        Spacer(
                            Modifier.width(4.dp)
                        )

                        Text(
                            "শেয়ার",
                            fontSize = 12.sp,
                            fontWeight =
                                FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    OutlinedButton(
                        onClick = onShare,
                        modifier =
                            Modifier
                                .weight(1f)
                                .height(54.dp),
                        shape =
                            RoundedCornerShape(15.dp),
                        contentPadding =
                            PaddingValues(
                                horizontal = 5.dp,
                                vertical = 4.dp
                            )
                    ) {

                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            modifier =
                                Modifier.size(19.dp)
                        )

                        Spacer(
                            Modifier.width(4.dp)
                        )

                        Text(
                            "PDF",
                            fontSize = 12.sp,
                            fontWeight =
                                FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Button(
                        onClick = onAddReturn,
                        enabled =
                            remaining > 0.0,
                        modifier =
                            Modifier
                                .weight(1.25f)
                                .height(54.dp),
                        shape =
                            RoundedCornerShape(15.dp),
                        contentPadding =
                            PaddingValues(
                                horizontal = 5.dp,
                                vertical = 4.dp
                            ),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    TealGreen
                            )
                    ) {

                        Icon(
                            if (
                                remaining > 0.0
                            ) {
                                Icons.Default.Add
                            } else {
                                Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            modifier =
                                Modifier.size(19.dp)
                        )

                        Spacer(
                            Modifier.width(4.dp)
                        )

                        Text(
                            if (
                                remaining > 0.0
                            ) {
                                "ফেরত যোগ"
                            } else {
                                "সম্পূর্ণ"
                            },
                            fontSize = 12.sp,
                            fontWeight =
                                FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}


// =====================================================================
// LENDING SUMMARY CARD
// =====================================================================

@Composable
private fun LendingSummaryCard(
    modifier: Modifier,
    title: String,
    amount: Double
) {

    Surface(
        modifier = modifier,
        shape =
            RoundedCornerShape(16.dp),
        color =
            TealGreen.copy(
                alpha = 0.07f
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 13.dp
                )
        ) {

            Text(
                title,
                fontSize = 11.sp,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                Modifier.height(5.dp)
            )

            Text(
                "৳${formatMoney(amount)}",
                fontSize = 15.sp,
                fontWeight =
                    FontWeight.ExtraBold,
                color =
                    TealGreen
            )
        }
    }
}
