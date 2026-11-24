
package com.example.stockmate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stockmate.data.local.entity.SaleItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * Aggregated sales performance for an individual product.
 * Monetary values are stored in cents (LKR).
 */
data class ProductSalesSummary(
    val productId: Long,
    val productName: String,
    val sku: String,
    val unitsSold: Long,
    val revenue: Long,
    val grossProfit: Long
)

@Dao
interface SaleItemDao {

    // Insert sale items
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSaleItems(
        items: List<SaleItemEntity>
    ): List<Long>

    // Retrieve items belonging to a sale
    @Query("""
        SELECT * FROM sale_items
        WHERE saleId = :saleId
        ORDER BY saleItemId ASC
    """)
    fun getItemsBySale(
        saleId: Long
    ): Flow<List<SaleItemEntity>>

    // Retrieve sale items once
    @Query("""
        SELECT * FROM sale_items
        WHERE saleId = :saleId
        ORDER BY saleItemId ASC
    """)
    suspend fun getItemsBySaleOnce(
        saleId: Long
    ): List<SaleItemEntity>

    // Count items in a sale
    @Query("""
        SELECT COUNT(*) FROM sale_items
        WHERE saleId = :saleId
    """)
    suspend fun getItemCount(saleId: Long): Int

    // Calculate sale total from its items
    @Query("""
        SELECT COALESCE(SUM(lineTotal), 0)
        FROM sale_items
        WHERE saleId = :saleId
    """)
    suspend fun getSaleTotal(saleId: Long): Long

    // Retrieve best-selling products within a date range
    @Query("""
        SELECT
            p.productId AS productId,
            p.productName AS productName,
            p.sku AS sku,
            COALESCE(SUM(si.quantity), 0) AS unitsSold,
            COALESCE(SUM(si.lineTotal), 0) AS revenue,
            COALESCE(SUM(si.grossProfit), 0) AS grossProfit
        FROM sale_items AS si
        INNER JOIN sales AS s
            ON si.saleId = s.saleId
        INNER JOIN products AS p
            ON si.productId = p.productId
        WHERE
            s.status = 'COMPLETED'
            AND s.saleDate BETWEEN :startDate AND :endDate
        GROUP BY
            p.productId,
            p.productName,
            p.sku
        ORDER BY unitsSold DESC
    """)
    fun getProductSalesSummary(
        startDate: Long,
        endDate: Long
    ): Flow<List<ProductSalesSummary>>
}
