package com.eleyas.expensetracker

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight

private val AccentGreen = Color(0xFF00E676)
private val BarBackground = Color(0xFF181B21)
private val UnselectedGray = Color(0xFF9E9E9E)
private val FloatingBlack = Color(0xFF101217)

private class MagicNavigationShape(
    private val selectedCenter: Float
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {

        val path = Path()

        val center = selectedCenter

        val notchWidth = with(density) { 34.dp.toPx() }
        val notchDepth = with(density) { 28.dp.toPx() }
        val cornerRadius = with(density) { 24.dp.toPx() }
        val curve = with(density) { 12.dp.toPx() }

        path.moveTo(cornerRadius, 0f)
        path.lineTo(center - notchWidth, 0f)

        // LEFT SIDE OF ROUND NOTCH
        path.cubicTo(
            center - notchWidth + curve, 0f,
            center - notchWidth + curve, notchDepth,
            center, notchDepth
        )

        // RIGHT SIDE OF ROUND NOTCH
        path.cubicTo(
            center + notchWidth - curve, notchDepth,
            center + notchWidth - curve, 0f,
            center + notchWidth, 0f
        )

        path.lineTo(size.width - cornerRadius, 0f)
        path.quadraticBezierTo(size.width, 0f, size.width, cornerRadius)
        path.lineTo(size.width, size.height - cornerRadius)
        path.quadraticBezierTo(size.width, size.height, size.width - cornerRadius, size.height)
        path.lineTo(cornerRadius, size.height)
        path.quadraticBezierTo(0f, size.height, 0f, size.height - cornerRadius)
        path.lineTo(0f, cornerRadius)
        path.quadraticBezierTo(0f, 0f, cornerRadius, 0f)
        path.close()

        return Outline.Generic(path)
    }
}

@Composable
fun AnimatedBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("হোম", "আয়", "খরচ", "রিপোর্ট", "ঋণ", "সেটিংস")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.AccountBalanceWallet,
        Icons.Default.ShoppingCart,
        Icons.Default.BarChart,
        Icons.Default.CreditCard,
        Icons.Default.Settings
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(85.dp)
    ) {
        val horizontalPadding = 16.dp
        val barWidth = maxWidth - horizontalPadding * 2
        val itemWidth = barWidth / tabs.size
        val selectedCenter = itemWidth * selectedTab + itemWidth / 2

        val animatedCenter by animateDpAsState(
            targetValue = selectedCenter,
            animationSpec = tween(450),
            label = "magicNavigation"
        )

        // MAIN NAVIGATION BAR
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
                .height(64.dp)
                .align(Alignment.BottomCenter)
                .shadow(12.dp, shape = MagicNavigationShape(
                    selectedCenter = with(LocalDensity.current) { animatedCenter.toPx() }
                ), ambientColor = Color.Black, spotColor = AccentGreen.copy(alpha = 0.5f))
                .clip(
                    MagicNavigationShape(
                        selectedCenter = with(LocalDensity.current) { animatedCenter.toPx() }
                    )
                )
                .background(BarBackground)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, title ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onTabSelected(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedTab != index) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icons[index],
                                    contentDescription = title,
                                    tint = UnselectedGray,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = title,
                                    fontSize = 9.sp,
                                    color = UnselectedGray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentGreen,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // FLOATING CIRCLE (ACTIVE TAB)
        Box(
            modifier = Modifier
                .offset(
                    x = horizontalPadding + animatedCenter - 25.dp,
                    y = (-5).dp
                )
                .size(50.dp)
                .clip(CircleShape)
                .background(FloatingBlack)
                .padding(4.dp)
                .clip(CircleShape)
                .background(AccentGreen)
                .shadow(8.dp, CircleShape, spotColor = AccentGreen)
                .clickable { onTabSelected(selectedTab) },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icons[selectedTab],
                contentDescription = tabs[selectedTab],
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
