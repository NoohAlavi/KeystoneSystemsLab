package inventory.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class OrderHistoryDialog extends JDialog {
    public OrderHistoryDialog(Frame owner, List<String[]> history) {
        super(owner, "Order History (Restocks)", true);
        setSize(800, 400);
        setLocationRelativeTo(owner);

        String[] columns = {"Date/Time", "Product ID", "Action", "Quantity", "Notes"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        for (String[] row : history) {
            model.addRow(row);
        }

        JTable table = new JTable(model);
        setLayout(new BorderLayout());
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());
        JPanel p = new JPanel(); p.add(close);
        add(p, BorderLayout.SOUTH);
    }
}