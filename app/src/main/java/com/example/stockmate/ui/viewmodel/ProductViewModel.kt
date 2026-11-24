
package com.example.stockmate.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.StockMateApplication
import com.example.stockmate.data.local.entity.CategoryEntity
import com.example.stockmate.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ProductFilter {
    ALL,
    LOW_STOCK,
    OUT_OF_STOCK
}

data class ProductsUiState(
    val products: List<ProductEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: ProductFilter = ProductFilter.ALL,
    val totalProducts: Int = 0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0
)

class ProductViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val app = application as StockMateApplication

    private val productRepository = app.productRepository
    private val categoryRepository = app.categoryRepository

    private val searchQuery = MutableStateFlow("")
    private val selectedFilter = MutableStateFlow(ProductFilter.ALL)

    val categories = categoryRepository.allCategories.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val uiState = combine(
        productRepository.allProducts,
        categories,
        searchQuery,
        selectedFilter,
        productRepository.lowStockCount,
        productRepository.outOfStockCount
    ) { values ->

        @Suppress("UNCHECKED_CAST")
        val products = values[0] as List<ProductEntity>

        @Suppress("UNCHECKED_CAST")
        val categoryList = values[1] as List<CategoryEntity>

        val query = values[2] as String
        val filter = values[3] as ProductFilter
        val lowStock = values[4] as Int
        val outOfStock = values[5] as Int

        val filteredProducts = products.filter { product ->

            val matchesSearch =
                product.productName.contains(query, ignoreCase = true) ||
                        product.sku.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                ProductFilter.ALL -> true

                ProductFilter.LOW_STOCK ->
                    product.isActive &&
                            product.quantityInStock > 0 &&
                            product.quantityInStock <= product.minimumStockLevel

                ProductFilter.OUT_OF_STOCK ->
                    product.isActive && product.quantityInStock <= 0
            }

            matchesSearch && matchesFilter
        }

        ProductsUiState(
            products = filteredProducts,
            categories = categoryList,
            searchQuery = query,
            selectedFilter = filter,
            totalProducts = products.count { it.isActive },
            lowStockCount = lowStock,
            outOfStockCount = outOfStock
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ProductsUiState()
    )

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun updateFilter(filter: ProductFilter) {
        selectedFilter.value = filter
    }

    fun addProduct(
        productName: String,
        sku: String,
        description: String,
        categoryId: Long?,
        purchasePrice: Long,
        sellingPrice: Long,
        quantity: Int,
        minimumStock: Int,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val product = ProductEntity(
                    productName = productName.trim(),
                    sku = sku.trim(),
                    description = description.trim(),
                    categoryId = categoryId,
                    purchasePrice = purchasePrice,
                    sellingPrice = sellingPrice,
                    quantityInStock = quantity,
                    minimumStockLevel = minimumStock
                )

                productRepository.addProduct(product)

                onResult(true, "Product added successfully.")
            } catch (exception: Exception) {
                onResult(
                    false,
                    exception.message ?: "Unable to add product."
                )
            }
        }
    }

    fun updateProduct(
        productId: Long,
        productName: String,
        sku: String,
        description: String,
        categoryId: Long?,
        purchasePrice: Long,
        sellingPrice: Long,
        minimumStock: Int,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val existing = productRepository.getProductById(productId)
                    ?: throw IllegalArgumentException("Product not found.")

                productRepository.updateProduct(
                    existing.copy(
                        productName = productName.trim(),
                        sku = sku.trim(),
                        description = description.trim(),
                        categoryId = categoryId,
                        purchasePrice = purchasePrice,
                        sellingPrice = sellingPrice,
                        minimumStockLevel = minimumStock
                    )
                )

                onResult(true, "Product updated successfully.")
            } catch (exception: Exception) {
                onResult(
                    false,
                    exception.message ?: "Unable to update product."
                )
            }
        }
    }

    fun deactivateProduct(
        productId: Long,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                productRepository.deactivateProduct(productId)
                onResult(true, "Product deactivated successfully.")
            } catch (exception: Exception) {
                onResult(
                    false,
                    exception.message ?: "Unable to deactivate product."
                )
            }
        }
    }

    fun reactivateProduct(
        productId: Long,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                productRepository.reactivateProduct(productId)
                onResult(true, "Product activated successfully.")
            } catch (exception: Exception) {
                onResult(
                    false,
                    exception.message ?: "Unable to activate product."
                )
            }
        }
    }
}
