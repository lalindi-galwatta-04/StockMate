
package com.example.stockmate.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.stockmate.ui.screens.categories.CategoryManagementScreen
import com.example.stockmate.ui.screens.dashboard.DashboardScreen
import com.example.stockmate.ui.screens.inventory.InventoryScreen
import com.example.stockmate.ui.screens.products.ProductsScreen
import com.example.stockmate.ui.screens.purchases.CreatePurchaseScreen
import com.example.stockmate.ui.screens.purchases.PurchaseDetailsScreen
import com.example.stockmate.ui.screens.purchases.PurchaseListScreen
import com.example.stockmate.ui.screens.suppliers.AddEditSupplierScreen
import com.example.stockmate.ui.screens.suppliers.SupplierListScreen
import com.example.stockmate.ui.screens.sales.SalesScreen
import com.example.stockmate.ui.screens.sales.CreateSaleScreen
import com.example.stockmate.ui.screens.sales.SaleDetailsScreen
import com.example.stockmate.ui.screens.reports.ReportsScreen

private sealed class StockMateTab(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    data object Dashboard :
        StockMateTab("dashboard", "Dashboard", Icons.Default.Dashboard)

    data object Products :
        StockMateTab("products", "Products", Icons.Default.Inventory)

    data object Inventory :
        StockMateTab("inventory", "Inventory", Icons.Default.Warehouse)

    data object Sales :
        StockMateTab("sales", "Sales", Icons.Default.PointOfSale)

    data object Reports :
        StockMateTab("reports", "Reports", Icons.Default.Assessment)
}

