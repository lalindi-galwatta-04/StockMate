
package com.example.stockmate.ui.screens.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stockmate.data.local.entity.CategoryEntity
import com.example.stockmate.data.local.entity.ProductEntity
import com.example.stockmate.ui.theme.StockMateBackground
import com.example.stockmate.ui.theme.StockMateCharcoal
import com.example.stockmate.ui.theme.StockMateDanger
import com.example.stockmate.ui.theme.StockMateMuted
import com.example.stockmate.ui.theme.StockMateOrange
import com.example.stockmate.ui.theme.StockMateSuccess
import com.example.stockmate.ui.viewmodel.ProductFilter
import com.example.stockmate.ui.viewmodel.ProductViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

private val StockMateWarningColor = Color(0xFFE58A25)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    onManageCategories: () -> Unit = {},
    productViewModel: ProductViewModel = viewModel()
) {
    val uiState by productViewModel.uiState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }

    var showDeactivateDialog by remember { mutableStateOf(false) }
    var showReactivateDialog by remember { mutableStateOf(false) }

    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = StockMateBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Products",
                            fontWeight = FontWeight.Bold,
                            color = StockMateCharcoal
                        )
                        Text(
                            "Manage your products and prices",
                            style = MaterialTheme.typography.bodySmall,
                            color = StockMateMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StockMateBackground
                ),
                actions = {
                    IconButton(onClick = onManageCategories) {
                        Icon(
                            Icons.Default.Category,
                            contentDescription = "Manage categories",
                            tint = StockMateOrange
                        )
                    }

                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add product",
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
            ProductSummary(
                total = uiState.totalProducts,
                lowStock = uiState.lowStockCount,
                outOfStock = uiState.outOfStockCount
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = productViewModel::updateSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search product or SKU") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                productViewModel.updateSearchQuery("")
                            }
                        ) {
                            Icon(Icons.Default.Close, "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = stockMateTextFieldColors()
            )

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedFilter == ProductFilter.ALL,
                    onClick = {
                        productViewModel.updateFilter(ProductFilter.ALL)
                    },
                    label = { Text("All") }
                )

                FilterChip(
                    selected = uiState.selectedFilter == ProductFilter.LOW_STOCK,
                    onClick = {
                        productViewModel.updateFilter(ProductFilter.LOW_STOCK)
                    },
                    label = { Text("Low stock") }
                )

                FilterChip(
                    selected = uiState.selectedFilter == ProductFilter.OUT_OF_STOCK,
                    onClick = {
                        productViewModel.updateFilter(ProductFilter.OUT_OF_STOCK)
                    },
                    label = { Text("Out of stock") }
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Product List",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "${uiState.products.size} products",
                    style = MaterialTheme.typography.bodySmall,
                    color = StockMateMuted
                )
            }

            Spacer(Modifier.height(10.dp))

            if (uiState.products.isEmpty()) {
                EmptyProductsState()
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = uiState.products,
                        key = { it.productId }
                    ) { product ->
                        ProductCard(
                            product = product,
                            onEdit = {
                                editingProduct = product
                            },
                            onDeactivate = {
                                selectedProduct = product
                                showDeactivateDialog = true
                            },
                            onReactivate = {
                                selectedProduct = product
                                showReactivateDialog = true
                            }
                        )
                    }

                    item {
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StockMateOrange,
                    contentColor = StockMateCharcoal
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add New Product", fontWeight = FontWeight.Bold)
            }
        }
    }

    // ADD PRODUCT
    if (showAddDialog) {
        AddProductDialog(
            categories = uiState.categories,
            initialProduct = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, sku, description, categoryId,
                       purchase, selling, quantity, minimum ->

                productViewModel.addProduct(
                    productName = name,
                    sku = sku,
                    description = description,
                    categoryId = categoryId,
                    purchasePrice = purchase,
                    sellingPrice = selling,
                    quantity = quantity,
                    minimumStock = minimum
                ) { success, resultMessage ->
                    message = resultMessage
                    if (success) showAddDialog = false
                }
            }
        )
    }

    // EDIT PRODUCT
    editingProduct?.let { product ->
        AddProductDialog(
            categories = uiState.categories,
            initialProduct = product,
            onDismiss = { editingProduct = null },
            onSave = { name, sku, description, categoryId,
                       purchase, selling, _, minimum ->

                productViewModel.updateProduct(
                    productId = product.productId,
                    productName = name,
                    sku = sku,
                    description = description,
                    categoryId = categoryId,
                    purchasePrice = purchase,
                    sellingPrice = selling,
                    minimumStock = minimum
                ) { success, resultMessage ->
                    message = resultMessage
                    if (success) editingProduct = null
                }
            }
        )
    }

    // DEACTIVATE CONFIRMATION
    if (showDeactivateDialog && selectedProduct != null) {
        AlertDialog(
            onDismissRequest = {
                showDeactivateDialog = false
                selectedProduct = null
            },
            title = {
                Text("Deactivate product?")
            },
            text = {
                Text(
                    "Are you sure you want to deactivate " +
                            "${selectedProduct?.productName}? " +
                            "The product will remain in your product list " +
                            "but will no longer be active."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedProduct?.let { product ->
                            productViewModel.deactivateProduct(
                                product.productId
                            ) { success, resultMessage ->
                                message = resultMessage
                            }
                        }

                        showDeactivateDialog = false
                        selectedProduct = null
                    }
                ) {
                    Text("Deactivate", color = StockMateDanger)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeactivateDialog = false
                        selectedProduct = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // REACTIVATE CONFIRMATION
    if (showReactivateDialog && selectedProduct != null) {
        AlertDialog(
            onDismissRequest = {
                showReactivateDialog = false
                selectedProduct = null
            },
            title = {
                Text("Reactivate product?")
            },
            text = {
                Text(
                    "Do you want to reactivate " +
                            "${selectedProduct?.productName}? " +
                            "It will become available as an active product again."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedProduct?.let { product ->
                            productViewModel.reactivateProduct(
                                product.productId
                            ) { success, resultMessage ->
                                message = resultMessage
                            }
                        }

                        showReactivateDialog = false
                        selectedProduct = null
                    }
                ) {
                    Text(
                        "Activate",
                        color = StockMateSuccess
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showReactivateDialog = false
                        selectedProduct = null
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
            onDismissRequest = { message = null },
            title = { Text("StockMate") },
            text = { Text(resultMessage) },
            confirmButton = {
                TextButton(onClick = { message = null }) {
                    Text("OK")
                }
            }
        )
    }
}

// --------------------------------------------------
// PRODUCT SUMMARY
// --------------------------------------------------

@Composable
private fun ProductSummary(
    total: Int,
    lowStock: Int,
    outOfStock: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        SummaryCard(
            "Products",
            total.toString(),
            Modifier.weight(1f),
            StockMateCharcoal
        )

        SummaryCard(
            "Low Stock",
            lowStock.toString(),
            Modifier.weight(1f),
            StockMateWarningColor
        )

        SummaryCard(
            "Out of Stock",
            outOfStock.toString(),
            Modifier.weight(1f),
            StockMateDanger
        )
    }
}

@Composable
private fun SummaryCard(
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
        Column(Modifier.padding(12.dp)) {
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

// --------------------------------------------------
// PRODUCT CARD
// --------------------------------------------------

@Composable
private fun ProductCard(
    product: ProductEntity,
    onEdit: () -> Unit,
    onDeactivate: () -> Unit,
    onReactivate: () -> Unit
) {
    val stockColor = when {
        product.quantityInStock <= 0 -> StockMateDanger
        product.quantityInStock <= product.minimumStockLevel ->
            StockMateWarningColor
        else -> StockMateSuccess
    }

    val stockLabel = when {
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
        Column(Modifier.padding(15.dp)) {

            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
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

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        product.productName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        "SKU: ${product.sku}",
                        style = MaterialTheme.typography.bodySmall,
                        color = StockMateMuted
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        stockLabel,
                        color = stockColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = if (product.isActive) "Active" else "Inactive",
                        color = if (product.isActive) {
                            StockMateSuccess
                        } else {
                            StockMateMuted
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProductDetail(
                    "Purchase price",
                    formatLkr(product.purchasePrice)
                )

                ProductDetail(
                    "Selling price",
                    formatLkr(product.sellingPrice)
                )

                ProductDetail(
                    "Stock",
                    product.quantityInStock.toString()
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Minimum stock: ${product.minimumStockLevel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = StockMateMuted
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = StockMateOrange
                        )

                        Spacer(Modifier.width(4.dp))

                        Text(
                            "Edit",
                            color = StockMateOrange
                        )
                    }

                    if (product.isActive) {
                        TextButton(onClick = onDeactivate) {
                            Text(
                                "Deactivate",
                                color = StockMateDanger
                            )
                        }
                    } else {
                        TextButton(onClick = onReactivate) {
                            Text(
                                "Activate",
                                color = StockMateSuccess
                            )
                        }
                    }
                }
            }
        }
    }
}

// --------------------------------------------------
// PRODUCT DETAIL
// --------------------------------------------------

@Composable
private fun ProductDetail(
    label: String,
    value: String
) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = StockMateMuted
        )

        Spacer(Modifier.height(4.dp))

        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = StockMateCharcoal
        )
    }
}

