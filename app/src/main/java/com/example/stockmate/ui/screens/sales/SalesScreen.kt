
package com.example.stockmate.ui.screens.sales

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stockmate.data.local.entity.SaleEntity
import com.example.stockmate.ui.theme.StockMateBackground
import com.example.stockmate.ui.theme.StockMateCharcoal
import com.example.stockmate.ui.theme.StockMateMuted
import com.example.stockmate.ui.theme.StockMateOrange
import com.example.stockmate.ui.viewmodel.SalesViewModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val FILTER_ALL = ""
private const val FILTER_CASH = "CASH"
private const val FILTER_CARD = "CARD"
private const val FILTER_BANK = "BANK_TRANSFER"
private const val FILTER_OTHER = "OTHER"

private const val DATE_ALL = "All Time"
private const val DATE_TODAY = "Today"
private const val DATE_WEEK = "This Week"
private const val DATE_MONTH = "This Month"
private const val DATE_CUSTOM = "Custom Range"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    onNewSale: () -> Unit,
    onSaleClick: (Long) -> Unit,
    salesViewModel: SalesViewModel = viewModel()
) {
    val filteredSales by salesViewModel.filteredSales.collectAsState()
    val saleCount by salesViewModel.saleCount.collectAsState()
    val totalRevenue by salesViewModel.totalRevenue.collectAsState()
    val totalGrossProfit by salesViewModel.totalGrossProfit.collectAsState()

    val searchQuery by salesViewModel.searchQuery.collectAsState()
    val selectedPaymentMethod by
    salesViewModel.selectedPaymentMethod.collectAsState()

    var selectedDateFilter by remember {
        mutableStateOf(DATE_ALL)
    }

    var showStartDatePicker by remember {
        mutableStateOf(false)
    }

    var showEndDatePicker by remember {
        mutableStateOf(false)
    }

    var customStartDate by remember {
        mutableStateOf<LocalDate?>(null)
    }

    var customRangeLabel by remember {
        mutableStateOf("")
    }

    var dateRangeError by remember {
        mutableStateOf(false)
    }

    Scaffold(
        containerColor = StockMateBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Sales",
                            fontWeight = FontWeight.Bold,
                            color = StockMateCharcoal
                        )

                        Text(
                            text = "Manage transactions and track revenue",
                            style = MaterialTheme.typography.bodySmall,
                            color = StockMateMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StockMateBackground
                )
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // SALES SUMMARY

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    SalesSummaryCard(
                        title = "Total Sales",
                        value = saleCount.toString(),
                        modifier = Modifier.weight(1f),
                        valueColor = StockMateCharcoal
                    )

                    SalesSummaryCard(
                        title = "Revenue",
                        value = formatCompactLkr(totalRevenue),
                        modifier = Modifier.weight(1f),
                        valueColor = StockMateCharcoal
                    )

                    SalesSummaryCard(
                        title = "Gross Profit",
                        value = formatCompactLkr(totalGrossProfit),
                        modifier = Modifier.weight(1f),
                        valueColor = StockMateOrange
                    )
                }
            }

            // CREATE SALE BUTTON

            item {
                Button(
                    onClick = onNewSale,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StockMateOrange,
                        contentColor = StockMateCharcoal
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )

                    Spacer(Modifier.padding(horizontal = 4.dp))

                    Text(
                        text = "Create New Sale",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // SEARCH SALES

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Search Sales",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = StockMateCharcoal
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            salesViewModel.updateSearchQuery(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("Search by sale ID or customer")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StockMateOrange,
                            cursorColor = StockMateOrange,
                            focusedLeadingIconColor = StockMateOrange
                        )
                    )
                }
            }

            // PAYMENT METHOD FILTER

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Payment Method",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = StockMateCharcoal
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PaymentFilterChip(
                            label = "All",
                            value = FILTER_ALL,
                            selectedValue = selectedPaymentMethod,
                            onSelect = salesViewModel::updatePaymentMethod
                        )

                        PaymentFilterChip(
                            label = "Cash",
                            value = FILTER_CASH,
                            selectedValue = selectedPaymentMethod,
                            onSelect = salesViewModel::updatePaymentMethod
                        )

                        PaymentFilterChip(
                            label = "Card",
                            value = FILTER_CARD,
                            selectedValue = selectedPaymentMethod,
                            onSelect = salesViewModel::updatePaymentMethod
                        )

                        PaymentFilterChip(
                            label = "Bank Transfer",
                            value = FILTER_BANK,
                            selectedValue = selectedPaymentMethod,
                            onSelect = salesViewModel::updatePaymentMethod
                        )

                        PaymentFilterChip(
                            label = "Other",
                            value = FILTER_OTHER,
                            selectedValue = selectedPaymentMethod,
                            onSelect = salesViewModel::updatePaymentMethod
                        )
                    }
                }
            }

            // DATE RANGE FILTER

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Date Range",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = StockMateCharcoal
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DateFilterChip(
                            label = DATE_ALL,
                            selected = selectedDateFilter == DATE_ALL,
                            onClick = {
                                selectedDateFilter = DATE_ALL
                                customRangeLabel = ""

                                salesViewModel.updateDateRange(
                                    0L,
                                    Long.MAX_VALUE
                                )
                            }
                        )

                        DateFilterChip(
                            label = DATE_TODAY,
                            selected = selectedDateFilter == DATE_TODAY,
                            onClick = {
                                selectedDateFilter = DATE_TODAY
                                customRangeLabel = ""

                                val range = getDateRange(DATE_TODAY)

                                salesViewModel.updateDateRange(
                                    range.first,
                                    range.second
                                )
                            }
                        )

                        DateFilterChip(
                            label = DATE_WEEK,
                            selected = selectedDateFilter == DATE_WEEK,
                            onClick = {
                                selectedDateFilter = DATE_WEEK
                                customRangeLabel = ""

                                val range = getDateRange(DATE_WEEK)

                                salesViewModel.updateDateRange(
                                    range.first,
                                    range.second
                                )
                            }
                        )

                        DateFilterChip(
                            label = DATE_MONTH,
                            selected = selectedDateFilter == DATE_MONTH,
                            onClick = {
                                selectedDateFilter = DATE_MONTH
                                customRangeLabel = ""

                                val range = getDateRange(DATE_MONTH)

                                salesViewModel.updateDateRange(
                                    range.first,
                                    range.second
                                )
                            }
                        )

                        DateFilterChip(
                            label = DATE_CUSTOM,
                            selected = selectedDateFilter == DATE_CUSTOM,
                            onClick = {
                                dateRangeError = false
                                showStartDatePicker = true
                            }
                        )
                    }

                    if (
                        selectedDateFilter == DATE_CUSTOM &&
                        customRangeLabel.isNotBlank()
                    ) {
                        Text(
                            text = "Selected: $customRangeLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = StockMateOrange,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // SALES HISTORY HEADER

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sales History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = StockMateCharcoal
                    )

                    Text(
                        text = "${filteredSales.size} records",
                        style = MaterialTheme.typography.bodySmall,
                        color = StockMateMuted
                    )
                }
            }

            // SALES HISTORY LIST

            if (filteredSales.isEmpty()) {
                item {
                    EmptySalesCard(
                        hasFilters = searchQuery.isNotBlank() ||
                                selectedPaymentMethod.isNotBlank() ||
                                selectedDateFilter != DATE_ALL
                    )
                }
            } else {
                items(
                    items = filteredSales,
                    key = { it.saleId }
                ) { sale ->
                    SaleHistoryCard(
                        sale = sale,
                        onClick = {
                            onSaleClick(sale.saleId)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // START DATE PICKER


    if (showStartDatePicker) {
        val startPickerState = rememberDatePickerState(
            initialSelectedDateMillis = customStartDate
                ?.atStartOfDay(ZoneOffset.UTC)
                ?.toInstant()
                ?.toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = {
                showStartDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis =
                            startPickerState.selectedDateMillis

                        if (selectedMillis != null) {
                            customStartDate = Instant
                                .ofEpochMilli(selectedMillis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()

                            showStartDatePicker = false
                            showEndDatePicker = true
                            dateRangeError = false
                        }
                    }
                ) {
                    Text("Next", color = StockMateOrange)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showStartDatePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Select Start Date",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = StockMateCharcoal,
                    modifier = Modifier.padding(
                        top = 12.dp,
                        bottom = 8.dp
                    )
                )

                Text(
                    text = "Choose the first day of your sales report.",
                    style = MaterialTheme.typography.bodySmall,
                    color = StockMateMuted,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                DatePicker(
                    state = startPickerState,
                    title = {}
                )
            }
        }
    }


    // END DATE PICKER


    if (showEndDatePicker) {
        val endPickerState = rememberDatePickerState(
            initialSelectedDateMillis = customStartDate
                ?.atStartOfDay(ZoneOffset.UTC)
                ?.toInstant()
                ?.toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = {
                showEndDatePicker = false
                dateRangeError = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val endMillis =
                            endPickerState.selectedDateMillis

                        val startDate = customStartDate

                        if (endMillis != null && startDate != null) {
                            val endDate = Instant
                                .ofEpochMilli(endMillis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()

                            if (!endDate.isBefore(startDate)) {
                                val zone = ZoneId.systemDefault()

                                val startMillis = startDate
                                    .atStartOfDay(zone)
                                    .toInstant()
                                    .toEpochMilli()

                                val endOfDayMillis = endDate
                                    .plusDays(1)
                                    .atStartOfDay(zone)
                                    .toInstant()
                                    .toEpochMilli() - 1L

                                salesViewModel.updateDateRange(
                                    startMillis,
                                    endOfDayMillis
                                )

                                selectedDateFilter = DATE_CUSTOM

                                val formatter =
                                    DateTimeFormatter.ofPattern(
                                        "dd MMM yyyy",
                                        Locale.getDefault()
                                    )

                                customRangeLabel =
                                    "${startDate.format(formatter)} – " +
                                            endDate.format(formatter)

                                dateRangeError = false
                                showEndDatePicker = false
                            } else {
                                dateRangeError = true
                            }
                        }
                    }
                ) {
                    Text("Apply", color = StockMateOrange)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEndDatePicker = false
                        dateRangeError = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Select End Date",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = StockMateCharcoal,
                    modifier = Modifier.padding(
                        top = 12.dp,
                        bottom = 8.dp
                    )
                )

                Text(
                    text = "Choose the last day of your sales report.",
                    style = MaterialTheme.typography.bodySmall,
                    color = StockMateMuted,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                DatePicker(
                    state = endPickerState,
                    title = {}
                )

                if (dateRangeError) {
                    Text(
                        text = "End date cannot be before start date.",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(
                            vertical = 8.dp
                        )
                    )
                }
            }
        }
    }

}

// PAYMENT FILTER CHIP

@Composable
private fun PaymentFilterChip(
    label: String,
    value: String,
    selectedValue: String,
    onSelect: (String) -> Unit
) {
    FilterChip(
        selected = selectedValue == value,
        onClick = {
            onSelect(value)
        },
        label = {
            Text(label)
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = StockMateOrange,
            selectedLabelColor = StockMateCharcoal
        )
    )
}

// DATE FILTER CHIP

@Composable
private fun DateFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontWeight = if (selected) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                }
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = StockMateOrange,
            selectedLabelColor = StockMateCharcoal
        )
    )
}

