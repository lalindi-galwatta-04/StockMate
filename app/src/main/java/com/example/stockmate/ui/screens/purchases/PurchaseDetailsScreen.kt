
package com.example.stockmate.ui.screens.purchases

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stockmate.data.local.entity.PurchaseEntity
import com.example.stockmate.ui.theme.StockMateBackground
import com.example.stockmate.ui.theme.StockMateCharcoal
import com.example.stockmate.ui.theme.StockMateDanger
import com.example.stockmate.ui.theme.StockMateMuted
import com.example.stockmate.ui.theme.StockMateOrange
import com.example.stockmate.ui.theme.StockMateSuccess
import com.example.stockmate.ui.viewmodel.PurchaseViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseDetailsScreen(
    purchaseId: Long,
    onBack: () -> Unit,
    purchaseViewModel: PurchaseViewModel = viewModel()
) {
    val purchaseFlow =
        remember(purchaseId) {
            purchaseViewModel.getPurchaseById(purchaseId)
        }

    val purchase by purchaseFlow.collectAsState(initial = null)
    val purchaseItems by purchaseViewModel
        .getPurchaseItems(purchaseId)
        .collectAsState(initial = emptyList())

    val suppliers by purchaseViewModel.suppliers.collectAsState()
    val products by purchaseViewModel.products.collectAsState()
    val isLoading by purchaseViewModel.isLoading.collectAsState()
    val message by purchaseViewModel.message.collectAsState()

    var showReceiveConfirmation by remember {
        mutableStateOf(false)
    }

    val supplier = purchase?.let { currentPurchase ->
        suppliers.firstOrNull {
            it.supplierId == currentPurchase.supplierId
        }
    }

    Scaffold(
        containerColor = StockMateBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Purchase Details",
                            color = StockMateCharcoal,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Purchase #$purchaseId",
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

        if (purchase == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = StockMateOrange
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Loading purchase details...",
                    color = StockMateMuted
                )
            }
        } else {

            val currentPurchase = purchase!!
            val isReceived =
                currentPurchase.status == PurchaseEntity.STATUS_RECEIVED

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                item {
                    // PURCHASE STATUS

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Purchase #${currentPurchase.purchaseId}",
                                    color = StockMateCharcoal,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Surface(
                                    color = (
                                            if (isReceived) StockMateSuccess
                                            else StockMateOrange
                                            ).copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        if (isReceived) "RECEIVED"
                                        else "PENDING",
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 6.dp
                                        ),
                                        color = if (isReceived)
                                            StockMateSuccess
                                        else StockMateOrange,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            DetailRow(
                                "Purchase date",
                                formatDate(currentPurchase.purchaseDate)
                            )

                            DetailRow(
                                "Supplier",
                                supplier?.name
                                    ?: "Supplier #${currentPurchase.supplierId}"
                            )

                            if (currentPurchase.receivedAt != null) {
                                DetailRow(
                                    "Received date",
                                    formatDate(currentPurchase.receivedAt)
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        "Purchased Products (${purchaseItems.size})",
                        color = StockMateCharcoal,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (purchaseItems.isEmpty()) {
                    item {
                        Text(
                            "No purchase items found.",
                            color = StockMateMuted
                        )
                    }
                } else {
                    items(
                        items = purchaseItems,
                        key = { it.purchaseItemId }
                    ) { item ->

                        val product = products.firstOrNull {
                            it.productId == item.productId
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(15.dp),
                                verticalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Inventory,
                                        contentDescription = null,
                                        tint = StockMateOrange
                                    )

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 10.dp)
                                    ) {
                                        Text(
                                            product?.productName
                                                ?: "Product #${item.productId}",
                                            color = StockMateCharcoal,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Product ID: ${item.productId}",
                                            color = StockMateMuted,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                DetailRow(
                                    "Quantity",
                                    item.quantity.toString()
                                )

                                DetailRow(
                                    "Unit cost",
                                    formatLkr(item.unitCost)
                                )

                                DetailRow(
                                    "Line total",
                                    formatLkr(item.lineTotal),
                                    bold = true
                                )
                            }
                        }
                    }
                }

                item {
                    // PURCHASE TOTAL

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = StockMateCharcoal
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {
                            Text(
                                "Total Purchase Amount",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                formatLkr(currentPurchase.totalAmount),
                                color = StockMateOrange,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (currentPurchase.notes.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    "Additional Notes",
                                    color = StockMateCharcoal,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    currentPurchase.notes,
                                    color = StockMateMuted
                                )
                            }
                        }
                    }

                    // RECEIVE PURCHASE BUTTON
                    Spacer(Modifier.height(12.dp))
                    if (!isReceived) {
                        Button(
                            onClick = {
                                showReceiveConfirmation = true
                            },
                            enabled = !isLoading &&
                                    purchaseItems.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StockMateOrange,
                                contentColor = StockMateCharcoal
                            )
                        ) {
                            Text(
                                if (isLoading) "Processing..."
                                else "Receive Purchase",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = StockMateSuccess.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = StockMateSuccess
                                )

                                Text(
                                    "  Purchase received. Inventory updated.",
                                    color = StockMateSuccess,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }

    // RECEIVE CONFIRMATION

    if (showReceiveConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showReceiveConfirmation = false
            },
            title = {
                Text("Receive Purchase?")
            },
            text = {
                Text(
                    "Are you sure you want to mark this purchase as received? " +
                            "The product stock quantities will be updated automatically."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showReceiveConfirmation = false

                        purchaseViewModel.receivePurchase(
                            purchaseId = purchaseId
                        )
                    }
                ) {
                    Text(
                        "Confirm",
                        color = StockMateOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showReceiveConfirmation = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // RESULT MESSAGE

    message?.let { resultMessage ->
        AlertDialog(
            onDismissRequest = {
                purchaseViewModel.clearMessage()
            },
            title = {
                Text("StockMate")
            },
            text = {
                Text(resultMessage)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        purchaseViewModel.clearMessage()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = StockMateMuted,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            value,
            color = StockMateCharcoal,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold
            else FontWeight.Medium
        )
    }
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat(
        "dd MMM yyyy, hh:mm a",
        Locale.getDefault()
    ).format(Date(timestamp))
}

private fun formatLkr(amountInCents: Long): String {
    val rupees = amountInCents / 100
    val cents = (amountInCents % 100)
        .toString()
        .padStart(2, '0')

    return "LKR ${"%,d".format(rupees)}.$cents"
}
