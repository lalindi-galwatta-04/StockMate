
package com.example.stockmate.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.stockmate.data.local.entity.ProductEntity
import com.example.stockmate.data.local.relation.ProductWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    // All products, including inactive products
    @Query("""
        SELECT * FROM products
        ORDER BY productName COLLATE NOCASE ASC
    """)
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE productId = :productId")
    suspend fun getProductById(productId: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE sku = :sku LIMIT 1")
    suspend fun getProductBySku(sku: String): ProductEntity?

    // Search active and inactive products
    @Query("""
        SELECT * FROM products
        WHERE productName LIKE '%' || :query || '%'
        OR sku LIKE '%' || :query || '%'
        ORDER BY productName COLLATE NOCASE ASC
    """)
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("""
        SELECT * FROM products
        WHERE categoryId = :categoryId
        AND isActive = 1
        ORDER BY productName COLLATE NOCASE ASC
    """)
    fun getProductsByCategory(categoryId: Long): Flow<List<ProductEntity>>

    @Query("""
        SELECT * FROM products
        WHERE isActive = 1
        AND quantityInStock > 0
        AND quantityInStock <= minimumStockLevel
        ORDER BY quantityInStock ASC
    """)
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Query("""
        SELECT * FROM products
        WHERE isActive = 1
        AND quantityInStock <= 0
        ORDER BY productName ASC
    """)
    fun getOutOfStockProducts(): Flow<List<ProductEntity>>

    @Query("""
        SELECT COUNT(*) FROM products
        WHERE isActive = 1
    """)
    fun getProductCount(): Flow<Int>

    @Query("""
        SELECT COALESCE(SUM(quantityInStock), 0)
        FROM products
        WHERE isActive = 1
    """)
    fun getTotalStockQuantity(): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM products
        WHERE isActive = 1
        AND quantityInStock > 0
        AND quantityInStock <= minimumStockLevel
    """)
    fun getLowStockCount(): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM products
        WHERE isActive = 1
        AND quantityInStock <= 0
    """)
    fun getOutOfStockCount(): Flow<Int>

    @Transaction
    @Query("""
        SELECT * FROM products
        WHERE isActive = 1
        ORDER BY productName COLLATE NOCASE ASC
    """)
    fun getProductsWithCategories(): Flow<List<ProductWithCategory>>

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM products
            WHERE sku = :sku
            AND productId != :excludeProductId
        )
    """)
    suspend fun isSkuTaken(
        sku: String,
        excludeProductId: Long = 0
    ): Boolean
}
