
package com.example.stockmate.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.StockMateApplication
import com.example.stockmate.data.local.entity.ProductEntity
import com.example.stockmate.data.local.entity.SaleEntity
import com.example.stockmate.data.local.entity.SaleItemEntity
import com.example.stockmate.data.repository.SaleInputItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SalesViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val applicationInstance =
        application as StockMateApplication

    private val salesRepository =
        applicationInstance.salesRepository

    private val productRepository =
        applicationInstance.productRepository

    // ---------------------------------------------------------
    // SALES DATA
    // ---------------------------------------------------------

    val sales: StateFlow<List<SaleEntity>> =
        salesRepository.allSales.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    val saleCount: StateFlow<Int> =
        salesRepository.saleCount.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            0
        )

    val totalRevenue: StateFlow<Long> =
        salesRepository.totalRevenue.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            0L
        )

    val totalCost: StateFlow<Long> =
        salesRepository.totalCost.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            0L
        )

    val totalGrossProfit: StateFlow<Long> =
        salesRepository.totalGrossProfit.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            0L
        )

    // ---------------------------------------------------------
    // PRODUCT DATA
    // ---------------------------------------------------------

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

    private val _selectedSaleId =
        MutableStateFlow<Long?>(null)

    val selectedSaleId: StateFlow<Long?> =
        _selectedSaleId.asStateFlow()

    // ---------------------------------------------------------
    // SEARCH AND FILTER STATE
    // ---------------------------------------------------------

    private val _searchQuery = MutableStateFlow("")

    val searchQuery: StateFlow<String> =
        _searchQuery.asStateFlow()

    private val _selectedPaymentMethod =
        MutableStateFlow("")

    val selectedPaymentMethod: StateFlow<String> =
        _selectedPaymentMethod.asStateFlow()

    private val _startDate = MutableStateFlow(0L)

    private val _endDate =
        MutableStateFlow(Long.MAX_VALUE)

    // ---------------------------------------------------------
    // FILTERED SALES
    // ---------------------------------------------------------

    private data class FilterValues(
        val query: String,
        val paymentMethod: String,
        val startDate: Long,
        val endDate: Long
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredSales: StateFlow<List<SaleEntity>> =
        combine(
            _searchQuery,
            _selectedPaymentMethod,
            _startDate,
            _endDate
        ) { query, paymentMethod, startDate, endDate ->

            FilterValues(
                query = query,
                paymentMethod = paymentMethod,
                startDate = startDate,
                endDate = endDate
            )

        }.flatMapLatest { filters ->

            salesRepository.getFilteredSales(
                searchQuery = filters.query,
                paymentMethod = filters.paymentMethod,
                startDate = filters.startDate,
                endDate = filters.endDate
            )

        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    // ---------------------------------------------------------
    // SALE DETAILS
    // ---------------------------------------------------------

    fun selectSale(saleId: Long) {
        _selectedSaleId.value = saleId
    }

    fun clearSelectedSale() {
        _selectedSaleId.value = null
    }

    fun getSaleById(
        saleId: Long
    ) = salesRepository.getSaleByIdFlow(saleId)

    fun getSaleItems(
        saleId: Long
    ) = salesRepository.getSaleItems(saleId)

    suspend fun getProductById(
        productId: Long
    ): ProductEntity? {
        return productRepository.getProductById(productId)
    }

    fun getSalesByDateRange(
        startDate: Long,
        endDate: Long
    ) = salesRepository.getSalesByDateRange(
        startDate,
        endDate
    )

    // ---------------------------------------------------------
    // SEARCH AND FILTER MANAGEMENT
    // ---------------------------------------------------------

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updatePaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
    }

    fun updateDateRange(
        startDate: Long,
        endDate: Long
    ) {
        if (startDate <= endDate) {
            _startDate.value = startDate
            _endDate.value = endDate
        }
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedPaymentMethod.value = ""
        _startDate.value = 0L
        _endDate.value = Long.MAX_VALUE
    }

    // ---------------------------------------------------------
    // CREATE SALE
    // ---------------------------------------------------------

    fun createSale(
        items: List<SaleInputItem>,
        customerName: String = "Walk-in Customer",
        customerPhone: String = "",
        paymentMethod: String = SaleEntity.PAYMENT_CASH,
        notes: String = "",
        saleDate: Long = System.currentTimeMillis(),
        onComplete: (Long?) -> Unit = {}
    ) {

        if (_isLoading.value) return

        viewModelScope.launch {

            _isLoading.value = true
            _message.value = null

            try {

                val result = salesRepository.createSale(
                    items = items,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    paymentMethod = paymentMethod,
                    notes = notes,
                    saleDate = saleDate
                )

                result.onSuccess { saleId ->

                    _message.value =
                        "Sale #$saleId completed successfully. Stock updated."

                    onComplete(saleId)
                }

                result.onFailure { exception ->

                    _message.value =
                        exception.message
                            ?: "Unable to complete sale."

                    onComplete(null)
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
