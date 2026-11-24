package com.example.stockmate.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockmate.StockMateApplication
import com.example.stockmate.data.local.dao.ProductSalesSummary
import com.example.stockmate.data.local.entity.SaleEntity
import com.example.stockmate.utils.SalesReportEntry
import com.example.stockmate.utils.SalesReportLine
import com.example.stockmate.data.local.relation.ProductWithCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ---------------------------------------------------------
// REPORT MODELS
// ---------------------------------------------------------

data class ReportsSummary(
    val totalRevenue: Long = 0L,
    val totalCost: Long = 0L,
    val grossProfit: Long = 0L,
    val totalSales: Int = 0,
    val unitsSold: Long = 0L
)

data class SalesTrendPoint(
    val label: String,
    val revenue: Long,
    val grossProfit: Long
)

data class InventorySummary(
    val activeProducts: Int = 0,
    val totalUnits: Long = 0L,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    // Values are stored in cents, matching ProductEntity.purchasePrice.
    val inventoryValue: Long = 0L
)

data class CategoryStockSummary(
    val categoryName: String,
    val productCount: Int,
    val unitsInStock: Long,
    val inventoryValue: Long,
    val percentage: Float
)

data class InventoryHealthSummary(
    val healthyProducts: Int = 0,
    val lowStockProducts: Int = 0,
    val outOfStockProducts: Int = 0,
    val healthPercentage: Float = 0f
)

data class RestockRecommendation(
    val productId: Long,
    val productName: String,
    val currentStock: Int,
    val minimumStockLevel: Int,
    val recommendedQuantity: Int,
    val estimatedCost: Long
)

// ---------------------------------------------------------
// REPORTS VIEWMODEL
// ---------------------------------------------------------

