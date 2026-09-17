package com.eleyas.expensetracker.ui.birthday

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.eleyas.expensetracker.model.Birthday
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun BirthdayCard(
    birthday: Birthday,
    onEdit: () -> Unit,
    onDelete: () -> Unit = {}
) {
    val birthDate = parseDate(birthday.birthDate)

    val age = if (birthDate != null) {
        calculateAge(birthDate)
    } else {
        null
    }

    val daysRemaining = if (birthDate != null) {
        calculateDaysUntilBirthday(birthDate)
    } else {
        null
    }

    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.secondaryContainer
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(gradient)
            .padding(20.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Default.Cake,
                contentDescription = "জন্মদিন"
            )

            Spacer(modifier = Modifier.padding(horizontal = 6.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = birthday.name,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "জন্মদিন: ${birthday.birthDate}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            IconButton(
                onClick = onEdit
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "এডিট"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (age != null) {
            Text(
                text = "বর্তমান বয়স: $age বছর",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (daysRemaining != null) {

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Icon(
                    imageVector = Icons.Default.Celebration,
                    contentDescription = null
                )

                Text(
                    text = when (daysRemaining) {
                        0 -> "আজ জন্মদিন! 🎉"
                        1 -> "আগামীকাল জন্মদিন"
                        else -> "$daysRemaining দিন পর জন্মদিন"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        if (birthday.note.isNotBlank()) {

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = birthday.note,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun parseDate(date: String): Calendar? {
    return try {

        val formatter = SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        )

        formatter.isLenient = false

        val parsed = formatter.parse(date) ?: return null

        Calendar.getInstance().apply {
            time = parsed
        }

    } catch (_: Exception) {
        null
    }
}

private fun calculateAge(birthDate: Calendar): Int {

    val today = Calendar.getInstance()

    var age =
        today.get(Calendar.YEAR) -
                birthDate.get(Calendar.YEAR)

    val birthdayThisYear = Calendar.getInstance().apply {
        set(
            Calendar.YEAR,
            today.get(Calendar.YEAR)
        )
        set(
            Calendar.MONTH,
            birthDate.get(Calendar.MONTH)
        )
        set(
            Calendar.DAY_OF_MONTH,
            birthDate.get(Calendar.DAY_OF_MONTH)
        )
    }

    if (today.before(birthdayThisYear)) {
        age--
    }

    return age
}

private fun calculateDaysUntilBirthday(
    birthDate: Calendar
): Int {

    val today = Calendar.getInstance()

    today.set(
        Calendar.HOUR_OF_DAY,
        0
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

    val nextBirthday = Calendar.getInstance().apply {

        set(
            Calendar.MONTH,
            birthDate.get(Calendar.MONTH)
        )

        set(
            Calendar.DAY_OF_MONTH,
            birthDate.get(Calendar.DAY_OF_MONTH)
        )

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

        if (before(today)) {
            add(Calendar.YEAR, 1)
        }
    }

    val difference =
        nextBirthday.timeInMillis -
                today.timeInMillis

    return (
            difference /
                    (24L * 60L * 60L * 1000L)
            ).toInt()
}