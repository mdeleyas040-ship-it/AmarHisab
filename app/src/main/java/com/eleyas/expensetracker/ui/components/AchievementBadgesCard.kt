package com.eleyas.expensetracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleyas.expensetracker.model.AchievementBadge

@Composable
fun AchievementBadgesCard(
    badges: List<AchievementBadge>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text("অ্যাচিভমেন্ট ব্যাজ", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Text(
                "বাজেট মেনে চলুন, নতুন ব্যাজ আনলক করুন",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(badges, key = { it.id }) { badge ->
                    Surface(
                        modifier = Modifier.width(155.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = if (badge.earned) Color(0xFFFFF3CD) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(badge.icon, fontSize = 25.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    badge.title,
                                    fontWeight = FontWeight.Bold,
                                    color = if (badge.earned) Color(0xFF9A6700) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                if (badge.earned) "অর্জিত ✓" else "${badge.progressMonths}/${badge.requiredMonths} মাস",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(5.dp))
                            LinearProgressIndicator(
                                progress = { badge.progressMonths.toFloat() / badge.requiredMonths },
                                modifier = Modifier.fillMaxWidth(),
                                color = if (badge.earned) Color(0xFFFFB300) else MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(badge.description, fontSize = 10.sp, lineHeight = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
