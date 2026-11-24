package com.example.stockmate.ui.screens.purchases

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stockmate.data.local.entity.PurchaseEntity
import com.example.stockmate.ui.theme.StockMateBackground
import com.example.stockmate.ui.theme.StockMateCharcoal
import com.example.stockmate.ui.theme.StockMateMuted
import com.example.stockmate.ui.theme.StockMateOrange
import com.example.stockmate.ui.theme.StockMateSuccess
import com.example.stockmate.ui.viewmodel.PurchaseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseListScreen(
    onBack: () -> Unit,
    onNewPurchase: () -> Unit,
    onPurchaseClick: (Long) -> Unit = {},
    purchaseViewModel: PurchaseViewModel = viewModel()
) {
    val purchases by purchaseViewModel.purchases.collectAsState()
    val suppliers by purchaseViewModel.suppliers.collectAsState()
    val purchaseCount by purchaseViewModel.purchaseCount.collectAsState()
    val pendingCount by purchaseViewModel.pendingPurchaseCount.collectAsState()
    val receivedAmount by purchaseViewModel.totalReceivedPurchaseAmount.collectAsState()

    Scaffold(
        containerColor = StockMateBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Purchases",
                            color = StockMateCharcoal,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Manage supplier orders and stock arrivals",
                            color = StockMateMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = StockMateCharcoal
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StockMateBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryCard(
                    title = "Purchases",
                    value = purchaseCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Pending",
                    value = pendingCount.toString(),
                    modifier = Modifier.weight(1f),
                    valueColor = StockMateOrange
                )
            }

            Spacer(Modifier.height(10.dp))

            SummaryCard(
                title = "Received purchase value",
                value = formatLkr(receivedAmount),
                modifier = Modifier.fillMaxWidth(),
                valueColor = StockMateSuccess
            )

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = onNewPurchase,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StockMateOrange,
                    contentColor = StockMateCharcoal
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("New Purchase", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(18.dp))

            Text(
                "Purchase History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = StockMateCharcoal
            )

            Spacer(Modifier.height(10.dp))

            if (purchases.isEmpty()) {
                EmptyPurchases()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = purchases,
                        key = { it.purchaseId }
                    ) { purchase ->
                        val supplier = suppliers.firstOrNull {
                            it.supplierId == purchase.supplierId
                        }
                        PurchaseCard(
                            purchase = purchase,
                            supplierName = supplier?.name
                                ?: "Supplier #${purchase.supplierId}",
                            onClick = { onPurchaseClick(purchase.purchaseId) }
                        )
                    }
                    item { Spacer(Modifier.height(12.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier,
    valueColor: Color = StockMateCharcoal
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                title,
                color = StockMateMuted,
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                color = valueColor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PurchaseCard(
    purchase: PurchaseEntity,
    supplierName: String,
    onClick: () -> Unit
) {
    val isReceived = purchase.status == PurchaseEntity.STATUS_RECEIVED
    val statusColor = if (isReceived) StockMateSuccess else StockMateOrange
    val dateText = SimpleDateFormat(
        "dd MMM yyyy",
        Locale.getDefault()
    ).format(Date(purchase.purchaseDate))

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(15.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Purchase #${purchase.purchaseId}",
                        color = StockMateCharcoal,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        supplierName,
                        color = StockMateMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        if (isReceived) "RECEIVED" else "PENDING",
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 6.dp
                        ),
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    dateText,
                    color = StockMateMuted,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    formatLkr(purchase.totalAmount),
                    color = StockMateCharcoal,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (purchase.notes.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    purchase.notes,
                    color = StockMateMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun EmptyPurchases() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ReceiptLong,
            contentDescription = null,
            tint = StockMateMuted,
            modifier = Modifier.size(52.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "No purchases yet",
            color = StockMateCharcoal,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Create your first purchase to start tracking supplier orders.",
            color = StockMateMuted,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun formatLkr(amountInCents: Long): String {
    val rupees = amountInCents / 100
    val cents = (amountInCents % 100).toString().padStart(2, '0')
    return "LKR ${"%,d".format(rupees)}.$cents"
}
