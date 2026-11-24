
package com.example.stockmate.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",

    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],

    indices = [
        Index(value = ["sku"], unique = true),
        Index(value = ["categoryId"])
    ]
)
data class ProductEntity(

    @PrimaryKey(autoGenerate = true)
    val productId: Long = 0,

    val productName: String,

    val sku: String,

    val description: String = "",

    val categoryId: Long? = null,

    // Monetary values stored in cents (LKR)
    val purchasePrice: Long,

    val sellingPrice: Long,

    // Inventory quantities
    val quantityInStock: Int = 0,

    val minimumStockLevel: Int = 5,

    val isActive: Boolean = true,

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)