@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModel(application: Application) : AndroidViewModel(application) {

    private val applicationInstance = application as StockMateApplication
    private val salesRepository = applicationInstance.salesRepository
    private val inventoryRepository = applicationInstance.inventoryRepository
    private val productDao = applicationInstance.database.productDao()

    // DATE FILTER STATE
    private val _startDate = MutableStateFlow(0L)
    val startDate: StateFlow<Long> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow(Long.MAX_VALUE)
    val endDate: StateFlow<Long> = _endDate.asStateFlow()

    private val dateRange = combine(_startDate, _endDate) { start, end -> start to end }

    // COMPLETED SALES
    val completedSales: StateFlow<List<SaleEntity>> =
        dateRange.flatMapLatest { (start, end) ->
            salesRepository.getSalesByDateRange(startDate = start, endDate = end)
        }.map { sales ->
            sales.filter { it.status == SaleEntity.STATUS_COMPLETED }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // PRODUCT SALES ANALYTICS
    val productSales: StateFlow<List<ProductSalesSummary>> =
        dateRange.flatMapLatest { (start, end) ->
            salesRepository.getProductSalesSummary(startDate = start, endDate = end)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // OVERALL SALES SUMMARY
    val summary: StateFlow<ReportsSummary> = combine(completedSales, productSales) { sales, products ->
        ReportsSummary(
            totalRevenue = sales.sumOf { it.totalAmount },
            totalCost = sales.sumOf { it.totalCost },
            grossProfit = sales.sumOf { it.grossProfit },
            totalSales = sales.size,
            unitsSold = products.sumOf { it.unitsSold }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportsSummary())

    val totalRevenue: StateFlow<Long> = summary.map { it.totalRevenue }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)
    val totalCost: StateFlow<Long> = summary.map { it.totalCost }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)
    val grossProfit: StateFlow<Long> = summary.map { it.grossProfit }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)
    val totalSales: StateFlow<Int> = summary.map { it.totalSales }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    val unitsSold: StateFlow<Long> = summary.map { it.unitsSold }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val bestSellingProducts: StateFlow<List<ProductSalesSummary>> = productSales
        .map { products -> products.sortedByDescending { it.unitsSold } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // SALES PERFORMANCE TREND
    val salesTrend: StateFlow<List<SalesTrendPoint>> = combine(completedSales, dateRange) { sales, range ->
        val (start, end) = range
        val useHourlyGrouping = start != 0L && end != Long.MAX_VALUE &&
                Calendar.getInstance().apply { timeInMillis = start }.let {
                    val startDay = Calendar.getInstance().apply {
                        timeInMillis = start
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    val endDay = Calendar.getInstance().apply {
                        timeInMillis = end
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    startDay == endDay
                }
        val useMonthlyGrouping = start == 0L && end == Long.MAX_VALUE

        val grouped = sales.groupBy { sale ->
            val cal = Calendar.getInstance().apply { timeInMillis = sale.saleDate }
            when {
                useHourlyGrouping -> {
                    cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                }
                useMonthlyGrouping -> {
                    cal.set(Calendar.DAY_OF_MONTH, 1); cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                }
                else -> {
                    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                }
            }
            cal.timeInMillis
        }
        val formatter = when {
            useHourlyGrouping -> SimpleDateFormat("ha", Locale.getDefault())
            useMonthlyGrouping -> SimpleDateFormat("MMM yy", Locale.getDefault())
            else -> SimpleDateFormat("dd MMM", Locale.getDefault())
        }
        val points = grouped.toSortedMap().map { (timestamp, items) ->
            SalesTrendPoint(
                label = formatter.format(Date(timestamp)),
                revenue = items.sumOf { it.totalAmount },
                grossProfit = items.sumOf { it.grossProfit }
            )
        }
        if (useMonthlyGrouping) points.takeLast(6) else points
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // -----------------------------------------------------
    // LIVE INVENTORY ANALYTICS (NOT AFFECTED BY SALES DATES)
    // -----------------------------------------------------

    val lowStockProducts = inventoryRepository.lowStockProducts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    val outOfStockProducts = inventoryRepository.outOfStockProducts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    val productsWithCategories: StateFlow<List<ProductWithCategory>> =
        productDao.getProductsWithCategories().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
        )

    val inventorySummary: StateFlow<InventorySummary> = combine(
        productsWithCategories,
        inventoryRepository.totalStockQuantity,
        inventoryRepository.lowStockCount,
        inventoryRepository.outOfStockCount
    ) { products, totalUnits, lowCount, outCount ->
        InventorySummary(
            activeProducts = products.size,
            totalUnits = totalUnits.toLong(),
            lowStockCount = lowCount,
            outOfStockCount = outCount,
            inventoryValue = products.sumOf { row ->
                row.product.purchasePrice * row.product.quantityInStock.toLong()
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InventorySummary())

    val categoryStockDistribution: StateFlow<List<CategoryStockSummary>> =
        productsWithCategories.map { products ->
            val grouped = products.groupBy { it.category?.name ?: "Uncategorized" }
            val totalValue = products.sumOf {
                it.product.purchasePrice * it.product.quantityInStock.toLong()
            }
            grouped.map { (name, rows) ->
                val value = rows.sumOf {
                    it.product.purchasePrice * it.product.quantityInStock.toLong()
                }
                CategoryStockSummary(
                    categoryName = name,
                    productCount = rows.size,
                    unitsInStock = rows.sumOf { it.product.quantityInStock.toLong() },
                    inventoryValue = value,
                    percentage = if (totalValue > 0L) {
                        (value.toDouble() / totalValue.toDouble()).toFloat().coerceIn(0f, 1f)
                    } else 0f
                )
            }.sortedByDescending { it.inventoryValue }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // INVENTORY HEALTH AND RESTOCK RECOMMENDATIONS

    val inventoryHealth: StateFlow<InventoryHealthSummary> =
        inventorySummary.map { summary ->
            val healthy = (summary.activeProducts - summary.lowStockCount - summary.outOfStockCount)
                .coerceAtLeast(0)
            InventoryHealthSummary(
                healthyProducts = healthy,
                lowStockProducts = summary.lowStockCount,
                outOfStockProducts = summary.outOfStockCount,
                healthPercentage = if (summary.activeProducts > 0) {
                    (healthy.toFloat() / summary.activeProducts.toFloat()).coerceIn(0f, 1f)
                } else 0f
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InventoryHealthSummary())

    val restockRecommendations: StateFlow<List<RestockRecommendation>> =
        productsWithCategories.map { products ->
            products.map { it.product }
                .filter { it.isActive && it.quantityInStock <= it.minimumStockLevel }
                .map { product ->
                    val recommended = (product.minimumStockLevel - product.quantityInStock)
                        .coerceAtLeast(0)
                    RestockRecommendation(
                        productId = product.productId,
                        productName = product.productName,
                        currentStock = product.quantityInStock,
                        minimumStockLevel = product.minimumStockLevel,
                        recommendedQuantity = recommended,
                        estimatedCost = product.purchasePrice * recommended.toLong()
                    )
                }
                .sortedWith(compareBy<RestockRecommendation> { it.currentStock }.thenByDescending { it.recommendedQuantity })
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalRecommendedRestockUnits: StateFlow<Int> = restockRecommendations
        .map { recommendations -> recommendations.sumOf { it.recommendedQuantity } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val estimatedRestockCost: StateFlow<Long> = restockRecommendations
        .map { recommendations -> recommendations.sumOf { it.estimatedCost } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    // DATE FILTER MANAGEMENT
    fun setAllTime() { _startDate.value = 0L; _endDate.value = Long.MAX_VALUE }

    fun setToday() {
        val calendar = Calendar.getInstance()
        setStartOfDay(calendar)
        val start = calendar.timeInMillis
        setEndOfDay(calendar)
        _startDate.value = start; _endDate.value = calendar.timeInMillis
    }

    fun setThisWeek() {
        val calendar = Calendar.getInstance().apply { firstDayOfWeek = Calendar.MONDAY }
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        setStartOfDay(calendar)
        val start = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 6)
        setEndOfDay(calendar)
        _startDate.value = start; _endDate.value = calendar.timeInMillis
    }

    fun setThisMonth() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        setStartOfDay(calendar)
        val start = calendar.timeInMillis
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        setEndOfDay(calendar)
        _startDate.value = start; _endDate.value = calendar.timeInMillis
    }

    fun setCustomDateRange(startDate: Long, endDate: Long) {
        if (startDate <= endDate) {
            _startDate.value = startDate
            _endDate.value = endDate
        }
    }

    private fun setStartOfDay(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
    }

    private fun setEndOfDay(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59); calendar.set(Calendar.MILLISECOND, 999)
    }

    suspend fun buildSalesReportEntries(
        sales: List<SaleEntity>
    ): List<SalesReportEntry> {
        return sales
            .filter { it.status == SaleEntity.STATUS_COMPLETED }
            .sortedBy { it.saleDate }
            .map { sale ->
                val items = salesRepository.getSaleItemsOnce(sale.saleId).map { item ->
                    val product = productDao.getProductById(item.productId)
                    SalesReportLine(
                        productName = product?.productName ?: "Product #${item.productId}",
                        sku = product?.sku ?: "N/A",
                        quantity = item.quantity,
                        unitSellingPrice = item.unitSellingPrice,
                        unitCost = item.unitCost,
                        lineTotal = item.lineTotal,
                        costTotal = item.costTotal,
                        grossProfit = item.grossProfit
                    )
                }
                SalesReportEntry(sale, items)
            }
    }
}
