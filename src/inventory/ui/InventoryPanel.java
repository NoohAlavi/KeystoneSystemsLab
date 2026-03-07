package inventory.ui;

import inventory.model.Product;
import inventory.model.Role;
import inventory.service.AuthService;
import inventory.service.InventoryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class InventoryPanel extends JPanel {
    private AuthService authService;
    private InventoryService inventoryService;
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private Runnable onLogout;

    public InventoryPanel(AuthService authService, InventoryService inventoryService, Runnable onLogout) {
        this.authService = authService;
        this.inventoryService = inventoryService;
        this.onLogout = onLogout;

        setLayout(new BorderLayout(10, 10));
        initComponents();
        refreshInventoryTable();
    }

    private void initComponents() {
        // Top panel - User info and logout
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel userLabel = new JLabel("Logged in as: " + authService.getCurrentUser().getName() +
            " (" + authService.getCurrentUser().getRole() + ")");
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        topPanel.add(userLabel, BorderLayout.WEST);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            authService.logout();
            onLogout.run();
        });
        topPanel.add(logoutButton, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center panel - Inventory table
        String[] columns = {"ID", "Barcode", "Name", "Brand", "Price", "Quantity", "Supplier", "Storage"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        inventoryTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel - Actions
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search by ID/Barcode:"));
        searchField = new JTextField(15);
        searchPanel.add(searchField);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> handleSearch());
        searchPanel.add(searchButton);
        JButton refreshButton = new JButton("Show All");
        refreshButton.addActionListener(e -> refreshInventoryTable());
        searchPanel.add(refreshButton);
        bottomPanel.add(searchPanel, BorderLayout.NORTH);

        // Action buttons panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // Employee buttons
        JButton decreaseStockBtn = new JButton("Decrease Stock (Sale)");
        decreaseStockBtn.addActionListener(e -> handleDecreaseStock());
        actionPanel.add(decreaseStockBtn);

        // Manager buttons
        if (authService.isCurrentUserManager()) {
            JButton addProductBtn = new JButton("Add Product");
            addProductBtn.addActionListener(e -> handleAddProduct());
            actionPanel.add(addProductBtn);

            JButton editProductBtn = new JButton("Edit Product");
            editProductBtn.addActionListener(e -> handleEditProduct());
            actionPanel.add(editProductBtn);

            JButton increaseStockBtn = new JButton("Increase Stock (Shipment)");
            increaseStockBtn.addActionListener(e -> handleIncreaseStock());
            actionPanel.add(increaseStockBtn);

            JButton createUserBtn = new JButton("Create Employee");
            createUserBtn.addActionListener(e -> handleCreateUser());
            actionPanel.add(createUserBtn);
        }

        bottomPanel.add(actionPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void refreshInventoryTable() {
        tableModel.setRowCount(0);
        for (Product product : inventoryService.getAllProducts()) {
            tableModel.addRow(new Object[]{
                product.getId(),
                product.getBarcode(),
                product.getName(),
                product.getBrand(),
                String.format("$%.2f", product.getPrice()),
                product.getQuantity(),
                product.getSupplier(),
                product.getStorageCondition()
            });
        }
    }

    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an ID or barcode");
            return;
        }

        Product product = inventoryService.getProductById(searchTerm);
        if (product == null) {
            product = inventoryService.getProductByBarcode(searchTerm);
        }

        if (product != null) {
            String details = String.format(
                "Product Details:\n\n" +
                "ID: %s\n" +
                "Barcode: %s\n" +
                "Name: %s\n" +
                "Brand: %s\n" +
                "Price: $%.2f\n" +
                "Quantity: %d\n" +
                "Supplier: %s\n" +
                "Storage: %s",
                product.getId(), product.getBarcode(), product.getName(),
                product.getBrand(), product.getPrice(), product.getQuantity(),
                product.getSupplier(), product.getStorageCondition()
            );
            JOptionPane.showMessageDialog(this, details, "Product Found", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Product not found", "Search", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void handleDecreaseStock() {
        String id = JOptionPane.showInputDialog(this, "Enter Product ID:");
        if (id == null || id.trim().isEmpty()) return;

        String amountStr = JOptionPane.showInputDialog(this, "Enter quantity to decrease:");
        if (amountStr == null || amountStr.trim().isEmpty()) return;

        try {
            int amount = Integer.parseInt(amountStr.trim());
            if (inventoryService.decreaseStock(id.trim(), amount)) {
                JOptionPane.showMessageDialog(this, "Stock decreased successfully");
                refreshInventoryTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to decrease stock. Check product ID and quantity.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleIncreaseStock() {
        String id = JOptionPane.showInputDialog(this, "Enter Product ID:");
        if (id == null || id.trim().isEmpty()) return;

        String amountStr = JOptionPane.showInputDialog(this, "Enter quantity to increase:");
        if (amountStr == null || amountStr.trim().isEmpty()) return;

        try {
            int amount = Integer.parseInt(amountStr.trim());
            if (inventoryService.increaseStock(id.trim(), amount)) {
                JOptionPane.showMessageDialog(this, "Stock increased successfully");
                refreshInventoryTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to increase stock. Check product ID.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAddProduct() {
        JTextField idField = new JTextField();
        JTextField barcodeField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField brandField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField quantityField = new JTextField();
        JTextField supplierField = new JTextField();
        JTextField storageField = new JTextField();

        Object[] message = {
            "ID:", idField,
            "Barcode:", barcodeField,
            "Name:", nameField,
            "Brand:", brandField,
            "Price:", priceField,
            "Quantity:", quantityField,
            "Supplier:", supplierField,
            "Storage Condition:", storageField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add New Product", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                Product product = new Product(
                    idField.getText().trim(),
                    barcodeField.getText().trim(),
                    nameField.getText().trim(),
                    brandField.getText().trim(),
                    Double.parseDouble(priceField.getText().trim()),
                    Integer.parseInt(quantityField.getText().trim()),
                    supplierField.getText().trim(),
                    storageField.getText().trim()
                );

                if (inventoryService.addProduct(product)) {
                    JOptionPane.showMessageDialog(this, "Product added successfully");
                    refreshInventoryTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Product ID or barcode already exists",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid price or quantity",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleEditProduct() {
        String id = JOptionPane.showInputDialog(this, "Enter Product ID to edit:");
        if (id == null || id.trim().isEmpty()) return;

        Product product = inventoryService.getProductById(id.trim());
        if (product == null) {
            JOptionPane.showMessageDialog(this, "Product not found", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField nameField = new JTextField(product.getName());
        JTextField brandField = new JTextField(product.getBrand());
        JTextField priceField = new JTextField(String.valueOf(product.getPrice()));
        JTextField supplierField = new JTextField(product.getSupplier());
        JTextField storageField = new JTextField(product.getStorageCondition());

        Object[] message = {
            "Name:", nameField,
            "Brand:", brandField,
            "Price:", priceField,
            "Supplier:", supplierField,
            "Storage Condition:", storageField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Edit Product", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                inventoryService.updateProduct(
                    id.trim(),
                    nameField.getText().trim(),
                    brandField.getText().trim(),
                    Double.parseDouble(priceField.getText().trim()),
                    supplierField.getText().trim(),
                    storageField.getText().trim()
                );
                JOptionPane.showMessageDialog(this, "Product updated successfully");
                refreshInventoryTable();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid price", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleCreateUser() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField nameField = new JTextField();
        JComboBox<Role> roleCombo = new JComboBox<>(Role.values());

        Object[] message = {
            "Username:", usernameField,
            "Password:", passwordField,
            "Full Name:", nameField,
            "Role:", roleCombo
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Create Employee Account",
            JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            if (authService.createUser(
                usernameField.getText().trim(),
                new String(passwordField.getPassword()),
                nameField.getText().trim(),
                (Role) roleCombo.getSelectedItem()
            )) {
                JOptionPane.showMessageDialog(this, "Employee account created successfully");
            } else {
                JOptionPane.showMessageDialog(this, "Username already exists",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
