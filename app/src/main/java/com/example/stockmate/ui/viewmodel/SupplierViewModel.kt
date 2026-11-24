package com.example.stockmate.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.StockMateApplication
import com.example.stockmate.data.local.entity.SupplierEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SupplierViewModel(application: Application) : AndroidViewModel(application) {

    private val repository =
        (application as StockMateApplication).supplierRepository

    val suppliers = repository.allSuppliers.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val supplierCount = repository.supplierCount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        0
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val searchResults = combine(_searchQuery, repository.allSuppliers) { query, all ->
        if (query.isBlank()) {
            all
        } else {
            all.filter { supplier ->
                supplier.name.contains(query, ignoreCase = true) ||
                        supplier.contactPerson.contains(query, ignoreCase = true) ||
                        supplier.phone.contains(query, ignoreCase = true) ||
                        supplier.email.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    private val _selectedSupplier = MutableStateFlow<SupplierEntity?>(null)
    val selectedSupplier: StateFlow<SupplierEntity?> = _selectedSupplier

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadSupplier(supplierId: Long) {
        viewModelScope.launch {
            _selectedSupplier.value = repository.getSupplierById(supplierId).first()
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun addSupplier(
        name: String,
        contactPerson: String,
        phone: String,
        email: String,
        address: String,
        notes: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.addSupplier(
                    name, contactPerson, phone, email, address, notes
                )
                _message.value = "Supplier added successfully."
                onComplete(true)
            } catch (exception: Exception) {
                _message.value = exception.message ?: "Unable to add supplier."
                onComplete(false)
            }
        }
    }

    fun updateSupplier(
        supplier: SupplierEntity,
        name: String,
        contactPerson: String,
        phone: String,
        email: String,
        address: String,
        notes: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.updateSupplier(
                    supplier.copy(
                        name = name,
                        contactPerson = contactPerson,
                        phone = phone,
                        email = email,
                        address = address,
                        notes = notes
                    )
                )
                _message.value = "Supplier updated successfully."
                onComplete(true)
            } catch (exception: Exception) {
                _message.value = exception.message ?: "Unable to update supplier."
                onComplete(false)
            }
        }
    }

    fun setSupplierActive(supplierId: Long, isActive: Boolean) {
        viewModelScope.launch {
            try {
                repository.setSupplierActive(supplierId, isActive)
                _message.value =
                    if (isActive) "Supplier activated." else "Supplier deactivated."
            } catch (exception: Exception) {
                _message.value = exception.message ?: "Unable to update supplier."
            }
        }
    }
}
