package com.eleyas.expensetracker.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import com.eleyas.expensetracker.model.*
import com.eleyas.expensetracker.ui.components.*
import com.eleyas.expensetracker.ui.theme.*
import com.eleyas.expensetracker.util.displayLoanDate
import com.eleyas.expensetracker.util.formatMoney
import com.eleyas.expensetracker.util.savePersonProfiles
import com.eleyas.expensetracker.util.userIdForPerson
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
private val TealGreen = Color(0xFF16A085)

@Composable
fun LoansScreen(
    modifier: Modifier,
    loans: List<LoanAccount>,
    loanPayments: List<LoanPayment>,
    lendings: List<LendingAccount>,
    people: List<PersonProfile> = emptyList(),
    lendingReturns: List<LendingReturn>,
    onAddLoan: () -> Unit,
    onAddLoanPayment: (LoanAccount) -> Unit,
    onEditLoan: (LoanAccount) -> Unit,
    onDeleteLoan: (LoanAccount) -> Unit = {},
    onEditBorrowing: (LoanAccount, LoanBorrowing) -> Unit,
    onDeleteBorrowing: (LoanAccount, LoanBorrowing) -> Unit,
    onEditLoanPayment: (LoanPayment) -> Unit = {},
    onDeleteLoanPayment: (LoanPayment) -> Unit = {},
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
    var showLending by remember { mutableStateOf(false) }
    var shareOptionsLoan by remember { mutableStateOf<LoanAccount?>(null) }
    var shareOptionsLending by remember { mutableStateOf<LendingAccount?>(null) }
    var showPaymentHistoryLoan by remember { mutableStateOf<LoanAccount?>(null) }
    var expandedLoanId by remember { mutableStateOf<Long?>(null) }
    var expandedLendingId by remember { mutableStateOf<Long?>(null) }
    var loanToDelete by remember { mutableStateOf<LoanAccount?>(null) }
    var locallyDeletedLoanIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var profileDialogTarget by remember { mutableStateOf<PersonProfile?>(null) }
    var localPeople by remember(people) { mutableStateOf(people) }

    val context = LocalContext.current
    val activeLoans = loans.filterNot { it.id in locallyDeletedLoanIds }
    val activeLoanPayments = loanPayments.filterNot { it.loanId in locallyDeletedLoanIds }
    val activeLoanInterestTerms = loanInterestTerms.filterNot { it.loanId in locallyDeletedLoanIds }

    if (profileDialogTarget != null) {
        val target = profileDialogTarget!!
        var phone by remember(target.id) { mutableStateOf(target.phone) }
        var note by remember(target.id) { mutableStateOf(target.note) }
        AlertDialog(
            onDismissRequest = { profileDialogTarget = null },
            title = { Text(if (target.phone.isBlank() && target.note.isBlank()) "ব্যক্তির প্রোফাইল যোগ করুন" else "ব্যক্তির প্রোফাইল এডিট") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(target.name.ifBlank { "ব্যক্তি" }, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text("AH User ID: ${userIdForPerson(AccountStorage.getPrefs(context, FirebaseAuth.getInstance().currentUser?.uid ?: "guest"), target)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, modifier = Modifier.fillMaxWidth(), label = { Text("ফোন নম্বর") }, singleLine = true)
                    OutlinedTextField(value = note, onValueChange = { note = it }, modifier = Modifier.fillMaxWidth(), label = { Text("নোট") }, minLines = 2)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
                    val prefs = AccountStorage.getPrefs(context, uid)
                    val updated = target.copy(phone = phone.trim(), note = note.trim(), updatedAt = System.currentTimeMillis())
                    val merged = localPeople.filterNot { it.id == updated.id } + updated
                    savePersonProfiles(prefs, merged)
                    localPeople = merged
                    profileDialogTarget = null
                }) { Text("সংরক্ষণ") }
            },
            dismissButton = { TextButton(onClick = { profileDialogTarget = null }) { Text("বাতিল") } }
        )
    }

    if (shareOptionsLoan != null) {
        AlertDialog(
            onDismissRequest = { shareOptionsLoan = null },
            title = { Text("স্টেটমেন্ট শেয়ার করুন") },
            text = { Text("${shareOptionsLoan!!.name} ঋণের স্টেটমেন্ট কিভাবে শেয়ার করতে চান?") },
            confirmButton = {
                Button(onClick = {
                    onShareLoan(shareOptionsLoan!!, true)
                    shareOptionsLoan = null
                }) { Text("PDF ফাইল") }
            },
            dismissButton = {
                TextButton(onClick = {
                    onShareLoan(shareOptionsLoan!!, false)
                    shareOptionsLoan = null
                }) { Text("মেসেজ (Text)") }
            }
        )
    }

    if (loanToDelete != null) {
        val targetLoan = loanToDelete!!
        AlertDialog(
            onDismissRequest = { loanToDelete = null },
            title = { Text("ঋণ মুছে ফেলবেন?") },
            text = {
                Text("\"${targetLoan.name.ifBlank { "ঋণ" }}\"-এর মূল ঋণ, নেওয়ার History, পরিশোধের History এবং সংশ্লিষ্ট সুদের তথ্য মুছে যাবে। এই কাজটি পূর্বাবস্থায় ফেরানো যাবে না।")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val loanId = targetLoan.id
                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
                        val prefs = AccountStorage.getPrefs(context, userId)
                        val remainingLoans = loans.filter { it.id != loanId }
                        val remainingPayments = loanPayments.filter { it.loanId != loanId }
                        val remainingInterest = loanInterestTerms.filter { it.loanId != loanId }

                        saveLoans(prefs, remainingLoans)
                        saveLoanPayments(prefs, remainingPayments)
                        saveLoanInterestTerms(prefs, remainingInterest)
                        locallyDeletedLoanIds = locallyDeletedLoanIds + loanId
                        loanToDelete = null

                        if (userId != "guest") {
                            val firestore = FirebaseFirestore.getInstance()
                            val root = firestore.collection("users").document(userId)
                            val batch = firestore.batch()
                            batch.delete(root.collection("loans").document(loanId.toString()))
                            batch.delete(root.collection("loanInterestTerms").document(loanId.toString()))
                            root.collection("loanPayments")
                                .whereEqualTo("loanId", loanId)
                                .get()
                                .addOnSuccessListener { snapshot ->
                                    snapshot.documents.forEach { batch.delete(it.reference) }
                                    batch.commit()
                                        .addOnSuccessListener { onDeleteLoan(targetLoan) }
                                        .addOnFailureListener { onDeleteLoan(targetLoan) }
                                }
                                .addOnFailureListener {
                                    batch.commit()
                                        .addOnSuccessListener { onDeleteLoan(targetLoan) }
                                        .addOnFailureListener { onDeleteLoan(targetLoan) }
                                }
                        } else {
                            onDeleteLoan(targetLoan)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) { Text("মুছে ফেলুন") }
            },
            dismissButton = { TextButton(onClick = { loanToDelete = null }) { Text("বাতিল") } }
        )
    }

    if (showPaymentHistoryLoan != null) {
        val historyLoan = showPaymentHistoryLoan!!
        val paymentHistory = activeLoanPayments.filter { it.loanId == historyLoan.id }
        AlertDialog(
            onDismissRequest = { showPaymentHistoryLoan = null },
            title = { Text("পরিশোধের তথ্য") },
            text = {
                Column {
                    if (paymentHistory.isEmpty()) Text("কোনো পরিশোধের তথ্য নেই")
                    else paymentHistory.forEach { payment ->
                        Text("✓ ${displayLoanDate(payment.date)} — ৳${formatMoney(payment.amount)} পরিশোধ করা হয়েছে")
                        Spacer(Modifier.height(6.dp))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPaymentHistoryLoan = null }) { Text("ঠিক আছে") } }
        )
    }

    val filteredLoans = activeLoans.filter { loan ->
        loan.name.contains(searchQuery, ignoreCase = true) || loan.note.contains(searchQuery, ignoreCase = true)
    }
    val totalBorrowed = activeLoans.sumOf { it.principal }
    val totalInterest = activeLoans.sumOf { loan -> activeLoanInterestTerms.firstOrNull { it.loanId == loan.id }?.totalInterest ?: 0.0 }
    val totalPaid = activeLoanPayments.sumOf { it.amount }
    val totalRemaining = (totalBorrowed + totalInterest - totalPaid).coerceAtLeast(0.0)
    val totalLent = lendings.sumOf { it.amount }
    val totalReturned = lendingReturns.sumOf { it.amount }
    val totalReceivable = (totalLent - totalReturned).coerceAtLeast(0.0)

    LazyColumn(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
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
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PremiumTabButton(Modifier.weight(1f), !showLending, Icons.Default.CreditCard, "আমার ঋণ", Green) { showLending = false }
                PremiumTabButton(Modifier.weight(1f), showLending, Icons.Default.Handshake, "ধার দিয়েছি", TealGreen) { showLending = true }
            }
        }
        if (!showLending) {
            item {
                Button(
                    onClick = onAddLoan,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(17.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(23.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("ঋণ যোগ করুন", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            item { Text("আমার নেওয়া ঋণ", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold) }
            if (filteredLoans.isEmpty()) {
                item { EmptyFinanceCard(Icons.Default.CreditCard, "এখনও কোনো ঋণ যোগ করা হয়নি", "ঋণের হিসাব এখানে দেখা যাবে") }
            } else {
                items(filteredLoans, key = { it.id }) { loan ->
                    LoanPremiumCard(
                        loan = loan,
                        people = localPeople,
                        loanPayments = activeLoanPayments,
                        loanInterestTerms = activeLoanInterestTerms,
                        expanded = expandedLoanId == loan.id,
                        onExpand = { expandedLoanId = if (expandedLoanId == loan.id) null else loan.id },
                        onShare = { shareOptionsLoan = loan },
                        onEdit = { onEditLoan(loan) },
                        onDelete = { loanToDelete = loan },
                        onPayment = {
                            val paid = activeLoanPayments.filter { it.loanId == loan.id }.sumOf { it.amount }
                            val interest = activeLoanInterestTerms.firstOrNull { it.loanId == loan.id }?.totalInterest ?: 0.0
                            val remaining = (loan.principal + interest - paid).coerceAtLeast(0.0)
                            if (remaining > 0.0) onAddLoanPayment(loan) else showPaymentHistoryLoan = loan
                        },
                        onEditBorrowing = onEditBorrowing,
                        onDeleteBorrowing = onDeleteBorrowing,
                        onEditLoanPayment = onEditLoanPayment,
                        onDeleteLoanPayment = onDeleteLoanPayment,
                        onProfileClick = {
                            profileDialogTarget = localPeople.firstOrNull { it.id == loan.personId || it.name.trim().equals(loan.name.trim(), ignoreCase = true) }
                                ?: PersonProfile(id = loan.personId ?: java.util.UUID.randomUUID().toString(), name = loan.name.trim())
                        }
                    )
                }
            }
        } else {
            item {
                Button(
                    onClick = onAddLending,
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(17.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TealGreen)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(23.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("ধার দিন", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("আমি যাদের ধার দিয়েছি", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                        Text("পাওনা ও ফেরতের বিস্তারিত হিসাব", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(shape = RoundedCornerShape(13.dp), color = TealGreen.copy(alpha = 0.10f)) {
                        Text("${lendings.size} টি", modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp), color = TealGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (lendings.isEmpty()) {
                item { EmptyFinanceCard(Icons.Default.Handshake, "এখনও কাউকে ধার দেওয়ার হিসাব নেই", "ধার দেওয়ার হিসাব এখানে দেখা যাবে") }
            } else {
                items(lendings, key = { it.id }) { lending ->
                    LendingPremiumCard(
                        lending = lending,
                        people = localPeople,
                        lendingReturns = lendingReturns,
                        expanded = expandedLendingId == lending.id,
                        onExpand = { expandedLendingId = if (expandedLendingId == lending.id) null else lending.id },
                        onShare = { shareOptionsLending = lending },
                        onEdit = { onEditLending(lending) },
                        onDelete = { onDeleteLending(lending) },
                        onAddReturn = { onAddLendingReturn(lending) },
                        onProfileClick = {
                            profileDialogTarget = localPeople.firstOrNull { it.id == lending.personId || it.name.trim().equals(lending.person.trim(), ignoreCase = true) }
                                ?: PersonProfile(id = lending.personId ?: java.util.UUID.randomUUID().toString(), name = lending.person.trim())
                        }
                    )
                }
            }
        }
    }
}

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
    val primaryColor = if (isLending) TealGreen else Green
    val balance = if (isLending) totalReceivable else totalRemaining
    val total = if (isLending) totalLent else totalBorrowed
    val returned = if (isLending) totalReturned else totalPaid
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), color = primaryColor, shadowElevation = 7.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.13f), modifier = Modifier.size(58.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(if (isLending) Icons.Default.Handshake else Icons.Default.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(if (isLending) "LENDING BALANCE" else "LOAN BALANCE", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(3.dp))
                        Text(if (isLending) "বর্তমান পাওনা" else "বর্তমান বাকি ঋণ", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Surface(shape = RoundedCornerShape(15.dp), color = Color.White.copy(alpha = 0.10f)) {
                    Text(if (isLending) "ধার" else "ঋণ", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 15.dp, vertical = 11.dp))
                }
            }
            Spacer(Modifier.height(22.dp))
            Text("৳${formatMoney(balance)}", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                PremiumBalanceMiniCard(Modifier.weight(1f), Icons.Default.AccountBalance, if (isLending) "মোট ধার" else "মোট নেওয়া", total)
                PremiumBalanceMiniCard(Modifier.weight(1f), Icons.Default.CheckCircle, if (isLending) "ফেরত" else "পরিশোধ", returned)
                PremiumBalanceMiniCard(Modifier.weight(1f), Icons.Default.Schedule, if (isLending) "পাওনা" else "বাকি", balance)
            }
        }
    }
}

@Composable
private fun PremiumBalanceMiniCard(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, amount: Double) {
    Surface(modifier = modifier, shape = RoundedCornerShape(17.dp), color = Color.White.copy(alpha = 0.10f)) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(5.dp))
                Text(title, color = Color.White.copy(alpha = 0.72f), fontSize = 10.sp, maxLines = 1)
            }
            Spacer(Modifier.height(7.dp))
            Text("৳${formatMoney(amount)}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
        }
    }
}

@Composable
private fun PremiumTabButton(modifier: Modifier, selected: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick, modifier = modifier.height(58.dp), shape = RoundedCornerShape(17.dp), colors = ButtonDefaults.buttonColors(containerColor = color), contentPadding = PaddingValues(horizontal = 8.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(7.dp))
            Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier.height(58.dp), shape = RoundedCornerShape(17.dp), colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f), contentColor = MaterialTheme.colorScheme.onSurfaceVariant), border = null, contentPadding = PaddingValues(horizontal = 8.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(7.dp))
            Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun EmptyFinanceCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp))
            Spacer(Modifier.height(10.dp))
            Text(title, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(3.dp))
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PersonProfileAvatar(personId: String?, people: List<PersonProfile>, tint: Color, modifier: Modifier = Modifier.size(56.dp)) {
    val path = people.firstOrNull { it.id == personId }?.photoUri
    val bitmap = remember(path) { path?.takeIf { it.isNotBlank() }?.let { runCatching { BitmapFactory.decodeFile(File(it).absolutePath) }.getOrNull() } }
    Surface(shape = CircleShape, color = tint.copy(alpha = 0.12f), modifier = modifier) {
        if (bitmap != null) {
            Image(bitmap = bitmap.asImageBitmap(), contentDescription = "ব্যক্তির ছবি", modifier = Modifier.fillMaxSize())
        } else {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(Icons.Default.Person, contentDescription = null, tint = tint, modifier = Modifier.size(30.dp))
            }
        }
    }
}

