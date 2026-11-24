
package com.example.stockmate.ui.screens.sales

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stockmate.data.local.entity.ProductEntity
import com.example.stockmate.data.local.entity.SaleEntity
import com.example.stockmate.data.local.entity.SaleItemEntity
import com.example.stockmate.ui.viewmodel.SalesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SaleDetailsScreen(
    saleId: Long,
    onBack: () -> Unit,
    salesViewModel: SalesViewModel = viewModel()
) {

    val saleFlow = remember(saleId) {
        salesViewModel.getSaleById(saleId)
    }

    val itemsFlow = remember(saleId) {
        salesViewModel.getSaleItems(saleId)
    }

    val sale by saleFlow.collectAsState(initial = null)
    val saleItems by itemsFlow.collectAsState(initial = emptyList())

    if (sale == null) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()

            Spacer(modifier = Modifier.height(12.dp))

            Text("Loading sale details...")
        }

        return
    }

    val currentSale = sale!!

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // HEADER
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Column {
                    Text(
                        text = "Sale Details",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Sale #${currentSale.saleId}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // TRANSACTION INFORMATION
        item {
            DetailsCard(title = "Transaction Information") {

                DetailRow("Sale ID", "#${currentSale.saleId}")

                DetailRow(
                    "Date",
                    formatSaleDate(currentSale.saleDate)
                )

                DetailRow("Status", currentSale.status)

                DetailRow(
                    "Payment Method",
                    currentSale.paymentMethod.replace("_", " ")
                )
            }
        }

        // CUSTOMER INFORMATION
        item {
            DetailsCard(title = "Customer Information") {

                DetailRow(
                    "Customer",
                    currentSale.customerName
                )

                DetailRow(
                    "Phone",
                    currentSale.customerPhone.ifBlank {
                        "Not provided"
                    }
                )
            }
        }

        // PURCHASED PRODUCTS
        item {
            Text(
                text = "Purchased Products (${saleItems.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (saleItems.isEmpty()) {

            item {
                Text(
                    text = "No sale items found.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        } else {

            items(
                items = saleItems,
                key = { it.saleItemId }
            ) { item ->

                SaleDetailItemCard(
                    item = item,
                    salesViewModel = salesViewModel
                )
            }
        }

        // FINANCIAL SUMMARY
        item {
            DetailsCard(title = "Financial Summary") {

                DetailRow(
                    "Total Cost",
                    formatSaleLkr(currentSale.totalCost)
                )

                DetailRow(
                    "Total Revenue",
                    formatSaleLkr(currentSale.totalAmount)
                )

                androidx.compose.material3.HorizontalDivider()

                DetailRow(
                    "Gross Profit",
                    formatSaleLkr(currentSale.grossProfit),
                    highlight = true
                )
            }
        }

        // NOTES
        if (currentSale.notes.isNotBlank()) {

            item {
                DetailsCard(title = "Additional Notes") {
                    Text(
                        text = currentSale.notes,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

// ---------------------------------------------------------
// DETAILS CARD
// ---------------------------------------------------------

@Composable
private fun DetailsCard(
    title: String,
    content: @Composable () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            content()
        }
    }
}

// ---------------------------------------------------------
// DETAIL ROW
// ---------------------------------------------------------

@Composable
private fun DetailRow(
    label: String,
    value: String,
    highlight: Boolean = false
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = value,
            fontWeight = if (highlight) {
                FontWeight.Bold
            } else {
                FontWeight.Medium
            },
            color = if (highlight) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// ---------------------------------------------------------
// SALE ITEM CARD
// ---------------------------------------------------------

@Composable
private fun SaleDetailItemCard(
    item: SaleItemEntity,
    salesViewModel: SalesViewModel
) {

    val product by produceState<ProductEntity?>(
        initialValue = null,
        key1 = item.productId
    ) {
        value = salesViewModel.getProductById(item.productId)
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = product?.productName
                    ?: "Product #${item.productId}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            product?.sku?.let { sku ->
                Text(
                    text = "SKU: $sku",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            DetailRow(
                "Quantity",
                item.quantity.toString()
            )

            DetailRow(
                "Unit Selling Price",
                formatSaleLkr(item.unitSellingPrice)
            )

            DetailRow(
                "Unit Cost",
                formatSaleLkr(item.unitCost)
            )

            androidx.compose.material3.HorizontalDivider()

            DetailRow(
                "Subtotal",
                formatSaleLkr(item.lineTotal),
                highlight = true
            )

            DetailRow(
                "Item Profit",
                formatSaleLkr(item.grossProfit),
                highlight = true
            )
        }
    }
}

// ---------------------------------------------------------
// FORMATTING
// ---------------------------------------------------------

private fun formatSaleLkr(amountInCents: Long): String {

    return "LKR " + String.format(
        Locale.US,
        "%,.2f",
        amountInCents / 100.0
    )
}

private fun formatSaleDate(timestamp: Long): String {

    return SimpleDateFormat(
        "dd MMM yyyy, hh:mm a",
        Locale.getDefault()
    ).format(Date(timestamp))
}
