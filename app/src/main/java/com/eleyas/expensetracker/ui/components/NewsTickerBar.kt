package com.eleyas.expensetracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.eleyas.expensetracker.ui.theme.AccentGreen
import com.eleyas.expensetracker.ui.theme.Green
import com.eleyas.expensetracker.util.FinancialTip
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

/** টিকার স্ক্রল গতি (পিক্সেল/সেকেন্ড) */
private const val TICKER_SPEED_PX_PER_SEC = 90f

/** প্রতিটি বার্তা স্ক্রল শেষে সংক্ষিপ্ত বিরতি (মিলিসেকেন্ড) */
private const val TICKER_PAUSE_MILLIS = 1800L

/**
 * টিভি নিউজ টিকার মতো ডান থেকে বামে চলমান (marquee) বার।
 * [messages]-এর প্রতিটি বার্তা ডান থেকে বামে পুরোটা ধরে সরে যায় — এরপর পরের বার্তা আসে।
 * পুরো বারটি ট্যাপ করলে [TickerDetailDialog] খোলে।
 *
 * প্রস্থ সঠিকভাবে মাপতে লুকানো Text/লেআউট ট্রিকের বদলে Compose-র TextMeasurer ব্যবহার করা হয়,
 * ফলে লম্বা বাংলা বার্তাও পুরোটা স্ক্রল হয়ে দৃশ্যমান হয় (কেটে যায় না)।
 */
@Composable
fun NewsTickerBar(
    messages: List<String>,
    modifier: Modifier = Modifier,
    onTickerClick: () -> Unit = {},
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    if (messages.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(
        color = contentColor,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .background(backgroundColor)
            .clickable(onClick = onTickerClick)
    ) {
        val viewportWidthPx = constraints.maxWidth

        // বার্তাগুলো ঘুরে ঘুরে আসে (ইনসাইট → টিপস → আবার ইনসাইট …)
        var messageIndex by remember(messages) { mutableIntStateOf(0) }
        val currentIndex = messageIndex % messages.size
        val unitText = remember(messages, currentIndex) {
            messages[currentIndex].trim().ifBlank { "💡 ট্যাপ করে আজকের টিপস ও ইনসাইট দেখুন" } + "     "
        }

        // TextMeasurer দিয়ে সঠিক প্রস্থ — যেকোনো দৈর্ঘ্যের বার্তার জন্য নির্ভরযোগ্য।
        val measuredWidthPx = remember(unitText, textMeasurer, textStyle) {
            runCatching {
                textMeasurer.measure(
                    text = AnnotatedString(unitText),
                    style = textStyle
                ).size.width
            }.getOrDefault(viewportWidthPx)
        }
        val textWidthPx = maxOf(measuredWidthPx, 1)
        val offsetX = remember { Animatable(viewportWidthPx.toFloat()) }

        LaunchedEffect(currentIndex, unitText, textWidthPx, viewportWidthPx) {
            val durationMillis =
                ((textWidthPx + viewportWidthPx) * 1000f / TICKER_SPEED_PX_PER_SEC).toInt()
                    .coerceAtLeast(1500)
            offsetX.snapTo(viewportWidthPx.toFloat())      // ডান প্রান্তের ঠিক বাইরে থেকে শুরু
            offsetX.animateTo(
                targetValue = -textWidthPx.toFloat(),      // পুরো টেক্সট বাম দিকে শুকিয়ে শেষ
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = LinearEasing
                )
            )
            delay(TICKER_PAUSE_MILLIS)                     // সামান্য বিরতি
            messageIndex += 1                              // পরের বার্তা
        }
Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // বাঁ পাশে স্থায়ী লেবেল
            Surface(
                color = contentColor.copy(alpha = 0.16f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(start = 6.dp)
            ) {
                Text(
                    text = "💡 টিপস ও ইনসাইট",
                    color = contentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.width(4.dp))

            // চলমান এলাকা — টেক্সট এখানে ক্লিপ হয়
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clipToBounds(),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                ) {
                    Text(
                        text = unitText,
                        style = textStyle,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // বিস্তারিত দেখার সংকেত
            Text(
                text = "বিস্তারিত ›",
                color = contentColor.copy(alpha = 0.85f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}
/**
 * টিকার বারে ট্যাপ করলে খোলে — ইনসাইট ও আজকের টিপসের সম্পূর্ণ বিস্তারিত।
 */
@Composable
fun TickerDetailDialog(
    insight: String,
    tip: FinancialTip,
    onDismiss: () -> Unit
) {
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface
    val orange = Color(0xFFE65100)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📰", fontSize = 28.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "টিপস ও ফিন্যান্সিয়াল ইনসাইট",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // স্মার্ট ইনসাইট
                Surface(
                    color = Green.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            "💡 আপনার ব্যক্তিগত ইনসাইট",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Green
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            insight,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = onSurface
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // আজকের টিপস
                Surface(
                    color = orange.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            "📜 আজকের টিপস  •  ${tip.category}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = orange
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "\u201C${tip.quote}\u201D",
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            fontStyle = FontStyle.Italic,
                            color = onSurface
                        )
                        if (!tip.author.isNullOrBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "— ${tip.author}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    Text(
                        "ঠিক আছে, বন্ধ করুন",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}