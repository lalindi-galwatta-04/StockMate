
package com.example.stockmate.ui.screens.suppliers

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.Scaffold
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
import com.example.stockmate.data.local.entity.SupplierEntity
import com.example.stockmate.ui.theme.StockMateBackground
import com.example.stockmate.ui.theme.StockMateCharcoal
import com.example.stockmate.ui.theme.StockMateDanger
import com.example.stockmate.ui.theme.StockMateMuted
import com.example.stockmate.ui.theme.StockMateOrange
import com.example.stockmate.ui.theme.StockMateSuccess
import com.example.stockmate.ui.viewmodel.SupplierViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierListScreen(
    onBack: () -> Unit,
    onAddSupplier: () -> Unit,
    onEditSupplier: (Long) -> Unit,
    supplierViewModel: SupplierViewModel = viewModel()
) {
    val query by supplierViewModel.searchQuery.collectAsState()
    val suppliers by supplierViewModel.suppliers.collectAsState()
    val results by supplierViewModel.searchResults.collectAsState()
    val count by supplierViewModel.supplierCount.collectAsState()
    val message by supplierViewModel.message.collectAsState()

    var supplierToToggle by remember {
        mutableStateOf<SupplierEntity?>(null)
    }

    val visibleSuppliers: List<SupplierEntity> =
        if (query.isBlank()) suppliers else results

    Scaffold(
        containerColor = StockMateBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Suppliers",
                            fontWeight = FontWeight.Bold,
                            color = StockMateCharcoal
                        )

                        Text(
                            "$count suppliers registered",
                            style = MaterialTheme.typography.bodySmall,
                            color = StockMateMuted
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
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {

            // SEARCH SUPPLIERS
            OutlinedTextField(
                value = query,
                onValueChange = supplierViewModel::updateSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = {
                    Text("Search suppliers")
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null
                    )
                },
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.height(14.dp))

            // ADD SUPPLIER BUTTON
            Button(
                onClick = onAddSupplier,
                modifier = Modifier.fillMaxWidth(),
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

                Text(
                    "  Add Supplier",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(14.dp))

            // SUPPLIER LIST
            if (visibleSuppliers.isEmpty()) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Business,
                            contentDescription = null,
                            tint = StockMateMuted,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            if (query.isBlank()) {
                                "No suppliers added yet"
                            } else {
                                "No matching suppliers"
                            }
                        )

                        Text(
                            if (query.isBlank()) {
                                "Add your first supplier to get started."
                            } else {
                                "Try another search."
                            },
                            color = StockMateMuted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

            } else {

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = visibleSuppliers,
                        key = { supplier: SupplierEntity ->
                            supplier.supplierId
                        }
                    ) { supplier ->

                        SupplierCard(
                            supplier = supplier,
                            onEdit = {
                                onEditSupplier(supplier.supplierId)
                            },
                            onToggle = {
                                supplierToToggle = supplier
                            }
                        )
                    }
                }
            }
        }
    }

    // ACTIVATE / DEACTIVATE CONFIRMATION
    supplierToToggle?.let { supplier ->

        AlertDialog(
            onDismissRequest = {
                supplierToToggle = null
            },

            title = {
                Text(
                    if (supplier.isActive) {
                        "Deactivate supplier?"
                    } else {
                        "Activate supplier?"
                    }
                )
            },

            text = {
                Text(
                    if (supplier.isActive) {
                        "This supplier will remain in your records but won't be available for new purchases."
                    } else {
                        "This supplier will become available for new purchases again."
                    }
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        supplierViewModel.setSupplierActive(
                            supplier.supplierId,
                            !supplier.isActive
                        )

                        supplierToToggle = null
                    }
                ) {
                    Text(
                        text = if (supplier.isActive) {
                            "Deactivate"
                        } else {
                            "Activate"
                        },
                        color = if (supplier.isActive) {
                            StockMateDanger
                        } else {
                            StockMateSuccess
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        supplierToToggle = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // RESULT MESSAGE
    message?.let { result ->

        AlertDialog(
            onDismissRequest = supplierViewModel::clearMessage,
            title = {
                Text("StockMate")
            },
            text = {
                Text(result)
            },
            confirmButton = {
                TextButton(
                    onClick = supplierViewModel::clearMessage
                ) {
                    Text("OK")
                }
            }
        )
    }
}

// --------------------------------------------------
// SUPPLIER CARD
// --------------------------------------------------

@Composable
private fun SupplierCard(
    supplier: SupplierEntity,
    onEdit: () -> Unit,
    onToggle: () -> Unit
) {
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
                        .size(44.dp)
                        .background(
                            StockMateOrange.copy(alpha = 0.13f),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Business,
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
                        supplier.name,
                        fontWeight = FontWeight.Bold,
                        color = StockMateCharcoal
                    )

                    Text(
                        supplier.contactPerson.ifBlank {
                            supplier.phone.ifBlank {
                                "No contact details"
                            }
                        },
                        color = StockMateMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // ACTIVE / INACTIVE STATUS
                Text(
                    if (supplier.isActive) "Active" else "Inactive",
                    color = if (supplier.isActive) {
                        StockMateSuccess
                    } else {
                        StockMateMuted
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // PHONE NUMBER
            if (supplier.phone.isNotBlank()) {
                Spacer(Modifier.height(8.dp))

                Text(
                    "Phone: ${supplier.phone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = StockMateMuted
                )
            }

            // EMAIL
            if (supplier.email.isNotBlank()) {
                Text(
                    "Email: ${supplier.email}",
                    style = MaterialTheme.typography.bodySmall,
                    color = StockMateMuted
                )
            }

            Spacer(Modifier.height(10.dp))

            // EDIT / ACTIVATE / DEACTIVATE ACTIONS
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                TextButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = StockMateOrange
                    )

                    Text(
                        "  Edit",
                        color = StockMateOrange,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                TextButton(onClick = onToggle) {
                    Text(
                        text = if (supplier.isActive) {
                            "Deactivate"
                        } else {
                            "Activate"
                        },
                        color = if (supplier.isActive) {
                            StockMateDanger
                        } else {
                            StockMateSuccess
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
