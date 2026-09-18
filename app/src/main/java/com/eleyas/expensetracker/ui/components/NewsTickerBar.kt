package com.eleyas.expensetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.eleyas.expensetracker.ui.theme.AccentGreen
import com.eleyas.expensetracker.ui.theme.Green
import com.eleyas.expensetracker.util.FinancialTip
import kotlinx.coroutines.delay

private const val TICKER_MESSAGE_DISPLAY_MILLIS = 5000L

@Composable
fun NewsTickerBar(
    messages: List<String>,
    modifier: Modifier = Modifier,
    onTickerClick: () -> Unit = {},
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    if (messages.isEmpty()) return

    val textStyle = TextStyle(
        color = contentColor,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
    )

    var messageIndex by remember(messages) { mutableIntStateOf(0) }
    val currentIndex = messageIndex % messages.size
    val currentMessage = messages[currentIndex].trim()
        .ifBlank { "আজকের টিপস ও ইনসাইট" }

    LaunchedEffect(currentIndex, messages) {
        if (messages.size > 1) {
            delay(TICKER_MESSAGE_DISPLAY_MILLIS)
            messageIndex = (messageIndex + 1) % messages.size
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(backgroundColor)
            .clickable(onClick = onTickerClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "💡 টিপস ও ইনসাইট",
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )

        Spacer(Modifier.width(10.dp))

        Text(
            text = currentMessage,
            style = textStyle,
            modifier = Modifier
                .weight(1f)
                .clipToBounds(),
            maxLines = 1,
            softWrap = false,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

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
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 12.dp
            )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(20.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "📰",
                        fontSize = 28.sp
                    )

                    Spacer(
                        modifier = Modifier.width(10.dp)
                    )

                    Text(
                        text = "টিপস ও ফিন্যান্সিয়াল ইনসাইট",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onDismiss
                    ) {

                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "বন্ধ করুন",
                            tint = MaterialTheme
                                .colorScheme
                                .onSurfaceVariant
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Surface(
                    color = Green.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {

                        Text(
                            text = "💡 আপনার ব্যক্তিগত ইনসাইট",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Green
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            text = insight,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = onSurface
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(14.dp)
                )

                Surface(
                    color = orange.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {

                        Text(
                            text = "📜 আজকের টিপস  •  ${tip.category}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = orange
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            text = "\"${tip.quote}\"",
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            fontStyle = FontStyle.Italic,
                            color = onSurface
                        )

                        if (!tip.author.isNullOrBlank()) {

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text = "— ${tip.author}",
                                fontSize = 12.sp,
                                color = MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(
                    modifier = Modifier.height(18.dp)
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGreen
                    )
                ) {

                    Text(
                        text = "ঠিক আছে, বন্ধ করুন",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}