
package com.example.stockmate

import android.app.Application
import com.example.stockmate.data.local.StockMateDatabase
import com.example.stockmate.data.local.entity.CategoryEntity
import com.example.stockmate.data.repository.CategoryRepository
import com.example.stockmate.data.repository.InventoryRepository
import com.example.stockmate.data.repository.ProductRepository
import com.example.stockmate.data.repository.SupplierRepository
import com.example.stockmate.data.repository.PurchaseRepository
import com.example.stockmate.data.repository.SalesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StockMateApplication : Application() {

    private val applicationScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: StockMateDatabase by lazy {
        StockMateDatabase.getDatabase(this)
    }

    val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(database.categoryDao())
    }

    val productRepository: ProductRepository by lazy {
        ProductRepository(database.productDao())
    }

    val inventoryRepository: InventoryRepository by lazy {
        InventoryRepository(database)
    }

    val supplierRepository: SupplierRepository by lazy {
        SupplierRepository(database.supplierDao())
    }

    val purchaseRepository: PurchaseRepository by lazy {
        PurchaseRepository(database)
    }

    // Sales repository
    val salesRepository: SalesRepository by lazy {
        SalesRepository(database)
    }

    override fun onCreate() {
        super.onCreate()
        seedDefaultCategories()
    }

    private fun seedDefaultCategories() {
        applicationScope.launch {

            val existingCategories =
                database.categoryDao().getAllCategories().first()

            if (existingCategories.isEmpty()) {

                val defaultCategories = listOf(
                    "Electronics",
                    "Clothing & Fashion",
                    "Groceries",
                    "Beauty & Personal Care",
                    "Home & Kitchen",
                    "Stationery",
                    "Accessories",
                    "Other"
                )

                defaultCategories.forEach { categoryName ->
                    database.categoryDao().insertCategory(
                        CategoryEntity(name = categoryName)
                    )
                }
            }
        }
    }
}
