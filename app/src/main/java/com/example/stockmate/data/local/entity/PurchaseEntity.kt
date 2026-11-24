
package com.example.stockmate.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchases",
    foreignKeys = [
        ForeignKey(
            entity = SupplierEntity::class,
            parentColumns = ["supplierId"],
            childColumns = ["supplierId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["supplierId"]),
        Index(value = ["status"]),
        Index(value = ["purchaseDate"])
    ]
)
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true)
    val purchaseId: Long = 0,

    val supplierId: Long,

    val purchaseDate: Long = System.currentTimeMillis(),

    // PENDING or RECEIVED
    val status: String = STATUS_PENDING,

    // Total amount in cents (LKR)
    val totalAmount: Long = 0L,

    val notes: String = "",

    val receivedAt: Long? = null,

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_RECEIVED = "RECEIVED"
    }
}
