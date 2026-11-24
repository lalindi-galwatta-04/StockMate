
package com.example.stockmate.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.StockMateApplication
import com.example.stockmate.data.local.entity.PurchaseEntity
import com.example.stockmate.data.local.entity.PurchaseItemEntity
import com.example.stockmate.data.repository.PurchaseInputItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PurchaseViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val applicationInstance =
        application as StockMateApplication

    private val purchaseRepository =
        applicationInstance.purchaseRepository

    private val supplierRepository =
        applicationInstance.supplierRepository

    private val productRepository =
        applicationInstance.productRepository

    // ---------------------------------------------------------
    // PURCHASE DATA
    // ---------------------------------------------------------

    val purchases: StateFlow<List<PurchaseEntity>> =
        purchaseRepository.allPurchases.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    val purchaseCount: StateFlow<Int> =
        purchaseRepository.purchaseCount.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            0
        )

    val pendingPurchaseCount: StateFlow<Int> =
        purchaseRepository.pendingPurchaseCount.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            0
        )

    val totalReceivedPurchaseAmount: StateFlow<Long> =
        purchaseRepository.totalReceivedPurchaseAmount.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            0L
        )

    // ---------------------------------------------------------
    // SUPPLIER AND PRODUCT DATA
    // ---------------------------------------------------------

    val suppliers = supplierRepository.activeSuppliers.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val products = productRepository.allProducts.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    // ---------------------------------------------------------
    // UI STATE
    // ---------------------------------------------------------

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> =
        _message.asStateFlow()

    private val _selectedPurchaseId =
        MutableStateFlow<Long?>(null)

    val selectedPurchaseId: StateFlow<Long?> =
        _selectedPurchaseId.asStateFlow()

    // ---------------------------------------------------------
    // PURCHASE DETAILS
    // ---------------------------------------------------------

    fun selectPurchase(purchaseId: Long) {
        _selectedPurchaseId.value = purchaseId
    }

    fun clearSelectedPurchase() {
        _selectedPurchaseId.value = null
    }

    fun getPurchaseById(
        purchaseId: Long
    ): Flow<PurchaseEntity?> {
        return purchaseRepository.getPurchaseById(purchaseId)
    }

    fun getPurchaseItems(
        purchaseId: Long
    ): Flow<List<PurchaseItemEntity>> {
        return purchaseRepository.getPurchaseItems(purchaseId)
    }

    fun getPurchasesBySupplier(
        supplierId: Long
    ): Flow<List<PurchaseEntity>> {
        return purchaseRepository.getPurchasesBySupplier(supplierId)
    }

    // ---------------------------------------------------------
    // CREATE PURCHASE
    // ---------------------------------------------------------

    fun createPurchase(
        supplierId: Long,
        items: List<PurchaseInputItem>,
        notes: String = "",
        purchaseDate: Long = System.currentTimeMillis(),
        onComplete: (Long?) -> Unit = {}
    ) {

        if (_isLoading.value) return

        viewModelScope.launch {

            _isLoading.value = true
            _message.value = null

            try {

                val result = purchaseRepository.createPurchase(
                    supplierId = supplierId,
                    items = items,
                    notes = notes,
                    purchaseDate = purchaseDate
                )

                result.onSuccess { purchaseId ->

                    _message.value =
                        "Purchase #$purchaseId created successfully."

                    // Return the actual newly created purchase ID.
                    onComplete(purchaseId)
                }

                result.onFailure { exception ->

                    _message.value =
                        exception.message
                            ?: "Unable to create purchase."

                    onComplete(null)
                }

            } finally {
                _isLoading.value = false
            }
        }
    }

    // ---------------------------------------------------------
    // RECEIVE PURCHASE
    // ---------------------------------------------------------

    fun receivePurchase(
        purchaseId: Long,
        onComplete: (Boolean) -> Unit = {}
    ) {

        if (_isLoading.value) return

        viewModelScope.launch {

            _isLoading.value = true
            _message.value = null

            try {

                val result =
                    purchaseRepository.receivePurchase(purchaseId)

                result.onSuccess {

                    _message.value =
                        "Purchase received successfully. Stock updated."

                    onComplete(true)
                }

                result.onFailure { exception ->

                    _message.value =
                        exception.message
                            ?: "Unable to receive purchase."

                    onComplete(false)
                }

            } finally {
                _isLoading.value = false
            }
        }
    }

    // ---------------------------------------------------------
    // MESSAGE MANAGEMENT
    // ---------------------------------------------------------

    fun clearMessage() {
        _message.value = null
    }
}
