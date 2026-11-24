
package com.example.stockmate.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

import com.example.stockmate.data.local.dao.CategoryDao
import com.example.stockmate.data.local.dao.ProductDao
import com.example.stockmate.data.local.dao.StockMovementDao
import com.example.stockmate.data.local.dao.SupplierDao
import com.example.stockmate.data.local.dao.PurchaseDao
import com.example.stockmate.data.local.dao.PurchaseItemDao
import com.example.stockmate.data.local.dao.SaleDao
import com.example.stockmate.data.local.dao.SaleItemDao

import com.example.stockmate.data.local.entity.CategoryEntity
import com.example.stockmate.data.local.entity.ProductEntity
import com.example.stockmate.data.local.entity.StockMovementEntity
import com.example.stockmate.data.local.entity.SupplierEntity
import com.example.stockmate.data.local.entity.PurchaseEntity
import com.example.stockmate.data.local.entity.PurchaseItemEntity
import com.example.stockmate.data.local.entity.SaleEntity
import com.example.stockmate.data.local.entity.SaleItemEntity

@Database(
    entities = [
        CategoryEntity::class,
        ProductEntity::class,
        StockMovementEntity::class,
        SupplierEntity::class,
        PurchaseEntity::class,
        PurchaseItemEntity::class,
        SaleEntity::class,
        SaleItemEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class StockMateDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao

    abstract fun productDao(): ProductDao

    abstract fun stockMovementDao(): StockMovementDao

    abstract fun supplierDao(): SupplierDao

    abstract fun purchaseDao(): PurchaseDao

    abstract fun purchaseItemDao(): PurchaseItemDao

    abstract fun saleDao(): SaleDao

    abstract fun saleItemDao(): SaleItemDao

    companion object {

        @Volatile
        private var INSTANCE: StockMateDatabase? = null

        // Version 1 -> Version 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {

            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS stock_movements (
                        movementId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        productId INTEGER NOT NULL,
                        movementType TEXT NOT NULL,
                        quantityChange INTEGER NOT NULL,
                        previousQuantity INTEGER NOT NULL,
                        newQuantity INTEGER NOT NULL,
                        reason TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        FOREIGN KEY(productId)
                            REFERENCES products(productId)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_stock_movements_productId
                    ON stock_movements(productId)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_stock_movements_createdAt
                    ON stock_movements(createdAt)
                    """.trimIndent()
                )
            }
        }

        // Version 2 -> Version 3
        private val MIGRATION_2_3 = object : Migration(2, 3) {

            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS suppliers (
                        supplierId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        contactPerson TEXT NOT NULL,
                        phone TEXT NOT NULL,
                        email TEXT NOT NULL,
                        address TEXT NOT NULL,
                        notes TEXT NOT NULL,
                        isActive INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_suppliers_name
                    ON suppliers(name)
                    """.trimIndent()
                )
            }
        }

        // Version 3 -> Version 4
        private val MIGRATION_3_4 = object : Migration(3, 4) {

            override fun migrate(database: SupportSQLiteDatabase) {

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS purchases (
                        purchaseId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        supplierId INTEGER NOT NULL,
                        purchaseDate INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        totalAmount INTEGER NOT NULL,
                        notes TEXT NOT NULL,
                        receivedAt INTEGER,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        FOREIGN KEY(supplierId)
                            REFERENCES suppliers(supplierId)
                            ON UPDATE NO ACTION
                            ON DELETE RESTRICT
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_purchases_supplierId
                    ON purchases(supplierId)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_purchases_status
                    ON purchases(status)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_purchases_purchaseDate
                    ON purchases(purchaseDate)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS purchase_items (
                        purchaseItemId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        purchaseId INTEGER NOT NULL,
                        productId INTEGER NOT NULL,
                        quantity INTEGER NOT NULL,
                        unitCost INTEGER NOT NULL,
                        lineTotal INTEGER NOT NULL,
                        FOREIGN KEY(purchaseId)
                            REFERENCES purchases(purchaseId)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE,
                        FOREIGN KEY(productId)
                            REFERENCES products(productId)
                            ON UPDATE NO ACTION
                            ON DELETE RESTRICT
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_purchase_items_purchaseId
                    ON purchase_items(purchaseId)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_purchase_items_productId
                    ON purchase_items(productId)
                    """.trimIndent()
                )
            }
        }

        // Version 4 -> Version 5
        private val MIGRATION_4_5 = object : Migration(4, 5) {

            override fun migrate(database: SupportSQLiteDatabase) {

                // Create sales table
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sales (
                        saleId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        customerName TEXT NOT NULL,
                        customerPhone TEXT NOT NULL,
                        saleDate INTEGER NOT NULL,
                        paymentMethod TEXT NOT NULL,
                        totalAmount INTEGER NOT NULL,
                        totalCost INTEGER NOT NULL,
                        grossProfit INTEGER NOT NULL,
                        notes TEXT NOT NULL,
                        status TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                // Sales indexes
                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_sales_saleDate
                    ON sales(saleDate)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_sales_status
                    ON sales(status)
                    """.trimIndent()
                )

                // Create sale_items table
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sale_items (
                        saleItemId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        saleId INTEGER NOT NULL,
                        productId INTEGER NOT NULL,
                        quantity INTEGER NOT NULL,
                        unitSellingPrice INTEGER NOT NULL,
                        unitCost INTEGER NOT NULL,
                        lineTotal INTEGER NOT NULL,
                        costTotal INTEGER NOT NULL,
                        grossProfit INTEGER NOT NULL,
                        FOREIGN KEY(saleId)
                            REFERENCES sales(saleId)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE,
                        FOREIGN KEY(productId)
                            REFERENCES products(productId)
                            ON UPDATE NO ACTION
                            ON DELETE RESTRICT
                    )
                    """.trimIndent()
                )

                // Sale item indexes
                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_sale_items_saleId
                    ON sale_items(saleId)
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_sale_items_productId
                    ON sale_items(productId)
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): StockMateDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StockMateDatabase::class.java,
                    "stockmate_database"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5
                    )
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}
