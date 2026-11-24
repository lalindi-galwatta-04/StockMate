
package com.example.stockmate.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "suppliers",
    indices = [
        Index(value = ["name"])
    ]
)
data class SupplierEntity(

    @PrimaryKey(autoGenerate = true)
    val supplierId: Long = 0,

    val name: String,

    val contactPerson: String = "",

    val phone: String = "",

    val email: String = "",

    val address: String = "",

    val notes: String = "",

    val isActive: Boolean = true,

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)
