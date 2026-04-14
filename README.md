# 🛒 Keystone Systems - Grocery Store Inventory Management

> [!IMPORTANT]
> **For End-Users:** Download the latest standalone Windows executable from our **[Releases Page](https://github.com/NoohAlavi/KeystoneSystemsLab/releases)**. No Java installation required!

## 📌 Overview

Keystone Systems is a professional software application designed to help grocery stores efficiently manage inventory, track stock levels, and maintain accurate product records.

Featuring a modern dark-themed dashboard, real-time currency conversion, and data-driven logistics analytics, it empowers both managers and employees to maintain accurate operational integrity.

This project is being developed using Agile methodologies across multiple iterations.

---

## 🚀 Key Updates (Latest Iteration)

- **Logistics & Analytics Dashboard** - Real-time visualizations for stock levels, storage distribution, and top-selling products.
- **Modern UI Overhaul** - Sleek sidebar navigation with Helvetica typography and a Courier New data log aesthetic.
- **Enhanced Search & Sorting** - Dynamic multi-attribute filtering and intelligent numeric sorting.
- **Audit Logging** - Every stock change is now recorded with a timestamp and reason for full traceability.

---

## 🏗️ Project Structure

```text
KeystoneSystemsLab/
├── src/inventory/
│   ├── Main.java                # Application entry point
│   ├── AppFrame.java            # Main application window
│   ├── model/
│   │   ├── Product.java         # Product data model
│   │   ├── User.java            # User data model
│   │   ├── Role.java            # User role enum (MANAGER/EMPLOYEE)
│   │   └── InventoryEvent.java  # Stock transaction model
│   ├── service/
│   │   ├── AuthService.java     # Authentication logic
│   │   └── InventoryService.java # Business logic & data handling
│   ├── ui/
│   │   ├── LoginPanel.java      # Login interface
│   │   ├── InventoryPanel.java  # Main dashboard
│   │   ├── LogisticsPanel.java  # Analytics charts
│   │   └── OrderHistoryDialog.java # Transaction history view
│   └── util/
│       ├── CSVHandler.java      # Persistence utility
│       └── CurrencyConverter.java # API-driven conversion
├── libs/                        # External libraries (org.json)
├── data/                        # CSV database files
├── build.bat                    # Auto-detecting build script
└── package_app.bat              # Standalone EXE generator
```

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
- **Iteration II:** Auditing, notifications, and UI Overhaul.
- **Iteration III:** Reporting, trend analysis, and logistics dashboard.

---

## 🖥️ System Architecture

### **Technology Stack**
- **Language:** Java
- **GUI Framework:** Java Swing
- **Architecture:** MVC (Model-View-Controller)
- **IDE:** IntelliJ IDEA

---

## 🛠️ Developer Setup & Packaging

### **Prerequisites**
- **Java JDK 17 or 21** (Required for compilation).
- Any Java IDE (IntelliJ IDEA recommended).
- The `org.json` library (included in `libs/`).

### **Standard Build & Run**
To compile and run the project immediately:
1. Double-click `build.bat` in the root directory.
2. The script will auto-detect your JDK and launch the app.

### **Packaging as a Standalone EXE**
To create a professional Windows `.exe` installer that includes its own Java runtime:
1. Run `package_app.bat`.
2. Find your packaged application in the `package_out/` folder.
3. This creates a portable version ready for distribution to clients.

---

## 📖 User Guide

### **Authentication System**
The system uses role-based access control:
- **Manager:** Full access to all features (Logistics, Admin, Product Management).
- **Employee:** Limited access (View dashboard, Search, and Process Sales).

### **Default Login Credentials**
- **Username:** `admin`
- **Password:** `admin123`
- **Role:** Manager

---

## 🔧 Features & Operations

### **For All Users (Manager & Employee)**

#### **Inventory Dashboard**
- **Global Search:** Instant filtering across all attributes (ID, Name, Supplier, etc.).
- **Smart Sorting:** Click any column header or use the "Sort By" dropdown.
- **Currency Control:** Switch between CAD, USD, EUR, and more with live exchange rates.

#### **Stock Control**
- **Process Sales:** Employees and Managers can select products to decrease stock upon purchase.
- **Input Validation:** Prevents negative inventory and ensures data integrity.

### **Manager-Only Features**

#### **Logistics & Analytics**
- **Stock Levels:** Visual warnings for low-stock items.
- **Sales Trends:** Automatically calculated "Top Selling Products" based on transaction history.
- **Storage Metrics:** Distribution overview of Frozen vs. Ambient storage.

#### **Product Management**
- **Add Product:** Register new inventory items with unique IDs and barcodes.
- **Edit Product:** Update details like names, brands, and prices dynamically.

#### **Administration**
- **Audit History:** View the full `inventory_events.csv` log through the history dialog.
- **Account Creation:** Securely create new employee accounts.

---

## 💾 Data Persistence

The system uses **CSV files** for human-readable persistence:
- `data/products.csv`: Main product database.
- `data/users.csv`: User accounts and roles.
- `data/inventory_events.csv`: Audit trail of all stock movements.

---

# ⚙️ Build and Run Instructions (Detailed)

This project includes a **smart** Windows batch script (`build.bat`) that attempts to automatically find your Java Development Kit (JDK) and run the application without manual configuration.

## Prerequisites

1.  **Java Development Kit (JDK) 11 or higher** must be installed.
    *   **Note:** You need the **JDK**, not just the JRE.

## Troubleshooting

### "javac is not recognized..." or "Could not find a JDK installation"
If the script fails with this error, it means it couldn't find your JDK in the standard installation folders.

**Solution 1: Reinstall JDK to Default Location**
Uninstall your current JDK and reinstall it, accepting the default installation path (usually `C:\Program Files\Java\...`).

**Solution 2: Manually Set JAVA_HOME (Advanced)**
1.  Find where your JDK is installed (look for a folder containing `bin\javac.exe`).
2.  Open `build.bat` in a text editor.
3.  Find the section labeled `:: JAVA DETECTION`.
4.  Add a line at the top of that section:
    ```bat
    set JAVA_HOME=C:\Path\To\Your\JDK
    set PATH=%JAVA_HOME%\bin;%PATH%
    goto :FOUND_JAVAC
    ```

### "Compilation failed!"
Check the error output in the console window. Ensure you have the `libs/` folder containing the required JARs.

---

## 📝 Development Notes

Keystone Systems is built using an Agile MVC architecture. It is designed for maximum portability and minimal dependencies, making it easy to deploy in local retail environments.
