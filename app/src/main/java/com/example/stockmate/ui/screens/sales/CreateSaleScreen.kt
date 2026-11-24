
package com.example.stockmate.ui.screens.sales

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stockmate.data.local.entity.ProductEntity
import com.example.stockmate.data.local.entity.SaleEntity
import com.example.stockmate.data.repository.SaleInputItem
import com.example.stockmate.ui.viewmodel.SalesViewModel
import java.util.Locale
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState

private data class SaleCartLine(
    val productId: Long,
    val quantity: Int
)

@Composable
fun CreateSaleScreen(
    onBack: () -> Unit,
    onSaleCreated: (Long) -> Unit,
    salesViewModel: SalesViewModel = viewModel()
) {

    val products by salesViewModel.products.collectAsState()
    val isLoading by salesViewModel.isLoading.collectAsState()
    val message by salesViewModel.message.collectAsState()

    val cartItems = remember {
        mutableStateListOf<SaleCartLine>()
    }

    var customerName by rememberSaveable {
        mutableStateOf("")
    }

    var customerPhone by rememberSaveable {
        mutableStateOf("")
    }

    var notes by rememberSaveable {
        mutableStateOf("")
    }

    var paymentMethod by rememberSaveable {
        mutableStateOf(SaleEntity.PAYMENT_CASH)
    }

    val selectedProducts = cartItems.mapNotNull { line ->
        products.find { it.productId == line.productId }
    }

    val totalAmount = cartItems.sumOf { line ->
        val product = products.find {
            it.productId == line.productId
        }

        (product?.sellingPrice ?: 0L) * line.quantity
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // -------------------------------------------------
        // HEADER
        // -------------------------------------------------

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Go back"
                )
            }

            Column {
                Text(
                    text = "Create New Sale",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Select products and complete checkout",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // -------------------------------------------------
        // CUSTOMER DETAILS
        // -------------------------------------------------

        Text(
            text = "Customer Details",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = customerName,
            onValueChange = { customerName = it },
            label = { Text("Customer Name") },
            placeholder = { Text("Walk-in Customer") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = customerPhone,
            onValueChange = { customerPhone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // -------------------------------------------------
        // PRODUCT SELECTION
        // -------------------------------------------------

        Text(
            text = "Available Products",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        if (products.isEmpty()) {

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No active products available. Add products to your inventory first.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        } else {

            products.forEach { product ->

                AvailableProductCard(
                    product = product,
                    currentQuantity = cartItems.find {
                        it.productId == product.productId
                    }?.quantity ?: 0,
                    onAdd = {

                        val index = cartItems.indexOfFirst {
                            it.productId == product.productId
                        }

                        if (index >= 0) {

                            val current = cartItems[index]

                            if (current.quantity < product.quantityInStock) {
                                cartItems[index] = current.copy(
                                    quantity = current.quantity + 1
                                )
                            }

                        } else if (product.quantityInStock > 0) {

                            cartItems.add(
                                SaleCartLine(
                                    productId = product.productId,
                                    quantity = 1
                                )
                            )
                        }
                    }
                )
            }
        }

        // -------------------------------------------------
        // SHOPPING CART
        // -------------------------------------------------

        Text(
            text = "Sale Items (${cartItems.sumOf { it.quantity }})",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        if (cartItems.isEmpty()) {

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "Your sale cart is empty. Add products to continue.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        } else {

            cartItems.toList().forEach { line ->

                val product = products.find {
                    it.productId == line.productId
                }

                if (product != null) {

                    SaleCartItemCard(
                        product = product,
                        quantity = line.quantity,
                        onIncrease = {

                            val index = cartItems.indexOfFirst {
                                it.productId == line.productId
                            }

                            if (
                                index >= 0 &&
                                cartItems[index].quantity < product.quantityInStock
                            ) {
                                cartItems[index] =
                                    cartItems[index].copy(
                                        quantity = cartItems[index].quantity + 1
                                    )
                            }
                        },
                        onDecrease = {

                            val index = cartItems.indexOfFirst {
                                it.productId == line.productId
                            }

                            if (index >= 0) {

                                if (cartItems[index].quantity > 1) {
                                    cartItems[index] =
                                        cartItems[index].copy(
                                            quantity = cartItems[index].quantity - 1
                                        )
                                } else {
                                    cartItems.removeAt(index)
                                }
                            }
                        },
                        onRemove = {
                            cartItems.removeAll {
                                it.productId == line.productId
                            }
                        }
                    )
                }
            }
        }

        // -------------------------------------------------
        // PAYMENT METHOD
        // -------------------------------------------------

        Text(
            text = "Payment Method",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        val paymentMethods = listOf(
            SaleEntity.PAYMENT_CASH to "Cash",
            SaleEntity.PAYMENT_CARD to "Card",
            SaleEntity.PAYMENT_BANK_TRANSFER to "Bank Transfer",
            SaleEntity.PAYMENT_OTHER to "Other"
        )


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            paymentMethods.forEach { (value, label) ->

                FilterChip(
                    selected = paymentMethod == value,
                    onClick = { paymentMethod = value },
                    label = { Text(label) }
                )
            }
        }


        // -------------------------------------------------
        // NOTES
        // -------------------------------------------------

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Additional Notes (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        // -------------------------------------------------
        // ORDER SUMMARY
        // -------------------------------------------------

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {

            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Text(
                    text = "Order Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                SummaryRow(
                    label = "Total Items",
                    value = cartItems.sumOf { it.quantity }.toString()
                )

                SummaryRow(
                    label = "Products",
                    value = cartItems.size.toString()
                )

                androidx.compose.material3.HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = "Total Amount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = formatSaleLkr(totalAmount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // -------------------------------------------------
        // ERROR / SUCCESS MESSAGE
        // -------------------------------------------------

        message?.let { text ->

            Text(
                text = text,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // -------------------------------------------------
        // COMPLETE SALE
        // -------------------------------------------------

        Button(
            onClick = {

                salesViewModel.createSale(
                    items = cartItems.map {
                        SaleInputItem(
                            productId = it.productId,
                            quantity = it.quantity
                        )
                    },
                    customerName = customerName.ifBlank {
                        "Walk-in Customer"
                    },
                    customerPhone = customerPhone,
                    paymentMethod = paymentMethod,
                    notes = notes,
                    onComplete = { saleId ->

                        if (saleId != null) {
                            onSaleCreated(saleId)
                        }
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            enabled = cartItems.isNotEmpty() && !isLoading
        ) {

            Text(
                text = if (isLoading) {
                    "Processing Sale..."
                } else {
                    "Complete Sale"
                },
                fontWeight = FontWeight.Bold
            )
        }

        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Cancel")
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

// ---------------------------------------------------------
// AVAILABLE PRODUCT CARD
// ---------------------------------------------------------

@Composable
private fun AvailableProductCard(
    product: ProductEntity,
    currentQuantity: Int,
    onAdd: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {

            Text(
                text = product.productName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "SKU: ${product.sku}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {
                    Text(
                        text = formatSaleLkr(product.sellingPrice),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Stock: ${product.quantityInStock}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = onAdd,
                    enabled = product.quantityInStock > currentQuantity
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )

                    Text(
                        if (currentQuantity > 0) {
                            "Add More"
                        } else if (product.quantityInStock <= 0) {
                            "Out of Stock"
                        } else {
                            "Add"
                        }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------
// CART ITEM CARD
// ---------------------------------------------------------

@Composable
private fun SaleCartItemCard(
    product: ProductEntity,
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = product.productName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${formatSaleLkr(product.sellingPrice)} each",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                TextButton(onClick = onRemove) {
                    Text("Remove")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(onClick = onDecrease) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease quantity"
                        )
                    }

                    Text(
                        text = quantity.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = onIncrease,
                        enabled = quantity < product.quantityInStock
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase quantity"
                        )
                    }
                }

                Text(
                    text = formatSaleLkr(
                        product.sellingPrice * quantity
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ---------------------------------------------------------
// SUMMARY ROW
// ---------------------------------------------------------

@Composable
private fun SummaryRow(
    label: String,
    value: String
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            fontWeight = FontWeight.Medium
        )
    }
}

// ---------------------------------------------------------
// CURRENCY FORMATTING
// ---------------------------------------------------------

private fun formatSaleLkr(amountInCents: Long): String {

    return "LKR " + String.format(
        Locale.US,
        "%,.2f",
        amountInCents / 100.0
    )
}
