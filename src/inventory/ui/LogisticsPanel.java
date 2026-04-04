package inventory.ui;

import inventory.model.Product;
import inventory.service.InventoryService;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class LogisticsPanel extends JPanel {
    private final InventoryService inventoryService;

    public LogisticsPanel(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header
        JLabel header = new JLabel("Inventory Analytics & Logistics", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(header, BorderLayout.NORTH);

        // Main Grid
        JPanel chartsGrid = new JPanel(new GridLayout(2, 2, 20, 20));
        chartsGrid.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        chartsGrid.setOpaque(false);

        chartsGrid.add(createStockLevelsChart());
        chartsGrid.add(createStorageDistributionChart());
        chartsGrid.add(createPriceRangeChart());
        chartsGrid.add(createSalesVolumeChart());

        JScrollPane scrollPane = new JScrollPane(chartsGrid);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createStockLevelsChart() {
        List<Product> products = inventoryService.getAllProducts();
        products.sort(Comparator.comparingInt(Product::getQuantity));

        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        // Get bottom 3 and top 3
        for (int i = 0; i < Math.min(3, products.size()); i++) {
            labels.add(products.get(i).getName() + " (Low)");
            values.add(products.get(i).getQuantity());
        }
        for (int i = Math.max(0, products.size() - 3); i < products.size(); i++) {
            labels.add(products.get(i).getName() + " (High)");
            values.add(products.get(i).getQuantity());
        }

        return new ChartPanel("Stock Levels (Extreme Ends)", labels, values, Color.BLUE);
    }

    private JPanel createStorageDistributionChart() {
        Map<String, Long> counts = inventoryService.getAllProducts().stream()
                .collect(Collectors.groupingBy(Product::getStorageCondition, Collectors.counting()));

        List<String> labels = new ArrayList<>(counts.keySet());
        List<Integer> values = labels.stream().map(l -> counts.get(l).intValue()).collect(Collectors.toList());

        return new ChartPanel("Storage Distribution", labels, values, new Color(46, 204, 113));
    }

    private JPanel createPriceRangeChart() {
        List<Product> products = inventoryService.getAllProducts();
        int[] ranges = new int[4]; // 0-10, 10-50, 50-100, 100+
        for (Product p : products) {
            double price = p.getPrice();
            if (price < 10) ranges[0]++;
            else if (price < 50) ranges[1]++;
            else if (price < 100) ranges[2]++;
            else ranges[3]++;
        }

        List<String> labels = Arrays.asList("<$10", "$10-$50", "$50-$100", ">$100");
        List<Integer> values = Arrays.stream(ranges).boxed().collect(Collectors.toList());

        return new ChartPanel("Price Range Distribution", labels, values, new Color(241, 196, 15));
    }

    private JPanel createSalesVolumeChart() {
        List<String[]> events = inventoryService.getFullEventHistory();
        Map<String, Integer> sales = new HashMap<>();

        for (String[] event : events) {
            if (event[2].equals("DECREASE") || event[2].equals("SALE")) {
                String id = event[1];
                int qty = Integer.parseInt(event[3]);
                sales.put(id, sales.getOrDefault(id, 0) + qty);
            }
        }

        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();

        sales.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> {
                    Product p = inventoryService.getProductById(e.getKey());
                    labels.add(p != null ? p.getName() : e.getKey());
                    values.add(e.getValue());
                });

        return new ChartPanel("Top Selling Products", labels, values, new Color(155, 89, 182));
    }

    private static class ChartPanel extends JPanel {
        private final String title;
        private final List<String> labels;
        private final List<Integer> values;
        private final Color barColor;

        public ChartPanel(String title, List<String> labels, List<Integer> values, Color barColor) {
            this.title = title;
            this.labels = labels;
            this.values = values;
            this.barColor = barColor;
            setPreferredSize(new Dimension(300, 250));
            setBackground(new Color(248, 249, 250));
            setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int margin = 40;
            int chartW = w - 2 * margin;
            int chartH = h - 2 * margin - 30;

            // Title
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.drawString(title, margin, margin - 10);

            if (values.isEmpty()) {
                g2.drawString("No Data Available", w / 2 - 50, h / 2);
                return;
            }

            int max = values.stream().max(Integer::compare).orElse(1);
            int barW = chartW / Math.max(1, labels.size()) - 10;

            for (int i = 0; i < labels.size(); i++) {
                int barH = (int) ((double) values.get(i) / max * chartH);
                int x = margin + i * (barW + 10);
                int y = h - margin - barH - 20;

                // Bar
                g2.setColor(barColor);
                g2.fillRect(x, y, barW, barH);
                
                // Label
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                String label = labels.get(i);
                if (label.length() > 10) label = label.substring(0, 8) + "..";
                g2.drawString(label, x, h - margin);
                
                // Value
                g2.setColor(Color.BLACK);
                g2.drawString(String.valueOf(values.get(i)), x + barW/2 - 5, y - 5);
            }
        }
    }
}
