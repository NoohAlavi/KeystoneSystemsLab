package inventory;

import inventory.service.AuthService;
import inventory.service.InventoryService;
import inventory.ui.InventoryPanel;
import inventory.ui.LoginPanel;

import javax.swing.*;

public class AppFrame extends JFrame {
    private AuthService authService;
    private InventoryService inventoryService;

    public AppFrame() {
        authService = new AuthService();
        inventoryService = new InventoryService();

        setTitle("Grocery Store Inventory System");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        showLoginPanel();
        setVisible(true);
    }

    private void showLoginPanel() {
        getContentPane().removeAll();
        add(new LoginPanel(authService, this::showInventoryPanel));
        revalidate();
        repaint();
    }

    private void showInventoryPanel() {
        getContentPane().removeAll();
        add(new InventoryPanel(authService, inventoryService, this::showLoginPanel));
        revalidate();
        repaint();
    }
}