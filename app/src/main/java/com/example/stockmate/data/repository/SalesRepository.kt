
package com.example.stockmate.data.repository

import androidx.room.withTransaction
import com.example.stockmate.data.local.StockMateDatabase
import com.example.stockmate.data.local.entity.SaleEntity
import com.example.stockmate.data.local.entity.SaleItemEntity
import com.example.stockmate.data.local.entity.StockMovementEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import java.math.BigInteger
import com.example.stockmate.data.local.dao.ProductSalesSummary

// ---------------------------------------------------------
// SALE INPUT ITEM
// ---------------------------------------------------------

data class SaleInputItem(
    val productId: Long,
    val quantity: Int
)

// ---------------------------------------------------------
// SALES REPOSITORY
// ---------------------------------------------------------

class SalesRepository(
    private val database: StockMateDatabase
) {

    private val saleDao = database.saleDao()
    private val saleItemDao = database.saleItemDao()
    private val productDao = database.productDao()
    private val stockMovementDao = database.stockMovementDao()

    // -----------------------------------------------------
    // SALES DATA
    // -----------------------------------------------------

    val allSales = saleDao.getAllSales()

    val saleCount = saleDao.getSaleCount()

    val totalRevenue = saleDao.getTotalRevenue()

    val totalCost = saleDao.getTotalCost()

    val totalGrossProfit = saleDao.getTotalGrossProfit()

    // -----------------------------------------------------
    // GET SALE DETAILS
    // -----------------------------------------------------

    suspend fun getSaleById(
        saleId: Long
    ): SaleEntity? {
        return saleDao.getSaleById(saleId)
    }

    fun getSaleByIdFlow(
        saleId: Long
    ): Flow<SaleEntity?> {
        return saleDao.getSaleByIdFlow(saleId)
    }

    fun getSaleItems(
        saleId: Long
    ): Flow<List<SaleItemEntity>> {
        return saleItemDao.getItemsBySale(saleId)
    }

    suspend fun getSaleItemsOnce(
        saleId: Long
    ): List<SaleItemEntity> {
        return saleItemDao.getItemsBySaleOnce(saleId)
    }

    // -----------------------------------------------------
    // PRODUCT SALES ANALYTICS
    // -----------------------------------------------------

    fun getProductSalesSummary(
        startDate: Long,
        endDate: Long
    ): Flow<List<ProductSalesSummary>> {
        return saleItemDao.getProductSalesSummary(
            startDate = startDate,
            endDate = endDate
        )
    }

    fun getSalesByDateRange(
        startDate: Long,
        endDate: Long
    ): Flow<List<SaleEntity>> {
        return saleDao.getSalesByDateRange(
            startDate,
            endDate
        )
    }

    // -----------------------------------------------------
// SEARCH AND FILTER SALES
// -----------------------------------------------------

    fun getFilteredSales(
        searchQuery: String = "",
        paymentMethod: String = "",
        startDate: Long,
        endDate: Long
    ): Flow<List<SaleEntity>> {

        return saleDao.getFilteredSales(
            searchQuery = searchQuery.trim(),
            paymentMethod = paymentMethod,
            startDate = startDate,
            endDate = endDate
        )
    }

    // -----------------------------------------------------
    // CREATE SALE
    // -----------------------------------------------------

    suspend fun createSale(
        items: List<SaleInputItem>,
        customerName: String = "Walk-in Customer",
        customerPhone: String = "",
        paymentMethod: String = SaleEntity.PAYMENT_CASH,
        notes: String = "",
        saleDate: Long = System.currentTimeMillis()
    ): Result<Long> {

        return try {

            val saleId = database.withTransaction {

                // Validate sale items
                require(items.isNotEmpty()) {
                    "Add at least one product to the sale."
                }

                require(items.all { it.productId > 0L }) {
                    "Invalid product selected."
                }

                require(items.all { it.quantity > 0 }) {
                    "Product quantities must be greater than zero."
                }

                // Prevent duplicate products in the same sale
                require(
                    items.map { it.productId }.distinct().size ==
                            items.size
                ) {
                    "A product can only appear once in a sale. Update its quantity instead."
                }

                // Validate payment method
                val allowedPaymentMethods = setOf(
                    SaleEntity.PAYMENT_CASH,
                    SaleEntity.PAYMENT_CARD,
                    SaleEntity.PAYMENT_BANK_TRANSFER,
                    SaleEntity.PAYMENT_OTHER
                )

                require(paymentMethod in allowedPaymentMethods) {
                    "Invalid payment method."
                }

                // Calculate totals using BigInteger
                var totalAmount = BigInteger.ZERO
                var totalCost = BigInteger.ZERO

                val saleItems = mutableListOf<SaleItemEntity>()

                val productsToUpdate = mutableListOf<
                        Pair<
                                com.example.stockmate.data.local.entity.ProductEntity,
                                Int
                                >
                        >()

                // -----------------------------------------
                // VALIDATE PRODUCTS AND CALCULATE TOTALS
                // -----------------------------------------

                for (item in items) {

                    val product = productDao.getProductById(
                        item.productId
                    ) ?: throw IllegalArgumentException(
                        "A selected product could not be found."
                    )

                    require(product.isActive) {
                        "${product.productName} is inactive and cannot be sold."
                    }

                    require(product.sellingPrice >= 0L) {
                        "Invalid selling price for ${product.productName}."
                    }

                    require(product.purchasePrice >= 0L) {
                        "Invalid purchase cost for ${product.productName}."
                    }

                    require(
                        product.quantityInStock >= item.quantity
                    ) {
                        "Insufficient stock for ${product.productName}. Available: ${product.quantityInStock}."
                    }

                    // Historical selling price and cost
                    val unitSellingPrice = product.sellingPrice
                    val unitCost = product.purchasePrice

                    // Calculate line totals safely
                    val lineTotal = Math.multiplyExact(
                        unitSellingPrice,
                        item.quantity.toLong()
                    )

                    val costTotal = Math.multiplyExact(
                        unitCost,
                        item.quantity.toLong()
                    )

                    val lineProfit = Math.subtractExact(
                        lineTotal,
                        costTotal
                    )

                    totalAmount = totalAmount.add(
                        BigInteger.valueOf(lineTotal)
                    )

                    totalCost = totalCost.add(
                        BigInteger.valueOf(costTotal)
                    )

                    // Store historical sale item
                    saleItems.add(
                        SaleItemEntity(
                            saleId = 0L,
                            productId = product.productId,
                            quantity = item.quantity,
                            unitSellingPrice = unitSellingPrice,
                            unitCost = unitCost,
                            lineTotal = lineTotal,
                            costTotal = costTotal,
                            grossProfit = lineProfit
                        )
                    )

                    productsToUpdate.add(
                        product to item.quantity
                    )
                }

                // Ensure totals fit in Long
                val finalTotalAmount = totalAmount.longValueExact()
                val finalTotalCost = totalCost.longValueExact()

                val finalGrossProfit = Math.subtractExact(
                    finalTotalAmount,
                    finalTotalCost
                )

                // -----------------------------------------
                // CREATE SALE RECORD
                // -----------------------------------------

                val sale = SaleEntity(
                    customerName = customerName.trim()
                        .ifBlank { "Walk-in Customer" },
                    customerPhone = customerPhone.trim(),
                    saleDate = saleDate,
                    paymentMethod = paymentMethod,
                    totalAmount = finalTotalAmount,
                    totalCost = finalTotalCost,
                    grossProfit = finalGrossProfit,
                    notes = notes.trim(),
                    status = SaleEntity.STATUS_COMPLETED
                )

                val newSaleId = saleDao.insertSale(sale)

                // -----------------------------------------
                // SAVE SALE ITEMS
                // -----------------------------------------

                saleItemDao.insertSaleItems(
                    saleItems.map { item ->
                        item.copy(saleId = newSaleId)
                    }
                )

                // -----------------------------------------
                // DEDUCT INVENTORY AND RECORD MOVEMENTS
                // -----------------------------------------

                for ((product, quantitySold) in productsToUpdate) {

                    val previousQuantity = product.quantityInStock

                    val newQuantity = Math.subtractExact(
                        previousQuantity,
                        quantitySold
                    )

                    // Update product stock
                    productDao.updateProduct(
                        product.copy(
                            quantityInStock = newQuantity,
                            updatedAt = System.currentTimeMillis()
                        )
                    )

                    // Record stock movement
                    stockMovementDao.insertMovement(
                        StockMovementEntity(
                            productId = product.productId,
                            movementType = "REMOVE",
                            quantityChange = quantitySold,
                            previousQuantity = previousQuantity,
                            newQuantity = newQuantity,
                            reason = "Sale #$newSaleId completed"
                        )
                    )
                }

                newSaleId
            }

            Result.success(saleId)

        } catch (exception: CancellationException) {

            throw exception

        } catch (exception: Exception) {

            Result.failure(exception)
        }
    }
}
