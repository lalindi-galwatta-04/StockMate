package com.example.stockmate.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.StockMateApplication
import com.example.stockmate.data.local.entity.ProductEntity
import com.example.stockmate.data.local.entity.StockMovementEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventoryViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val app = application as StockMateApplication

    private val productRepository = app.productRepository
    private val inventoryRepository = app.inventoryRepository

    val products = productRepository.allProducts.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    // Number of active products currently managed by the business.
    val activeProductCount = products
        .map { productList ->
            productList.count { it.isActive }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            0
        )

    // Inventory value is calculated at purchase cost:
    // purchasePrice is stored in cents, so the result is also in cents.
    val inventoryValue = products
        .map { productList ->
            productList
                .filter { it.isActive }
                .sumOf { product ->
                    product.purchasePrice * product.quantityInStock.toLong()
                }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            0L
        )

    val lowStockProducts =
        inventoryRepository.lowStockProducts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    val outOfStockProducts =
        inventoryRepository.outOfStockProducts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    val totalStockQuantity =
        inventoryRepository.totalStockQuantity.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            0
        )

    val lowStockCount =
        inventoryRepository.lowStockCount.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            0
        )

    val outOfStockCount =
        inventoryRepository.outOfStockCount.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            0
        )

    val movements =
        inventoryRepository.allMovements.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList<StockMovementEntity>()
        )

    fun adjustStock(
        productId: Long,
        quantity: Int,
        movementType: String,
        reason: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                inventoryRepository.adjustStock(
                    productId = productId,
                    quantity = quantity,
                    movementType = movementType,
                    reason = reason
                )

                onResult(
                    true,
                    "Stock updated successfully."
                )

            } catch (exception: Exception) {
                onResult(
                    false,
                    exception.message ?: "Unable to update stock."
                )
            }
        }
    }
}
