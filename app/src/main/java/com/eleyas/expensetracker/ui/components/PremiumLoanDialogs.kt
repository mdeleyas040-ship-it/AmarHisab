package com.eleyas.expensetracker.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.eleyas.expensetracker.model.LoanAccount
import com.eleyas.expensetracker.util.formatMoney
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.eleyas.expensetracker.ui.components.WarningPopupManager


@Composable
fun PremiumLoanDialog(
    onDismiss: () -> Unit,
    existingLoan: LoanAccount? = null,
    existingNames: List<String> = emptyList(),
    onSave: (String, String, Double, Double, String, String, String?) -> Unit
) {
    val context = LocalContext.current
    val editing = existingLoan != null

    val isBankLoan = sourceTypeIsBank(
        existingLoan?.sourceType ?: "bank"
    )

    var name by remember(existingLoan?.id) {
        mutableStateOf(existingLoan?.name ?: "")
    }

    var sourceType by remember(existingLoan?.id) {
        mutableStateOf(existingLoan?.sourceType ?: "person")
    }

    var principal by remember(existingLoan?.id) {
        mutableStateOf(
            existingLoan?.principal?.let(::formatMoney) ?: ""
        )
    }

    var installment by remember(existingLoan?.id) {
        mutableStateOf(
            existingLoan?.monthlyInstallment?.let(::formatMoney) ?: ""
        )
    }

    var date by remember(existingLoan?.id) {
        mutableStateOf(
            existingLoan?.startDate
                ?: SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale.getDefault()
                ).format(Date())
        )
    }

    var dueDate by remember(existingLoan?.id) {
        mutableStateOf(
            existingLoan?.dueDate ?: ""
        )
    }

    var note by remember(existingLoan?.id) {
        mutableStateOf(
            existingLoan?.note ?: ""
        )
    }

    var sourceExpanded by remember {
        mutableStateOf(false)
    }

    var namesExpanded by remember {
        mutableStateOf(false)
    }

    val bankBlue = Color(0xFF2D7FEA)
    val bankBlueDark = Color(0xFF286BAF)

    val personalPurple = Color(0xFF9346A8)
    val personalPurpleDark = Color(0xFF71377F)

    val accent = if (sourceTypeIsBank(sourceType)) {
        bankBlue
    } else {
        personalPurple
    }

    val headerColor = if (sourceTypeIsBank(sourceType)) {
        bankBlueDark
    } else {
        personalPurpleDark
    }

    val accentSoft = if (sourceTypeIsBank(sourceType)) {
        Color(0xFF162A43)
    } else {
        Color(0xFF2A1735)
    }

    fun pickDate(
        initial: String,
        onSelected: (String) -> Unit
    ) {
        val calendar = Calendar.getInstance()

        try {
            SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            ).parse(initial)?.let {
                calendar.time = it
            }
        } catch (_: Exception) {
        }

        DatePickerDialog(
            context,
            { _, year, month, day ->
                onSelected(
                    "%02d/%02d/%04d".format(
                        day,
                        month + 1,
                        year
                    )
                )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 18.dp
            )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    bottom = 18.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ---------------------------------------------------------
                // PREMIUM HEADER
                // ---------------------------------------------------------
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 0.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(
                                topStart = 30.dp,
                                topEnd = 30.dp,
                                bottomStart = 26.dp,
                                bottomEnd = 26.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = headerColor
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 0.dp
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 22.dp,
                                        top = 20.dp,
                                        end = 12.dp,
                                        bottom = 20.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Box(
                                    modifier = Modifier
                                        .size(62.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Color.White.copy(alpha = 0.16f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (
                                            sourceTypeIsBank(sourceType)
                                        ) {
                                            Icons.Default.AccountBalance
                                        } else {
                                            Icons.Default.Person
                                        },
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(
                                    modifier = Modifier.width(16.dp)
                                )

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    Text(
                                        text = if (editing) {
                                            if (
                                                sourceTypeIsBank(sourceType)
                                            ) {
                                                "BANK LOAN"
                                            } else {
                                                "PERSONAL LOAN"
                                            }
                                        } else {
                                            if (
                                                sourceTypeIsBank(sourceType)
                                            ) {
                                                "BANK LOAN"
                                            } else {
                                                "PERSONAL LOAN"
                                            }
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.5.sp,
                                        color = Color.White.copy(
                                            alpha = 0.72f
                                        )
                                    )

                                    Spacer(
                                        modifier = Modifier.height(3.dp)
                                    )

                                    Text(
                                        text = if (
                                            sourceTypeIsBank(sourceType)
                                        ) {
                                            if (editing) {
                                                "ব্যাংক ঋণ\nএডিট করুন"
                                            } else {
                                                "ব্যাংক ঋণ যোগ\nকরুন"
                                            }
                                        } else {
                                            if (editing) {
                                                "ব্যক্তিগত ঋণ\nএডিট করুন"
                                            } else {
                                                "ব্যক্তিগত ঋণ\nযোগ করুন"
                                            }
                                        },
                                        fontSize = 22.sp,
                                        lineHeight = 26.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )

                                    Spacer(
                                        modifier = Modifier.height(5.dp)
                                    )

                                    Text(
                                        text = if (
                                            sourceTypeIsBank(sourceType)
                                        ) {
                                            "ব্যাংক থেকে নেওয়া ঋণের তথ্য"
                                        } else {
                                            "ব্যক্তির কাছ থেকে নেওয়া ঋণের তথ্য দিন"
                                        },
                                        fontSize = 11.sp,
                                        color = Color.White.copy(
                                            alpha = 0.72f
                                        )
                                    )
                                }

                                IconButton(
                                    onClick = onDismiss
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "বন্ধ",
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ---------------------------------------------------------
                // LOAN TYPE
                // ---------------------------------------------------------
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {

                        Text(
                            text = "ঋণের ধরন",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(
                            modifier = Modifier.height(7.dp)
                        )

                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            OutlinedButton(
                                onClick = {
                                    sourceExpanded = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(70.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = accent
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.8.dp,
                                    accent
                                ),
                                contentPadding = PaddingValues(
                                    horizontal = 16.dp
                                )
                            ) {

                                Icon(
                                    imageVector = if (
                                        sourceTypeIsBank(sourceType)
                                    ) {
                                        Icons.Default.AccountBalance
                                    } else {
                                        Icons.Default.Person
                                    },
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(27.dp)
                                )

                                Spacer(
                                    modifier = Modifier.width(13.dp)
                                )

                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = if (
                                            sourceTypeIsBank(sourceType)
                                        ) {
                                            "ব্যাংক ঋণ"
                                        } else {
                                            "ব্যক্তিগত ঋণ"
                                        },
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        text = if (
                                            sourceTypeIsBank(sourceType)
                                        ) {
                                            "ব্যাংক থেকে"
                                        } else {
                                            "ব্যক্তির কাছ থেকে"
                                        },
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = accent,
                                    modifier = Modifier.size(27.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = sourceExpanded,
                                onDismissRequest = {
                                    sourceExpanded = false
                                }
                            ) {

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "ব্যাংক ঋণ",
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.AccountBalance,
                                            contentDescription = null,
                                            tint = bankBlue
                                        )
                                    },
                                    onClick = {
                                        sourceType = "bank"
                                        sourceExpanded = false
                                    }
                                )

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "ব্যক্তিগত ঋণ",
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = personalPurple
                                        )
                                    },
                                    onClick = {
                                        sourceType = "person"
                                        sourceExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // ---------------------------------------------------------
                // NAME
                // ---------------------------------------------------------
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {

                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text(
                                    if (
                                        sourceTypeIsBank(sourceType)
                                    ) {
                                        "ব্যাংকের নাম"
                                    } else {
                                        "ব্যক্তির নাম"
                                    }
                                )
                            },
                            placeholder = {
                                Text(
                                    if (
                                        sourceTypeIsBank(sourceType)
                                    ) {
                                        "যেমন: Bank of Maldives"
                                    } else {
                                        "যার কাছ থেকে নিয়েছেন"
                                    }
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (
                                        sourceTypeIsBank(sourceType)
                                    ) {
                                        Icons.Default.AccountBalance
                                    } else {
                                        Icons.Default.Person
                                    },
                                    contentDescription = null,
                                    tint = accent
                                )
                            },
                            trailingIcon = {
                                if (
                                    !editing &&
                                    existingNames.any {
                                        it.isNotBlank()
                                    }
                                ) {
                                    IconButton(
                                        onClick = {
                                            namesExpanded = true
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            contentDescription = "আগের নাম",
                                            tint = accent
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accent,
                                focusedLabelColor = accent,
                                cursorColor = accent
                            )
                        )

                        DropdownMenu(
                            expanded = namesExpanded,
                            onDismissRequest = {
                                namesExpanded = false
                            }
                        ) {
                            existingNames
                                .filter {
                                    it.isNotBlank()
                                }
                                .distinct()
                                .forEach { existingName ->

                                    DropdownMenuItem(
                                        text = {
                                            Text(existingName)
                                        },
                                        onClick = {
                                            name = existingName
                                            namesExpanded = false
                                        }
                                    )
                                }
                        }
                    }
                }

                // ---------------------------------------------------------
                // PRINCIPAL
                // ---------------------------------------------------------
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {

                        OutlinedTextField(
                            value = principal,
                            onValueChange = {
                                principal = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text(
                                    if (
                                        sourceTypeIsBank(sourceType)
                                    ) {
                                        "মোট ঋণের টাকা"
                                    } else {
                                        "কত টাকা নিয়েছেন"
                                    }
                                )
                            },
                            placeholder = {
                                Text("যেমন: 50000")
                            },
                            leadingIcon = {
                                Text(
                                    "৳",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = accent
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accent,
                                focusedLabelColor = accent,
                                cursorColor = accent
                            )
                        )
                    }
                }

                // ---------------------------------------------------------
                // PERSONAL INFO CARD
                // ---------------------------------------------------------
                if (!sourceTypeIsBank(sourceType)) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = accentSoft
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                personalPurple.copy(alpha = 0.38f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(
                                            personalPurple.copy(
                                                alpha = 0.18f
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = personalPurple,
                                        modifier = Modifier.size(23.dp)
                                    )
                                }

                                Spacer(
                                    modifier = Modifier.width(12.dp)
                                )

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        "ব্যক্তিগত ঋণ",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = personalPurple
                                    )

                                    Spacer(
                                        modifier = Modifier.height(3.dp)
                                    )

                                    Text(
                                        "এই ঋণের পেমেন্ট পরে আলাদাভাবে ট্র্যাক করা যাবে।",
                                        fontSize = 11.sp,
                                        lineHeight = 17.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // ---------------------------------------------------------
                // MONTHLY INSTALLMENT
                // ---------------------------------------------------------
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {

                        OutlinedTextField(
                            value = installment,
                            onValueChange = {
                                installment = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text(
                                    if (
                                        sourceTypeIsBank(sourceType)
                                    ) {
                                        "মাসিক কিস্তি (না থাকলে 0)"
                                    } else {
                                        "মাসিক কিস্তি (ঐচ্ছিক)"
                                    }
                                )
                            },
                            placeholder = {
                                Text(
                                    if (
                                        sourceTypeIsBank(sourceType)
                                    ) {
                                        "না থাকলে 0"
                                    } else {
                                        "কিস্তি না থাকলে খালি রাখুন"
                                    }
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = accent
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accent,
                                focusedLabelColor = accent,
                                cursorColor = accent
                            )
                        )
                    }
                }

                // ---------------------------------------------------------
                // DATES
                // ---------------------------------------------------------
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(
                            10.dp
                        )
                    ) {

                        LoanDatePickerCard(
                            modifier = Modifier.weight(1f),
                            title = "শুরুর তারিখ",
                            value = date,
                            icon = Icons.Default.CalendarMonth,
                            accent = accent,
                            onClick = {
                                pickDate(date) {
                                    date = it
                                }
                            }
                        )

                        LoanDatePickerCard(
                            modifier = Modifier.weight(1f),
                            title = if (
                                sourceTypeIsBank(sourceType)
                            ) {
                                "পরিশোধের তারিখ"
                            } else {
                                "ফেরত দেওয়ার তারিখ"
                            },
                            value = dueDate.ifBlank {
                                "সিলেক্ট করুন"
                            },
                            icon = Icons.Default.EventAvailable,
                            accent = accent,
                            onClick = {
                                pickDate(
                                    dueDate.ifBlank {
                                        date
                                    }
                                ) {
                                    dueDate = it
                                }
                            }
                        )
                    }
                }

                // ---------------------------------------------------------
                // NOTE
                // ---------------------------------------------------------
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {

                        OutlinedTextField(
                            value = note,
                            onValueChange = {
                                note = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = {
                                Text(
                                    if (
                                        sourceTypeIsBank(sourceType)
                                    ) {
                                        "নোট (ঐচ্ছিক)"
                                    } else {
                                        "নোট / অতিরিক্ত তথ্য"
                                    }
                                )
                            },
                            placeholder = {
                                Text(
                                    if (
                                        sourceTypeIsBank(sourceType)
                                    ) {
                                        "ঋণ সম্পর্কে অতিরিক্ত তথ্য"
                                    } else {
                                        "চুক্তি বা অন্য কোনো তথ্য"
                                    }
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Notes,
                                    contentDescription = null,
                                    tint = accent
                                )
                            },
                            maxLines = 3,
                            shape = RoundedCornerShape(18.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accent,
                                focusedLabelColor = accent,
                                cursorColor = accent
                            )
                        )
                    }
                }

                // ---------------------------------------------------------
                // BUTTONS
                // ---------------------------------------------------------
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 20.dp,
                                end = 20.dp,
                                top = 2.dp
                            ),
                        horizontalArrangement = Arrangement.spacedBy(
                            10.dp
                        )
                    ) {

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(0.72f)
                                .height(58.dp),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text(
                                "বাতিল",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                val amount = principal
                                    .replace(",", "")
                                    .trim()
                                    .toDoubleOrNull()

                                val monthly = installment
                                    .replace(",", "")
                                    .trim()
                                    .toDoubleOrNull()
                                    ?: 0.0

                                if (
                                    name.isBlank() ||
                                    amount == null ||
                                    amount <= 0.0 ||
                                    monthly < 0.0
                                ) {
                                    WarningPopupManager.show(
                                        title = "ঋণের তথ্য সঠিক নয়",
                                        message = "নাম ও সঠিক ঋণের টাকা দিন।"
                                    )
                                } else {
                                    onSave(
                                        name.trim(),
                                        sourceType,
                                        amount,
                                        monthly,
                                        date,
                                        note.trim(),
                                        dueDate.takeIf {
                                            it.isNotBlank()
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .weight(1.28f)
                                .height(58.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accent
                            )
                        ) {

                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )

                            Spacer(
                                modifier = Modifier.width(7.dp)
                            )

                            Text(
                                if (editing) {
                                    "আপডেট করুন"
                                } else {
                                    "ঋণ সংরক্ষণ"
                                },
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                // ---------------------------------------------------------
                // SECURITY FOOTER
                // ---------------------------------------------------------
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 20.dp,
                                vertical = 2.dp
                            ),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = accent.copy(alpha = 0.72f),
                            modifier = Modifier.size(15.dp)
                        )

                        Spacer(
                            modifier = Modifier.width(5.dp)
                        )

                        Text(
                            "আপনার তথ্য নিরাপদে সংরক্ষিত থাকবে",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}


// ============================================================================
// DATE PICKER CARD
// ============================================================================

@Composable
private fun LoanDatePickerCard(
    modifier: Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(78.dp),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.4.dp,
            MaterialTheme.colorScheme.outline.copy(
                alpha = 0.72f
            )
        ),
        contentPadding = PaddingValues(
            horizontal = 11.dp,
            vertical = 8.dp
        )
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(21.dp)
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {

                Text(
                    title,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                Text(
                    value,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}


// ============================================================================
// SOURCE TYPE HELPER
// ============================================================================

private fun sourceTypeIsBank(
    sourceType: String
): Boolean {
    return sourceType.equals(
        "bank",
        ignoreCase = true
    )
}


// ============================================================================
// BANK LOAN PAYMENT DIALOG
// ============================================================================

@Composable
fun PremiumBankLoanPaymentDialog(
    loan: LoanAccount,
    onDismiss: () -> Unit,
    onSave: (Double, String, String) -> Unit
) {
    val context = LocalContext.current

    val bankBlue = Color(0xFF2D7FEA)

    var amount by remember {
        mutableStateOf("")
    }

    var date by remember {
        mutableStateOf(
            SimpleDateFormat(
                "dd/MM/yyyy",
                Locale.getDefault()
            ).format(Date())
        )
    }

    var note by remember {
        mutableStateOf("")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {

        Card(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(30.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 18.dp
            )
        ) {

            Column(
                Modifier
                    .fillMaxWidth()
            ) {

                // ---------------------------------------------------------
                // HEADER
                // ---------------------------------------------------------
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(
                        topStart = 30.dp,
                        topEnd = 30.dp,
                        bottomStart = 24.dp,
                        bottomEnd = 24.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF286BAF)
                    )
                ) {

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 20.dp,
                                top = 19.dp,
                                end = 10.dp,
                                bottom = 19.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Box(
                            Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.White.copy(alpha = 0.16f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {

                            Icon(
                                Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(29.dp)
                            )
                        }

                        Spacer(
                            Modifier.width(13.dp)
                        )

                        Column(
                            Modifier.weight(1f)
                        ) {

                            Text(
                                "BANK LOAN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                color = Color.White.copy(
                                    alpha = 0.72f
                                )
                            )

                            Text(
                                "ঋণ পরিশোধ",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Text(
                                loan.name,
                                fontSize = 11.sp,
                                color = Color.White.copy(
                                    alpha = 0.72f
                                )
                            )
                        }

                        IconButton(
                            onClick = onDismiss
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "বন্ধ",
                                tint = Color.White,
                                modifier = Modifier.size(29.dp)
                            )
                        }
                    }
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {

                    // -----------------------------------------------------
                    // LOAN SUMMARY
                    // -----------------------------------------------------
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF162A43)
                        )
                    ) {

                        Column(
                            Modifier.padding(18.dp)
                        ) {

                            Text(
                                "এই ঋণের পেমেন্ট রেকর্ড করুন",
                                fontSize = 12.sp,
                                color = Color.White.copy(
                                    alpha = 0.75f
                                )
                            )

                            Spacer(
                                Modifier.height(4.dp)
                            )

                            Text(
                                "৳${formatMoney(loan.principal)}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Text(
                                "মূল ঋণের পরিমাণ",
                                fontSize = 10.sp,
                                color = Color.White.copy(
                                    alpha = 0.62f
                                )
                            )
                        }
                    }

                    Spacer(
                        Modifier.height(13.dp)
                    )

                    // -----------------------------------------------------
                    // PAYMENT AMOUNT
                    // -----------------------------------------------------
                    OutlinedTextField(
                        value = amount,
                        onValueChange = {
                            amount = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("পরিশোধের টাকা")
                        },
                        leadingIcon = {
                            Text(
                                "৳",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = bankBlue
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = bankBlue,
                            focusedLabelColor = bankBlue,
                            cursorColor = bankBlue
                        )
                    )

                    Spacer(
                        Modifier.height(10.dp)
                    )

                    // -----------------------------------------------------
                    // DATE
                    // -----------------------------------------------------
                    OutlinedButton(
                        onClick = {
                            val calendar = Calendar.getInstance()

                            try {
                                SimpleDateFormat(
                                    "dd/MM/yyyy",
                                    Locale.getDefault()
                                ).parse(date)?.let {
                                    calendar.time = it
                                }
                            } catch (_: Exception) {
                            }

                            DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    date = "%02d/%02d/%04d".format(
                                        day,
                                        month + 1,
                                        year
                                    )
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(62.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {

                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = bankBlue
                        )

                        Spacer(
                            Modifier.width(9.dp)
                        )

                        Column(
                            Modifier.weight(1f),
                            horizontalAlignment = Alignment.Start
                        ) {

                            Text(
                                "পরিশোধের তারিখ",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                date,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(
                        Modifier.height(10.dp)
                    )

                    // -----------------------------------------------------
                    // NOTE
                    // -----------------------------------------------------
                    OutlinedTextField(
                        value = note,
                        onValueChange = {
                            note = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text("নোট (ঐচ্ছিক)")
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Notes,
                                contentDescription = null,
                                tint = bankBlue
                            )
                        },
                        maxLines = 3,
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = bankBlue,
                            focusedLabelColor = bankBlue,
                            cursorColor = bankBlue
                        )
                    )

                    Spacer(
                        Modifier.height(15.dp)
                    )

                    // -----------------------------------------------------
                    // BUTTONS
                    // -----------------------------------------------------
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(
                            10.dp
                        )
                    ) {

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(0.72f)
                                .height(55.dp),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text(
                                "বাতিল",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                val value = amount
                                    .replace(",", "")
                                    .trim()
                                    .toDoubleOrNull()

                                if (
                                    value == null ||
                                    value <= 0.0
                                ) {
                                    WarningPopupManager.show(
                                        title = "পরিশোধের টাকা সঠিক নয়",
                                        message = "দয়া করে সঠিক পরিশোধের টাকা দিন।"
                                    )
                                } else {
                                    onSave(
                                        value,
                                        date,
                                        note.trim()
                                    )
                                }
                            },
                            modifier = Modifier
                                .weight(1.28f)
                                .height(55.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = bankBlue
                            )
                        ) {

                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )

                            Spacer(
                                Modifier.width(7.dp)
                            )

                            Text(
                                "পরিশোধ সংরক্ষণ",
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}