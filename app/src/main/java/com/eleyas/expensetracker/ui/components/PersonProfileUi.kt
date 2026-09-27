package com.eleyas.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.PersonProfile
import com.eleyas.expensetracker.util.userIdForPerson
import android.content.SharedPreferences

/** Compact profile action shown beside a person's name after a transaction is saved. */
@Composable
fun PersonProfileAction(
    person: PersonProfile?,
    prefs: SharedPreferences,
    onCreateProfile: () -> Unit,
    onEditProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val hasProfile = person != null

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = primary.copy(alpha = 0.09f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = if (hasProfile) onEditProfile else onCreateProfile,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = if (hasProfile) Icons.Default.Edit else Icons.Default.PersonAdd,
                    contentDescription = if (hasProfile) "প্রোফাইল এডিট" else "প্রোফাইল তৈরি",
                    tint = primary,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
fun PersonUserIdBadge(
    person: PersonProfile,
    prefs: SharedPreferences,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val userId = userIdForPerson(prefs, person)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(primary.copy(alpha = 0.08f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Person,
            contentDescription = null,
            tint = primary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            userId,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = primary
        )
    }
}
