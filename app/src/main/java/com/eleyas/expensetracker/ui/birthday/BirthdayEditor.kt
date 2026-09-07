package com.eleyas.expensetracker.ui.birthday

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.Birthday

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun BirthdayEditor(
    birthday: Birthday? = null,
    onSave: (Birthday) -> Unit,
    onBack: () -> Unit
) {
    var name by remember {
        mutableStateOf(birthday?.name ?: "")
    }

    var birthDate by remember {
        mutableStateOf(birthday?.birthDate ?: "")
    }

    var note by remember {
        mutableStateOf(birthday?.note ?: "")
    }

    var nameError by remember {
        mutableStateOf(false)
    }

    var dateError by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(birthday?.id) {
        name = birthday?.name ?: ""
        birthDate = birthday?.birthDate ?: ""
        note = birthday?.note ?: ""
        nameError = false
        dateError = false
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (birthday == null) {
                                "জন্মদিন যোগ করুন"
                            } else {
                                "জন্মদিন এডিট করুন"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (birthday == null) {
                                "বিশেষ দিনটি সংরক্ষণ করুন"
                            } else {
                                "জন্মদিনের তথ্য পরিবর্তন করুন"
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },

                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান"
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),

            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            /*
             * Premium Header
             */
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                        .padding(20.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Surface(
                            modifier = Modifier.size(54.dp),
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.18f)
                        ) {

                            Icon(
                                imageVector = if (birthday == null) {
                                    Icons.Default.Cake
                                } else {
                                    Icons.Default.Edit
                                },

                                contentDescription = null,

                                tint = Color.White,

                                modifier = Modifier.padding(14.dp)
                            )
                        }

                        Spacer(
                            modifier = Modifier.width(14.dp)
                        )

                        Column {

                            Text(
                                text = if (birthday == null) {
                                    "নতুন জন্মদিন"
                                } else {
                                    "জন্মদিন আপডেট করুন"
                                },

                                color = Color.White,

                                fontSize = 20.sp,

                                fontWeight = FontWeight.ExtraBold
                            )

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Text(
                                text = "সঠিক তথ্য দিলে জন্মদিনের countdown আরও নির্ভুল হবে",

                                color = Color.White.copy(alpha = 0.88f),

                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            /*
             * Form title
             */
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text(
                    text = "জন্মদিনের তথ্য",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            /*
             * Name
             */
            OutlinedTextField(
                value = name,

                onValueChange = {
                    name = it
                    nameError = false
                },

                modifier = Modifier.fillMaxWidth(),

                label = {
                    Text("নাম")
                },

                placeholder = {
                    Text("যেমন: বাবা, মা, বন্ধু")
                },

                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null
                    )
                },

                singleLine = true,

                isError = nameError,

                supportingText = {
                    if (nameError) {
                        Text(
                            "নাম লিখুন",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },

                shape = RoundedCornerShape(16.dp)
            )

            /*
             * Birth Date
             */
            OutlinedTextField(
                value = birthDate,

                onValueChange = {
                    birthDate = it
                    dateError = false
                },

                modifier = Modifier.fillMaxWidth(),

                label = {
                    Text("জন্মতারিখ")
                },

                placeholder = {
                    Text("DD/MM/YYYY")
                },

                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null
                    )
                },

                singleLine = true,

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),

                isError = dateError,

                supportingText = {
                    if (dateError) {

                        Text(
                            "সঠিক ফরম্যাটে জন্মতারিখ দিন",
                            color = MaterialTheme.colorScheme.error
                        )

                    } else {

                        Text(
                            "ফরম্যাট: 25/12/2000"
                        )
                    }
                },

                shape = RoundedCornerShape(16.dp)
            )

            /*
             * Note
             */
            OutlinedTextField(
                value = note,

                onValueChange = {
                    note = it
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp),

                label = {
                    Text("নোট")
                },

                placeholder = {
                    Text("ঐচ্ছিক তথ্য লিখুন")
                },

                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Notes,
                        contentDescription = null
                    )
                },

                maxLines = 4,

                shape = RoundedCornerShape(16.dp)
            )

            Spacer(
                modifier = Modifier.height(2.dp)
            )

            /*
             * Save Button
             */
            Button(
                onClick = {

                    val cleanName =
                        name.trim()

                    val cleanDate =
                        birthDate.trim()

                    val validName =
                        cleanName.isNotEmpty()

                    val validDate =
                        isValidBirthdayDate(cleanDate)

                    nameError =
                        !validName

                    dateError =
                        !validDate

                    if (
                        validName &&
                        validDate
                    ) {

                        val savedBirthday =
                            Birthday(

                                id =
                                    birthday?.id
                                        ?: System.currentTimeMillis()
                                            .toString(),

                                name =
                                    cleanName,

                                birthDate =
                                    cleanDate,

                                note =
                                    note.trim(),

                                createdAt =
                                    birthday?.createdAt
                                        ?: System.currentTimeMillis()
                            )

                        onSave(
                            savedBirthday
                        )
                    }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),

                shape = RoundedCornerShape(17.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        MaterialTheme.colorScheme.primary
                )
            ) {

                Icon(
                    imageVector =
                        Icons.Default.Save,

                    contentDescription = null,

                    modifier =
                        Modifier.size(20.dp)
                )

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Text(
                    text =
                        if (birthday == null) {
                            "জন্মদিন সংরক্ষণ করুন"
                        } else {
                            "পরিবর্তন সংরক্ষণ করুন"
                        },

                    fontSize = 15.sp,

                    fontWeight =
                        FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(4.dp)
            )
        }
    }
}

private fun isValidBirthdayDate(
    date: String
): Boolean {

    if (
        !Regex(
            "^\\d{2}/\\d{2}/\\d{4}$"
        ).matches(date)
    ) {
        return false
    }

    return try {

        val parts =
            date.split("/")

        val day =
            parts[0].toInt()

        val month =
            parts[1].toInt()

        val year =
            parts[2].toInt()

        if (
            month !in 1..12
        ) {
            return false
        }

        if (
            year !in
            1900..CalendarMaxYear.current()
        ) {
            return false
        }

        val calendar =
            java.util.Calendar.getInstance()

        calendar.setLenient(false)

        calendar.set(
            year,
            month - 1,
            day,
            0,
            0,
            0
        )

        calendar.set(
            java.util.Calendar.MILLISECOND,
            0
        )

        calendar.time

        true

    } catch (
        _: Exception
    ) {
        false
    }
}

private object CalendarMaxYear {

    fun current(): Int {
        return java.util.Calendar
            .getInstance()
            .get(
                java.util.Calendar.YEAR
            )
    }
}