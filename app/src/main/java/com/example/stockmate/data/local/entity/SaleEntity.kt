
package com.example.stockmate.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    indices = [
        Index(value = ["saleDate"]),
        Index(value = ["status"])
    ]
)
data class SaleEntity(

    @PrimaryKey(autoGenerate = true)
    val saleId: Long = 0,

    val customerName: String = "Walk-in Customer",

    val customerPhone: String = "",

    val saleDate: Long = System.currentTimeMillis(),

    val paymentMethod: String = PAYMENT_CASH,

    // Monetary values stored in LKR cents
    val totalAmount: Long = 0L,

    val totalCost: Long = 0L,

    val grossProfit: Long = 0L,

    val notes: String = "",

    val status: String = STATUS_COMPLETED,

    val createdAt: Long = System.currentTimeMillis()

) {
    companion object {

        const val PAYMENT_CASH = "CASH"
        const val PAYMENT_CARD = "CARD"
        const val PAYMENT_BANK_TRANSFER = "BANK_TRANSFER"
        const val PAYMENT_OTHER = "OTHER"

        const val STATUS_COMPLETED = "COMPLETED"
    }
}
