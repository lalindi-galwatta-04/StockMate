
package com.example.stockmate.data.repository

import com.example.stockmate.data.local.dao.CategoryDao
import com.example.stockmate.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class CategoryRepository(
    private val categoryDao: CategoryDao
) {
    val allCategories: Flow<List<CategoryEntity>> =
        categoryDao.getAllCategories()

    val categoryCount: Flow<Int> =
        categoryDao.getCategoryCount()

    suspend fun addCategory(
        name: String,
        description: String = ""
    ): Long {
        val cleanName = name.trim()

        require(cleanName.isNotBlank()) {
            "Category name cannot be empty."
        }

        val existing = allCategories.first()

        require(existing.none {
            it.name.equals(cleanName, ignoreCase = true)
        }) {
            "A category with this name already exists."
        }

        return categoryDao.insertCategory(
            CategoryEntity(
                name = cleanName,
                description = description.trim()
            )
        )
    }

    suspend fun updateCategory(category: CategoryEntity) {
        val cleanName = category.name.trim()

        require(cleanName.isNotBlank()) {
            "Category name cannot be empty."
        }

        val existing = allCategories.first()

        require(existing.none {
            it.categoryId != category.categoryId &&
                    it.name.equals(cleanName, ignoreCase = true)
        }) {
            "A category with this name already exists."
        }

        categoryDao.updateCategory(
            category.copy(
                name = cleanName,
                description = category.description.trim(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
    }

    suspend fun getCategoryById(id: Long): CategoryEntity? {
        return categoryDao.getCategoryById(id)
    }
}
