
package com.example.stockmate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.stockmate.data.local.entity.PurchaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {

    @Insert
    suspend fun insertPurchase(purchase: PurchaseEntity): Long

    @Update
    suspend fun updatePurchase(purchase: PurchaseEntity)

    @Query("""
        SELECT * FROM purchases
        ORDER BY purchaseDate DESC
    """)
    fun getAllPurchases(): Flow<List<PurchaseEntity>>

    @Query("""
        SELECT * FROM purchases
        WHERE purchaseId = :purchaseId
        LIMIT 1
    """)
    suspend fun getPurchaseById(
        purchaseId: Long
    ): PurchaseEntity?

    @Query("""
        SELECT * FROM purchases
        WHERE purchaseId = :purchaseId
        LIMIT 1
    """)
    fun getPurchaseByIdFlow(
        purchaseId: Long
    ): Flow<PurchaseEntity?>

    @Query("""
        SELECT * FROM purchases
        WHERE supplierId = :supplierId
        ORDER BY purchaseDate DESC
    """)
    fun getPurchasesBySupplier(
        supplierId: Long
    ): Flow<List<PurchaseEntity>>

    @Query("""
        SELECT * FROM purchases
        WHERE status = :status
        ORDER BY purchaseDate DESC
    """)
    fun getPurchasesByStatus(
        status: String
    ): Flow<List<PurchaseEntity>>

    @Query("SELECT COUNT(*) FROM purchases")
    fun getPurchaseCount(): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM purchases
        WHERE status = 'PENDING'
    """)
    fun getPendingPurchaseCount(): Flow<Int>

    @Query("""
        SELECT COALESCE(SUM(totalAmount), 0)
        FROM purchases
        WHERE status = 'RECEIVED'
    """)
    fun getTotalReceivedPurchaseAmount(): Flow<Long>
}
