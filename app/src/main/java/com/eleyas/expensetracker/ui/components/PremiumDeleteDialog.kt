package com.eleyas.expensetracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun PremiumDeleteDialog(
    title: String = "তথ্য মুছে ফেলবেন?",
    message: String,
    confirmText: String = "স্থায়ীভাবে মুছুন",
    dismissText: String = "বাতিল",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme

    var visible by remember {
        mutableStateOf(false)
    }

    val iconScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.65f,
        animationSpec = tween(
            durationMillis = 320,
            easing = FastOutSlowInEasing
        ),
        label = "deleteIconScale"
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {

        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(
                animationSpec = tween(
                    260,
                    easing = FastOutSlowInEasing
                ),
                initialScale = 0.86f
            )
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(
                    containerColor = scheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 20.dp
                )
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 22.dp,
                            end = 22.dp,
                            top = 24.dp,
                            bottom = 20.dp
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Delete Icon
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .scale(iconScale)
                            .background(
                                scheme.errorContainer,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector =
                                Icons.Default.DeleteForever,
                            contentDescription = "Delete",
                            modifier = Modifier.size(38.dp),
                            tint = scheme.error
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(18.dp)
                    )

                    Text(
                        text = title,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = scheme.onSurface
                    )

                    Spacer(
                        modifier = Modifier.height(9.dp)
                    )

                    Text(
                        text = message,
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = scheme.onSurfaceVariant
                    )

                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )

                    // Warning section
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(17.dp),
                        color = scheme.errorContainer.copy(
                            alpha = 0.45f
                        )
                    ) {

                        Row(
                            modifier = Modifier.padding(13.dp),
                            verticalAlignment =
                                Alignment.Top
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = scheme.error
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(9.dp)
                            )

                            Text(
                                text =
                                    "এই কাজটি পূর্বাবস্থায় ফিরিয়ে আনা যাবে না।",
                                modifier =
                                    Modifier.weight(1f),
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                fontWeight =
                                    FontWeight.SemiBold,
                                color = scheme.onSurface
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.spacedBy(10.dp)
                    ) {

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {

                            Text(
                                text = dismissText,
                                fontWeight =
                                    FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = onConfirm,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = scheme.error,
                                contentColor = scheme.onError
                            )
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.DeleteForever,
                                contentDescription = null,
                                modifier = Modifier.size(19.dp)
                            )

                            Spacer(
                                modifier =
                                    Modifier.width(6.dp)
                            )

                            Text(
                                text = confirmText,
                                fontWeight =
                                    FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}