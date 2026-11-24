package com.example.stockmate.ui.screens.suppliers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stockmate.data.local.entity.SupplierEntity
import com.example.stockmate.ui.theme.StockMateBackground
import com.example.stockmate.ui.theme.StockMateCharcoal
import com.example.stockmate.ui.theme.StockMateMuted
import com.example.stockmate.ui.theme.StockMateOrange
import com.example.stockmate.ui.viewmodel.SupplierViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSupplierScreen(
    supplierId: Long?,
    onBack: () -> Unit,
    supplierViewModel: SupplierViewModel = viewModel()
) {
    val existingSupplier by supplierViewModel.selectedSupplier.collectAsState()
    val message by supplierViewModel.message.collectAsState()

    var name by remember { mutableStateOf("") }
    var contactPerson by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    var initialised by remember(supplierId) { mutableStateOf(supplierId == null) }

    LaunchedEffect(supplierId) {
        if (supplierId != null && supplierId > 0) {
            supplierViewModel.loadSupplier(supplierId)
        }
    }

    LaunchedEffect(existingSupplier, supplierId) {
        if (supplierId != null && supplierId > 0 && existingSupplier != null &&
            existingSupplier?.supplierId == supplierId && !initialised
        ) {
            name = existingSupplier?.name.orEmpty()
            contactPerson = existingSupplier?.contactPerson.orEmpty()
            phone = existingSupplier?.phone.orEmpty()
            email = existingSupplier?.email.orEmpty()
            address = existingSupplier?.address.orEmpty()
            notes = existingSupplier?.notes.orEmpty()
            initialised = true
        }
    }

    val isEditing = supplierId != null && supplierId > 0
    val canSave = name.isNotBlank() && (initialised || !isEditing)

    Scaffold(
        containerColor = StockMateBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditing) "Edit Supplier" else "Add Supplier", fontWeight = FontWeight.Bold, color = StockMateCharcoal)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = StockMateCharcoal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StockMateBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Supplier information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            SupplierField("Supplier name *", name, { name = it }, "Enter business or supplier name")
            SupplierField("Contact person", contactPerson, { contactPerson = it }, "Enter contact person's name")
            SupplierField("Phone number", phone, { phone = it }, "e.g. 0771234567")
            SupplierField("Email address", email, { email = it }, "e.g. supplier@example.com")
            SupplierField("Address", address, { address = it }, "Supplier's business address", minLines = 2)
            SupplierField("Notes", notes, { notes = it }, "Additional information", minLines = 3)

            validationError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    if (name.isBlank()) {
                        validationError = "Supplier name is required."
                    } else if (isEditing) {
                        val supplier = existingSupplier
                        if (supplier == null) {
                            validationError = "Supplier information is still loading. Please try again."
                        } else {
                            supplierViewModel.updateSupplier(
                                supplier, name, contactPerson, phone, email, address, notes
                            ) { success -> if (success) onBack() }
                        }
                    } else {
                        supplierViewModel.addSupplier(
                            name, contactPerson, phone, email, address, notes
                        ) { success -> if (success) onBack() }
                    }
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StockMateOrange,
                    contentColor = StockMateCharcoal
                )
            ) {
                Text(if (isEditing) "Save Changes" else "Save Supplier", fontWeight = FontWeight.Bold)
            }

            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", color = StockMateMuted)
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SupplierField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder, color = StockMateMuted) },
        minLines = minLines,
        maxLines = if (minLines == 1) 1 else 4,
        shape = RoundedCornerShape(12.dp)
    )
}
