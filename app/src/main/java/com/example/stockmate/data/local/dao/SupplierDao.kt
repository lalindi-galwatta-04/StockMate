
package com.example.stockmate.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.stockmate.data.local.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {

    @Query(
        """
        SELECT * FROM suppliers
        ORDER BY isActive DESC, name COLLATE NOCASE ASC
        """
    )
    fun getAllSuppliers(): Flow<List<SupplierEntity>>

    @Query(
        """
        SELECT * FROM suppliers
        WHERE isActive = 1
        ORDER BY name COLLATE NOCASE ASC
        """
    )
    fun getActiveSuppliers(): Flow<List<SupplierEntity>>

    @Query(
        """
        SELECT * FROM suppliers
        WHERE supplierId = :supplierId
        LIMIT 1
        """
    )
    suspend fun getSupplierById(
        supplierId: Long
    ): SupplierEntity?

    @Query(
        """
        SELECT * FROM suppliers
        WHERE supplierId = :supplierId
        LIMIT 1
        """
    )
    fun getSupplierByIdFlow(
        supplierId: Long
    ): Flow<SupplierEntity?>

    @Query(
        """
        SELECT * FROM suppliers
        WHERE name LIKE '%' || :query || '%'
        OR contactPerson LIKE '%' || :query || '%'
        OR phone LIKE '%' || :query || '%'
        OR email LIKE '%' || :query || '%'
        ORDER BY isActive DESC, name COLLATE NOCASE ASC
        """
    )
    fun searchSuppliers(
        query: String
    ): Flow<List<SupplierEntity>>

    @Query("SELECT COUNT(*) FROM suppliers")
    fun getSupplierCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSupplier(
        supplier: SupplierEntity
    ): Long

    @Update
    suspend fun updateSupplier(
        supplier: SupplierEntity
    )

    @Delete
    suspend fun deleteSupplier(
        supplier: SupplierEntity
    )

    @Query(
        """
        UPDATE suppliers
        SET isActive = :isActive,
            updatedAt = :updatedAt
        WHERE supplierId = :supplierId
        """
    )
    suspend fun updateSupplierStatus(
        supplierId: Long,
        isActive: Boolean,
        updatedAt: Long
    )
}
