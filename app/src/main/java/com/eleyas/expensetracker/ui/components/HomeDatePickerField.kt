package com.eleyas.expensetracker.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeDatePickerField(
    value: String,
    label: String,
    onDateSelected: (String) -> Unit
) {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply { isLenient = false }
    val initial = runCatching { formatter.parse(value) }.getOrNull()
    val calendar = Calendar.getInstance().apply { if (initial != null) time = initial }

    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val dialog = DatePickerDialog(
                    /* context is supplied by LocalContext below via overload */
                    android.view.ContextThemeWrapper(null, 0),
                    null,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )
                dialog.show()
            },
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
        readOnly = true,
        singleLine = true
    )
}
