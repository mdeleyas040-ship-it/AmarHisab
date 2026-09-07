package com.eleyas.expensetracker.ui.birthday

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.Birthday
import com.eleyas.expensetracker.repository.BirthdayStorage

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun BirthdayScreen(
    context: Context,
    userId: String,
    onBack: () -> Unit
) {

    val birthdays = remember {
        mutableStateListOf<Birthday>()
    }

    var showEditor by remember {
        mutableStateOf(false)
    }

    var deletingBirthday by remember {
        mutableStateOf<Birthday?>(null)
    }

    LaunchedEffect(userId) {

        birthdays.clear()

        birthdays.addAll(
            BirthdayStorage.load(
                context = context,
                userId = userId
            )
        )
    }

    if (showEditor) {

        BirthdayEditor(
            birthday = null,

            onBack = {
                showEditor = false
            },

            onSave = { birthday ->

                birthdays.add(birthday)

                BirthdayStorage.save(
                    context = context,
                    birthdays = birthdays.toList(),
                    userId = userId
                )

                showEditor = false
            }
        )

        return
    }

    Scaffold(

        containerColor =
            MaterialTheme.colorScheme.background,

        topBar = {

            TopAppBar(

                title = {

                    Column {

                        Text(
                            text = "জন্মদিন",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "প্রিয় মানুষগুলোর বিশেষ দিন",
                            fontSize = 11.sp,
                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,

                            contentDescription =
                                "ফিরে যান"
                        )
                    }
                },

                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor =
                            MaterialTheme.colorScheme.background
                    )
            )
        }

    ) { paddingValues ->

        if (birthdays.isEmpty()) {

            EmptyBirthdayState(
                paddingValues = paddingValues,

                onAdd = {
                    showEditor = true
                }
            )

        } else {

            LazyColumn(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),

                contentPadding =
                    PaddingValues(
                        start = 16.dp,
                        top = 14.dp,
                        end = 16.dp,
                        bottom = 30.dp
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(14.dp)
            ) {

                item {

                    BirthdayHeroHeader(
                        totalBirthdays =
                            birthdays.size,

                        onAdd = {
                            showEditor = true
                        }
                    )
                }

                item {

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Event,

                            contentDescription = null,

                            tint =
                                MaterialTheme.colorScheme.primary,

                            modifier =
                                Modifier.size(20.dp)
                        )

                        Spacer(
                            modifier =
                                Modifier.width(8.dp)
                        )

                        Text(
                            text =
                                "সংরক্ষিত জন্মদিন",

                            fontSize = 16.sp,

                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.weight(1f)
                        )

                        Text(
                            text =
                                "${birthdays.size} জন",

                            fontSize = 12.sp,

                            color =
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(

                    items =
                        birthdays.sortedBy {
                            daysUntilBirthday(
                                it.birthDate
                            )
                        },

                    key = {
                        it.id
                    }

                ) { birthday ->

                    BirthdayListItem(
                        birthday = birthday,

                        onDelete = {
                            deletingBirthday =
                                birthday
                        }
                    )
                }
            }
        }
    }

    deletingBirthday?.let { birthday ->

        AlertDialog(

            onDismissRequest = {
                deletingBirthday = null
            },

            icon = {

                Surface(
                    modifier =
                        Modifier.size(50.dp),

                    shape =
                        CircleShape,

                    color =
                        MaterialTheme.colorScheme.errorContainer
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Delete,

                        contentDescription = null,

                        tint =
                            MaterialTheme.colorScheme.error,

                        modifier =
                            Modifier.padding(13.dp)
                    )
                }
            },

            title = {

                Text(
                    text =
                        "জন্মদিন মুছে ফেলবেন?",

                    fontWeight =
                        FontWeight.Bold
                )
            },

            text = {

                Text(
                    text =
                        "\"${birthday.name}\"-এর জন্মদিনের তথ্য স্থায়ীভাবে মুছে যাবে।"
                )
            },

            confirmButton = {

                Button(

                    onClick = {

                        birthdays.removeAll {
                            it.id == birthday.id
                        }

                        BirthdayStorage.save(
                            context = context,

                            birthdays =
                                birthdays.toList(),

                            userId =
                                userId
                        )

                        deletingBirthday = null
                    },

                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                MaterialTheme.colorScheme.error
                        )
                ) {

                    Text("মুছে ফেলুন")
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        deletingBirthday = null
                    }
                ) {

                    Text("বাতিল")
                }
            }
        )
    }
}


