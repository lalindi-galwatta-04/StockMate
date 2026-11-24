
package com.example.stockmate.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_movements",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["productId"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["productId"]),
        Index(value = ["createdAt"])
    ]
)
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true)
    val movementId: Long = 0,

    val productId: Long,

    // ADD or REMOVE
    val movementType: String,

    // Positive for additions, negative for removals
    val quantityChange: Int,

    val previousQuantity: Int,
    val newQuantity: Int,

    val reason: String,

    val createdAt: Long = System.currentTimeMillis()
)
