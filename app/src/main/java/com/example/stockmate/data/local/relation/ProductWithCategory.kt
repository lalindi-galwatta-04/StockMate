
package com.example.stockmate.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.stockmate.data.local.entity.CategoryEntity
import com.example.stockmate.data.local.entity.ProductEntity

data class ProductWithCategory(

    @Embedded
    val product: ProductEntity,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "categoryId"
    )
    val category: CategoryEntity?
)