// EMPTY SALES CARD

@Composable
private fun EmptySalesCard(
    hasFilters: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PointOfSale,
                contentDescription = null,
                tint = StockMateOrange
            )

            Text(
                text = if (hasFilters) {
                    "No Matching Sales"
                } else {
                    "No Sales Yet"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StockMateCharcoal
            )

            Text(
                text = if (hasFilters) {
                    "Try changing your search or filters."
                } else {
                    "Your completed sales will appear here."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = StockMateMuted
            )
        }
    }
}

// SALES SUMMARY CARD

@Composable
private fun SalesSummaryCard(
    title: String,
    value: String,
    modifier: Modifier,
    valueColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = StockMateMuted
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                fontSize = 15.sp,
                maxLines = 2
            )
        }
    }
}

// SALE HISTORY CARD

@Composable
private fun SaleHistoryCard(
    sale: SaleEntity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "Sale #${sale.saleId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = StockMateCharcoal
                )

                Text(
                    text = sale.customerName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = StockMateCharcoal
                )

                Text(
                    text = formatDate(sale.saleDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = StockMateMuted
                )

                Text(
                    text = sale.paymentMethod.replace("_", " "),
                    style = MaterialTheme.typography.labelSmall,
                    color = StockMateOrange,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = formatLkr(sale.totalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = StockMateCharcoal
                )

                Text(
                    text = "Profit: ${formatLkr(sale.grossProfit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = StockMateOrange
                )
            }
        }
    }
}

