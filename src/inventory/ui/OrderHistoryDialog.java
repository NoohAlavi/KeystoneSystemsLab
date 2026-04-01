package inventory.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrderHistoryDialog extends JDialog {
    private JTable table;
    private DefaultTableModel model;
    private List<String[]> fullHistory;
    private JTextField productIdFilter;
    private JComboBox<String> typeFilterBox;

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a");

    public OrderHistoryDialog(Frame owner, List<String[]> history) {
        super(owner, "Inventory Transaction History", true);
        this.fullHistory = history;
        setSize(1000, 600);
        setLocationRelativeTo(owner);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));

        filterPanel.add(new JLabel("Event Type:"));
        String[] options = {"ALL EVENTS", "RESTOCK", "PURCHASE", "RETURNED", "LOSSES (DAMAGED/EXPIRED)"};
        typeFilterBox = new JComboBox<>(options);
        filterPanel.add(typeFilterBox);

        filterPanel.add(new JLabel("Product ID:"));
        productIdFilter = new JTextField(10);
        filterPanel.add(productIdFilter);

        JButton applyBtn = new JButton("Apply Filters");
        applyBtn.addActionListener(e -> applyFilters());
        filterPanel.add(applyBtn);

        JButton resetBtn = new JButton("Reset");
        resetBtn.addActionListener(e -> {
            productIdFilter.setText("");
            typeFilterBox.setSelectedIndex(0);
            applyFilters();
        });
        filterPanel.add(resetBtn);

        String[] columns = {"Date/Time", "Product ID", "Event Type", "Quantity", "Notes"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.getColumnModel().getColumn(0).setPreferredWidth(180);

        applyFilters();

        setLayout(new BorderLayout());
        add(filterPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton close = new JButton("Close Window");
        close.addActionListener(e -> dispose());
        JPanel p = new JPanel(); p.add(close);
        add(p, BorderLayout.SOUTH);
    }

    private String formatTimestamp(String raw) {
        try {
            LocalDateTime ldt = LocalDateTime.parse(raw);
            return ldt.format(DISPLAY_FORMAT);
        } catch (Exception e) {
            return raw;
        }
    }

    private void applyFilters() {
        model.setRowCount(0);
        String selectedType = (String) typeFilterBox.getSelectedItem();
        String idSearch = productIdFilter.getText().trim().toLowerCase();

        for (String[] row : fullHistory) {
            // CRITICAL FIX: Ensure row has enough columns to avoid ArrayIndexOutOfBounds
            if (row.length < 4) continue;

            String rawTimestamp = row[0];
            String rowId = row[1].toLowerCase();
            String rowType = row[2].toUpperCase();

            boolean idMatches = idSearch.isEmpty() || rowId.contains(idSearch);
            boolean typeMatches = false;

            if (selectedType.equals("ALL EVENTS")) {
                typeMatches = true;
            } else if (selectedType.equals("LOSSES (DAMAGED/EXPIRED)")) {
                typeMatches = (rowType.equals("DAMAGED") || rowType.equals("EXPIRED"));
            } else {
                typeMatches = rowType.equals(selectedType);
            }

            if (idMatches && typeMatches) {
                // Safety check for the 'Notes' column (Index 4)
                String notes = (row.length > 4) ? row[4] : "";

                model.addRow(new Object[]{
                        formatTimestamp(rawTimestamp),
                        row[1],
                        row[2],
                        row[3],
                        notes
                });
            }
        }
    }
}