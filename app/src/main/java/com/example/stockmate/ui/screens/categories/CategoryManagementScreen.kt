
package com.example.stockmate.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stockmate.data.local.entity.CategoryEntity
import com.example.stockmate.ui.theme.StockMateBackground
import com.example.stockmate.ui.theme.StockMateCharcoal
import com.example.stockmate.ui.theme.StockMateDanger
import com.example.stockmate.ui.theme.StockMateMuted
import com.example.stockmate.ui.theme.StockMateOrange
import com.example.stockmate.ui.viewmodel.CategoryViewModel
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.IconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    onBack: () -> Unit,
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val categories by categoryViewModel.categories.collectAsState()

    var showEditor by remember { mutableStateOf(false) }
    var editingCategory by remember {
        mutableStateOf<CategoryEntity?>(null)
    }
    var deletingCategory by remember {
        mutableStateOf<CategoryEntity?>(null)
    }
    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = StockMateBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Categories",
                            fontWeight = FontWeight.Bold,
                            color = StockMateCharcoal
                        )
                        Text(
                            "Organise your products",
                            style = MaterialTheme.typography.bodySmall,
                            color = StockMateMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Go back",
                            tint = StockMateOrange
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StockMateBackground
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = StockMateCharcoal
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Category,
                        contentDescription = null,
                        tint = StockMateOrange,
                        modifier = Modifier.size(34.dp)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 14.dp)
                    ) {
                        Text(
                            "Product Categories",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Total: ${categories.size} categories",
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "All Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "${categories.size} items",
                    color = StockMateMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(12.dp))

            if (categories.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Category,
                        contentDescription = null,
                        tint = StockMateMuted,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("No categories yet")
                    Text(
                        "Add a category to organise your products.",
                        color = StockMateMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = categories,
                        key = { it.categoryId }
                    ) { category ->
                        CategoryCard(
                            category = category,
                            onEdit = {
                                editingCategory = category
                                showEditor = true
                            },
                            onDelete = {
                                deletingCategory = category
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    editingCategory = null
                    showEditor = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StockMateOrange,
                    contentColor = StockMateCharcoal
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(
                    "Add New Category",
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showEditor) {
        CategoryEditorDialog(
            category = editingCategory,
            onDismiss = {
                showEditor = false
            },
            onSave = { name, description ->
                val current = editingCategory

                if (current == null) {
                    categoryViewModel.addCategory(
                        name,
                        description
                    ) { success, result ->
                        message = result
                        if (success) showEditor = false
                    }
                } else {
                    categoryViewModel.updateCategory(
                        current,
                        name,
                        description
                    ) { success, result ->
                        message = result
                        if (success) showEditor = false
                    }
                }
            }
        )
    }

    deletingCategory?.let { category ->
        AlertDialog(
            onDismissRequest = {
                deletingCategory = null
            },
            title = {
                Text("Delete category?")
            },
            text = {
                Text(
                    "Are you sure you want to delete " +
                            "\"${category.name}\"? Products assigned to " +
                            "this category will remain, but their category " +
                            "will be cleared."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        categoryViewModel.deleteCategory(category) {
                                _, result ->
                            message = result
                        }
                        deletingCategory = null
                    }
                ) {
                    Text("Delete", color = StockMateDanger)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        deletingCategory = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    message?.let { result ->
        AlertDialog(
            onDismissRequest = { message = null },
            title = { Text("StockMate") },
            text = { Text(result) },
            confirmButton = {
                TextButton(onClick = { message = null }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun CategoryCard(
    category: CategoryEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Category,
                contentDescription = null,
                tint = StockMateOrange,
                modifier = Modifier.size(28.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    category.name,
                    fontWeight = FontWeight.SemiBold,
                    color = StockMateCharcoal
                )

                if (category.description.isNotBlank()) {
                    Text(
                        category.description,
                        color = StockMateMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit category",
                    tint = StockMateOrange
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete category",
                    tint = StockMateDanger
                )
            }
        }
    }
}

@Composable
private fun CategoryEditorDialog(
    category: CategoryEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember(category?.categoryId) {
        mutableStateOf(category?.name ?: "")
    }

    var description by remember(category?.categoryId) {
        mutableStateOf(category?.description ?: "")
    }

    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (category == null) {
                    "Add Category"
                } else {
                    "Edit Category"
                },
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = null
                    },
                    label = { Text("Category name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                error?.let {
                    Text(
                        it,
                        color = StockMateDanger,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        error = "Enter a category name."
                    } else {
                        onSave(name.trim(), description.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = StockMateOrange,
                    contentColor = StockMateCharcoal
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