// DATE RANGE HELPER

private fun getDateRange(
    filter: String
): Pair<Long, Long> {
    val calendar = Calendar.getInstance()

    when (filter) {
        DATE_TODAY -> {
            // Today's date.
        }

        DATE_WEEK -> {
            val dayOfWeek =
                calendar.get(Calendar.DAY_OF_WEEK)

            val daysFromMonday =
                (dayOfWeek - Calendar.MONDAY + 7) % 7

            calendar.add(
                Calendar.DAY_OF_MONTH,
                -daysFromMonday
            )
        }

        DATE_MONTH -> {
            calendar.set(
                Calendar.DAY_OF_MONTH,
                1
            )
        }
    }

    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val startDate = calendar.timeInMillis

    when (filter) {
        DATE_TODAY -> {
            // End date remains today.
        }

        DATE_WEEK -> {
            calendar.add(
                Calendar.DAY_OF_MONTH,
                6
            )
        }

        DATE_MONTH -> {
            calendar.set(
                Calendar.DAY_OF_MONTH,
                calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            )
        }
    }

    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)

    return Pair(
        startDate,
        calendar.timeInMillis
    )
}

// FORMATTING HELPERS

private fun formatLkr(amountInCents: Long): String {
    return "LKR " + String.format(
        Locale.US,
        "%,.2f",
        amountInCents / 100.0
    )
}

private fun formatCompactLkr(amountInCents: Long): String {
    return String.format(
        Locale.US,
        "%,.0f",
        amountInCents / 100.0
    )
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat(
        "dd MMM yyyy, hh:mm a",
        Locale.getDefault()
    ).format(Date(timestamp))
}