// --------------------------------------------------
// EMPTY STATE
// --------------------------------------------------

@Composable
private fun ColumnScope.EmptyProductsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Inventory2,
            contentDescription = null,
            modifier = Modifier.size(55.dp),
            tint = StockMateMuted
        )

        Spacer(Modifier.height(14.dp))

        Text(
            "No products found",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(5.dp))

        Text(
            "Add your first product to get started.",
            color = StockMateMuted,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

// --------------------------------------------------
// ADD / EDIT PRODUCT DIALOG
// --------------------------------------------------

@Composable
private fun AddProductDialog(
    categories: List<CategoryEntity>,
    initialProduct: ProductEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        String, String, String, Long?,
        Long, Long, Int, Int
    ) -> Unit
) {
    var name by remember(initialProduct) {
        mutableStateOf(initialProduct?.productName.orEmpty())
    }

    var sku by remember(initialProduct) {
        mutableStateOf(initialProduct?.sku.orEmpty())
    }

    var description by remember(initialProduct) {
        mutableStateOf(initialProduct?.description.orEmpty())
    }

    var purchasePrice by remember(initialProduct) {
        mutableStateOf(
            initialProduct?.let {
                BigDecimal.valueOf(it.purchasePrice, 2).toPlainString()
            }.orEmpty()
        )
    }

    var sellingPrice by remember(initialProduct) {
        mutableStateOf(
            initialProduct?.let {
                BigDecimal.valueOf(it.sellingPrice, 2).toPlainString()
            }.orEmpty()
        )
    }

    var quantity by remember(initialProduct) {
        mutableIntStateOf(initialProduct?.quantityInStock ?: 0)
    }

    var minimumStock by remember(initialProduct) {
        mutableIntStateOf(initialProduct?.minimumStockLevel ?: 5)
    }

    var selectedCategory by remember(initialProduct, categories) {
        mutableStateOf(
            categories.firstOrNull {
                it.categoryId == initialProduct?.categoryId
            }
        )
    }

    var categoryExpanded by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initialProduct == null) "Add New Product" else "Edit Product",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.height(490.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        FormTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = "Product name"
                        )
                    }

                    item {
                        FormTextField(
                            value = sku,
                            onValueChange = { sku = it },
                            label = "SKU / Product code"
                        )
                    }

                    item {
                        FormTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = "Description (optional)"
                        )
                    }

                    item {
                        Box {
                            OutlinedButton(
                                onClick = { categoryExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    selectedCategory?.name
                                        ?: "Select category (optional)",
                                    color = StockMateCharcoal
                                )

                                Spacer(Modifier.weight(1f))

                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Choose category",
                                    tint = StockMateOrange
                                )
                            }

                            DropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = {
                                    categoryExpanded = false
                                }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("No category") },
                                    onClick = {
                                        selectedCategory = null
                                        categoryExpanded = false
                                    }
                                )

                                categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category.name) },
                                        onClick = {
                                            selectedCategory = category
                                            categoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        FormTextField(
                            value = purchasePrice,
                            onValueChange = { purchasePrice = it },
                            label = "Purchase price (LKR)",
                            keyboardType = KeyboardType.Decimal
                        )
                    }

                    item {
                        FormTextField(
                            value = sellingPrice,
                            onValueChange = { sellingPrice = it },
                            label = "Selling price (LKR)",
                            keyboardType = KeyboardType.Decimal
                        )
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (initialProduct == null) {
                                QuantityStepper(
                                    label = "Initial stock",
                                    value = quantity,
                                    onValueChange = { quantity = it },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            Color.White,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        "Current stock",
                                        color = StockMateMuted,
                                        style = MaterialTheme.typography.labelMedium
                                    )

                                    Text(
                                        initialProduct.quantityInStock.toString(),
                                        color = StockMateCharcoal,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Text(
                                        "Manage in Inventory",
                                        color = StockMateMuted,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                            QuantityStepper(
                                label = "Minimum stock",
                                value = minimumStock,
                                onValueChange = { minimumStock = it },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        validationMessage?.let {
                            Text(
                                it,
                                color = StockMateDanger,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val purchase = parseLkrToCents(purchasePrice)
                    val selling = parseLkrToCents(sellingPrice)

                    validationMessage = when {
                        name.isBlank() ->
                            "Enter a product name."

                        sku.isBlank() ->
                            "Enter a SKU."

                        purchase == null ->
                            "Enter a valid purchase price (up to 2 decimal places)."

                        selling == null ->
                            "Enter a valid selling price (up to 2 decimal places)."

                        else -> null
                    }

                    if (validationMessage == null) {
                        onSave(
                            name,
                            sku,
                            description,
                            selectedCategory?.categoryId,
                            purchase!!,
                            selling!!,
                            quantity,
                            minimumStock
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = StockMateOrange,
                    contentColor = StockMateCharcoal
                )
            ) {
                Text(
                    if (initialProduct == null) "Save Product"
                    else "Save Changes"
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// --------------------------------------------------
// QUANTITY STEPPER
// --------------------------------------------------

@Composable
private fun QuantityStepper(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = StockMateMuted
        )

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(horizontal = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = {
                    if (value > 0) onValueChange(value - 1)
                },
                enabled = value > 0
            ) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = "Decrease $label",
                    tint = if (value > 0) StockMateOrange else StockMateMuted
                )
            }

            Text(
                value.toString(),
                fontWeight = FontWeight.Bold,
                color = StockMateCharcoal,
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(
                onClick = {
                    if (value < Int.MAX_VALUE) {
                        onValueChange(value + 1)
                    }
                },
                enabled = value < Int.MAX_VALUE
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Increase $label",
                    tint = StockMateOrange
                )
            }
        }
    }
}

// --------------------------------------------------
// FORM TEXT FIELD
// --------------------------------------------------

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        shape = RoundedCornerShape(12.dp),
        colors = stockMateTextFieldColors()
    )
}

// --------------------------------------------------
// TEXT FIELD COLORS
// --------------------------------------------------

@Composable
private fun stockMateTextFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = StockMateOrange,
        unfocusedBorderColor = Color(0xFFB8B8BE),
        focusedLabelColor = StockMateOrange,
        unfocusedLabelColor = StockMateMuted,
        cursorColor = StockMateOrange,
        focusedTextColor = StockMateCharcoal,
        unfocusedTextColor = StockMateCharcoal,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
    )

// --------------------------------------------------
// CURRENCY FORMATTING
// --------------------------------------------------

private fun parseLkrToCents(value: String): Long? {
    return try {
        val amount = BigDecimal(value.trim())

        if (amount < BigDecimal.ZERO) {
            null
        } else {
            amount
                .setScale(2, RoundingMode.UNNECESSARY)
                .movePointRight(2)
                .longValueExact()
        }
    } catch (exception: Exception) {
        null
    }
}

private fun formatLkr(cents: Long): String {
    val amount = BigDecimal.valueOf(cents, 2)

    return "Rs. " + String.format(
        Locale.US,
        "%,.2f",
        amount
    )
}
