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
└── data/                        # CSV data files (planned)
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

## ⚙️ Current Limitations

- CSV-based data storage (planned for future implementation)
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
- CSV file integration for data persistence

---

## 🐛 Troubleshooting

### **Cannot Login**
- Verify username and password are correct
- Default credentials: `admin` / `admin123`

### **Cannot Add Product**
- Ensure Product ID and Barcode are unique
- Check all required fields are filled

### **Cannot Decrease Stock**
- Verify sufficient quantity is available
- Check that you've selected a product from the table

### **Manager Features Not Visible**
- Confirm you're logged in as a Manager
- Employee accounts have limited access by design

---

## 📝 Development Notes

This system is being developed iteratively following Agile principles. The current version implements the core MVP functionality with basic CRUD operations and role-based access. Future iterations will expand on reporting, notifications, and intelligent restocking features.