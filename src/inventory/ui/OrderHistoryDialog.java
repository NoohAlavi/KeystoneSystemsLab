package inventory.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class OrderHistoryDialog extends JDialog {
    private JTable table;
    private DefaultTableModel model;
    private List<String[]> fullHistory;

    public OrderHistoryDialog(Frame owner, List<String[]> history) {
        super(owner, "Inventory Transaction History", true);
        this.fullHistory = history;
        setSize(950, 500);
        setLocationRelativeTo(owner);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter by Type:"));

        // Options updated to match your request
        String[] options = {"ALL EVENTS", "RESTOCK", "PURCHASE", "RETURNED", "LOSSES (DAMAGED/EXPIRED)"};
        JComboBox<String> filterBox = new JComboBox<>(options);
        filterBox.addActionListener(e -> applyFilter((String) filterBox.getSelectedItem()));
        filterPanel.add(filterBox);

        String[] columns = {"Date/Time", "Product ID", "Event Type", "Quantity", "Notes"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);

        applyFilter("ALL EVENTS");

        setLayout(new BorderLayout());
        add(filterPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton close = new JButton("Close Window");
        close.addActionListener(e -> dispose());
        JPanel p = new JPanel(); p.add(close);
        add(p, BorderLayout.SOUTH);
    }

    private void applyFilter(String filterType) {
        model.setRowCount(0);
        for (String[] row : fullHistory) {
            String rowType = row[2].toUpperCase();

            if (filterType.equals("ALL EVENTS")) {
                model.addRow(row);
            } else if (filterType.equals("LOSSES (DAMAGED/EXPIRED)")) {
                if (rowType.equals("DAMAGED") || rowType.equals("EXPIRED")) {
                    model.addRow(row);
                }
            } else if (rowType.equals(filterType)) {
                model.addRow(row);
            }
        }
    }
}