@Composable
private fun LoanPremiumCard(
    loan: LoanAccount,
    people: List<PersonProfile> = emptyList(),
    loanPayments: List<LoanPayment>,
    loanInterestTerms: List<LoanInterestTerms>,
    expanded: Boolean,
    onExpand: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPayment: () -> Unit,
    onEditBorrowing: (LoanAccount, LoanBorrowing) -> Unit,
    onDeleteBorrowing: (LoanAccount, LoanBorrowing) -> Unit,
    onEditLoanPayment: (LoanPayment) -> Unit,
    onDeleteLoanPayment: (LoanPayment) -> Unit,
    onProfileClick: () -> Unit = {}
) {
    val paid = loanPayments.filter { it.loanId == loan.id }.sumOf { it.amount }
    val interest = loanInterestTerms.firstOrNull { it.loanId == loan.id }?.totalInterest ?: 0.0
    val totalPayable = loan.principal + interest
    val remaining = (totalPayable - paid).coerceAtLeast(0.0)
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(19.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), onClick = onExpand) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                if (loan.sourceType != "bank") {
                    PersonProfileAvatar(personId = loan.personId, people = people, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(50.dp))
                    Spacer(Modifier.width(10.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(loan.name.ifBlank { "ঋণ" }, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f, fill = false))
                        if (loan.sourceType != "bank") IconButton(onClick = onProfileClick, modifier = Modifier.size(34.dp)) { Icon(Icons.Default.Person, contentDescription = "ব্যক্তির প্রোফাইল", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) }
                    }
                    if (loan.note.isNotBlank()) Text(loan.note, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (loan.sourceType == "bank") "🏦 ব্যাংক ঋণ" else "👤 ব্যক্তিগত ঋণ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
                        Text("৳${formatMoney(if (remaining <= 0.0) paid else remaining)}", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = if (remaining <= 0.0) IncomeGreen else ExpenseRed)
                    }
                    Text(if (remaining <= 0.0) "পরিশোধিত" else "বাকি আছে", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (remaining <= 0.0) IncomeGreen else ExpenseRed)
                }
            }
            if (expanded) {
                Spacer(Modifier.height(14.dp)); HorizontalDivider(); Spacer(Modifier.height(14.dp))
                LoanInfoRow("মোট ঋণ", loan.principal)
                LoanInfoRow("মোট সুদ", interest)
                LoanInfoRow("মোট পরিশোধযোগ্য", totalPayable)
                LoanInfoRow("পরিশোধিত", paid)
                LoanInfoRow("বাকি", remaining)
                if (loan.monthlyInstallment > 0) LoanInfoRow("মাসিক কিস্তি", loan.monthlyInstallment)
                LoanInfoRowText("শুরু", displayLoanDate(loan.startDate))
                if (loan.dueDate != null) LoanInfoRowText("পরিশোধের তারিখ", displayLoanDate(loan.dueDate))
                if (loan.borrowings.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp)); Text("নেওয়ার History", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    loan.borrowings.sortedByDescending { it.date }.forEachIndexed { index, borrowing ->
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text("${index + 1}. ৳${formatMoney(borrowing.amount)} — ${displayLoanDate(borrowing.date)}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            if (borrowing.note.isNotBlank()) Text(borrowing.note, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                IconButton(onClick = { onEditBorrowing(loan, borrowing) }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                IconButton(onClick = { onDeleteBorrowing(loan, borrowing) }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(16.dp)) }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("💰 পরিশোধের ইতিহাস (${loanPayments.count { it.loanId == loan.id }} বার)", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(8.dp))
                val paymentHistory = loanPayments.filter { it.loanId == loan.id }.sortedByDescending { it.date }
                if (paymentHistory.isEmpty()) {
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f)) { Text("এখনও কোনো টাকা পরিশোধ করা হয়নি।", modifier = Modifier.padding(12.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                } else {
                    paymentHistory.forEachIndexed { index, payment ->
                        val fromHome = payment.note.contains("বাড়িতে পাঠানো") || payment.note.contains("বাড়িতে পাঠানো")
                        Surface(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(16.dp), color = IncomeGreen.copy(alpha = 0.07f)) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = IncomeGreen.copy(alpha = 0.12f), modifier = Modifier.size(38.dp)) { Box(contentAlignment = Alignment.Center) { Text("${index + 1}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IncomeGreen) } }
                                Spacer(Modifier.width(11.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("৳${formatMoney(payment.amount)} পরিশোধ", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = IncomeGreen)
                                    Spacer(Modifier.height(2.dp))
                                    Text(displayLoanDate(payment.date), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (fromHome) {
                                        Spacer(Modifier.height(5.dp)); Surface(shape = RoundedCornerShape(8.dp), color = TealGreen.copy(alpha = 0.12f)) { Text("🏠 বাড়িতে পাঠানো টাকা থেকে", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TealGreen) }
                                    } else if (payment.note.isNotBlank()) {
                                        Spacer(Modifier.height(3.dp)); Text(payment.note, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Spacer(Modifier.width(4.dp))
                                IconButton(onClick = { onEditLoanPayment(payment) }, modifier = Modifier.size(34.dp)) { Icon(Icons.Default.Edit, contentDescription = "পরিশোধ এডিট", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
                                IconButton(onClick = { onDeleteLoanPayment(payment) }, modifier = Modifier.size(34.dp)) { Icon(Icons.Default.Delete, contentDescription = "পরিশোধ মুছুন", tint = ExpenseRed, modifier = Modifier.size(18.dp)) }
                            }
                        }
                    }
                }
                if (paymentHistory.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = IncomeGreen.copy(alpha = 0.10f)) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("মোট পরিশোধ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("৳${formatMoney(paid)}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = IncomeGreen)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(5.dp)); Text("Edit")
                    }
                    OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed)) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(5.dp)); Text("Delete")
                    }
                    Button(onClick = onPayment, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Green)) {
                        Icon(if (remaining > 0.0) Icons.Default.Add else Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(5.dp)); Text(if (remaining > 0.0) "পরিশোধ" else "পরিশোধিত")
                    }
                }
            }
        }
    }
}

