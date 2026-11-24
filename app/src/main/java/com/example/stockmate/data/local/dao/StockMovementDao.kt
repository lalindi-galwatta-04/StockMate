
package com.example.stockmate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.stockmate.data.local.entity.StockMovementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockMovementDao {

    @Insert
    suspend fun insertMovement(
        movement: StockMovementEntity
    ): Long

    @Query("""
        SELECT * FROM stock_movements
        ORDER BY createdAt DESC
    """)
    fun getAllMovements(): Flow<List<StockMovementEntity>>

    @Query("""
        SELECT * FROM stock_movements
        WHERE productId = :productId
        ORDER BY createdAt DESC
    """)
    fun getMovementsByProduct(
        productId: Long
    ): Flow<List<StockMovementEntity>>

    @Query("""
        SELECT COUNT(*) FROM stock_movements
    """)
    fun getMovementCount(): Flow<Int>
}
