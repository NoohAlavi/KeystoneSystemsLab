package inventory.ui;

import inventory.model.Product;
import inventory.model.Role;
import inventory.service.AuthService;
import inventory.service.InventoryService;
import inventory.util.CurrencyConverter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.RowSorterEvent;
import javax.swing.table.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class InventoryPanel extends JPanel {
    private final AuthService authService;
    private final InventoryService inventoryService;
    private final Runnable onLogout;

    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> currencySelector;
    private JComboBox<String> sortSelector;
    private TableRowSorter<DefaultTableModel> sorter;

    private String currentCurrency = "CAD";
    private boolean isUpdatingSortUI = false;

    // Sidebar color scheme
    private final Color SIDEBAR_COLOR = new Color(45, 52, 54);
    private final Color SIDEBAR_TEXT_COLOR = Color.WHITE;
    private final Color ACCENT_COLOR = new Color(0, 184, 148);

    public InventoryPanel(AuthService authService, InventoryService inventoryService, Runnable onLogout) {
        this.authService = authService;
        this.inventoryService = inventoryService;
        this.onLogout = onLogout;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);

        refreshInventoryTable();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));

        // Header
        JLabel title = new JLabel("KEYSTONE");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(ACCENT_COLOR);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(title);

        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));

        // User Info
        JLabel userLabel = new JLabel("User: " + authService.getCurrentUser().getName());
        userLabel.setForeground(Color.LIGHT_GRAY);
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(userLabel);

        sidebar.add(Box.createRigidArea(new Dimension(0, 40)));

        // Navigation Buttons
        addSidebarButton(sidebar, "Stock Control", e -> showStockOptions());
        
        if (authService.isCurrentUserManager()) {
            addSidebarButton(sidebar, "Product Management", e -> showProductOptions());
            addSidebarButton(sidebar, "Transaction History", e -> handleViewOrderHistory());
            addSidebarButton(sidebar, "Administration", e -> showAdminOptions());
        }

        sidebar.add(Box.createVerticalGlue());

        // Logout
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setBackground(new Color(231, 76, 60));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.addActionListener(e -> {
            authService.logout();
            onLogout.run();
        });
        sidebar.add(logoutBtn);

        return sidebar;
    }

    private void addSidebarButton(JPanel panel, String text, java.awt.event.ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(SIDEBAR_COLOR);
        btn.setForeground(SIDEBAR_TEXT_COLOR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.addActionListener(listener);
        
        // Hover effect placeholder (could be improved with MouseListener)
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        panel.add(btn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setBackground(Color.WHITE);

        // Top Control Bar (Search, Currency, Sort)
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);
        searchField = new JTextField(15);
        searchField.addActionListener(e -> handleSearch());
        searchPanel.add(new JLabel("Search Inventory:"));
        searchPanel.add(searchField);
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> handleSearch());
        searchPanel.add(searchBtn);
        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> { searchField.setText(""); refreshInventoryTable(); });
        searchPanel.add(clearBtn);

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setOpaque(false);

        currencySelector = new JComboBox<>(CurrencyConverter.getAvailableCurrencies());
        currencySelector.setSelectedItem(currentCurrency);
        currencySelector.addActionListener(e -> {
            currentCurrency = (String) currencySelector.getSelectedItem();
            refreshInventoryTable();
        });
        filterPanel.add(new JLabel("Currency:"));
        filterPanel.add(currencySelector);

        sortSelector = new JComboBox<>(new String[]{"ID", "Barcode", "Name", "Brand", "Price", "Quantity", "Supplier", "Storage"});
        sortSelector.addActionListener(e -> {
            if (isUpdatingSortUI) return;
            sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(sortSelector.getSelectedIndex(), SortOrder.ASCENDING)));
            sorter.sort();
        });
        filterPanel.add(new JLabel("Sort By:"));
        filterPanel.add(sortSelector);

        topBar.add(searchPanel, BorderLayout.WEST);
        topBar.add(filterPanel, BorderLayout.EAST);

        content.add(topBar, BorderLayout.NORTH);

        // Table
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
        inventoryTable.setRowHeight(25);
        inventoryTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        inventoryTable.setGridColor(new Color(240, 240, 240));

        sorter = new TableRowSorter<>(tableModel);
        inventoryTable.setRowSorter(sorter);

        // Synchronize sorter with dropdown
        sorter.addRowSorterListener(e -> {
            if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED && !sorter.getSortKeys().isEmpty()) {
                isUpdatingSortUI = true;
                sortSelector.setSelectedIndex(sorter.getSortKeys().get(0).getColumn());
                isUpdatingSortUI = false;
            }
        });

        // Custom renderer for currency
        inventoryTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            { setHorizontalAlignment(SwingConstants.RIGHT); }
            @Override protected void setValue(Object v) {
                if (v instanceof Number) setText(CurrencyConverter.format(((Number) v).floatValue(), currentCurrency));
                else setText("");
            }
        });

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        content.add(scrollPane, BorderLayout.CENTER);

        return content;
    }

    // --- Action Panels ---

    private void showStockOptions() {
        String[] options = {"Decrease Stock (Sale)", "Increase Stock (Shipment)", "Record Damage/Loss", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this, "Select Stock Action:", "Stock Control",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        switch (choice) {
            case 0: handleDecreaseStock(); break;
            case 1: handleIncreaseStock(); break;
            case 2: handleRecordProductEvent(); break;
        }
    }

    private void showProductOptions() {
        String[] options = {"Add New Product", "Edit Existing Product", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this, "Select Product Action:", "Product Management",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (choice == 0) handleAddProduct();
        else if (choice == 1) handleEditProduct();
    }

    private void showAdminOptions() {
        String[] options = {"Create New Employee", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this, "Select Admin Action:", "Administration",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (choice == 0) handleCreateUser();
    }

    // --- Existing Logic Updated ---

    private void handleViewOrderHistory() {
        // Assuming OrderHistoryDialog exists from previous context
        try {
            List<String[]> history = inventoryService.getFullEventHistory();
            OrderHistoryDialog dialog = new OrderHistoryDialog((Frame) SwingUtilities.getWindowAncestor(this), history);
            dialog.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "History feature not fully initialized.");
        }
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
        if (term.isEmpty()) { refreshInventoryTable(); return; }
        tableModel.setRowCount(0);
        for (Product p : inventoryService.getAllProducts()) {
            if (p.getName().toLowerCase().contains(term) || p.getId().toLowerCase().contains(term) || p.getBarcode().contains(term)) {
                float price = CurrencyConverter.convert((float) p.getPrice(), "CAD", currentCurrency);
                tableModel.addRow(new Object[]{p.getId(), p.getBarcode(), p.getName(), p.getBrand(), (double) price, p.getQuantity(), p.getSupplier(), p.getStorageCondition()});
            }
        }
    }

    private void handleDecreaseStock() {
        String id = JOptionPane.showInputDialog(this, "Enter Product ID:");
        if (id == null) return;
        String qty = JOptionPane.showInputDialog(this, "Enter Quantity to Decrease:");
        if (qty == null) return;
        try {
            if (inventoryService.decreaseStock(id.trim(), Integer.parseInt(qty.trim()), "Sale")) {
                refreshInventoryTable();
            } else JOptionPane.showMessageDialog(this, "Could not decrease stock.");
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Invalid quantity."); }
    }

    private void handleIncreaseStock() {
        String id = JOptionPane.showInputDialog(this, "Enter Product ID:");
        if (id == null) return;
        String qty = JOptionPane.showInputDialog(this, "Enter Quantity to Increase:");
        if (qty == null) return;
        String reason = JOptionPane.showInputDialog(this, "Reason for Shipment:");
        if (reason == null) return;
        try {
            if (inventoryService.increaseStock(id.trim(), Integer.parseInt(qty.trim()), reason)) {
                refreshInventoryTable();
            } else JOptionPane.showMessageDialog(this, "Could not increase stock.");
        } catch (Exception e) { JOptionPane.showMessageDialog(this, "Invalid quantity."); }
    }

    private void handleRecordProductEvent() {
        String id = JOptionPane.showInputDialog(this, "Enter Product ID:");
        if (id == null) return;
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"DAMAGED", "RETURNED", "EXPIRED"});
        JTextField qtyField = new JTextField();
        Object[] msg = {"Event Type:", typeBox, "Quantity:", qtyField};
        if (JOptionPane.showConfirmDialog(this, msg, "Record Loss Event", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                inventoryService.recordProductEvent(id.trim(), (String)typeBox.getSelectedItem(), Integer.parseInt(qtyField.getText()), "Recorded via GUI");
                refreshInventoryTable();
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Invalid entry."); }
        }
    }

    private void handleAddProduct() {
        JTextField idF = new JTextField(); JTextField barF = new JTextField(); JTextField nameF = new JTextField();
        JTextField brandF = new JTextField(); JTextField priceF = new JTextField(); JTextField qtyF = new JTextField();
        JTextField supF = new JTextField(); JTextField storF = new JTextField();
        Object[] msg = {"ID:", idF, "Barcode:", barF, "Name:", nameF, "Brand:", brandF, "Price:", priceF, "Qty:", qtyF, "Supplier:", supF, "Storage:", storF};
        if (JOptionPane.showConfirmDialog(this, msg, "Add New Product", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                Product p = new Product(idF.getText(), barF.getText(), nameF.getText(), brandF.getText(), Double.parseDouble(priceF.getText()), Integer.parseInt(qtyF.getText()), supF.getText(), storF.getText());
                if (inventoryService.addProduct(p)) refreshInventoryTable();
                else JOptionPane.showMessageDialog(this, "ID/Barcode conflict.");
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Invalid product details."); }
        }
    }

    private void handleEditProduct() {
        String id = JOptionPane.showInputDialog(this, "Enter Product ID to Edit:");
        if (id == null) return;
        Product p = inventoryService.getProductById(id.trim());
        if (p == null) { JOptionPane.showMessageDialog(this, "Product not found."); return; }
        
        JTextField nameF = new JTextField(p.getName()); 
        JTextField priceF = new JTextField(String.valueOf(p.getPrice()));
        Object[] msg = {"New Name:", nameF, "New Price:", priceF};
        
        if (JOptionPane.showConfirmDialog(this, msg, "Edit Product", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            inventoryService.updateProduct(id.trim(), nameF.getText(), p.getBrand(), Double.parseDouble(priceF.getText()), p.getSupplier(), p.getStorageCondition());
            refreshInventoryTable();
        }
    }

    private void handleCreateUser() {
        JTextField userF = new JTextField(); 
        JPasswordField passF = new JPasswordField(); 
        JComboBox<Role> roleF = new JComboBox<>(Role.values());
        Object[] msg = {"Username:", userF, "Password:", passF, "Role:", roleF};
        if (JOptionPane.showConfirmDialog(this, msg, "Create Employee Account", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            if (authService.createUser(userF.getText(), new String(passF.getPassword()), userF.getText(), (Role) roleF.getSelectedItem())) {
                JOptionPane.showMessageDialog(this, "User created successfully.");
            } else JOptionPane.showMessageDialog(this, "User already exists.");
        }
    }
}
