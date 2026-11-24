# 📦 StockMate – Inventory Management System

**A modern Android-based Inventory Management System designed for small businesses.**

StockMate is an offline-first inventory management application developed to simplify product management, stock tracking, supplier management, purchasing, sales and business reporting.

Built using Kotlin and Jetpack Compose, StockMate provides a user-friendly interface with real-time inventory updates and analytical insights to help small business owners manage their operations efficiently.

---

## ✨ Features

### 📊 Dashboard
- Overview of total products.
- In-stock, low-stock and out-of-stock counts.
- Today's sales and gross profit.
- Recent stock movement activities.

### 📦 Product Management
- Add and manage products.
- Update product information.
- Manage product categories.
- Activate and deactivate products.
- Search and view product details.

### 🏷️ Inventory Management
- Monitor available stock quantities.
- Perform stock adjustments.
- Track stock-in and stock-out activities.
- View stock movement history.
- Identify low-stock and out-of-stock products.
- Receive restocking recommendations.

### 🚚 Supplier Management
- Register and manage suppliers.
- Maintain supplier contact information.
- Edit supplier details.
- Activate and deactivate suppliers.

### 🛒 Purchase Management
- Record product purchases.
- Associate purchases with suppliers.
- Add multiple products to purchases.
- Calculate purchase totals.
- Automatically update inventory quantities.

### 💰 Sales Management
- Record customer sales.
- Add multiple products to a sale.
- Calculate sales totals.
- Automatically deduct sold quantities from inventory.
- Calculate total cost and gross profit.
- Maintain sales transaction records.

### 📈 Reports & Analytics
- Sales analytics.
- Inventory analytics.
- Best-selling product information.
- Inventory health summaries.
- Category-based stock distribution.
- Restock recommendations.
- Date-based reporting filters.

### 📄 PDF Reports
- Generate PDF reports.
- View financial and inventory information.
- Export and share reports using Android sharing functionality.

---

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Kotlin | Primary programming language |
| Jetpack Compose | Modern declarative UI development |
| Material 3 | UI components and styling |
| Room Database | Local data persistence |
| MVVM Architecture | Application architecture |
| Kotlin Coroutines | Asynchronous operations |
| Kotlin Flow | Reactive data observation |
| Navigation Compose | Screen navigation |
| Gradle Kotlin DSL | Build configuration |
| Android Studio | Development environment |

---

## 🏗️ Architecture

StockMate follows the **Model–View–ViewModel (MVVM)** architectural pattern to maintain separation of concerns and improve maintainability.

The application uses a layered architecture:

- **UI Layer:** Jetpack Compose screens and user interactions.
- **ViewModel Layer:** Manages UI state and business logic.
- **Repository Layer:** Handles data operations and communication with the database.
- **Data Layer:** Room Database, entities and DAOs.

---

## 🗄️ Database

StockMate uses Room Database for offline-first data storage.

The database manages information related to:

- Products
- Categories
- Suppliers
- Purchases
- Purchase Items
- Sales
- Sale Items
- Stock Movements

All core inventory operations are designed to work locally without requiring an internet connection.

---

## 🧪 Software Testing

StockMate underwent a structured manual testing process to evaluate functionality, reliability and usability.

The testing activities covered:

- Functional Testing
- Integration Testing
- System Testing
- End-to-End Testing
- Database Testing
- Input Validation Testing
- Negative Testing
- Regression Testing
- UI and Usability Testing
- Smoke Testing
- Manual Performance Testing
- Compatibility and Installation Testing

Testing included product management, supplier management, purchasing, sales, inventory updates, financial calculations, dashboard accuracy, reports and PDF generation.

Test cases were organised using unique test IDs with expected results and Pass/Fail tracking.

---

## 🎨 UI Design

StockMate uses a professional **Charcoal & Orange** visual theme to provide a clean and modern
