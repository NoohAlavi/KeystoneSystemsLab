package inventory;

import javax.swing.*;
import inventory.ui.LoginPanel;

public class AppFrame extends JFrame {

    public AppFrame() {

        setTitle("Grocery Store Inventory System");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        add(new LoginPanel());

        setVisible(true);
    }
}