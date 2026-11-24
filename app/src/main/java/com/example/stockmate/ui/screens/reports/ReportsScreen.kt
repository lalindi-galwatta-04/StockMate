
package com.example.stockmate.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.TrendingUp
import android.content.ClipData
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import java.util.Calendar
import java.util.TimeZone
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.example.stockmate.data.local.dao.ProductSalesSummary
import com.example.stockmate.data.local.entity.SaleEntity
import com.example.stockmate.utils.InventoryPdfGenerator
import com.example.stockmate.utils.SalesPdfGenerator
import com.example.stockmate.ui.theme.*
import com.example.stockmate.ui.viewmodel.ReportsViewModel
import com.example.stockmate.ui.viewmodel.InventorySummary
import com.example.stockmate.ui.viewmodel.CategoryStockSummary
import com.example.stockmate.ui.viewmodel.InventoryHealthSummary
import com.example.stockmate.ui.viewmodel.RestockRecommendation
import com.example.stockmate.ui.viewmodel.SalesTrendPoint
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class ReportPeriod(val label: String) {
    ALL_TIME("All Time"),
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    CUSTOM("Custom Range")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen() {

    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val coroutineScope = rememberCoroutineScope()
    var pendingPdfUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var savedPdfUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val savePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { destinationUri ->
        val sourceUri = pendingPdfUri
        if (destinationUri != null && sourceUri != null) {
            coroutineScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(sourceUri).use { input ->
                            requireNotNull(input) { "Unable to read the generated PDF." }
                            context.contentResolver.openOutputStream(destinationUri).use { output ->
                                requireNotNull(output) { "Unable to save the PDF." }
                                input.copyTo(output)
                            }
                        }
                    }
                    savedPdfUri = destinationUri
                    Toast.makeText(context, "PDF saved successfully.", Toast.LENGTH_LONG).show()
                } catch (exception: Exception) {
                    Toast.makeText(context, "Unable to save PDF: ${exception.localizedMessage ?: "Unknown error"}", Toast.LENGTH_LONG).show()
                } finally {
                    pendingPdfUri = null
                }
            }
        } else {
            pendingPdfUri = null
        }
    }

    val reportsViewModel: ReportsViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory
            .getInstance(application)
    )

    val summary by reportsViewModel.summary.collectAsState()
    val products by reportsViewModel.bestSellingProducts.collectAsState()
    val sales by reportsViewModel.completedSales.collectAsState()
    val salesTrend by reportsViewModel.salesTrend.collectAsState()
    val inventorySummary by reportsViewModel.inventorySummary.collectAsState()
    val categoryStock by reportsViewModel.categoryStockDistribution.collectAsState()
    val lowStockProducts by reportsViewModel.lowStockProducts.collectAsState()
    val outOfStockProducts by reportsViewModel.outOfStockProducts.collectAsState()
    val inventoryHealth by reportsViewModel.inventoryHealth.collectAsState()
    val restockRecommendations by reportsViewModel.restockRecommendations.collectAsState()
    val totalRecommendedRestockUnits by reportsViewModel.totalRecommendedRestockUnits.collectAsState()
    val estimatedRestockCost by reportsViewModel.estimatedRestockCost.collectAsState()
    val productsWithCategories by reportsViewModel.productsWithCategories.collectAsState()

    var selectedPeriod by remember {
        mutableStateOf(ReportPeriod.ALL_TIME)
    }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var pendingStartDateMillis by remember { mutableStateOf<Long?>(null) }
    var previousPeriod by remember { mutableStateOf(ReportPeriod.ALL_TIME) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(StockMateBackground),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 38.dp,
            end = 16.dp,
            bottom = 18.dp
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        item {
            ReportsHeader()
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SectionTitle("Reporting Period")

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportPeriod.entries.forEach { period ->

                        FilterChip(
                            selected = selectedPeriod == period,
                            onClick = {
                                if (period == ReportPeriod.CUSTOM) {
                                    previousPeriod = selectedPeriod
                                }
                                selectedPeriod = period

                                when (period) {
                                    ReportPeriod.ALL_TIME ->
                                        reportsViewModel.setAllTime()

                                    ReportPeriod.TODAY ->
                                        reportsViewModel.setToday()

                                    ReportPeriod.THIS_WEEK ->
                                        reportsViewModel.setThisWeek()

                                    ReportPeriod.THIS_MONTH ->
                                        reportsViewModel.setThisMonth()

                                    ReportPeriod.CUSTOM -> {
                                        pendingStartDateMillis = null
                                        showStartDatePicker = true
                                    }
                                }
                            },
                            label = {
                                Text(
                                    text = period.label,
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                }
            }
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionTitle("Business Overview")

                SummaryGrid(
                    revenue = summary.totalRevenue,
                    profit = summary.grossProfit,
                    sales = summary.totalSales,
                    units = summary.unitsSold
                )
            }
        }

        // SALES PERFORMANCE CHART
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionTitle("Sales Performance")

                SalesPerformanceChart(
                    salesTrend = salesTrend
                )
            }
        }

        item {
            SectionTitle("Best-Selling Products")
        }

        if (products.isEmpty()) {
            item {
                EmptyReportCard(
                    message = "No product sales found for this period."
                )
            }
        } else {
            items(
                items = products.take(5),
                key = { it.productId }
            ) { product ->
                BestSellingProductCard(product)
            }
        }

        item {
            SectionTitle("Payment Breakdown")
        }

        item {
            PaymentBreakdownCard(sales)
        }
        // CURRENT INVENTORY ANALYTICS
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Inventory Overview")
                InventoryOverviewCard(inventorySummary)
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Category-wise Stock Distribution")
                CategoryStockCard(categoryStock)
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Stock Alerts")
                StockAlertsCard(
                    lowStockProducts = lowStockProducts,
                    outOfStockProducts = outOfStockProducts
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Inventory Health")
                InventoryHealthCard(inventoryHealth)
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Restock Recommendations")
                RestockRecommendationsCard(
                    recommendations = restockRecommendations,
                    totalUnits = totalRecommendedRestockUnits,
                    estimatedCost = estimatedRestockCost
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Inventory Insights")
                InventoryInsightsCard(
                    summary = inventorySummary,
                    health = inventoryHealth,
                    recommendations = restockRecommendations
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Export Reports")
                PdfReportExportCard(
                    onSaveInventory = {
                        coroutineScope.launch {
                            try {
                                val uri = withContext(Dispatchers.IO) {
                                    InventoryPdfGenerator.generateInventoryReport(context, productsWithCategories)
                                }
                                pendingPdfUri = uri
                                savedPdfUri = null
                                savePdfLauncher.launch("StockMate_Inventory_Report.pdf")
                            } catch (exception: Exception) {
                                Toast.makeText(context, "Unable to generate inventory PDF: ${exception.localizedMessage ?: "Unknown error"}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onSaveSales = {
                        coroutineScope.launch {
                            try {
                                val entries = withContext(Dispatchers.IO) {
                                    reportsViewModel.buildSalesReportEntries(sales)
                                }
                                val uri = withContext(Dispatchers.IO) {
                                    SalesPdfGenerator.generateSalesReport(context, entries, selectedPeriod.label)
                                }
                                pendingPdfUri = uri
                                savedPdfUri = null
                                savePdfLauncher.launch("StockMate_Sales_Report.pdf")
                            } catch (exception: Exception) {
                                Toast.makeText(context, "Unable to generate sales PDF: ${exception.localizedMessage ?: "Unknown error"}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onShareSaved = {
                        savedPdfUri?.let { uri ->
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                clipData = ClipData.newUri(context.contentResolver, "StockMate PDF Report", uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share StockMate Report"))
                        }
                    },
                    hasSavedReport = savedPdfUri != null
                )
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

// ---------------------------------------------------------
// CUSTOM REPORT DATE RANGE PICKERS
// ---------------------------------------------------------

    if (showStartDatePicker) {
        val startPickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = {
                showStartDatePicker = false
                selectedPeriod = previousPeriod
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingStartDateMillis =
                            startPickerState.selectedDateMillis

                        showStartDatePicker = false

                        if (pendingStartDateMillis != null) {
                            showEndDatePicker = true
                        } else {
                            selectedPeriod = previousPeriod
                        }
                    },
                    enabled = startPickerState.selectedDateMillis != null
                ) {
                    Text("Next")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showStartDatePicker = false
                        selectedPeriod = previousPeriod
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            Column {
                Text(
                    text = "Select Start Date",
                    modifier = Modifier.padding(
                        start = 24.dp,
                        top = 20.dp,
                        bottom = 4.dp
                    ),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StockMateCharcoal
                )
                Text(
                    text = "Choose the first day of your report.",
                    modifier = Modifier.padding(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 8.dp
                    ),
                    fontSize = 12.sp,
                    color = StockMateMuted
                )
                DatePicker(
                    state = startPickerState,
                    title = null
                )
            }
        }
    }

    if (showEndDatePicker) {
        val endPickerState = rememberDatePickerState(
            initialSelectedDateMillis = pendingStartDateMillis
        )

        val validEndDate =
            endPickerState.selectedDateMillis != null &&
                    pendingStartDateMillis != null &&
                    endPickerState.selectedDateMillis!! >= pendingStartDateMillis!!

        DatePickerDialog(
            onDismissRequest = {
                showEndDatePicker = false
                selectedPeriod = previousPeriod
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val startMillis = pendingStartDateMillis
                        val endMillis = endPickerState.selectedDateMillis

                        if (startMillis != null && endMillis != null) {
                            val localStart = datePickerMillisToLocalStart(startMillis)
                            val localEnd = datePickerMillisToLocalEnd(endMillis)

                            reportsViewModel.setCustomDateRange(
                                startDate = localStart,
                                endDate = localEnd
                            )

                            showEndDatePicker = false
                        }
                    },
                    enabled = validEndDate
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEndDatePicker = false
                        selectedPeriod = previousPeriod
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            Column {
                Text(
                    text = "Select End Date",
                    modifier = Modifier.padding(
                        start = 24.dp,
                        top = 20.dp,
                        bottom = 4.dp
                    ),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StockMateCharcoal
                )
                Text(
                    text = "Choose the last day. It must be on or after the start date.",
                    modifier = Modifier.padding(
                        start = 24.dp,
                        end = 24.dp,
                        bottom = 8.dp
                    ),
                    fontSize = 12.sp,
                    color = StockMateMuted
                )
                DatePicker(
                    state = endPickerState,
                    title = null
                )
            }
        }
    }

}

// ---------------------------------------------------------
// PDF EXPORT CARD
// ---------------------------------------------------------

@Composable
private fun PdfReportExportCard(
    onSaveInventory: () -> Unit,
    onSaveSales: () -> Unit,
    onShareSaved: () -> Unit,
    hasSavedReport: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StockMateWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Choose a report to download", color = StockMateCharcoal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text("Save the PDF to your device first. You can share it afterwards if you wish.", color = StockMateMuted, fontSize = 12.sp)
            Button(onClick = onSaveInventory, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Inventory2, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Download Inventory Report")
            }
            Button(onClick = onSaveSales, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.BarChart, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Download Sales Report")
            }
            if (hasSavedReport) {
                Text("Your latest report has been saved.", color = StockMateSuccess, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Button(onClick = onShareSaved, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Payments, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Share Downloaded PDF")
                }
            }
        }
    }
}

// ---------------------------------------------------------
// HEADER
// ---------------------------------------------------------

@Composable
private fun ReportsHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = "Reports",
            color = StockMateCharcoal,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Track business performance and insights",
            color = StockMateMuted,
            fontSize = 14.sp
        )
    }
}

// ---------------------------------------------------------
// SECTION TITLE
// ---------------------------------------------------------

@Composable
private fun SectionTitle(title: String) {

    Text(
        text = title,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = StockMateCharcoal
    )
}

// ---------------------------------------------------------
// SUMMARY GRID
// ---------------------------------------------------------

@Composable
private fun SummaryGrid(
    revenue: Long,
    profit: Long,
    sales: Int,
    units: Long
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ReportStatCard(
                title = "Total Revenue",
                value = formatLkr(revenue),
                subtitle = "Completed sales",
                icon = Icons.Outlined.AccountBalanceWallet,
                color = StockMateOrange,
                modifier = Modifier.weight(1f)
            )

            ReportStatCard(
                title = "Gross Profit",
                value = formatLkr(profit),
                subtitle = "Sales profit",
                icon = Icons.Outlined.TrendingUp,
                color = StockMateSuccess,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ReportStatCard(
                title = "Total Sales",
                value = sales.toString(),
                subtitle = "Transactions",
                icon = Icons.Outlined.ShoppingCart,
                color = StockMateOrange,
                modifier = Modifier.weight(1f)
            )

            ReportStatCard(
                title = "Units Sold",
                value = units.toString(),
                subtitle = "Products sold",
                icon = Icons.Outlined.Inventory2,
                color = StockMateWarning,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ReportStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {

            Box(
                modifier = Modifier
                    .size(37.dp)
                    .background(
                        color.copy(alpha = 0.13f),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color
                )
            }

            Text(
                text = value,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = StockMateCharcoal,
                maxLines = 1
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

// ---------------------------------------------------------
// SALES PERFORMANCE CHART
// ---------------------------------------------------------

@Composable
private fun SalesPerformanceChart(
    salesTrend: List<SalesTrendPoint>
) {
    val maxValue = salesTrend.maxOfOrNull {
        maxOf(it.revenue, it.grossProfit)
    }?.coerceAtLeast(1L) ?: 1L

    var selectedPoint by remember(salesTrend) {
        mutableStateOf<SalesTrendPoint?>(null)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = StockMateWhite
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // CHART LEGEND
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(StockMateOrange, CircleShape)
                    )
                    Text(
                        text = "Revenue",
                        fontSize = 11.sp,
                        color = StockMateText
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(StockMateSuccess, CircleShape)
                    )
                    Text(
                        text = "Profit",
                        fontSize = 11.sp,
                        color = StockMateText
                    )
                }
            }

            if (salesTrend.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShowChart,
                        contentDescription = null,
                        tint = StockMateMuted,
                        modifier = Modifier.size(35.dp)
                    )
                    Text(
                        text = "No sales data available for this period.",
                        color = StockMateMuted,
                        fontSize = 12.sp
                    )
                }
            } else {
                // VERTICAL GROUPED BAR CHART.
                // Tap either bar in a pair to reveal the exact values.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    salesTrend.forEach { point ->
                        val isSelected = selectedPoint?.label == point.label

                        Column(
                            modifier = Modifier
                                .width(58.dp)
                                .height(190.dp)
                                .clickable {
                                    selectedPoint = point
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                val revenueHeight =
                                    (point.revenue.toFloat() / maxValue.toFloat())
                                        .coerceIn(0f, 1f) * 140f

                                val profitHeight =
                                    (point.grossProfit.toFloat() / maxValue.toFloat())
                                        .coerceIn(0f, 1f) * 140f

                                // REVENUE BAR
                                Box(
                                    modifier = Modifier
                                        .width(19.dp)
                                        .height(
                                            revenueHeight.dp.coerceAtLeast(
                                                if (point.revenue > 0L) 3.dp else 0.dp
                                            )
                                        )
                                        .background(
                                            if (isSelected) StockMateOrange
                                            else StockMateOrange.copy(alpha = 0.82f),
                                            RoundedCornerShape(
                                                topStart = 5.dp,
                                                topEnd = 5.dp
                                            )
                                        )
                                )

                                Spacer(modifier = Modifier.width(3.dp))

                                // PROFIT BAR
                                Box(
                                    modifier = Modifier
                                        .width(19.dp)
                                        .height(
                                            profitHeight.dp.coerceAtLeast(
                                                if (point.grossProfit > 0L) 3.dp else 0.dp
                                            )
                                        )
                                        .background(
                                            if (isSelected) StockMateSuccess
                                            else StockMateSuccess.copy(alpha = 0.82f),
                                            RoundedCornerShape(
                                                topStart = 5.dp,
                                                topEnd = 5.dp
                                            )
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(7.dp))

                            Text(
                                text = point.label,
                                fontSize = 10.sp,
                                color = if (isSelected) StockMateCharcoal else StockMateMuted,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }

                // SELECTED PERIOD VALUES
                if (selectedPoint != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = StockMateBackground
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(13.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Performance • ${selectedPoint!!.label}",
                                color = StockMateCharcoal,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Revenue",
                                    modifier = Modifier.weight(1f),
                                    color = StockMateMuted,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = formatLkr(selectedPoint!!.revenue),
                                    color = StockMateOrange,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Gross Profit",
                                    modifier = Modifier.weight(1f),
                                    color = StockMateMuted,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = formatLkr(selectedPoint!!.grossProfit),
                                    color = StockMateSuccess,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Tap a bar to view its exact revenue and profit values.",
                        color = StockMateMuted,
                        fontSize = 10.sp
                    )
                }

                Text(
                    text = "Based on completed sales.",
                    color = StockMateMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// ---------------------------------------------------------
// BEST-SELLING PRODUCT CARD
// ---------------------------------------------------------

@Composable
private fun BestSellingProductCard(
    product: ProductSalesSummary
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = StockMateWhite
        ),
        shape = RoundedCornerShape(15.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        StockMateOrange.copy(alpha = 0.13f),
                        RoundedCornerShape(11.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = StockMateOrange
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {

                Text(
                    text = product.productName,
                    color = StockMateCharcoal,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                Text(
                    text = "SKU: ${product.sku}",
                    color = StockMateMuted,
                    fontSize = 10.sp
                )

                Text(
                    text = "${product.unitsSold} units sold",
                    color = StockMateOrange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = formatLkr(product.revenue),
                color = StockMateCharcoal,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

// ---------------------------------------------------------
// PAYMENT BREAKDOWN
// ---------------------------------------------------------

@Composable
private fun PaymentBreakdownCard(
    sales: List<SaleEntity>
) {

    val paymentMethods = listOf(
        SaleEntity.PAYMENT_CASH to "Cash",
        SaleEntity.PAYMENT_CARD to "Card",
        SaleEntity.PAYMENT_BANK_TRANSFER to "Bank Transfer",
        SaleEntity.PAYMENT_OTHER to "Other"
    )

    val totalRevenue = sales.sumOf { it.totalAmount }

    val paymentTotals = paymentMethods.map { (method, label) ->
        label to sales
            .filter { it.paymentMethod == method }
            .sumOf { it.totalAmount }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = StockMateWhite
        ),
        shape = RoundedCornerShape(16.dp)
    ) {

        Column(
            modifier = Modifier.padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Payments,
                    contentDescription = null,
                    tint = StockMateOrange
                )

                Text(
                    text = "Revenue by Payment Method",
                    modifier = Modifier.padding(start = 9.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StockMateCharcoal
                )
            }

            paymentTotals.forEach { (label, amount) ->

                val percentage = if (totalRevenue > 0L) {
                    (amount.toDouble() / totalRevenue.toDouble())
                        .toFloat()
                        .coerceIn(0f, 1f)
                } else {
                    0f
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.weight(1f),
                            color = StockMateText,
                            fontSize = 12.sp
                        )

                        Text(
                            text = formatLkr(amount),
                            color = StockMateCharcoal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(
                                StockMateBackground,
                                CircleShape
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(percentage)
                                .height(8.dp)
                                .background(
                                    StockMateOrange,
                                    CircleShape
                                )
                        )
                    }
                }
            }

            if (sales.isEmpty()) {
                Text(
                    text = "Payment information will appear after your first completed sale.",
                    color = StockMateMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ---------------------------------------------------------
// INVENTORY OVERVIEW
// ---------------------------------------------------------

@Composable
private fun InventoryOverviewCard(summary: InventorySummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StockMateWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Total Inventory Value", color = StockMateMuted, fontSize = 12.sp)
                Text(
                    formatLkr(summary.inventoryValue),
                    color = StockMateCharcoal,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Based on current stock at purchase cost", color = StockMateMuted, fontSize = 10.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                InventoryMetric("Active Products", summary.activeProducts.toString(), StockMateOrange, Modifier.weight(1f))
                InventoryMetric("Total Units", summary.totalUnits.toString(), StockMateCharcoal, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                InventoryMetric("Low Stock", summary.lowStockCount.toString(), StockMateWarning, Modifier.weight(1f))
                InventoryMetric("Out of Stock", summary.outOfStockCount.toString(), StockMateDanger, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun InventoryMetric(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(accent.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(value, color = accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = StockMateMuted, fontSize = 10.sp)
    }
}

// ---------------------------------------------------------
// CATEGORY-WISE STOCK DISTRIBUTION
// ---------------------------------------------------------

@Composable
private fun CategoryStockCard(categories: List<CategoryStockSummary>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StockMateWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        if (categories.isEmpty()) {
            Text(
                "Add products to see category-wise stock distribution.",
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                color = StockMateMuted,
                fontSize = 12.sp
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                categories.forEach { category ->
                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                category.categoryName,
                                modifier = Modifier.weight(1f),
                                color = StockMateCharcoal,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "${kotlin.math.round(category.percentage * 100).toInt()}%",
                                color = StockMateOrange,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth().height(8.dp)
                                .background(StockMateBackground, CircleShape)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(category.percentage)
                                    .height(8.dp)
                                    .background(StockMateOrange, CircleShape)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${category.productCount} ${if (category.productCount == 1) "product" else "products"} • ${category.unitsInStock} units",
                                modifier = Modifier.weight(1f),
                                color = StockMateMuted,
                                fontSize = 10.sp
                            )
                            Text(
                                formatLkr(category.inventoryValue),
                                color = StockMateCharcoal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Text(
                    "Percentages represent each category's share of total inventory value.",
                    color = StockMateMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// ---------------------------------------------------------
// STOCK ALERTS
// ---------------------------------------------------------

@Composable
private fun StockAlertsCard(
    lowStockProducts: List<com.example.stockmate.data.local.entity.ProductEntity>,
    outOfStockProducts: List<com.example.stockmate.data.local.entity.ProductEntity>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StockMateWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        val hasAlerts = lowStockProducts.isNotEmpty() || outOfStockProducts.isNotEmpty()
        if (!hasAlerts) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = StockMateSuccess)
                Text("All products are sufficiently stocked.", color = StockMateMuted, fontSize = 12.sp)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                outOfStockProducts.forEach { product ->
                    StockAlertRow(
                        productName = product.productName,
                        detail = "Out of stock",
                        quantity = "0 units",
                        alertColor = StockMateDanger
                    )
                }
                lowStockProducts.forEach { product ->
                    StockAlertRow(
                        productName = product.productName,
                        detail = "Low stock",
                        quantity = "${product.quantityInStock} / ${product.minimumStockLevel} units",
                        alertColor = StockMateWarning
                    )
                }
            }
        }
    }
}

@Composable
private fun StockAlertRow(
    productName: String,
    detail: String,
    quantity: String,
    alertColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.size(8.dp).background(alertColor, CircleShape)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(productName, color = StockMateCharcoal, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(detail, color = alertColor, fontSize = 10.sp)
        }
        Text(quantity, color = StockMateMuted, fontSize = 10.sp)
    }
}

// ---------------------------------------------------------
// INVENTORY HEALTH
// ---------------------------------------------------------

@Composable
private fun InventoryHealthCard(health: InventoryHealthSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StockMateWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Healthy stock", modifier = Modifier.weight(1f), color = StockMateCharcoal, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text("${kotlin.math.round(health.healthPercentage * 100).toInt()}%", color = StockMateSuccess, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier.fillMaxWidth().height(10.dp)
                    .background(StockMateBackground, CircleShape)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(health.healthPercentage)
                        .height(10.dp).background(StockMateSuccess, CircleShape)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InventoryMetric("Healthy", health.healthyProducts.toString(), StockMateSuccess, Modifier.weight(1f))
                InventoryMetric("Low Stock", health.lowStockProducts.toString(), StockMateWarning, Modifier.weight(1f))
                InventoryMetric("Out of Stock", health.outOfStockProducts.toString(), StockMateDanger, Modifier.weight(1f))
            }
            Text(
                "Health percentage represents active products that are above their minimum stock level.",
                color = StockMateMuted,
                fontSize = 10.sp
            )
        }
    }
}

// ---------------------------------------------------------
// RESTOCK RECOMMENDATIONS
// ---------------------------------------------------------

@Composable
private fun RestockRecommendationsCard(
    recommendations: List<RestockRecommendation>,
    totalUnits: Int,
    estimatedCost: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StockMateWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        if (recommendations.isEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = StockMateSuccess)
                Text("No restocking is currently recommended.", color = StockMateMuted, fontSize = 12.sp)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Suggested quantities bring each product back to its minimum stock level.",
                    color = StockMateMuted,
                    fontSize = 10.sp
                )
                recommendations.forEach { recommendation ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(recommendation.productName, color = StockMateCharcoal, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Current: ${recommendation.currentStock} • Minimum: ${recommendation.minimumStockLevel}",
                                color = StockMateMuted,
                                fontSize = 10.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("+${recommendation.recommendedQuantity} units", color = StockMateOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(formatLkr(recommendation.estimatedCost), color = StockMateMuted, fontSize = 10.sp)
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(StockMateBackground))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Total suggested units", color = StockMateMuted, fontSize = 10.sp)
                        Text(totalUnits.toString(), color = StockMateCharcoal, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Estimated restock cost", color = StockMateMuted, fontSize = 10.sp)
                        Text(formatLkr(estimatedCost), color = StockMateCharcoal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// INVENTORY INSIGHTS
// ---------------------------------------------------------

@Composable
private fun InventoryInsightsCard(
    summary: InventorySummary,
    health: InventoryHealthSummary,
    recommendations: List<RestockRecommendation>
) {
    val messages = buildList {
        if (summary.activeProducts == 0) {
            add("Add active products to start tracking inventory health.")
        } else {
            add("${health.healthyProducts} of ${summary.activeProducts} active products are above their minimum stock levels.")
            if (health.outOfStockProducts > 0) {
                add("${health.outOfStockProducts} product(s) are out of stock and may need urgent replenishment.")
            }
            if (health.lowStockProducts > 0) {
                add("${health.lowStockProducts} product(s) are at or below their minimum stock level.")
            }
            if (recommendations.isEmpty()) {
                add("Current stock levels meet all configured minimums.")
            }
        }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = StockMateWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            messages.forEach { message ->
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Box(modifier = Modifier.padding(top = 4.dp).size(7.dp).background(StockMateOrange, CircleShape))
                    Text(message, color = StockMateText, fontSize = 11.sp)
                }
            }
        }
    }
}

// ---------------------------------------------------------
// EMPTY STATE
// ---------------------------------------------------------

@Composable
private fun EmptyReportCard(
    message: String
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = StockMateWhite
        ),
        shape = RoundedCornerShape(15.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(25.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {

            Icon(
                imageVector = Icons.Outlined.BarChart,
                contentDescription = null,
                tint = StockMateMuted,
                modifier = Modifier.size(35.dp)
            )

            Text(
                text = message,
                color = StockMateMuted,
                fontSize = 12.sp
            )
        }
    }
}

// ---------------------------------------------------------
// DATE PICKER DATE CONVERSION
// Material 3 DatePicker returns UTC-midnight timestamps.
// Convert the selected calendar date to local day boundaries
// before querying Room using the app's local timestamps.
// ---------------------------------------------------------

private fun datePickerMillisToLocalStart(
    dateMillis: Long
): Long {
    val utcCalendar = Calendar.getInstance(
        TimeZone.getTimeZone("UTC")
    ).apply {
        timeInMillis = dateMillis
    }

    return Calendar.getInstance().apply {
        set(
            utcCalendar.get(Calendar.YEAR),
            utcCalendar.get(Calendar.MONTH),
            utcCalendar.get(Calendar.DAY_OF_MONTH),
            0,
            0,
            0
        )
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun datePickerMillisToLocalEnd(
    dateMillis: Long
): Long {
    val startOfDay = datePickerMillisToLocalStart(dateMillis)

    return Calendar.getInstance().apply {
        timeInMillis = startOfDay
        add(Calendar.DAY_OF_MONTH, 1)
        add(Calendar.MILLISECOND, -1)
    }.timeInMillis
}

// ---------------------------------------------------------
// CURRENCY FORMATTER
// ---------------------------------------------------------

private fun formatLkr(amountInCents: Long): String {

    val amount = amountInCents / 100.0

    val formatter = NumberFormat.getNumberInstance(
        Locale("en", "LK")
    )

    formatter.maximumFractionDigits = 2
    formatter.minimumFractionDigits = 2

    return "LKR ${formatter.format(amount)}"
}
