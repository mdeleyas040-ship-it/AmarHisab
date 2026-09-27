package com.eleyas.expensetracker.ui.components

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.*
import com.eleyas.expensetracker.util.AccountStorage
import com.eleyas.expensetracker.util.formatMoney
import com.eleyas.expensetracker.util.loadPersonProfiles
import com.eleyas.expensetracker.util.userIdForPerson
import kotlinx.coroutines.delay

private val ProfileTeal = Color(0xFF0FAF93)
private val ProfileBlue = Color(0xFF1688F7)

@Composable
fun SearchOverlay(
    active: Boolean,
    searchScope: Int,
    transactions: List<Transaction>,
    loans: List<LoanAccount>,
    lendings: List<LendingAccount>,
    loanPayments: List<LoanPayment>,
    lendingReturns: List<LendingReturn>,
    wallets: List<Wallet>,
    usdToBdt: Double,
    usdToMvr: Double,
    onEditTransaction: (Transaction) -> Unit,
    onDeleteTransaction: (Transaction) -> Unit,
    onShareResults: (String, Boolean) -> Unit,
    onClose: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val focusRequester = remember { FocusRequester() }
    var searchQuery by remember { mutableStateOf("") }

    val prefs = remember(context) {
        AccountStorage.getPrefs(context, AccountStorage.getCurrentUserId())
    }
    val people = remember(prefs) { loadPersonProfiles(prefs) }

    val matchedProfile = remember(searchQuery, people) {
        val q = searchQuery.trim()
        if (q.isBlank()) null
        else people.firstOrNull { person ->
            person.name.trim().equals(q, ignoreCase = true) ||
                userIdForPerson(prefs, person).equals(q, ignoreCase = true)
        }
    }

    val profileLoans = matchedProfile?.let { profile ->
        loans.filter { it.personId == profile.id || it.name.trim().equals(profile.name.trim(), true) }
    } ?: emptyList()

    val profileLendings = matchedProfile?.let { profile ->
        lendings.filter { it.personId == profile.id || it.person.trim().equals(profile.name.trim(), true) }
    } ?: emptyList()

    val filteredTransactions = if (searchQuery.isBlank()) emptyList() else transactions.filter {
        (searchScope == 0 || (searchScope == 1 && it.type == "income") || (searchScope == 2 && (it.type == "expense" || it.type == "home"))) &&
            (it.reason.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true) ||
                wallets.firstOrNull { w -> w.id == it.walletId }?.name?.contains(searchQuery, ignoreCase = true) == true)
    }

    val filteredWallets = if (searchQuery.isBlank()) emptyList() else wallets.filter {
        it.name.contains(searchQuery, true) || it.type.contains(searchQuery, true)
    }

    val filteredLoans = if (searchQuery.isBlank() || (searchScope != 0 && searchScope != 4)) emptyList() else loans.filter {
        it.name.contains(searchQuery, true) || it.note.contains(searchQuery, true) ||
            (matchedProfile != null && (it.personId == matchedProfile.id || it.name.equals(matchedProfile.name, true)))
    }

    val filteredLendings = if (searchQuery.isBlank() || (searchScope != 0 && searchScope != 4)) emptyList() else lendings.filter {
        it.person.contains(searchQuery, true) || it.note.contains(searchQuery, true) ||
            (matchedProfile != null && (it.personId == matchedProfile.id || it.person.equals(matchedProfile.name, true)))
    }

    val totalIncome = filteredTransactions.filter { it.type == "income" }.sumOf { it.amount }
    val totalExpense = filteredTransactions.filter { it.type != "income" }.sumOf { it.amount }
    val profileReceivable = profileLendings.sumOf { lending ->
        (lending.amount - lendingReturns.filter { it.lendingId == lending.id }.sumOf { it.amount }).coerceAtLeast(0.0)
    }
    val profileDebt = profileLoans.sumOf { loan ->
        (loan.principal - loanPayments.filter { it.loanId == loan.id }.sumOf { it.amount }).coerceAtLeast(0.0)
    }

    LaunchedEffect(active) {
        if (active) {
            delay(250)
            focusRequester.requestFocus()
        } else {
            searchQuery = ""
        }
    }

    AnimatedVisibility(
        visible = active,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(Modifier.fillMaxSize()) {
                Surface(
                    Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Column(Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                            placeholder = { Text("নাম, User ID, খরচ, আয়, ধার... খুঁজুন") },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = ProfileBlue) },
                            trailingIcon = {
                                IconButton(onClick = onClose) { Icon(Icons.Default.Clear, null) }
                            },
                            shape = RoundedCornerShape(18.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ProfileBlue,
                                focusedLabelColor = ProfileBlue,
                                cursorColor = ProfileBlue
                            )
                        )

                        if (searchQuery.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Card(
                                Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = ProfileBlue.copy(alpha = 0.08f))
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("সার্চ ফলাফল", fontWeight = FontWeight.Bold)
                                        Text(
                                            "${filteredTransactions.size}টি লেনদেন • ${filteredLoans.size + filteredLendings.size}টি হিসাব",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("আয় ৳${formatMoney(totalIncome)}", color = Color(0xFF22C55E), fontSize = 11.sp)
                                        Text("খরচ ৳${formatMoney(totalExpense)}", color = Color(0xFFEF4444), fontSize = 11.sp)
                                        Text("নেট ৳${formatMoney(totalIncome - totalExpense)}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }

                            if (matchedProfile != null) {
                                Spacer(Modifier.height(10.dp))
                                Card(
                                    Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = ProfileTeal.copy(alpha = 0.10f)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ProfileTeal.copy(alpha = 0.22f))
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth().padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            Modifier.size(46.dp).background(ProfileTeal.copy(alpha = 0.16f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Person, null, tint = ProfileTeal, modifier = Modifier.size(24.dp))
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(matchedProfile.name, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                            Text(
                                                userIdForPerson(prefs, matchedProfile),
                                                color = ProfileTeal,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "পাওনা ৳${formatMoney(profileReceivable)}  •  ঋণ ৳${formatMoney(profileDebt)}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(Icons.Default.Badge, null, tint = ProfileTeal)
                                    }
                                }
                            }

                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { onShareResults(searchQuery, false) }) { Text("শেয়ার") }
                                TextButton(onClick = { onShareResults(searchQuery, true) }) { Text("PDF") }
                            }
                        }
                    }
                }

                if (searchQuery.isNotEmpty()) {
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredTransactions, key = { it.id }) { trans ->
                            val wallet = wallets.firstOrNull { it.id == trans.walletId }
                            TransactionCard(
                                transaction = trans,
                                usdToBdt = usdToBdt,
                                usdToMvr = usdToMvr,
                                walletName = wallet?.name ?: "",
                                onEdit = { onEditTransaction(it); onClose() },
                                onDelete = { onDeleteTransaction(it); onClose() }
                            )
                        }

                        if (filteredWallets.isNotEmpty()) {
                            item { Text("ব্যাংক / অ্যাকাউন্ট", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp)) }
                            items(filteredWallets) { wallet ->
                                Card(
                                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(wallet.color.toLong() and 0xFFFFFFFFL))
                                ) {
                                    Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text(wallet.name, color = Color.White, fontWeight = FontWeight.Bold)
                                            Text(wallet.type, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                        }
                                        Text("লেনদেন: ${filteredTransactions.count { it.walletId == wallet.id }}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        if (filteredLoans.isNotEmpty()) {
                            item { Text("ঋণ (Loans)", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp)) }
                            items(filteredLoans) { loan ->
                                val paid = loanPayments.filter { it.loanId == loan.id }.sumOf { it.amount }
                                val remaining = (loan.principal - paid).coerceAtLeast(0.0)
                                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(14.dp)) {
                                    Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text(loan.name, fontWeight = FontWeight.Bold)
                                            Text("বাকি: ৳${formatMoney(remaining)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        if (filteredLendings.isNotEmpty()) {
                            item { Text("পাওনা", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp)) }
                            items(filteredLendings, key = { "lending_${it.id}" }) { lending ->
                                val returned = lendingReturns.filter { it.lendingId == lending.id }.sumOf { it.amount }
                                val remaining = (lending.amount - returned).coerceAtLeast(0.0)
                                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                                    Column(Modifier.padding(14.dp)) {
                                        Text(lending.person, fontWeight = FontWeight.Bold)
                                        Text("পাওনা: ৳${formatMoney(remaining)}", fontSize = 12.sp, color = ProfileTeal, fontWeight = FontWeight.SemiBold)
                                        if (lending.note.isNotBlank()) Text(lending.note, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        if (filteredTransactions.isEmpty() && filteredLoans.isEmpty() && filteredLendings.isEmpty() && filteredWallets.isEmpty() && matchedProfile == null) {
                            item {
                                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                                    Text("কোনো মিল পাওয়া যায়নি। অন্য নাম, User ID, ক্যাটাগরি বা তারিখ দিয়ে চেষ্টা করুন।", Modifier.padding(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
