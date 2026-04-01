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
            int colIndex = sortSelector.getSelectedIndex();
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
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                if (c == 4) return Double.class;
                if (c == 5) return Integer.class;
                return String.class;
            }
        };

        inventoryTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);

        Comparator<Object> numericComp = (o1, o2) -> {
            try {
                return Long.compare(Long.parseLong(o1.toString()), Long.parseLong(o2.toString()));
            } catch (Exception e) { return o1.toString().compareTo(o2.toString()); }
        };
        sorter.setComparator(0, numericComp);
        sorter.setComparator(1, numericComp);

        sorter.addRowSorterListener(e -> {
            if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED && !sorter.getSortKeys().isEmpty()) {
                isUpdatingSortUI = true;
                sortSelector.setSelectedIndex(sorter.getSortKeys().get(0).getColumn());
                isUpdatingSortUI = false;
            }
        });

        inventoryTable.setRowSorter(sorter);

        sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(0, SortOrder.ASCENDING)));
        sorter.sort();

        inventoryTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            { setHorizontalAlignment(SwingConstants.RIGHT); }
            @Override protected void setValue(Object v) {
                if (v instanceof Number) setText(CurrencyConverter.format(((Number) v).floatValue(), currentCurrency));
                else setText("");
            }
        });

        add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(15);
        searchField.addActionListener(e -> handleSearch());
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> handleSearch());
        searchPanel.add(searchButton);

        JButton refreshButton = new JButton("Show All");
        refreshButton.addActionListener(e -> { searchField.setText(""); refreshInventoryTable(); });
        searchPanel.add(refreshButton);
        bottomPanel.add(searchPanel, BorderLayout.NORTH);

        JPanel actionPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = 0;
        JButton decreaseStockBtn = new JButton("Decrease Stock (Sale)");
        decreaseStockBtn.addActionListener(e -> handleDecreaseStock());
        actionPanel.add(decreaseStockBtn, gbc);

        if (authService.isCurrentUserManager()) {
            JButton addProductBtn = new JButton("Add Product");
            addProductBtn.addActionListener(e -> handleAddProduct());
            actionPanel.add(addProductBtn, gbc);

            JButton editProductBtn = new JButton("Edit Product");
            editProductBtn.addActionListener(e -> handleEditProduct());
            actionPanel.add(editProductBtn, gbc);

            gbc.gridy = 1;
            JButton increaseStockBtn = new JButton("Increase Stock (Shipment)");
            increaseStockBtn.addActionListener(e -> handleIncreaseStock());
            actionPanel.add(increaseStockBtn, gbc);

            JButton recordEventBtn = new JButton("Record Damaged/Returned/Expired");
            recordEventBtn.addActionListener(e -> handleRecordProductEvent());
            actionPanel.add(recordEventBtn, gbc);

            // BUTTON UPDATED
            JButton historyBtn = new JButton("Transaction History");
            historyBtn.addActionListener(e -> handleViewOrderHistory());
            actionPanel.add(historyBtn, gbc);

            JButton createUserBtn = new JButton("Create Employee");
            createUserBtn.addActionListener(e -> handleCreateUser());
            actionPanel.add(createUserBtn, gbc);
        }

        bottomPanel.add(actionPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void handleViewOrderHistory() {
        List<String[]> history = inventoryService.getFullEventHistory();
        if (history.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No transaction history found.");
            return;
        }
        OrderHistoryDialog dialog = new OrderHistoryDialog((Frame) SwingUtilities.getWindowAncestor(this), history);
        dialog.setVisible(true);
    }

    private void refreshInventoryTable() {
        tableModel.setRowCount(0);
        for (Product product : inventoryService.getAllProducts()) {
            float price = CurrencyConverter.convert((float) product.getPrice(), "CAD", currentCurrency);
            tableModel.addRow(new Object[]{
                    product.getId(), product.getBarcode(), product.getName(), product.getBrand(),
                    (double) price, product.getQuantity(), product.getSupplier(), product.getStorageCondition()
            });
        }
    }

    private void handleSearch() {
        String term = searchField.getText().trim().toLowerCase();
        if (term.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter search term"); return; }
        tableModel.setRowCount(0);
        boolean found = false;
        for (Product p : inventoryService.getAllProducts()) {
            if (p.getName().toLowerCase().contains(term) || p.getId().toLowerCase().contains(term) || p.getBarcode().contains(term)) {
                float price = CurrencyConverter.convert((float) p.getPrice(), "CAD", currentCurrency);
                tableModel.addRow(new Object[]{p.getId(), p.getBarcode(), p.getName(), p.getBrand(), (double) price, p.getQuantity(), p.getSupplier(), p.getStorageCondition()});
                found = true;
            }
        }
        if (!found) JOptionPane.showMessageDialog(this, "No matches found.");
    }

    private void handleDecreaseStock() {
        String id = JOptionPane.showInputDialog(this, "Product ID:");
        String qty = JOptionPane.showInputDialog(this, "Quantity:");
        if (id != null && qty != null) {
            try {
                if (inventoryService.decreaseStock(id.trim(), Integer.parseInt(qty.trim()), "Customer Purchase")) {
                    refreshInventoryTable();
                } else JOptionPane.showMessageDialog(this, "Error decreasing stock.");
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Invalid input."); }
        }
    }

    private void handleIncreaseStock() {
        String id = JOptionPane.showInputDialog(this, "Product ID:");
        String qty = JOptionPane.showInputDialog(this, "Quantity:");
        String reason = JOptionPane.showInputDialog(this, "Reason (e.g. Shipment):");
        if (id != null && qty != null) {
            try {
                if (inventoryService.increaseStock(id.trim(), Integer.parseInt(qty.trim()), reason)) {
                    refreshInventoryTable();
                } else JOptionPane.showMessageDialog(this, "Error increasing stock.");
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Invalid input."); }
        }
    }

    private void handleRecordProductEvent() {
        String id = JOptionPane.showInputDialog(this, "Product ID:");
        if (id == null || id.trim().isEmpty()) return;
        Product p = inventoryService.getProductById(id.trim());
        if (p == null) { JOptionPane.showMessageDialog(this, "Not found."); return; }

        JComboBox<String> typeBox = new JComboBox<>(new String[]{"DAMAGED", "RETURNED", "EXPIRED"});
        JTextField qtyField = new JTextField();
        JTextField notesField = new JTextField();
        Object[] msg = {"Type:", typeBox, "Qty:", qtyField, "Notes:", notesField};

        if (JOptionPane.showConfirmDialog(this, msg, "Record Event", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                inventoryService.recordProductEvent(id.trim(), (String) typeBox.getSelectedItem(), Integer.parseInt(qtyField.getText()), notesField.getText());
                refreshInventoryTable();
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error."); }
        }
    }

    private void handleAddProduct() {
        JTextField idF = new JTextField(); JTextField barF = new JTextField(); JTextField nameF = new JTextField();
        JTextField brandF = new JTextField(); JTextField priceF = new JTextField(); JTextField qtyF = new JTextField();
        JTextField supF = new JTextField(); JTextField storF = new JTextField();
        Object[] msg = {"ID:", idF, "Barcode:", barF, "Name:", nameF, "Brand:", brandF, "Price:", priceF, "Qty:", qtyF, "Supplier:", supF, "Storage:", storF};
        if (JOptionPane.showConfirmDialog(this, msg, "Add Product", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                Product p = new Product(idF.getText(), barF.getText(), nameF.getText(), brandF.getText(), Double.parseDouble(priceF.getText()), Integer.parseInt(qtyF.getText()), supF.getText(), storF.getText());
                if (inventoryService.addProduct(p)) refreshInventoryTable();
                else JOptionPane.showMessageDialog(this, "ID already exists.");
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Invalid entry."); }
        }
    }

    private void handleEditProduct() {
        String id = JOptionPane.showInputDialog(this, "Edit ID:");
        if (id == null) return;
        Product p = inventoryService.getProductById(id.trim());
        if (p == null) return;
        JTextField nameF = new JTextField(p.getName()); JTextField priceF = new JTextField(String.valueOf(p.getPrice()));
        Object[] msg = {"Name:", nameF, "Price:", priceF};
        if (JOptionPane.showConfirmDialog(this, msg, "Edit", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            inventoryService.updateProduct(id.trim(), nameF.getText(), p.getBrand(), Double.parseDouble(priceF.getText()), p.getSupplier(), p.getStorageCondition());
            refreshInventoryTable();
        }
    }

    private void handleCreateUser() {
        JTextField userF = new JTextField(); JPasswordField passF = new JPasswordField(); JComboBox<Role> roleF = new JComboBox<>(Role.values());
        Object[] msg = {"User:", userF, "Pass:", passF, "Role:", roleF};
        if (JOptionPane.showConfirmDialog(this, msg, "Create Account", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            authService.createUser(userF.getText(), new String(passF.getPassword()), userF.getText(), (Role) roleF.getSelectedItem());
        }
    }
}