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

    private JPanel mainContent;
    private CardLayout cardLayout;
    private JPanel inventoryContent;
    private LogisticsPanel logisticsContent;

    // Consistency Theme
    private final Color DARK_BG = new Color(45, 52, 54);
    private final Color ACCENT_BLUE_GREEN = new Color(0, 184, 148);
    private final Font BUTTON_FONT = new Font("Helvetica", Font.BOLD, 14);
    private final Font TABLE_FONT = new Font("Courier New", Font.PLAIN, 12);

    public InventoryPanel(AuthService authService, InventoryService inventoryService, Runnable onLogout) {
        this.authService = authService;
        this.inventoryService = inventoryService;
        this.onLogout = onLogout;

        setLayout(new BorderLayout());
        setBackground(DARK_BG);

        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(DARK_BG);

        inventoryContent = createInventoryContent();
        logisticsContent = new LogisticsPanel(inventoryService);

        mainContent.add(inventoryContent, "INVENTORY");
        mainContent.add(logisticsContent, "LOGISTICS");

        add(createSidebar(), BorderLayout.WEST);
        add(mainContent, BorderLayout.CENTER);

        refreshInventoryTable();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(DARK_BG);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));

        // Header
        JLabel title = new JLabel("KEYSTONE");
        title.setFont(new Font("Helvetica", Font.BOLD, 22));
        title.setForeground(ACCENT_BLUE_GREEN);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(title);

        sidebar.add(Box.createRigidArea(new Dimension(0, 30)));

        // Navigation Buttons (Common to all roles)
        addSidebarButton(sidebar, "Dashboard", e -> cardLayout.show(mainContent, "INVENTORY"));
        addSidebarButton(sidebar, "Stock Control", e -> showStockOptions());
        
        // Navigation Buttons (Manager only)
        if (authService.isCurrentUserManager()) {
            addSidebarButton(sidebar, "Logistics & Analytics", e -> {
                logisticsContent.repaint();
                cardLayout.show(mainContent, "LOGISTICS");
            });
            addSidebarButton(sidebar, "Product Management", e -> showProductOptions());
            addSidebarButton(sidebar, "Transaction History", e -> handleViewOrderHistory());
            addSidebarButton(sidebar, "Administration", e -> showAdminOptions());
        }

        sidebar.add(Box.createVerticalGlue());

        // Logout
        JButton logoutBtn = new JButton("LOGOUT");
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setBackground(new Color(231, 76, 60));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFont(BUTTON_FONT);
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
        btn.setBackground(DARK_BG);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(BUTTON_FONT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(listener);
        
        panel.add(btn);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    private JPanel createInventoryContent() {
        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setBackground(DARK_BG);

        // Top Control Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);
        searchField = new JTextField(15);
        searchField.setBackground(new Color(60, 63, 65));
        searchField.setForeground(Color.WHITE);
        searchField.setCaretColor(Color.WHITE);
        searchField.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));
        searchField.addActionListener(e -> handleSearch());
        
        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setForeground(Color.LIGHT_GRAY);
        searchLbl.setFont(new Font("Helvetica", Font.PLAIN, 12));
        searchPanel.add(searchLbl);
        searchPanel.add(searchField);

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setOpaque(false);

        currencySelector = new JComboBox<>(CurrencyConverter.getAvailableCurrencies());
        currencySelector.setSelectedItem(currentCurrency);
        currencySelector.addActionListener(e -> {
            currentCurrency = (String) currencySelector.getSelectedItem();
            refreshInventoryTable();
        });
        filterPanel.add(new JLabel("Currency:") {{ setForeground(Color.LIGHT_GRAY); }});
        filterPanel.add(currencySelector);

        sortSelector = new JComboBox<>(new String[]{"ID", "Barcode", "Name", "Brand", "Price", "Quantity", "Supplier", "Storage"});
        sortSelector.addActionListener(e -> {
            if (isUpdatingSortUI) return;
            sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(sortSelector.getSelectedIndex(), SortOrder.ASCENDING)));
            sorter.sort();
        });
        filterPanel.add(new JLabel("Sort By:") {{ setForeground(Color.LIGHT_GRAY); }});
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
        inventoryTable.setFont(TABLE_FONT);
        inventoryTable.setBackground(new Color(60, 63, 65));
        inventoryTable.setForeground(Color.WHITE);
        inventoryTable.setGridColor(new Color(80, 80, 80));
        inventoryTable.setRowHeight(30);
        
        inventoryTable.getTableHeader().setBackground(DARK_BG);
        inventoryTable.getTableHeader().setForeground(ACCENT_BLUE_GREEN);
        inventoryTable.getTableHeader().setFont(new Font("Helvetica", Font.BOLD, 12));

        sorter = new TableRowSorter<>(tableModel);
        inventoryTable.setRowSorter(sorter);

        sorter.addRowSorterListener(e -> {
            if (e.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED && !sorter.getSortKeys().isEmpty()) {
                isUpdatingSortUI = true;
                sortSelector.setSelectedIndex(sorter.getSortKeys().get(0).getColumn());
                isUpdatingSortUI = false;
            }
        });

        inventoryTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            { setHorizontalAlignment(SwingConstants.RIGHT); }
            @Override protected void setValue(Object v) {
                if (v instanceof Number) {
                    setText(CurrencyConverter.format(((Number) v).floatValue(), currentCurrency));
                    setForeground(ACCENT_BLUE_GREEN);
                } else setText("");
            }
        });

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.getViewport().setBackground(new Color(60, 63, 65));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
        content.add(scrollPane, BorderLayout.CENTER);

        return content;
    }

    private void showStockOptions() {
        String[] options;
        if (authService.isCurrentUserManager()) {
            options = new String[]{"Decrease Stock (Sale)", "Increase Stock (Shipment)", "Record Damage/Loss", "Cancel"};
        } else {
            // Employee options (No shipments/manager-only losses usually, but we keep Sale)
            options = new String[]{"Decrease Stock (Sale)", "Cancel"};
        }

        int choice = JOptionPane.showOptionDialog(this, "Select Stock Action:", "Stock Control",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (authService.isCurrentUserManager()) {
            switch (choice) {
                case 0: handleDecreaseStock(); break;
                case 1: handleIncreaseStock(); break;
                case 2: handleRecordProductEvent(); break;
            }
        } else {
            if (choice == 0) handleDecreaseStock();
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

    private void handleViewOrderHistory() {
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
