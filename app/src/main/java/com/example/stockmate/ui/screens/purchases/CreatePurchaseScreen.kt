
package com.example.stockmate.ui.screens.purchases

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stockmate.data.local.entity.ProductEntity
import com.example.stockmate.data.repository.PurchaseInputItem
import com.example.stockmate.ui.theme.StockMateBackground
import com.example.stockmate.ui.theme.StockMateCharcoal
import com.example.stockmate.ui.theme.StockMateDanger
import com.example.stockmate.ui.theme.StockMateMuted
import com.example.stockmate.ui.theme.StockMateOrange
import com.example.stockmate.ui.viewmodel.PurchaseViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import androidx.compose.material.icons.filled.PersonAdd

private data class PurchaseDraftLine(
    val product: ProductEntity,
    val quantity: Int,
    val unitCost: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePurchaseScreen(
    onBack: () -> Unit,
    onPurchaseCreated: (Long) -> Unit,
    onAddSupplier: () -> Unit,
    purchaseViewModel: PurchaseViewModel = viewModel()
) {
    val suppliers by purchaseViewModel.suppliers.collectAsState()
    val products by purchaseViewModel.products.collectAsState()
    val isLoading by purchaseViewModel.isLoading.collectAsState()
    val message by purchaseViewModel.message.collectAsState()

    var selectedSupplierId by remember {
        mutableStateOf<Long?>(null)
    }

    var supplierMenuExpanded by remember {
        mutableStateOf(false)
    }

    var selectedProduct by remember {
        mutableStateOf<ProductEntity?>(null)
    }

    var productMenuExpanded by remember {
        mutableStateOf(false)
    }

    var quantityText by remember { mutableStateOf("1") }

    var unitCostText by remember { mutableStateOf("") }

    var notes by remember { mutableStateOf("") }

    var validationMessage by remember {
        mutableStateOf<String?>(null)
    }

    val draftItems = remember {
        mutableStateListOf<PurchaseDraftLine>()
    }

    val selectedSupplier = suppliers.firstOrNull {
        it.supplierId == selectedSupplierId
    }

    val totalAmount = draftItems.fold(0L) { total, item ->
        try {
            Math.addExact(
                total,
                Math.multiplyExact(item.unitCost, item.quantity.toLong())
            )
        } catch (_: ArithmeticException) {
            Long.MAX_VALUE
        }
    }

    Scaffold(
        containerColor = StockMateBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "New Purchase",
                            color = StockMateCharcoal,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Record products purchased from a supplier",
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // SUPPLIER SECTION

            FormSectionTitle("Supplier")

            Column {
                OutlinedTextField(
                    value = selectedSupplier?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Select supplier") },
                    placeholder = { Text("Choose a supplier") },
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        TextButton(
                            enabled = suppliers.isNotEmpty(),
                            onClick = {
                                supplierMenuExpanded = true
                            }
                        ) {
                            Text(
                                "Choose",
                                color = if (suppliers.isNotEmpty())
                                    StockMateOrange
                                else
                                    StockMateMuted
                            )
                        }
                    }
                )

                DropdownMenu(
                    expanded = supplierMenuExpanded,
                    onDismissRequest = {
                        supplierMenuExpanded = false
                    },
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    suppliers.forEach { supplier ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        supplier.name,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (supplier.phone.isNotBlank()) {
                                        Text(
                                            supplier.phone,
                                            color = StockMateMuted,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            },
                            onClick = {
                                selectedSupplierId = supplier.supplierId
                                supplierMenuExpanded = false
                                validationMessage = null
                            }
                        )
                    }
                }

                if (suppliers.isEmpty()) {

                    Text(
                        "No active suppliers available. Add a supplier to continue.",
                        color = StockMateDanger,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp)
                    )

                    Button(
                        onClick = onAddSupplier,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StockMateOrange,
                            contentColor = StockMateCharcoal
                        )
                    ) {
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = null
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            "Add New Supplier",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // PRODUCT SECTION

            FormSectionTitle("Add Products")

            Column {
                OutlinedTextField(
                    value = selectedProduct?.productName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Select product") },
                    placeholder = { Text("Choose a product") },
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        TextButton(
                            onClick = {
                                productMenuExpanded = true
                            }
                        ) {
                            Text(
                                "Choose",
                                color = StockMateOrange
                            )
                        }
                    }
                )

                DropdownMenu(
                    expanded = productMenuExpanded,
                    onDismissRequest = {
                        productMenuExpanded = false
                    },
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    products.forEach { product ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        product.productName,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "SKU: ${product.sku}",
                                        color = StockMateMuted,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            onClick = {
                                selectedProduct = product
                                unitCostText = centsToDecimal(
                                    product.purchasePrice
                                )
                                productMenuExpanded = false
                                validationMessage = null
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = {
                        quantityText = it.filter(Char::isDigit)
                        validationMessage = null
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = unitCostText,
                    onValueChange = {
                        unitCostText = it.filter { char ->
                            char.isDigit() || char == '.'
                        }
                        validationMessage = null
                    },
                    modifier = Modifier.weight(1.4f),
                    label = { Text("Unit cost (LKR)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Button(
                onClick = {
                    val product = selectedProduct
                    val quantity = quantityText.toIntOrNull()
                    val cost = parseLkrToCents(unitCostText)

                    validationMessage = when {
                        product == null ->
                            "Please select a product."

                        quantity == null || quantity <= 0 ->
                            "Enter a valid quantity."

                        cost == null ->
                            "Enter a valid unit cost."

                        draftItems.any {
                            it.product.productId == product.productId
                        } ->
                            "This product is already in the purchase."

                        else -> null
                    }

                    if (validationMessage == null && product != null) {
                        draftItems.add(
                            PurchaseDraftLine(
                                product = product,
                                quantity = quantity!!,
                                unitCost = cost!!
                            )
                        )

                        selectedProduct = null
                        quantityText = "1"
                        unitCostText = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StockMateOrange,
                    contentColor = StockMateCharcoal
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Add Product",
                    fontWeight = FontWeight.Bold
                )
            }

            validationMessage?.let {
                Text(
                    it,
                    color = StockMateDanger,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // PURCHASE ITEMS

            FormSectionTitle("Purchase Items (${draftItems.size})")

            if (draftItems.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Text(
                        "No products added yet.",
                        modifier = Modifier.padding(18.dp),
                        color = StockMateMuted
                    )
                }
            } else {
                draftItems.forEachIndexed { index, item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    item.product.productName,
                                    fontWeight = FontWeight.Bold,
                                    color = StockMateCharcoal
                                )
                                Text(
                                    "${item.quantity} × LKR ${formatLkr(item.unitCost)}",
                                    color = StockMateMuted,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "Line total: LKR ${
                                        formatLkr(
                                            item.unitCost * item.quantity.toLong()
                                        )
                                    }",
                                    fontWeight = FontWeight.SemiBold,
                                    color = StockMateCharcoal
                                )
                            }

                            IconButton(
                                onClick = {
                                    draftItems.removeAt(index)
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Remove product",
                                    tint = StockMateDanger
                                )
                            }
                        }
                    }
                }
            }

            // NOTES

            FormSectionTitle("Additional Notes")

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Purchase notes (optional)") },
                placeholder = {
                    Text("Add any relevant purchase information")
                },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp)
            )

            // TOTAL

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = StockMateCharcoal
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Purchase Total",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        "LKR ${formatLkr(totalAmount)}",
                        color = StockMateOrange,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            Button(
                onClick = {
                    validationMessage = when {
                        selectedSupplierId == null ->
                            "Please select a supplier."

                        draftItems.isEmpty() ->
                            "Add at least one product."

                        else -> null
                    }

                    if (validationMessage == null) {
                        purchaseViewModel.createPurchase(
                            supplierId = selectedSupplierId!!,
                            items = draftItems.map {
                                PurchaseInputItem(
                                    productId = it.product.productId,
                                    quantity = it.quantity,
                                    unitCost = it.unitCost
                                )
                            },
                            notes = notes
                        ) { purchaseId ->
                            if (purchaseId != null) {
                                onPurchaseCreated(purchaseId)
                            }
                        }
                    }
                },
                enabled = !isLoading,
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
                    if (isLoading) "Saving..." else "Save Purchase",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))
        }
    }

    message?.let { text ->
        AlertDialog(
            onDismissRequest = {
                purchaseViewModel.clearMessage()
            },
            title = {
                Text("StockMate")
            },
            text = {
                Text(text)
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
private fun FormSectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = StockMateCharcoal
    )
}

private fun parseLkrToCents(value: String): Long? {
    return try {
        val amount = BigDecimal(value.trim())
            .setScale(2, RoundingMode.UNNECESSARY)
        if (amount < BigDecimal.ZERO) {
            null
        } else {
            amount.movePointRight(2).longValueExact()
        }
    } catch (_: Exception) {
        null
    }
}

private fun centsToDecimal(cents: Long): String {
    return BigDecimal.valueOf(cents, 2).toPlainString()
}

private fun formatLkr(cents: Long): String {
    return BigDecimal.valueOf(cents, 2)
        .setScale(2, RoundingMode.HALF_UP)
        .toPlainString()
}