@Composable
fun StockMateNavigation() {

    val navController = rememberNavController()

    val tabs = listOf(
        StockMateTab.Dashboard,
        StockMateTab.Products,
        StockMateTab.Inventory,
        StockMateTab.Sales,
        StockMateTab.Reports
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentRoute = currentDestination?.route

    // Determine which main tab should remain selected
    // while the user visits its secondary screens.

    val selectedTabRoute = when (currentRoute) {

        "dashboard" -> StockMateTab.Dashboard.route

        "products",
        "categories" -> StockMateTab.Products.route

        "inventory",
        "suppliers",
        "supplier_form/{supplierId}",
        "purchases",
        "purchase_form",
        "purchase_details/{purchaseId}" -> StockMateTab.Inventory.route

        "sales",
        "create_sale",
        "sale_details/{saleId}" -> StockMateTab.Sales.route

        "reports" -> StockMateTab.Reports.route

        else -> StockMateTab.Dashboard.route
    }

    Scaffold(
        bottomBar = {
            // Bottom navigation is now visible on every screen.
            NavigationBar {

                tabs.forEach { tab ->

                    val selected =
                        selectedTabRoute == tab.route

                    NavigationBarItem(
                        selected = selected,

                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(
                                    navController.graph.findStartDestination().id
                                ) {
                                    saveState = true
                                }

                                launchSingleTop = true
                                restoreState = true
                            }
                        },

                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },

                        label = {
                            Text(tab.label)
                        }
                    )
                }
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = StockMateTab.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {

            // -------------------------------------------------
            // DASHBOARD
            // -------------------------------------------------

            composable(StockMateTab.Dashboard.route) {
                DashboardScreen()
            }

            // -------------------------------------------------
            // PRODUCTS
            // -------------------------------------------------

            composable(StockMateTab.Products.route) {
                ProductsScreen(
                    onManageCategories = {
                        navController.navigate("categories")
                    }
                )
            }

            // -------------------------------------------------
            // CATEGORIES
            // -------------------------------------------------

            composable("categories") {
                CategoryManagementScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // -------------------------------------------------
            // INVENTORY
            // -------------------------------------------------

            composable(StockMateTab.Inventory.route) {
                InventoryScreen(
                    onManageSuppliers = {
                        navController.navigate("suppliers")
                    },

                    onManagePurchases = {
                        navController.navigate("purchases")
                    }
                )
            }

            // -------------------------------------------------
            // PURCHASE LIST
            // -------------------------------------------------

            composable("purchases") {
                PurchaseListScreen(
                    onBack = {
                        navController.popBackStack()
                    },

                    onNewPurchase = {
                        navController.navigate("purchase_form")
                    },

                    onPurchaseClick = { purchaseId ->
                        navController.navigate(
                            "purchase_details/$purchaseId"
                        )
                    }
                )
            }

            // -------------------------------------------------
            // CREATE PURCHASE
            // -------------------------------------------------

            composable("purchase_form") {
                CreatePurchaseScreen(
                    onBack = {
                        navController.popBackStack()
                    },

                    onPurchaseCreated = { purchaseId ->
                        navController.navigate(
                            "purchase_details/$purchaseId"
                        ) {
                            popUpTo("purchase_form") {
                                inclusive = true
                            }
                        }
                    },

                    onAddSupplier = {
                        navController.navigate("supplier_form/0")
                    }
                )
            }

            // -------------------------------------------------
            // PURCHASE DETAILS
            // -------------------------------------------------

            composable(
                route = "purchase_details/{purchaseId}",
                arguments = listOf(
                    navArgument("purchaseId") {
                        type = NavType.LongType
                    }
                )
            ) { entry ->

                val purchaseId =
                    entry.arguments?.getLong("purchaseId") ?: 0L

                PurchaseDetailsScreen(
                    purchaseId = purchaseId,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // -------------------------------------------------
            // SUPPLIERS
            // -------------------------------------------------

            composable("suppliers") {
                SupplierListScreen(
                    onBack = {
                        navController.popBackStack()
                    },

                    onAddSupplier = {
                        navController.navigate("supplier_form/0")
                    },

                    onEditSupplier = { supplierId ->
                        navController.navigate(
                            "supplier_form/$supplierId"
                        )
                    }
                )
            }

            // -------------------------------------------------
            // ADD / EDIT SUPPLIER
            // -------------------------------------------------

            composable(
                route = "supplier_form/{supplierId}",
                arguments = listOf(
                    navArgument("supplierId") {
                        type = NavType.LongType
                    }
                )
            ) { entry ->

                val supplierId =
                    entry.arguments?.getLong("supplierId") ?: 0L

                AddEditSupplierScreen(
                    supplierId = supplierId.takeIf { it > 0L },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // -------------------------------------------------
            // SALES
            // -------------------------------------------------

            // -------------------------------------------------
// SALES
// -------------------------------------------------

            composable(StockMateTab.Sales.route) {
                SalesScreen(
                    onNewSale = {
                        navController.navigate("create_sale")
                    },
                    onSaleClick = { saleId ->
                        navController.navigate("sale_details/$saleId")
                    }
                )
            }

// -------------------------------------------------
// CREATE SALE
// -------------------------------------------------

            composable("create_sale") {
                CreateSaleScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onSaleCreated = { saleId ->
                        navController.navigate("sale_details/$saleId") {
                            popUpTo("create_sale") {
                                inclusive = true
                            }
                        }
                    }
                )
            }

// -------------------------------------------------
// SALE DETAILS
// -------------------------------------------------

            composable(
                route = "sale_details/{saleId}",
                arguments = listOf(
                    navArgument("saleId") {
                        type = NavType.LongType
                    }
                )
            ) { entry ->

                val saleId =
                    entry.arguments?.getLong("saleId") ?: 0L

                SaleDetailsScreen(
                    saleId = saleId,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // -------------------------------------------------
            // REPORTS
            // -------------------------------------------------

            composable(StockMateTab.Reports.route) {
                ReportsScreen()
            }
        }
    }
}

// ---------------------------------------------------------
// PLACEHOLDER SCREEN
// ---------------------------------------------------------

@Composable
private fun FeaturePlaceholderScreen(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = description,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
