# 🛒 Grocery Store Inventory Management System

## 📌 Overview

The Grocery Store Inventory Management System is a software application designed to help grocery stores efficiently manage inventory, track stock levels, and maintain accurate product records.

This system provides real-time visibility into inventory and supports role-based access control to ensure operational integrity between managers and employees.

The project is being developed using Agile methodologies across multiple iterations.

---

## 🎯 Project Goals

- Provide real-time inventory visibility
- Support role-based access (Manager vs Employee)
- Enable accurate stock updates during sales and shipment receiving
- Maintain structured product records with barcode/ID lookup
- Lay the foundation for future reporting and restocking analytics

---

## 👥 Team

- Mujib Ali — Project Manager  
- Nooh Alavi — Technical Manager / Back-End Lead  
- Bilal Salman Ahmad — Front-End Lead  
- Vilkiss Xie — Software Quality Lead  

---

## 🚀 Development Approach

The project is structured across three Agile iterations:

- **Iteration I:** Core MVP (authentication, inventory viewing, stock updates, product management)
- **Iteration II:** Auditing, notifications, and promotional features
- **Iteration III:** Reporting, trend analysis, and smarter restocking support

---

## 🔮 Future Enhancements

- Sales trend reporting
- Low-stock notifications
- Expiry tracking
- Promotion logic (discounts, multi-buy, coupons)
- Reorder suggestions based on data

---

## 🏗 Status

Currently in active development (Phase 1: Planning & MVP Implementation).

---

## 🖥️ System Architecture

### **Technology Stack**
- **Language:** Java
- **GUI Framework:** Java Swing
- **Architecture:** MVC (Model-View-Controller)
- **IDE:** IntelliJ IDEA

### **Project Structure**
```
src/inventory/
├── Main.java                    # Application entry point
├── AppFrame.java                # Main application window
├── model/
│   ├── Product.java            # Product data model
│   ├── User.java               # User data model
│   └── Role.java               # User role enum (MANAGER/EMPLOYEE)
├── service/
│   ├── AuthService.java        # Authentication & user management
│   └── InventoryService.java   # Inventory operations
├── ui/
│   ├── LoginPanel.java          # Login interface
│   └── InventoryPanel.java      # Main inventory management interface
├── util/
│   └── CSVHandler.java          # CSV file read/write utility
└── data/                        # CSV data files for persistence
    ├── products.csv             # Product inventory data
    └── users.csv                # User account data
```

---

## 🚀 How to Run

### **Prerequisites**
- Java Development Kit (JDK) 8 or higher
- IntelliJ IDEA (or any Java IDE)

### **Running the Application**
1. Open the project in IntelliJ IDEA
2. Navigate to `src/inventory/Main.java`
3. Run the `Main` class
4. The login window will appear

### **Default Login Credentials**
- **Username:** `admin`
- **Password:** `admin123`
- **Role:** Manager

---

## 📖 User Guide

### **Authentication System**

The system uses role-based access control with two user types:
- **Manager:** Full access to all features
- **Employee:** Limited access (can view inventory and decrease stock)

### **Login Process**
1. Enter your username and password
2. Click "Login"
3. Upon successful authentication, you'll see the inventory management interface

---

## 🔧 Features & Operations

### **For All Users (Manager & Employee)**

#### **View Inventory**
- The main table displays all products with:
  - Product ID
  - Barcode
  - Product Name
  - Brand
  - Price
  - Current Quantity
  - Supplier
  - Storage Condition

### Sorting & Organization

Users can sort inventory dynamically by:
- Product ID
- Price
- Barcode
- Storage Condition
- Quantity
- Name, Brand, and other fields

Sorting can be performed by:
- Clicking on column headers in the table
- Using the sorting dropdown menu

Default sorting is applied by Product ID for consistency.

---

### Currency Conversion

- Prices can be viewed in multiple international currencies
- The system supports real-time conversion from the base currency (CAD)
- Enables better usability for international contexts and analysis

#### **Search Products**
- Enter a Product ID or Barcode in the search field
- Click "Search" to find specific products
- Click "Show All" to display the complete inventory

#### **Decrease Stock (Process Sales)**
1. Select a product from the table
2. Click "Decrease Stock (Sale)"
3. Enter the quantity sold
4. The system will:
   - Validate the quantity is available
   - Update the stock level
   - Prevent negative inventory

#### **Logout**
- Click "Logout" button in the top-right corner to return to login screen

---

### **Manager-Only Features**

#### **Add New Product**
1. Click "Add Product"
2. Enter product details:
   - Product ID (unique identifier)
   - Barcode (unique)
   - Product Name
   - Brand
   - Price
   - Initial Quantity
   - Supplier
   - Storage Condition
3. Click "Add" to save

#### **Edit Product Details**
1. Select a product from the table
2. Click "Edit Product"
3. Modify any details (except ID and Barcode)
4. Click "Save" to update

#### **Increase Stock (Receive Shipments)**
1. Select a product from the table
2. Click "Increase Stock (Shipment)"
3. Enter the quantity received
4. Stock level will be updated immediately

#### **Create Employee Accounts**
1. Click "Create Employee"
2. Enter:
   - Username (unique)
   - Password
   - Employee Name
   - Role (MANAGER or EMPLOYEE)