@Composable
private fun BirthdayHeroHeader(
    totalBirthdays: Int,
    onAdd: () -> Unit
) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(28.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.Transparent
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 5.dp
            )
    ) {

        Box(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .padding(21.dp)
        ) {

            Column {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Surface(
                        modifier =
                            Modifier.size(56.dp),

                        shape =
                            CircleShape,

                        color =
                            Color.White.copy(
                                alpha = 0.18f
                            )
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Cake,

                            contentDescription = null,

                            tint =
                                Color.White,

                            modifier =
                                Modifier.padding(14.dp)
                        )
                    }

                    Spacer(
                        modifier =
                            Modifier.width(14.dp)
                    )

                    Column {

                        Text(
                            text =
                                "জন্মদিন মনে রাখুন",

                            color =
                                Color.White,

                            fontSize =
                                21.sp,

                            fontWeight =
                                FontWeight.ExtraBold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(4.dp)
                        )

                        Text(
                            text =
                                "প্রিয় মানুষদের বিশেষ দিন আর ভুলবেন না",

                            color =
                                Color.White.copy(
                                    alpha = 0.88f
                                ),

                            fontSize =
                                12.sp
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Surface(
                        shape =
                            RoundedCornerShape(14.dp),

                        color =
                            Color.White.copy(
                                alpha = 0.16f
                            )
                    ) {

                        Row(
                            modifier =
                                Modifier.padding(
                                    horizontal = 12.dp,
                                    vertical = 8.dp
                                ),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.People,

                                contentDescription = null,

                                tint =
                                    Color.White,

                                modifier =
                                    Modifier.size(18.dp)
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(7.dp)
                            )

                            Text(
                                text =
                                    "$totalBirthdays জন সংরক্ষিত",

                                color =
                                    Color.White,

                                fontSize =
                                    13.sp,

                                fontWeight =
                                    FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(
                        modifier =
                            Modifier.weight(1f)
                    )

                    Button(

                        onClick = onAdd,

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    Color.White,

                                contentColor =
                                    MaterialTheme.colorScheme.primary
                            ),

                        shape =
                            RoundedCornerShape(15.dp)
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.Add,

                            contentDescription = null,

                            modifier =
                                Modifier.size(18.dp)
                        )

                        Spacer(
                            modifier =
                                Modifier.width(5.dp)
                        )

                        Text(
                            text = "যোগ করুন",

                            fontWeight =
                                FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun BirthdayListItem(
    birthday: Birthday,
    onDelete: () -> Unit
) {

    val days =
        daysUntilBirthday(
            birthday.birthDate
        )

    val isToday =
        days == 0

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(24.dp),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation =
                    if (isToday) 6.dp else 2.dp
            ),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface
            )
    ) {

        Column {

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            if (isToday) {

                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary
                                    )
                                )

                            } else {

                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.secondaryContainer
                                    )
                                )
                            }
                        )
                        .padding(18.dp)
            ) {

                Column {

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
                                CircleShape,

                            color =
                                if (isToday) {
                                    Color.White.copy(
                                        alpha = 0.20f
                                    )
                                } else {
                                    MaterialTheme.colorScheme.surface.copy(
                                        alpha = 0.55f
                                    )
                                }
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.Cake,

                                contentDescription = null,

                                tint =
                                    if (isToday) {
                                        Color.White
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },

                                modifier =
                                    Modifier.padding(12.dp)
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.width(13.dp)
                        )

                        Column(
                            modifier =
                                Modifier.weight(1f)
                        ) {

                            Text(
                                text =
                                    birthday.name,

                                fontSize =
                                    20.sp,

                                fontWeight =
                                    FontWeight.ExtraBold,

                                color =
                                    if (isToday) {
                                        Color.White
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(3.dp)
                            )

                            Text(
                                text =
                                    "জন্মতারিখ • ${formatBirthdayDate(birthday.birthDate)}",

                                fontSize =
                                    12.sp,

                                color =
                                    if (isToday) {
                                        Color.White.copy(
                                            alpha = 0.85f
                                        )
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                            )
                        }
                    }

                    Spacer(
                        modifier =
                            Modifier.height(16.dp)
                    )

                    if (isToday) {

                        Surface(
                            shape =
                                RoundedCornerShape(14.dp),

                            color =
                                Color.White.copy(
                                    alpha = 0.18f
                                )
                        ) {

                            Text(
                                text =
                                    "🎉 আজ জন্মদিন!",

                                color =
                                    Color.White,

                                fontSize =
                                    15.sp,

                                fontWeight =
                                    FontWeight.ExtraBold,

                                modifier =
                                    Modifier.padding(
                                        horizontal = 14.dp,
                                        vertical = 9.dp
                                    )
                            )
                        }

                    } else {

                        Surface(
                            shape =
                                RoundedCornerShape(14.dp),

                            color =
                                MaterialTheme.colorScheme.surface.copy(
                                    alpha = 0.60f
                                )
                        ) {

                            Text(
                                text =
                                    when (days) {

                                        1 ->
                                            "🎂 আগামীকাল জন্মদিন"

                                        else ->
                                            "🎂 $days দিন পর জন্মদিন"
                                    },

                                fontSize =
                                    14.sp,

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    MaterialTheme.colorScheme.primary,

                                modifier =
                                    Modifier.padding(
                                        horizontal = 14.dp,
                                        vertical = 9.dp
                                    )
                            )
                        }
                    }

                    Spacer(
                        modifier =
                            Modifier.height(13.dp)
                    )

                    Text(
                        text =
                            "বর্তমান বয়স: ${calculateAge(birthday.birthDate)} বছর",

                        fontSize =
                            13.sp,

                        color =
                            if (isToday) {
                                Color.White.copy(
                                    alpha = 0.90f
                                )
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                    )

                    if (birthday.note.isNotBlank()) {

                        Spacer(
                            modifier =
                                Modifier.height(9.dp)
                        )

                        Text(
                            text =
                                birthday.note,

                            fontSize =
                                12.sp,

                            color =
                                if (isToday) {
                                    Color.White.copy(
                                        alpha = 0.82f
                                    )
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },

                            maxLines = 2
                        )
                    }
                }
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 14.dp,
                            vertical = 7.dp
                        ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text =
                        "সংরক্ষিত জন্মদিন",

                    fontSize =
                        11.sp,

                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(
                    modifier =
                        Modifier.weight(1f)
                )

                IconButton(
                    onClick = onDelete,

                    modifier =
                        Modifier.size(38.dp)
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Delete,

                        contentDescription =
                            "জন্মদিন মুছে ফেলুন",

                        tint =
                            MaterialTheme.colorScheme.error,

                        modifier =
                            Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}


@Composable
private fun EmptyBirthdayState(
    paddingValues: PaddingValues,
    onAdd: () -> Unit
) {

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center
    ) {

        Surface(

            modifier =
                Modifier.size(96.dp),

            shape =
                CircleShape,

            color =
                MaterialTheme.colorScheme.primaryContainer
        ) {

            Icon(
                imageVector =
                    Icons.Default.Cake,

                contentDescription = null,

                tint =
                    MaterialTheme.colorScheme.primary,

                modifier =
                    Modifier.padding(25.dp)
            )
        }

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        Text(
            text =
                "এখনও কোনো জন্মদিন যোগ করা হয়নি",

            fontSize =
                18.sp,

            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(7.dp)
        )

        Text(
            text =
                "পরিবার ও প্রিয় মানুষদের জন্মদিন সংরক্ষণ করুন",

            fontSize =
                13.sp,

            color =
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        Button(

            onClick = onAdd,

            shape =
                RoundedCornerShape(15.dp)
        ) {

            Icon(
                imageVector =
                    Icons.Default.PersonAdd,

                contentDescription = null,

                modifier =
                    Modifier.size(19.dp)
            )

            Spacer(
                modifier =
                    Modifier.width(7.dp)
            )

            Text(
                text =
                    "প্রথম জন্মদিন যোগ করুন",

                fontWeight =
                    FontWeight.Bold
            )
        }
    }
}


/*
 * জন্মতারিখকে সবসময় DD/MM/YYYY
 * হিসেবে দেখাবে।
 */
private fun formatBirthdayDate(
    birthDate: String
): String {

    val parts =
        birthDate.trim().split("/")

    if (parts.size != 3) {
        return birthDate
    }

    val day =
        parts[0].toIntOrNull()

    val month =
        parts[1].toIntOrNull()

    val year =
        parts[2].toIntOrNull()

    if (
        day == null ||
        month == null ||
        year == null
    ) {
        return birthDate
    }

    return "%02d/%02d/%04d".format(
        day,
        month,
        year
    )
}


/*
 * বয়স হিসাব
 */
private fun calculateAge(
    birthDate: String
): Int {

    return try {

        val parts =
            birthDate.split("/")

        val day =
            parts[0].toInt()

        val month =
            parts[1].toInt()

        val year =
            parts[2].toInt()

        val today =
            java.util.Calendar.getInstance()

        var age =
            today.get(
                java.util.Calendar.YEAR
            ) - year

        val currentMonth =
            today.get(
                java.util.Calendar.MONTH
            ) + 1

        val currentDay =
            today.get(
                java.util.Calendar.DAY_OF_MONTH
            )

        if (
            currentMonth < month ||
            (
                    currentMonth == month &&
                            currentDay < day
                    )
        ) {
            age--
        }

        age.coerceAtLeast(0)

    } catch (_: Exception) {

        0
    }
}


/*
 * পরবর্তী জন্মদিন পর্যন্ত দিন
 */
private fun daysUntilBirthday(
    birthDate: String
): Int {

    return try {

        val parts =
            birthDate.split("/")

        if (parts.size != 3) {
            return Int.MAX_VALUE
        }

        val day =
            parts[0].toInt()

        val month =
            parts[1].toInt()

        val today =
            java.util.Calendar.getInstance()

        val birthday =
            java.util.Calendar.getInstance().apply {

                set(
                    java.util.Calendar.YEAR,
                    today.get(
                        java.util.Calendar.YEAR
                    )
                )

                set(
                    java.util.Calendar.MONTH,
                    month - 1
                )

                set(
                    java.util.Calendar.DAY_OF_MONTH,
                    day
                )

                set(
                    java.util.Calendar.HOUR_OF_DAY,
                    0
                )

                set(
                    java.util.Calendar.MINUTE,
                    0
                )

                set(
                    java.util.Calendar.SECOND,
                    0
                )

                set(
                    java.util.Calendar.MILLISECOND,
                    0
                )
            }

        val todayOnly =
            java.util.Calendar.getInstance().apply {

                set(
                    java.util.Calendar.HOUR_OF_DAY,
                    0
                )

                set(
                    java.util.Calendar.MINUTE,
                    0
                )

                set(
                    java.util.Calendar.SECOND,
                    0
                )

                set(
                    java.util.Calendar.MILLISECOND,
                    0
                )
            }

        if (
            birthday.before(todayOnly)
        ) {

            birthday.add(
                java.util.Calendar.YEAR,
                1
            )
        }

        val difference =
            birthday.timeInMillis -
                    todayOnly.timeInMillis

        (
                difference /
                        (24L * 60L * 60L * 1000L)
                ).toInt()

    } catch (_: Exception) {

        Int.MAX_VALUE
    }
}