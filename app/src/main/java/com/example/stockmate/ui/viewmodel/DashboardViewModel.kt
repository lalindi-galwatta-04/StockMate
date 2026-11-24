
package com.example.stockmate.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.StockMateApplication
import com.example.stockmate.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DashboardSummary(
    val productCount: Int = 0,
    val lowStockCount: Int = 0,
    val inStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val salesToday: Long = 0L,
    val grossProfitToday: Long = 0L
)

data class DashboardActivity(
    val title: String,
    val product: String,
    val quantity: String,
    val isAddition: Boolean,
    val date: String
)

class DashboardViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val app = application as StockMateApplication

    private val productRepository = app.productRepository
    private val inventoryRepository = app.inventoryRepository
    private val salesRepository = app.salesRepository

    private val products = productRepository.allProducts

    private val todayRange: Pair<Long, Long>
        get() {
            val calendar = Calendar.getInstance()

            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val start = calendar.timeInMillis

            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val end = calendar.timeInMillis - 1L

            return start to end
        }

    private val salesToday = todayRange.let { range ->
        salesRepository.getSalesByDateRange(
            startDate = range.first,
            endDate = range.second
        )
    }

    val summary = combine(
        products,
        salesToday
    ) { productList, sales ->

        val activeProducts = productList.filter { it.isActive }

        val outOfStock = activeProducts.count {
            it.quantityInStock <= 0
        }

        val lowStock = activeProducts.count {
            it.quantityInStock > 0 &&
                    it.quantityInStock <= it.minimumStockLevel
        }

        val completedSales = sales.filter {
            it.status == SaleEntity.STATUS_COMPLETED
        }

        DashboardSummary(
            productCount = activeProducts.size,
            lowStockCount = lowStock,
            inStockCount = activeProducts.size - lowStock - outOfStock,
            outOfStockCount = outOfStock,
            salesToday = completedSales.sumOf { it.totalAmount },
            grossProfitToday = completedSales.sumOf { it.grossProfit }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        DashboardSummary()
    )

    val recentActivities = combine(
        inventoryRepository.allMovements,
        products
    ) { movements, productList ->

        val productNames = productList.associate {
            it.productId to it.productName
        }

        movements
            .sortedByDescending { it.createdAt }
            .take(3)
            .map { movement ->

                val isAddition = movement.movementType == "ADD"

                val signedQuantity = if (isAddition) {
                    "+${kotlin.math.abs(movement.quantityChange)} units"
                } else {
                    "-${kotlin.math.abs(movement.quantityChange)} units"
                }

                DashboardActivity(
                    title = if (isAddition) "Stock Added" else "Stock Removed",
                    product = productNames[movement.productId]
                        ?: "Product #${movement.productId}",
                    quantity = signedQuantity,
                    isAddition = isAddition,
                    date = SimpleDateFormat(
                        "dd MMM, hh:mm a",
                        Locale.getDefault()
                    ).format(Date(movement.createdAt))
                )
            }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )
}
