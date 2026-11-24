
package com.example.stockmate.data.repository

import androidx.room.withTransaction
import com.example.stockmate.data.local.StockMateDatabase
import com.example.stockmate.data.local.entity.PurchaseEntity
import com.example.stockmate.data.local.entity.PurchaseItemEntity
import com.example.stockmate.data.local.entity.ProductEntity
import com.example.stockmate.data.local.entity.StockMovementEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import java.math.BigInteger

/**
 * Represents one product being added to a purchase.
 *
 * unitCost is stored in cents (LKR).
 */
data class PurchaseInputItem(
    val productId: Long,
    val quantity: Int,
    val unitCost: Long
)

class PurchaseRepository(
    private val database: StockMateDatabase
) {

    private val purchaseDao = database.purchaseDao()
    private val purchaseItemDao = database.purchaseItemDao()
    private val supplierDao = database.supplierDao()
    private val productDao = database.productDao()
    private val stockMovementDao = database.stockMovementDao()

    // ---------------------------------------------------------
    // PURCHASE FLOWS
    // ---------------------------------------------------------

    val allPurchases: Flow<List<PurchaseEntity>> =
        purchaseDao.getAllPurchases()

    val purchaseCount: Flow<Int> =
        purchaseDao.getPurchaseCount()

    val pendingPurchaseCount: Flow<Int> =
        purchaseDao.getPendingPurchaseCount()

    val totalReceivedPurchaseAmount: Flow<Long> =
        purchaseDao.getTotalReceivedPurchaseAmount()

    fun getPurchaseById(purchaseId: Long): Flow<PurchaseEntity?> {
        return purchaseDao.getPurchaseByIdFlow(purchaseId)
    }

    fun getPurchasesBySupplier(
        supplierId: Long
    ): Flow<List<PurchaseEntity>> {
        return purchaseDao.getPurchasesBySupplier(supplierId)
    }

    fun getPurchasesByStatus(
        status: String
    ): Flow<List<PurchaseEntity>> {
        return purchaseDao.getPurchasesByStatus(status)
    }

    fun getPurchaseItems(
        purchaseId: Long
    ): Flow<List<PurchaseItemEntity>> {
        return purchaseItemDao.getItemsByPurchase(purchaseId)
    }

    // ---------------------------------------------------------
    // CREATE PURCHASE
    // ---------------------------------------------------------

    suspend fun createPurchase(
        supplierId: Long,
        items: List<PurchaseInputItem>,
        notes: String = "",
        purchaseDate: Long = System.currentTimeMillis()
    ): Result<Long> {

        return try {

            val purchaseId = database.withTransaction {

                require(supplierId > 0) {
                    "Please select a valid supplier."
                }

                require(items.isNotEmpty()) {
                    "A purchase must contain at least one product."
                }

                // Avoid ambiguous duplicate product entries.
                require(items.map { it.productId }.distinct().size == items.size) {
                    "A product can only be added once to a purchase."
                }

                val supplier = supplierDao.getSupplierById(supplierId)

                require(supplier != null) {
                    "Supplier not found."
                }

                require(supplier.isActive) {
                    "This supplier is inactive."
                }

                var totalAmount = BigInteger.ZERO

                val purchaseItems = mutableListOf<PurchaseItemEntity>()

                items.forEach { item ->

                    require(item.productId > 0) {
                        "Invalid product selected."
                    }

                    require(item.quantity > 0) {
                        "Product quantity must be greater than zero."
                    }

                    require(item.unitCost >= 0L) {
                        "Purchase cost cannot be negative."
                    }

                    val product = productDao.getProductById(item.productId)

                    require(product != null) {
                        "A selected product could not be found."
                    }

                    require(product.isActive) {
                        "Product ${product.productName} is inactive."
                    }

                    val lineTotal = Math.multiplyExact(
                        item.unitCost,
                        item.quantity.toLong()
                    )

                    totalAmount = totalAmount.add(
                        BigInteger.valueOf(lineTotal)
                    )

                    purchaseItems.add(
                        PurchaseItemEntity(
                            purchaseId = 0L,
                            productId = item.productId,
                            quantity = item.quantity,
                            unitCost = item.unitCost,
                            lineTotal = lineTotal
                        )
                    )
                }

                require(
                    totalAmount <= BigInteger.valueOf(Long.MAX_VALUE)
                ) {
                    "Purchase total is too large."
                }

                val now = System.currentTimeMillis()

                val purchase = PurchaseEntity(
                    supplierId = supplierId,
                    purchaseDate = purchaseDate,
                    status = PurchaseEntity.STATUS_PENDING,
                    totalAmount = totalAmount.toLong(),
                    notes = notes.trim(),
                    createdAt = now,
                    updatedAt = now
                )

                val newPurchaseId =
                    purchaseDao.insertPurchase(purchase)

                val finalItems = purchaseItems.map { item ->
                    item.copy(purchaseId = newPurchaseId)
                }

                purchaseItemDao.insertPurchaseItems(finalItems)

                newPurchaseId
            }

            Result.success(purchaseId)

        } catch (exception: CancellationException) {

            throw exception

        } catch (exception: Exception) {

            Result.failure(exception)
        }
    }

    // ---------------------------------------------------------
    // RECEIVE PURCHASE
    // ---------------------------------------------------------

    suspend fun receivePurchase(
        purchaseId: Long
    ): Result<Unit> {

        return try {

            database.withTransaction {

                val purchase =
                    purchaseDao.getPurchaseById(purchaseId)

                require(purchase != null) {
                    "Purchase not found."
                }

                require(
                    purchase.status == PurchaseEntity.STATUS_PENDING
                ) {
                    "This purchase has already been received."
                }

                val items =
                    purchaseItemDao.getItemsByPurchaseOnce(purchaseId)

                require(items.isNotEmpty()) {
                    "This purchase contains no products."
                }

                val now = System.currentTimeMillis()

                items.forEach { item ->

                    val product =
                        productDao.getProductById(item.productId)

                    require(product != null) {
                        "A product in this purchase no longer exists."
                    }

                    val previousQuantity = product.quantityInStock

                    val newQuantity = Math.addExact(
                        previousQuantity,
                        item.quantity
                    )

                    // Calculate weighted average purchase cost.
                    val newAverageCost = calculateWeightedAverageCost(
                        currentQuantity = previousQuantity,
                        currentAverageCost = product.purchasePrice,
                        incomingQuantity = item.quantity,
                        incomingUnitCost = item.unitCost
                    )

                    // Update product stock and average purchase cost.
                    val updatedProduct = product.copy(
                        quantityInStock = newQuantity,
                        purchasePrice = newAverageCost,
                        updatedAt = now
                    )

                    productDao.updateProduct(updatedProduct)

                    // Record the stock movement.
                    stockMovementDao.insertMovement(
                        StockMovementEntity(
                            productId = product.productId,
                            movementType = "ADD",
                            quantityChange = item.quantity,
                            previousQuantity = previousQuantity,
                            newQuantity = newQuantity,
                            reason = "Purchase #$purchaseId received",
                            createdAt = now
                        )
                    )
                }

                // Mark the purchase as received only after
                // all inventory updates have succeeded.
                purchaseDao.updatePurchase(
                    purchase.copy(
                        status = PurchaseEntity.STATUS_RECEIVED,
                        receivedAt = now,
                        updatedAt = now
                    )
                )
            }

            Result.success(Unit)

        } catch (exception: CancellationException) {

            throw exception

        } catch (exception: Exception) {

            Result.failure(exception)
        }
    }

    // ---------------------------------------------------------
    // WEIGHTED AVERAGE COST CALCULATION
    // ---------------------------------------------------------

    private fun calculateWeightedAverageCost(
        currentQuantity: Int,
        currentAverageCost: Long,
        incomingQuantity: Int,
        incomingUnitCost: Long
    ): Long {

        require(currentQuantity >= 0) {
            "Current stock quantity cannot be negative."
        }

        require(incomingQuantity > 0) {
            "Incoming quantity must be greater than zero."
        }

        require(currentAverageCost >= 0L && incomingUnitCost >= 0L) {
            "Purchase costs cannot be negative."
        }

        // If there is no existing stock, use the incoming cost.
        if (currentQuantity == 0) {
            return incomingUnitCost
        }

        val totalQuantity = Math.addExact(
            currentQuantity,
            incomingQuantity
        )

        val existingValue = BigInteger.valueOf(currentAverageCost)
            .multiply(BigInteger.valueOf(currentQuantity.toLong()))

        val incomingValue = BigInteger.valueOf(incomingUnitCost)
            .multiply(BigInteger.valueOf(incomingQuantity.toLong()))

        val totalValue = existingValue.add(incomingValue)

        // Round to the nearest cent.
        val divisor = BigInteger.valueOf(totalQuantity.toLong())

        val roundedAverage = totalValue
            .add(divisor.divide(BigInteger.valueOf(2)))
            .divide(divisor)

        require(
            roundedAverage <= BigInteger.valueOf(Long.MAX_VALUE)
        ) {
            "Calculated purchase cost is too large."
        }

        return roundedAverage.toLong()
    }
}
