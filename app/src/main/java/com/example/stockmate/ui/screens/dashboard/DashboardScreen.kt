
package com.example.stockmate.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stockmate.ui.theme.*
import com.example.stockmate.ui.viewmodel.DashboardActivity
import com.example.stockmate.ui.viewmodel.DashboardViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val summary by dashboardViewModel.summary.collectAsState()
    val recentActivities by dashboardViewModel.recentActivities.collectAsState()

    Scaffold(
        containerColor = StockMateBackground
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),

            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // Header
            item {
                DashboardHeader()
            }

            // Welcome
            item {
                Column {
                    Text(
                        text = "Good day!",
                        style = MaterialTheme.typography.headlineLarge,
                        color = StockMateCharcoal
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "Here's your business overview.",
                        color = StockMateMuted,
                        fontSize = 13.sp
                    )
                }
            }

            // Statistics heading
            item {
                SectionHeading("Business Overview")
            }

            // Products and Low Stock
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    StatCard(
                        title = "Products",
                        value = summary.productCount.toString(),
                        subtitle = "Registered",
                        icon = Icons.Outlined.Inventory2,
                        iconColor = StockMateOrange,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Low Stock",
                        value = summary.lowStockCount.toString(),
                        subtitle = "Need attention",
                        icon = Icons.Outlined.WarningAmber,
                        iconColor = StockMateWarning,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Sales and Gross Profit
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    StatCard(
                        title = "Sales Today",
                        value = formatLkr(summary.salesToday),
                        subtitle = "Total revenue",
                        icon = Icons.Outlined.TrendingUp,
                        iconColor = StockMateSuccess,
                        modifier = Modifier.weight(1f)
                    )

                    StatCard(
                        title = "Gross Profit",
                        value = formatLkr(summary.grossProfitToday),
                        subtitle = "Today's profit",
                        icon = Icons.Outlined.AccountBalanceWallet,
                        iconColor = StockMateOrange,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Stock status
            item {
                SectionHeading("Inventory Status")
            }

            item {
                StockStatusCard(
                    inStock = summary.inStockCount,
                    lowStock = summary.lowStockCount,
                    outOfStock = summary.outOfStockCount
                )
            }

            // Recent activity
            item {
                SectionHeading("Recent Stock Activity")
            }

            if (recentActivities.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = StockMateWhite
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "No stock activity recorded yet.",
                            modifier = Modifier.padding(18.dp),
                            color = StockMateMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(recentActivities) { activity ->
                    ActivityCard(activity)
                }
            }

            item {
                Text(
                    text = "StockMate • Manage smarter. Stock better.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    color = StockMateMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ----------------------------------------------------
// Currency Formatting
// ----------------------------------------------------

private fun formatLkr(amountInCents: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "LK"))
    formatter.maximumFractionDigits = 2
    formatter.minimumFractionDigits = 2

    return "Rs. ${formatter.format(amountInCents / 100.0)}"
}

// ----------------------------------------------------
// Dashboard Header
// ----------------------------------------------------

@Composable
private fun DashboardHeader() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = StockMateCharcoal,
                shape = RoundedCornerShape(18.dp)
            )
            .padding(16.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(45.dp)
                .background(
                    StockMateOrange,
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Inventory2,
                contentDescription = "StockMate logo",
                tint = StockMateCharcoal,
                modifier = Modifier.size(27.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {

            Text(
                text = "StockMate",
                color = StockMateWhite,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "INVENTORY MANAGEMENT",
                color = StockMateLightText,
                fontSize = 9.sp,
                letterSpacing = 1.2.sp
            )
        }

        Icon(
            imageVector = Icons.Outlined.NotificationsNone,
            contentDescription = "Notifications",
            tint = StockMateWhite,
            modifier = Modifier.size(25.dp)
        )
    }
}

// ----------------------------------------------------
// Section Heading
// ----------------------------------------------------

@Composable
private fun SectionHeading(title: String) {

    Text(
        text = title,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = StockMateCharcoal
    )
}

// ----------------------------------------------------
// Statistics Card
// ----------------------------------------------------

@Composable
private fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = StockMateWhite
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {

            Box(
                modifier = Modifier
                    .size(37.dp)
                    .background(
                        iconColor.copy(alpha = 0.13f),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor
                )
            }

            Text(
                text = value,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = StockMateCharcoal
            )

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = StockMateCharcoal
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = StockMateMuted
            )
        }
    }
}

// ----------------------------------------------------
// Inventory Status Card
// ----------------------------------------------------

@Composable
private fun StockStatusCard(
    inStock: Int,
    lowStock: Int,
    outOfStock: Int
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = StockMateWhite
        ),
        shape = RoundedCornerShape(16.dp)
    ) {

        Column(
            modifier = Modifier.padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {

            StockStatusRow(
                title = "In Stock",
                quantity = inStock.toString(),
                color = StockMateSuccess
            )

            StockStatusRow(
                title = "Low Stock",
                quantity = lowStock.toString(),
                color = StockMateWarning
            )

            StockStatusRow(
                title = "Out of Stock",
                quantity = outOfStock.toString(),
                color = StockMateDanger
            )
        }
    }
}

// ----------------------------------------------------
// Inventory Status Row
// ----------------------------------------------------

@Composable
private fun StockStatusRow(
    title: String,
    quantity: String,
    color: Color
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(9.dp)
                .background(color, CircleShape)
        )

        Text(
            text = title,
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp),
            color = StockMateText,
            fontSize = 13.sp
        )

        Text(
            text = quantity,
            color = StockMateCharcoal,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

// ----------------------------------------------------
// Recent Stock Activity Card
// ----------------------------------------------------

@Composable
private fun ActivityCard(
    activity: DashboardActivity
) {
    val activityColor = if (activity.isAddition) {
        StockMateSuccess
    } else {
        StockMateOrange
    }

    val activityIcon = if (activity.isAddition) {
        Icons.Outlined.ArrowDownward
    } else {
        Icons.Outlined.ArrowUpward
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = StockMateWhite
        ),
        shape = RoundedCornerShape(14.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        activityColor.copy(alpha = 0.12f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = activityIcon,
                    contentDescription = null,
                    tint = activityColor
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {

                Text(
                    text = activity.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = StockMateCharcoal
                )

                Text(
                    text = activity.product,
                    fontSize = 11.sp,
                    color = StockMateMuted
                )

                Text(
                    text = activity.date,
                    fontSize = 10.sp,
                    color = StockMateMuted
                )
            }

            Text(
                text = activity.quantity,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = activityColor
            )
        }
    }
}
