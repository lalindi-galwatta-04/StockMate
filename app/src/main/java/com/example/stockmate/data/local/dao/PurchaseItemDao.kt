
package com.example.stockmate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.stockmate.data.local.entity.PurchaseItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseItemDao {

    @Insert
    suspend fun insertPurchaseItems(
        items: List<PurchaseItemEntity>
    ): List<Long>

    @Query("""
        SELECT * FROM purchase_items
        WHERE purchaseId = :purchaseId
        ORDER BY purchaseItemId ASC
    """)
    fun getItemsByPurchase(
        purchaseId: Long
    ): Flow<List<PurchaseItemEntity>>

    @Query("""
        SELECT * FROM purchase_items
        WHERE purchaseId = :purchaseId
    """)
    suspend fun getItemsByPurchaseOnce(
        purchaseId: Long
    ): List<PurchaseItemEntity>

    @Query("""
        SELECT COALESCE(SUM(lineTotal), 0)
        FROM purchase_items
        WHERE purchaseId = :purchaseId
    """)
    suspend fun getPurchaseTotal(
        purchaseId: Long
    ): Long

    @Query("""
        SELECT COUNT(*) FROM purchase_items
        WHERE purchaseId = :purchaseId
    """)
    suspend fun getItemCount(
        purchaseId: Long
    ): Int
}