@Composable
private fun LendingPremiumCard(
    lending: LendingAccount,
    people: List<PersonProfile> = emptyList(),
    lendingReturns: List<LendingReturn>,
    expanded: Boolean,
    onExpand: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddReturn: () -> Unit,
    onProfileClick: () -> Unit = {}
) {
    val returned = lendingReturns.filter { it.lendingId == lending.id }.sumOf { it.amount }
    val remaining = (lending.amount - returned).coerceAtLeast(0.0)
    val progress = if (lending.amount > 0.0) (returned / lending.amount).coerceIn(0.0, 1.0).toFloat() else 0f
    val status = when { remaining <= 0.0 -> "পুরো ফেরত"; returned > 0.0 -> "আংশিক ফেরত"; else -> "পাওনা আছে" }
    val statusIcon = when { remaining <= 0.0 -> Icons.Default.CheckCircle; returned > 0.0 -> Icons.Default.Sync; else -> Icons.Default.AccessTime }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(21.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), onClick = onExpand) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                PersonProfileAvatar(personId = lending.personId, people = people, tint = TealGreen)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(lending.person.ifBlank { "ব্যক্তি" }, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f, fill = false))
                        IconButton(onClick = onProfileClick, modifier = Modifier.size(34.dp)) { Icon(Icons.Default.Person, contentDescription = "ব্যক্তির প্রোফাইল", tint = TealGreen, modifier = Modifier.size(20.dp)) }
                    }
                    Text("ধার দেওয়া: ${displayLoanDate(lending.date)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("৳${formatMoney(remaining)}", fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = if (remaining > 0.0) TealGreen else IncomeGreen)
                    Spacer(Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(statusIcon, contentDescription = null, tint = if (remaining > 0.0) TealGreen else IncomeGreen, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (remaining > 0.0) TealGreen else IncomeGreen)
                    }
                }
            }
            Spacer(Modifier.height(15.dp))
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(7.dp), color = TealGreen, trackColor = TealGreen.copy(alpha = 0.12f))
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LendingSummaryCard(Modifier.weight(1f), "ধার", lending.amount)
                LendingSummaryCard(Modifier.weight(1f), "ফেরত", returned)
                LendingSummaryCard(Modifier.weight(1f), "পাওনা", remaining)
            }
            if (lending.note.isNotBlank()) {
                Spacer(Modifier.height(10.dp)); Text("নোট: ${lending.note}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (expanded) {
                Spacer(Modifier.height(14.dp)); HorizontalDivider(); Spacer(Modifier.height(14.dp))
                Text("বিস্তারিত", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(8.dp))
                LoanInfoRowText("তারিখ", displayLoanDate(lending.date))
                if (lending.dueDate != null) LoanInfoRowText("পাওয়ার তারিখ", displayLoanDate(lending.dueDate))
                Spacer(Modifier.height(10.dp))
                val returns = lendingReturns.filter { it.lendingId == lending.id }.sortedByDescending { it.date }
                Text("ফেরতের ইতিহাস (${returns.size} বার)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(7.dp))
                if (returns.isEmpty()) Text("এখনও কোনো টাকা ফেরত পাওয়া যায়নি।", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                else returns.forEachIndexed { index, item ->
                    Surface(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), shape = RoundedCornerShape(12.dp), color = TealGreen.copy(alpha = 0.07f)) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("${index + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TealGreen)
                            Spacer(Modifier.width(9.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("৳${formatMoney(item.amount)} ফেরত", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(displayLoanDate(item.date), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (item.note.isNotBlank()) Text(item.note, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f).height(54.dp), shape = RoundedCornerShape(15.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(4.dp)); Text("Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f).height(54.dp), shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed)) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(4.dp)); Text("Delete", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(onClick = onShare, modifier = Modifier.weight(1f).height(54.dp), shape = RoundedCornerShape(15.dp), contentPadding = PaddingValues(horizontal = 5.dp, vertical = 4.dp)) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(4.dp)); Text("শেয়ার", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    }
                    OutlinedButton(onClick = onShare, modifier = Modifier.weight(1f).height(54.dp), shape = RoundedCornerShape(15.dp), contentPadding = PaddingValues(horizontal = 5.dp, vertical = 4.dp)) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(4.dp)); Text("PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    }
                    Button(onClick = onAddReturn, enabled = remaining > 0.0, modifier = Modifier.weight(1.25f).height(54.dp), shape = RoundedCornerShape(15.dp), contentPadding = PaddingValues(horizontal = 5.dp, vertical = 4.dp), colors = ButtonDefaults.buttonColors(containerColor = TealGreen)) {
                        Icon(if (remaining > 0.0) Icons.Default.Add else Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(19.dp)); Spacer(Modifier.width(4.dp)); Text(if (remaining > 0.0) "ফেরত যোগ" else "সম্পূর্ণ", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    }
                }
            }
        }
    }
}

@Composable
private fun LendingSummaryCard(modifier: Modifier, title: String, amount: Double) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = TealGreen.copy(alpha = 0.07f)) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 13.dp)) {
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(5.dp))
            Text("৳${formatMoney(amount)}", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = TealGreen)
        }
    }
}

@Composable
private fun LoanInfoRow(title: String, amount: Double) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("৳${formatMoney(amount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LoanInfoRowText(title: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
