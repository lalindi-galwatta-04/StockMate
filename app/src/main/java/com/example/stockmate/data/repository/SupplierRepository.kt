
package com.example.stockmate.data.repository

import com.example.stockmate.data.local.dao.SupplierDao
import com.example.stockmate.data.local.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow

class SupplierRepository(
    private val supplierDao: SupplierDao
) {

    val allSuppliers: Flow<List<SupplierEntity>> =
        supplierDao.getAllSuppliers()

    val activeSuppliers: Flow<List<SupplierEntity>> =
        supplierDao.getActiveSuppliers()

    val supplierCount: Flow<Int> =
        supplierDao.getSupplierCount()

    fun searchSuppliers(
        query: String
    ): Flow<List<SupplierEntity>> {
        return supplierDao.searchSuppliers(query.trim())
    }

    fun getSupplierById(
        supplierId: Long
    ): Flow<SupplierEntity?> {
        return supplierDao.getSupplierByIdFlow(supplierId)
    }

    suspend fun addSupplier(
        name: String,
        contactPerson: String,
        phone: String,
        email: String,
        address: String,
        notes: String
    ): Long {

        require(name.isNotBlank()) {
            "Supplier name is required."
        }

        val now = System.currentTimeMillis()

        val supplier = SupplierEntity(
            name = name.trim(),
            contactPerson = contactPerson.trim(),
            phone = phone.trim(),
            email = email.trim(),
            address = address.trim(),
            notes = notes.trim(),
            createdAt = now,
            updatedAt = now
        )

        return supplierDao.insertSupplier(supplier)
    }

    suspend fun updateSupplier(
        supplier: SupplierEntity
    ) {

        require(supplier.name.isNotBlank()) {
            "Supplier name is required."
        }

        supplierDao.updateSupplier(
            supplier.copy(
                name = supplier.name.trim(),
                contactPerson = supplier.contactPerson.trim(),
                phone = supplier.phone.trim(),
                email = supplier.email.trim(),
                address = supplier.address.trim(),
                notes = supplier.notes.trim(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteSupplier(
        supplier: SupplierEntity
    ) {
        supplierDao.deleteSupplier(supplier)
    }

    suspend fun setSupplierActive(
        supplierId: Long,
        isActive: Boolean
    ) {
        supplierDao.updateSupplierStatus(
            supplierId = supplierId,
            isActive = isActive,
            updatedAt = System.currentTimeMillis()
        )
    }
}
