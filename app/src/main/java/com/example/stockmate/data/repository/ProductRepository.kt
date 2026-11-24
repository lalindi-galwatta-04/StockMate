
package com.example.stockmate.data.repository

import com.example.stockmate.data.local.dao.ProductDao
import com.example.stockmate.data.local.entity.ProductEntity
import com.example.stockmate.data.local.relation.ProductWithCategory
import kotlinx.coroutines.flow.Flow

class ProductRepository(
    private val productDao: ProductDao
) {
    val allProducts: Flow<List<ProductEntity>> =
        productDao.getAllProducts()

    val productsWithCategories:
            Flow<List<ProductWithCategory>> =
        productDao.getProductsWithCategories()

    val lowStockProducts: Flow<List<ProductEntity>> =
        productDao.getLowStockProducts()

    val outOfStockProducts: Flow<List<ProductEntity>> =
        productDao.getOutOfStockProducts()

    val productCount: Flow<Int> =
        productDao.getProductCount()

    val lowStockCount: Flow<Int> =
        productDao.getLowStockCount()

    val outOfStockCount: Flow<Int> =
        productDao.getOutOfStockCount()

    val totalStockQuantity: Flow<Int> =
        productDao.getTotalStockQuantity()

    fun searchProducts(query: String): Flow<List<ProductEntity>> {
        return productDao.searchProducts(query.trim())
    }

    fun getProductsByCategory(
        categoryId: Long
    ): Flow<List<ProductEntity>> {
        return productDao.getProductsByCategory(categoryId)
    }

    suspend fun getProductById(
        productId: Long
    ): ProductEntity? {
        return productDao.getProductById(productId)
    }

    suspend fun addProduct(
        product: ProductEntity
    ): Long {
        validateProduct(product)

        val cleanSku = product.sku.trim()

        require(!productDao.isSkuTaken(cleanSku)) {
            "A product with this SKU already exists."
        }

        return productDao.insertProduct(
            product.copy(
                productId = 0,
                productName = product.productName.trim(),
                sku = cleanSku,
                description = product.description.trim(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateProduct(
        product: ProductEntity
    ) {
        validateProduct(product)

        require(product.productId > 0) {
            "Invalid product ID."
        }

        val existing = productDao.getProductById(
            product.productId
        )

        require(existing != null) {
            "Product not found."
        }

        val cleanSku = product.sku.trim()

        require(
            !productDao.isSkuTaken(
                cleanSku,
                product.productId
            )
        ) {
            "A product with this SKU already exists."
        }

        productDao.updateProduct(
            product.copy(
                productName = product.productName.trim(),
                sku = cleanSku,
                description = product.description.trim(),
                createdAt = existing.createdAt,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deactivateProduct(productId: Long) {
        val product = productDao.getProductById(productId)
            ?: throw IllegalArgumentException(
                "Product not found."
            )

        productDao.updateProduct(
            product.copy(
                isActive = false,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun reactivateProduct(productId: Long) {
        val product = productDao.getProductById(productId)
            ?: throw IllegalArgumentException(
                "Product not found."
            )

        productDao.updateProduct(
            product.copy(
                isActive = true,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    private fun validateProduct(product: ProductEntity) {
        require(product.productName.isNotBlank()) {
            "Product name cannot be empty."
        }

        require(product.sku.isNotBlank()) {
            "Product SKU cannot be empty."
        }

        require(product.purchasePrice >= 0) {
            "Purchase price cannot be negative."
        }

        require(product.sellingPrice >= 0) {
            "Selling price cannot be negative."
        }

        require(product.quantityInStock >= 0) {
            "Stock quantity cannot be negative."
        }

        require(product.minimumStockLevel >= 0) {
            "Minimum stock level cannot be negative."
        }
    }
}
