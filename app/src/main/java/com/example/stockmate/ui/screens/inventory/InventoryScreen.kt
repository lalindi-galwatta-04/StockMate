package com.example.stockmate.ui.screens.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stockmate.data.local.entity.ProductEntity
import com.example.stockmate.data.local.entity.StockMovementEntity
import com.example.stockmate.ui.theme.StockMateBackground
import com.example.stockmate.ui.theme.StockMateCharcoal
import com.example.stockmate.ui.theme.StockMateDanger
import com.example.stockmate.ui.theme.StockMateMuted
import com.example.stockmate.ui.theme.StockMateOrange
import com.example.stockmate.ui.theme.StockMateSuccess
import com.example.stockmate.ui.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val InventoryWarning = Color(0xFFE58A25)

private enum class InventoryTab {
    OVERVIEW,
    HISTORY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onManageSuppliers: () -> Unit,
    onManagePurchases: () -> Unit,
    inventoryViewModel: InventoryViewModel = viewModel()
) {
    val products by inventoryViewModel.products.collectAsState()

    val lowStockProducts by
    inventoryViewModel.lowStockProducts.collectAsState()

    val outOfStockProducts by
    inventoryViewModel.outOfStockProducts.collectAsState()

    val totalStock by
    inventoryViewModel.totalStockQuantity.collectAsState()

    val lowStockCount by
    inventoryViewModel.lowStockCount.collectAsState()

    val outOfStockCount by
    inventoryViewModel.outOfStockCount.collectAsState()

    val movements by
    inventoryViewModel.movements.collectAsState()

    var selectedTab by remember {
        mutableStateOf(InventoryTab.OVERVIEW)
    }

    var selectedProductId by remember {
        mutableStateOf<Long?>(null)
    }

    var message by remember {
        mutableStateOf<String?>(null)
    }

    // Get the latest product information from Room.
    val selectedProduct = products.firstOrNull {
        it.productId == selectedProductId
    }

    Scaffold(
        containerColor = StockMateBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Inventory",
                            fontWeight = FontWeight.Bold,
                            color = StockMateCharcoal
                        )

                        Text(
                            "Monitor and manage your stock",
                            style = MaterialTheme.typography.bodySmall,
                            color = StockMateMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StockMateBackground
                ),
                actions = {
                    IconButton(onClick = onManageSuppliers) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = "Manage suppliers",
                            tint = StockMateOrange
                        )
                    }
                    IconButton(onClick = onManagePurchases) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = "Manage purchases",
                            tint = StockMateOrange
                        )
                    }
                    IconButton(
                        onClick = {
                            selectedTab = InventoryTab.HISTORY
                        }
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Stock history",
                            tint = StockMateOrange
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {

            InventorySummary(
                totalStock = totalStock,
                lowStock = lowStockCount,
                outOfStock = outOfStockCount
            )

            Spacer(Modifier.height(18.dp))

            // Overview / History selector
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                InventoryTab.entries.forEachIndexed { index, tab ->

                    SegmentedButton(
                        selected = selectedTab == tab,
                        onClick = {
                            selectedTab = tab
                        },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = InventoryTab.entries.size
                        )
                    ) {
                        Text(
                            if (tab == InventoryTab.OVERVIEW) {
                                "Overview"
                            } else {
                                "History"
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            when (selectedTab) {

                InventoryTab.OVERVIEW -> {
                    InventoryOverview(
                        products = products,
                        lowStockProducts = lowStockProducts,
                        outOfStockProducts = outOfStockProducts,
                        onAdjustStock = { product ->
                            selectedProductId = product.productId
                        }
                    )
                }

                InventoryTab.HISTORY -> {
                    MovementHistory(
                        movements = movements,
                        products = products
                    )
                }
            }
        }
    }

    // Stock adjustment dialog
    selectedProduct?.let { product ->

        StockAdjustmentDialog(
            product = product,
            onDismiss = {
                selectedProductId = null
            },
            onSave = { quantity, type, reason ->

                inventoryViewModel.adjustStock(
                    productId = product.productId,
                    quantity = quantity,
                    movementType = type,
                    reason = reason
                ) { _, result ->

                    selectedProductId = null
                    message = result
                }
            }
        )
    }

    message?.let { result ->

        AlertDialog(
            onDismissRequest = {
                message = null
            },
            title = {
                Text("StockMate")
            },
            text = {
                Text(result)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        message = null
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun InventorySummary(
    totalStock: Int,
    lowStock: Int,
    outOfStock: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        InventorySummaryCard(
            title = "Total Units",
            value = totalStock.toString(),
            modifier = Modifier.weight(1f),
            valueColor = StockMateCharcoal
        )

        InventorySummaryCard(
            title = "Low Stock",
            value = lowStock.toString(),
            modifier = Modifier.weight(1f),
            valueColor = InventoryWarning
        )

        InventorySummaryCard(
            title = "Out of Stock",
            value = outOfStock.toString(),
            modifier = Modifier.weight(1f),
            valueColor = StockMateDanger
        )
    }
}

@Composable
private fun InventorySummaryCard(
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
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = StockMateMuted
            )

            Spacer(Modifier.height(6.dp))

            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
private fun InventoryOverview(
    products: List<ProductEntity>,
    lowStockProducts: List<ProductEntity>,
    outOfStockProducts: List<ProductEntity>,
    onAdjustStock: (ProductEntity) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        if (lowStockProducts.isNotEmpty()) {
            AlertStockBanner(
                title = "${lowStockProducts.size} products need restocking",
                description = "These products have reached their minimum stock level.",
                color = InventoryWarning
            )

            Spacer(Modifier.height(10.dp))
        }

        if (outOfStockProducts.isNotEmpty()) {
            AlertStockBanner(
                title = "${outOfStockProducts.size} products are out of stock",
                description = "These products currently have no available units.",
                color = StockMateDanger
            )

            Spacer(Modifier.height(10.dp))
        }

        Text(
            "All Products",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(10.dp))

        if (products.isEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = StockMateMuted,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(Modifier.height(12.dp))

                Text("No products available")

                Text(
                    "Add products from the Products section.",
                    color = StockMateMuted
                )
            }

        } else {

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = products,
                    key = { it.productId }
                ) { product ->

                    InventoryProductCard(
                        product = product,
                        onAdjustStock = {
                            onAdjustStock(product)
                        }
                    )
                }

                item {
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun AlertStockBanner(
    title: String,
    description: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.10f)
        )
    ) {
        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = color
            )

            Column(
                modifier = Modifier.padding(start = 10.dp)
            ) {
                Text(
                    title,
                    color = color,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    description,
                    color = StockMateMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun InventoryProductCard(
    product: ProductEntity,
    onAdjustStock: () -> Unit
) {
    val statusColor = when {
        product.quantityInStock <= 0 -> StockMateDanger

        product.quantityInStock <= product.minimumStockLevel ->
            InventoryWarning

        else -> StockMateSuccess
    }

    val status = when {
        product.quantityInStock <= 0 -> "Out of stock"

        product.quantityInStock <= product.minimumStockLevel ->
            "Low stock"

        else -> "In stock"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            StockMateOrange.copy(alpha = 0.13f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Inventory2,
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
                        product.productName,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "SKU: ${product.sku}",
                        color = StockMateMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    status,
                    color = statusColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Available stock",
                        color = StockMateMuted,
                        style = MaterialTheme.typography.labelSmall
                    )

                    Text(
                        "${product.quantityInStock} units",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        "Minimum level",
                        color = StockMateMuted,
                        style = MaterialTheme.typography.labelSmall
                    )

                    Text(
                        "${product.minimumStockLevel} units",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onAdjustStock,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StockMateOrange,
                    contentColor = StockMateCharcoal
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    "Adjust Stock",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MovementHistory(
    movements: List<StockMovementEntity>,
    products: List<ProductEntity>
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            "Stock Movement History",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(10.dp))

        if (movements.isEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    tint = StockMateMuted,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(Modifier.height(12.dp))

                Text("No stock movements yet")

                Text(
                    "Your stock adjustments will appear here.",
                    color = StockMateMuted
                )
            }

        } else {

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = movements,
                    key = { it.movementId }
                ) { movement ->

                    MovementCard(
                        movement = movement,
                        productName = products.firstOrNull {
                            it.productId == movement.productId
                        }?.productName
                            ?: "Product #${movement.productId}"
                    )
                }
            }
        }
    }
}

@Composable
private fun MovementCard(
    movement: StockMovementEntity,
    productName: String
) {
    val isAddition = movement.movementType == "ADD"

    val movementColor = if (isAddition) {
        StockMateSuccess
    } else {
        StockMateDanger
    }

    val signedQuantity = if (isAddition) {
        "+${movement.quantityChange}"
    } else {
        movement.quantityChange.toString()
    }

    val date = remember(movement.createdAt) {
        SimpleDateFormat(
            "dd MMM yyyy, hh:mm a",
            Locale.getDefault()
        ).format(Date(movement.createdAt))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        productName,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        date,
                        color = StockMateMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    signedQuantity,
                    color = movementColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Reason: ${movement.reason}",
                style = MaterialTheme.typography.bodySmall,
                color = StockMateMuted
            )

            Text(
                "${movement.previousQuantity} → ${movement.newQuantity} units",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StockAdjustmentDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onSave: (Int, String, String) -> Unit
) {
    var movementType by remember {
        mutableStateOf("ADD")
    }

    var quantityText by remember {
        mutableStateOf("1")
    }

    var reason by remember {
        mutableStateOf("")
    }

    var validationMessage by remember {
        mutableStateOf<String?>(null)
    }

    val quantity = quantityText.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                "Adjust Stock",
                fontWeight = FontWeight.Bold
            )
        },

        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    product.productName,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    "Current stock: ${product.quantityInStock} units",
                    color = StockMateMuted
                )

                // Add Stock / Remove Stock selector
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("ADD", "REMOVE").forEachIndexed { index, type ->

                        SegmentedButton(
                            selected = movementType == type,
                            onClick = {
                                movementType = type
                                validationMessage = null
                            },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = 2
                            )
                        ) {
                            Text(
                                text = if (type == "ADD") {
                                    "Add Stock"
                                } else {
                                    "Remove Stock"
                                },
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Quantity label
                Text(
                    "Quantity",
                    style = MaterialTheme.typography.labelLarge,
                    color = StockMateMuted
                )

                // Minus / Quantity / Plus controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    // Decrease quantity
                    IconButton(
                        onClick = {
                            val current = quantity ?: 1

                            if (current > 1) {
                                quantityText = (current - 1).toString()
                                validationMessage = null
                            }
                        },
                        enabled = (quantity ?: 1) > 1,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if ((quantity ?: 1) > 1) {
                                    StockMateOrange.copy(alpha = 0.15f)
                                } else {
                                    Color(0xFFE8E8E8)
                                },
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Decrease quantity",
                            tint = if ((quantity ?: 1) > 1) {
                                StockMateOrange
                            } else {
                                StockMateMuted
                            }
                        )
                    }

                    // Quantity input
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = {
                            quantityText = it.filter(Char::isDigit)
                            validationMessage = null
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = inventoryTextFieldColors()
                    )

                    // Increase quantity
                    IconButton(
                        onClick = {
                            val current = quantity ?: 0

                            if (current < Int.MAX_VALUE) {
                                quantityText = (current + 1).toString()
                                validationMessage = null
                            }
                        },
                        enabled = (quantity ?: 0) < Int.MAX_VALUE,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                StockMateOrange.copy(alpha = 0.15f),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Increase quantity",
                            tint = StockMateOrange
                        )
                    }
                }

                // Reason for adjustment
                OutlinedTextField(
                    value = reason,
                    onValueChange = {
                        reason = it
                        validationMessage = null
                    },
                    label = {
                        Text("Reason for adjustment")
                    },
                    placeholder = {
                        Text(
                            if (movementType == "ADD") {
                                "e.g. New stock received"
                            } else {
                                "e.g. Damaged products"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = inventoryTextFieldColors()
                )

                validationMessage?.let {
                    Text(
                        it,
                        color = StockMateDanger,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },

        confirmButton = {
            Button(
                onClick = {

                    validationMessage = when {
                        quantity == null || quantity <= 0 ->
                            "Enter a valid quantity greater than zero."

                        movementType == "REMOVE" &&
                                quantity > product.quantityInStock ->
                            "You cannot remove more than the available stock."

                        reason.isBlank() ->
                            "Please enter a reason for the adjustment."

                        else -> null
                    }

                    if (validationMessage == null) {
                        onSave(
                            quantity!!,
                            movementType,
                            reason.trim()
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = StockMateOrange,
                    contentColor = StockMateCharcoal
                )
            ) {
                Text("Save Adjustment")
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun inventoryTextFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = StockMateOrange,
        unfocusedBorderColor = Color(0xFF9E9EA5),
        focusedLabelColor = StockMateOrange,
        unfocusedLabelColor = StockMateMuted,
        cursorColor = StockMateOrange,
        focusedTextColor = StockMateCharcoal,
        unfocusedTextColor = StockMateCharcoal,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
    )
