
package com.example.stockmate.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.StockMateApplication
import com.example.stockmate.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        (application as StockMateApplication).categoryRepository

    val categories = repository.allCategories.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun addCategory(
        name: String,
        description: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.addCategory(name, description)
                onResult(true, "Category added successfully.")
            } catch (exception: Exception) {
                onResult(
                    false,
                    exception.message ?: "Unable to add category."
                )
            }
        }
    }

    fun updateCategory(
        category: CategoryEntity,
        name: String,
        description: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.updateCategory(
                    category.copy(
                        name = name,
                        description = description
                    )
                )
                onResult(true, "Category updated successfully.")
            } catch (exception: Exception) {
                onResult(
                    false,
                    exception.message ?: "Unable to update category."
                )
            }
        }
    }

    fun deleteCategory(
        category: CategoryEntity,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.deleteCategory(category)
                onResult(true, "Category deleted successfully.")
            } catch (exception: Exception) {
                onResult(
                    false,
                    exception.message ?: "Unable to delete category."
                )
            }
        }
    }
}