3. Click "Create" to add the new user

---

## 📊 Product Data Model

Each product in the system contains:
- **ID:** Unique product identifier
- **Barcode:** Product barcode for scanning
- **Name:** Product name
- **Brand:** Manufacturer/brand
- **Price:** Unit price (in dollars)
- **Quantity:** Current stock level
- **Supplier:** Supplier company name
- **Storage Condition:** Storage requirements (e.g., "Room Temperature", "Refrigerated")

---

## 🔐 Security Features

- Password-protected authentication
- Role-based access control
- Manager-only operations are hidden from employee view
- Default admin account for initial setup
- Session management with logout capability

---

## 💾 Data Persistence

The system uses **CSV files** for data storage, providing simple and human-readable persistence:

### **CSV Files Location**
- All data files are stored in `src/inventory/data/`
- Files are automatically created on first run

### **Data Files**

#### **users.csv**
Stores user account information with the following format:
```csv
username,password,name,role
admin,admin123,Administrator,MANAGER
employee1,pass123,John Doe,EMPLOYEE
```

#### **products.csv**
Stores product inventory with the following format:
```csv
id,barcode,name,brand,price,quantity,supplier,storageCondition
P001,123456789,Milk,Dairyland,3.99,50,Local Dairy Co,Refrigerated
P002,987654321,Bread,Wonder,2.49,100,Bakery Inc,Room Temperature
```

### **How It Works**
- **On Startup:** System automatically loads all users and products from CSV files
- **On Changes:** Any modification (add product, update stock, create user) is immediately saved to CSV
- **Persistence:** Data survives application restarts
- **Manual Editing:** CSV files can be manually edited when the application is closed

### **CSV File Management**
- Files use comma-separated values with headers in the first row
- All changes are written immediately (no manual save required)
- Default admin account is pre-populated in `users.csv`

---

## ⚙️ Current Limitations

- Basic inventory tracking only
- No reporting or analytics features (planned for Iteration III)
- No expiry date tracking (planned for future iterations)
- No low-stock notifications (planned for Iteration II)

---

## 🔮 Planned Enhancements (Upcoming Iterations)

### **Iteration II**
- Audit logging for all inventory changes
- Low-stock notifications
- Promotional pricing features
- Multi-buy discount logic

### **Iteration III**
- Sales trend analysis
- Inventory reports
- Reorder suggestions based on sales data
- Expiry tracking
- Enhanced CSV reporting features

---


# Build and Run Instructions

This project includes a **smart** Windows batch script (`build.bat`) and a Linux shell script (`build.sh`) that attempts to automatically find your Java Development Kit (JDK) and run the application without manual configuration.

## Prerequisites

1.  **Java Development Kit (JDK) 11 or higher** must be installed.
    *   If you don't have it, download and install it from Oracle.
    *   **Note:** You need the **JDK**, not just the JRE. The JRE only runs Java programs; the JDK includes the compiler (`javac`) needed to build them.

## How to Run (The Easy Way)

1.  Double-click `build.bat` or `built.sh` in the project root folder.
2.  **That's it!** The script will:
    *   Automatically search for your JDK installation in common locations.
    *   Compile the source code.
    *   Launch the application.

## Troubleshooting

### "javac is not recognized..." or "Could not find a JDK installation"
If the script fails with this error, it means it couldn't find your JDK in the standard installation folders (`C:\Program Files\Java`, etc.).

**Solution 1: Reinstall JDK to Default Location**
Uninstall your current JDK and reinstall it, accepting the default installation path (usually `C:\Program Files\Java\...`).

**Solution 2: Manually Set JAVA_HOME (Advanced)**
1.  Find where your JDK is installed (look for a folder containing `bin\javac.exe`).
2.  Open `build.bat` or `build.sh` in a text editor (Notepad).
3.  Find the section labeled `:: JAVA DETECTION`.
4.  Add a line at the top of that section:
    ```bat
    set JAVA_HOME=C:\Path\To\Your\JDK
    set PATH=%JAVA_HOME%\bin;%PATH%
    goto :FOUND_JAVAC
    ```

### "Compilation failed!"
This means there are errors in the Java code itself (e.g., syntax errors). The script will pause and display the error messages.
*   Check the error output in the console window.
*   Open the corresponding `.java` file in your IDE to fix the issue.

### "File not found" errors
Ensure you are running the script from the **root** of the project folder (the folder containing `src` and `build.bat` or `build.sh`). Do not move the script to another folder.


## 💡 Usage Tips

### **Login**
- Use the default credentials: `admin` / `admin123`
- The system validates credentials and grants appropriate role-based access

### **Adding Products**
- Product ID and Barcode must be unique
- All fields are required for successful product creation

### **Stock Management**
- Select a product from the table before processing sales or shipments
- The system automatically validates quantities to prevent negative inventory

### **Role-Based Features**
- Manager accounts have full access to all features
- Employee accounts can view inventory and process sales only

---

## 📝 Development Notes

This system is being developed iteratively following Agile principles. The current version implements the core MVP functionality with basic CRUD operations and role-based access. Future iterations will expand on reporting, notifications, and intelligent restocking features.