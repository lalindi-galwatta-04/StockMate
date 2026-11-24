
package com.example.stockmate.data.repository

import androidx.room.withTransaction
import com.example.stockmate.data.local.StockMateDatabase
import com.example.stockmate.data.local.entity.StockMovementEntity
import kotlinx.coroutines.flow.Flow

class InventoryRepository(
    private val database: StockMateDatabase
) {

    private val productDao = database.productDao()
    private val movementDao = database.stockMovementDao()

    val allMovements: Flow<List<StockMovementEntity>> =
        movementDao.getAllMovements()

    val movementCount: Flow<Int> =
        movementDao.getMovementCount()

    val lowStockProducts = productDao.getLowStockProducts()

    val outOfStockProducts = productDao.getOutOfStockProducts()

    val totalStockQuantity = productDao.getTotalStockQuantity()

    val lowStockCount = productDao.getLowStockCount()

    val outOfStockCount = productDao.getOutOfStockCount()

    fun getMovementsByProduct(
        productId: Long
    ): Flow<List<StockMovementEntity>> {
        return movementDao.getMovementsByProduct(productId)
    }

    suspend fun adjustStock(
        productId: Long,
        quantity: Int,
        movementType: String,
        reason: String
    ) {

        require(quantity > 0) {
            "Adjustment quantity must be greater than zero."
        }

        require(movementType == "ADD" || movementType == "REMOVE") {
            "Invalid stock adjustment type."
        }

        require(reason.isNotBlank()) {
            "Please provide a reason for the adjustment."
        }

        database.withTransaction {

            val product = productDao.getProductById(productId)
                ?: throw IllegalArgumentException(
                    "Product not found."
                )

            require(product.isActive) {
                "This product is inactive."
            }

            val change = if (movementType == "ADD") {
                quantity
            } else {
                -quantity
            }

            val previousQuantity = product.quantityInStock

            val newQuantity = previousQuantity.toLong() +
                    change.toLong()

            require(
                newQuantity >= 0 &&
                        newQuantity <= Int.MAX_VALUE
            ) {
                "The adjustment would create an invalid stock quantity."
            }

            val updatedProduct = product.copy(
                quantityInStock = newQuantity.toInt(),
                updatedAt = System.currentTimeMillis()
            )

            productDao.updateProduct(updatedProduct)

            movementDao.insertMovement(
                StockMovementEntity(
                    productId = productId,
                    movementType = movementType,
                    quantityChange = change,
                    previousQuantity = previousQuantity,
                    newQuantity = newQuantity.toInt(),
                    reason = reason.trim()
                )
            )
        }
    }
}
