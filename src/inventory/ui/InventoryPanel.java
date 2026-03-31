package inventory.ui;

import inventory.model.Product;
import inventory.model.Role;
import inventory.service.AuthService;
import inventory.service.InventoryService;
import inventory.util.CurrencyConverter;

import javax.swing.*;
import javax.swing.event.RowSorterEvent;
import javax.swing.table.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class InventoryPanel extends JPanel {
    private AuthService authService;
    private InventoryService inventoryService;
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private Runnable onLogout;

    private JComboBox<String> currencySelector;
    private String currentCurrency = "CAD";

    private JComboBox<String> sortSelector;
    private TableRowSorter<DefaultTableModel> sorter;
    private boolean isUpdatingSortUI = false;

    public InventoryPanel(AuthService authService, InventoryService inventoryService, Runnable onLogout) {
        this.authService = authService;
        this.inventoryService = inventoryService;
        this.onLogout = onLogout;

        setLayout(new BorderLayout(10, 10));
        initComponents();
        refreshInventoryTable();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel(new BorderLayout());

        JLabel userLabel = new JLabel("Logged in as: " +
                authService.getCurrentUser().getName() +
                " (" + authService.getCurrentUser().getRole() + ")");
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        topPanel.add(userLabel, BorderLayout.WEST);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));

        JPanel currencyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        currencyPanel.add(new JLabel("Currency:"));

        String[] currencies = CurrencyConverter.getAvailableCurrencies();
        currencySelector = new JComboBox<>(currencies);
        currencySelector.setSelectedItem(currentCurrency);
        currencySelector.addActionListener(e -> {
            currentCurrency = (String) currencySelector.getSelectedItem();
            if (searchField != null && !searchField.getText().trim().isEmpty()) {
                handleSearch();
            } else {
                refreshInventoryTable();
            }
        });
        currencyPanel.add(currencySelector);
        controlsPanel.add(currencyPanel);

        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        sortPanel.add(new JLabel("Sort by:"));

        String[] sortOptions = {"ID", "Barcode", "Name", "Brand", "Price", "Quantity", "Supplier", "Storage"};
        sortSelector = new JComboBox<>(sortOptions);
        sortSelector.addActionListener(e -> {
            if (isUpdatingSortUI) return;
            String selected = (String) sortSelector.getSelectedItem();
            int colIndex = 0;
            switch (selected) {
                case "ID": colIndex = 0; break;
                case "Barcode": colIndex = 1; break;
                case "Name": colIndex = 2; break;
                case "Brand": colIndex = 3; break;
                case "Price": colIndex = 4; break;
                case "Quantity": colIndex = 5; break;
                case "Supplier": colIndex = 6; break;
                case "Storage": colIndex = 7; break;
            }
            sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(colIndex, SortOrder.ASCENDING)));
            sorter.sort();
        });
        sortPanel.add(sortSelector);
        controlsPanel.add(sortPanel);

        topPanel.add(controlsPanel, BorderLayout.CENTER);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            authService.logout();
            onLogout.run();
        });
        topPanel.add(logoutButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Barcode", "Name", "Brand", "Price", "Quantity", "Supplier", "Storage"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) return Double.class;
                if (columnIndex == 5) return Integer.class;
                return String.class;
            }
        };

        inventoryTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);

        Comparator<Object> numericStringComparator = (o1, o2) -> {
            try {
                long n1 = Long.parseLong(o1.toString());
                long n2 = Long.parseLong(o2.toString());
                return Long.compare(n1, n2);
            } catch (NumberFormatException e) {
                return o1.toString().compareTo(o2.toString());
            }
        };
        sorter.setComparator(0, numericStringComparator);
        sorter.setComparator(1, numericStringComparator);

        sorter.addRowSorterListener(e -> {
            if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
                List<? extends RowSorter.SortKey> keys = sorter.getSortKeys();
                if (!keys.isEmpty()) {
                    int colIndex = keys.get(0).getColumn();
                    String colName = tableModel.getColumnName(colIndex);
                    isUpdatingSortUI = true;
                    sortSelector.setSelectedItem(colName);
                    isUpdatingSortUI = false;
                }
            }
        });

        inventoryTable.setRowSorter(sorter);
        sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        sorter.sort();

        inventoryTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            { setHorizontalAlignment(SwingConstants.RIGHT); }
            @Override
            protected void setValue(Object value) {
                if (value instanceof Number) {
                    float price = ((Number) value).floatValue();
                    setText(CurrencyConverter.format(price, currentCurrency));
                } else {
                    setText("");
                }
            }
        });

        add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));

        searchField = new JTextField(15);
        searchField.addActionListener(e -> handleSearch());
        searchPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> handleSearch());
        searchPanel.add(searchButton);

        JButton refreshButton = new JButton("Show All");
        refreshButton.addActionListener(e -> {
            searchField.setText("");
            refreshInventoryTable();
        });
        searchPanel.add(refreshButton);

        bottomPanel.add(searchPanel, BorderLayout.NORTH);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton decreaseStockBtn = new JButton("Decrease Stock (Sale)");
        decreaseStockBtn.addActionListener(e -> handleDecreaseStock());
        actionPanel.add(decreaseStockBtn);

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

            // Kept Teammate's new button
            JButton recordEventBtn = new JButton("Record Damaged/Returned/Expired");
            recordEventBtn.addActionListener(e -> handleRecordProductEvent());
            actionPanel.add(recordEventBtn);

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
            float convertedPrice = CurrencyConverter.convert((float) product.getPrice(), "CAD", currentCurrency);
            tableModel.addRow(new Object[]{
                    product.getId(),
                    product.getBarcode(),
                    product.getName(),
                    product.getBrand(),
                    (double) convertedPrice,
                    product.getQuantity(),
                    product.getSupplier(),
                    product.getStorageCondition()
            });
        }
    }

    private void handleSearch() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        if (searchTerm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a search term");
            return;
        }
        tableModel.setRowCount(0);
        boolean found = false;
        for (Product product : inventoryService.getAllProducts()) {
            if (product.getId().toLowerCase().contains(searchTerm) ||
                    product.getBarcode().toLowerCase().contains(searchTerm) ||
                    product.getName().toLowerCase().contains(searchTerm) ||
                    product.getBrand().toLowerCase().contains(searchTerm) ||
                    product.getSupplier().toLowerCase().contains(searchTerm) ||
                    product.getStorageCondition().toLowerCase().contains(searchTerm) ||
                    String.valueOf(product.getPrice()).contains(searchTerm) ||
                    String.valueOf(product.getQuantity()).contains(searchTerm)) {

                float convertedPrice = CurrencyConverter.convert((float) product.getPrice(), "CAD", currentCurrency);
                tableModel.addRow(new Object[]{
                        product.getId(),
                        product.getBarcode(),
                        product.getName(),
                        product.getBrand(),
                        (double) convertedPrice,
                        product.getQuantity(),
                        product.getSupplier(),
                        product.getStorageCondition()
                });
                found = true;
            }
        }
        if (!found) {
            JOptionPane.showMessageDialog(this, "No products found matching \"" + searchTerm + "\"", "Search Results", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleDecreaseStock() {
        String id = JOptionPane.showInputDialog(this, "Enter Product ID:");
        if (id == null || id.trim().isEmpty()) return;

        String amountStr = JOptionPane.showInputDialog(this, "Enter quantity to decrease:");
        if (amountStr == null || amountStr.trim().isEmpty()) return;

        String reason = JOptionPane.showInputDialog(this, "Enter reason (e.g., sale, correction):");
        if (reason == null || reason.trim().isEmpty()) reason = "Sale";

        try {
            int amount = Integer.parseInt(amountStr.trim());
            if (inventoryService.decreaseStock(id.trim(), amount, reason)) {
                JOptionPane.showMessageDialog(this, "Stock decreased successfully");
                refreshInventoryTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to decrease stock.", "Error", JOptionPane.ERROR_MESSAGE);
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

        String reason = JOptionPane.showInputDialog(this, "Enter reason (e.g., shipment, restock):");
        if (reason == null || reason.trim().isEmpty()) reason = "Shipment";

        try {
            int amount = Integer.parseInt(amountStr.trim());
            if (inventoryService.increaseStock(id.trim(), amount, reason)) {
                JOptionPane.showMessageDialog(this, "Stock increased successfully");
                refreshInventoryTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to increase stock.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Kept Teammate's new method
    private void handleRecordProductEvent() {
        String id = JOptionPane.showInputDialog(this, "Enter Product ID:");
        if (id == null || id.trim().isEmpty()) return;

        Product product = inventoryService.getProductById(id.trim());
        if (product == null) {
            JOptionPane.showMessageDialog(this, "Product not found", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JComboBox<String> eventTypeBox = new JComboBox<>(new String[]{"DAMAGED", "RETURNED", "EXPIRED"});
        JTextField quantityField = new JTextField();
        JTextField notesField = new JTextField();

        Object[] message = {
                "Event Type:", eventTypeBox,
                "Quantity:", quantityField,
                "Notes:", notesField
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                message,
                "Record Product Event",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (option == JOptionPane.OK_OPTION) {
            try {
                String eventType = (String) eventTypeBox.getSelectedItem();
                int quantity = Integer.parseInt(quantityField.getText().trim());
                String notes = notesField.getText().trim();

                if (inventoryService.recordProductEvent(id.trim(), eventType, quantity, notes)) {
                    JOptionPane.showMessageDialog(this, "Product event recorded successfully");
                    refreshInventoryTable();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Could not record event. Check product ID and available stock.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid quantity", "Error", JOptionPane.ERROR_MESSAGE);
            }
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
                "Price (CAD):", priceField,
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
                    JOptionPane.showMessageDialog(this, "Product ID or barcode already exists", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid price or quantity", "Error", JOptionPane.ERROR_MESSAGE);
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
                "Price (CAD):", priceField,
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

        int option = JOptionPane.showConfirmDialog(this, message, "Create Employee Account", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            if (authService.createUser(
                    usernameField.getText().trim(),
                    new String(passwordField.getPassword()),
                    nameField.getText().trim(),
                    (Role) roleCombo.getSelectedItem()
            )) {
                JOptionPane.showMessageDialog(this, "Employee account created successfully");
            } else {
                JOptionPane.showMessageDialog(this, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}