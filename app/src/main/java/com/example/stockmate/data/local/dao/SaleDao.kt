
package com.example.stockmate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.stockmate.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    // Insert a new sale
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSale(sale: SaleEntity): Long

    // Retrieve all sales, newest first
    @Query("""
        SELECT * FROM sales
        ORDER BY saleDate DESC
    """)
    fun getAllSales(): Flow<List<SaleEntity>>

    // Retrieve a specific sale
    @Query("""
        SELECT * FROM sales
        WHERE saleId = :saleId
    """)
    suspend fun getSaleById(saleId: Long): SaleEntity?

    // Observe a specific sale
    @Query("""
        SELECT * FROM sales
        WHERE saleId = :saleId
    """)
    fun getSaleByIdFlow(saleId: Long): Flow<SaleEntity?>

    // Retrieve sales by date range
    @Query("""
        SELECT * FROM sales
        WHERE saleDate BETWEEN :startDate AND :endDate
        ORDER BY saleDate DESC
    """)
    fun getSalesByDateRange(
        startDate: Long,
        endDate: Long
    ): Flow<List<SaleEntity>>

    // Search and filter sales
    @Query("""
        SELECT * FROM sales
        WHERE
            (
                :searchQuery = ''
                OR CAST(saleId AS TEXT) LIKE '%' || :searchQuery || '%'
                OR customerName LIKE '%' || :searchQuery || '%'
            )
            AND (
                :paymentMethod = ''
                OR paymentMethod = :paymentMethod
            )
            AND saleDate BETWEEN :startDate AND :endDate
        ORDER BY saleDate DESC
    """)
    fun getFilteredSales(
        searchQuery: String,
        paymentMethod: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<SaleEntity>>

    // Retrieve sales count
    @Query("""
        SELECT COUNT(*) FROM sales
    """)
    fun getSaleCount(): Flow<Int>

    // Calculate total revenue
    @Query("""
        SELECT COALESCE(SUM(totalAmount), 0)
        FROM sales
        WHERE status = 'COMPLETED'
    """)
    fun getTotalRevenue(): Flow<Long>

    // Calculate total cost
    @Query("""
        SELECT COALESCE(SUM(totalCost), 0)
        FROM sales
        WHERE status = 'COMPLETED'
    """)
    fun getTotalCost(): Flow<Long>

    // Calculate total gross profit
    @Query("""
        SELECT COALESCE(SUM(grossProfit), 0)
        FROM sales
        WHERE status = 'COMPLETED'
    """)
    fun getTotalGrossProfit(): Flow<Long>
}
