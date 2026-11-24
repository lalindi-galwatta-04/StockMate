
package com.example.stockmate.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["saleId"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["productId"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["saleId"]),
        Index(value = ["productId"])
    ]
)
data class SaleItemEntity(

    @PrimaryKey(autoGenerate = true)
    val saleItemId: Long = 0,

    val saleId: Long,

    val productId: Long,

    val quantity: Int,

    // Historical prices stored in LKR cents
    val unitSellingPrice: Long,

    val unitCost: Long,

    val lineTotal: Long,

    val costTotal: Long,

    val grossProfit: Long
)
