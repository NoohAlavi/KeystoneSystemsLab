package inventory.ui;

import inventory.model.Product;
import inventory.service.InventoryService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class LogisticsPanel extends JPanel {
    private final InventoryService inventoryService;

    public LogisticsPanel(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel header = new JLabel("Inventory Analytics & Logistics", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(header, BorderLayout.NORTH);

        // 3 Rows, 2 Columns
        JPanel chartsGrid = new JPanel(new GridLayout(3, 2, 20, 20));
        chartsGrid.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        chartsGrid.setOpaque(false);

        // ROW 1: Actionable Lists
        chartsGrid.add(createRestockRecommendationPanel());
        chartsGrid.add(createSlowMoversPanel());

        // ROW 2: Stock & Sales
        chartsGrid.add(createStockLevelsChart());
        chartsGrid.add(createSalesVolumeChart());

        // ROW 3: Distributions
        chartsGrid.add(createStorageDistributionChart());
        chartsGrid.add(createPriceRangeChart());

        JScrollPane scrollPane = new JScrollPane(chartsGrid);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createSlowMoversPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        JLabel listTitle = new JLabel(" Potential Clearances (Slow Movers)", SwingConstants.LEFT);
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        listTitle.setForeground(new Color(120, 120, 120));
        listTitle.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        panel.add(listTitle, BorderLayout.NORTH);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> slowList = new JList<>(listModel);
        slowList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        slowList.setBackground(new Color(248, 249, 250));

        List<Product> allProducts = inventoryService.getAllProducts();
        List<String[]> events = inventoryService.getFullEventHistory();

        // Map to store: ProductID -> All-Time Sales
        Map<String, Integer> allTimeSales = new HashMap<>();

        for (String[] event : events) {
            if (event[2].equalsIgnoreCase("PURCHASE")) {
                String id = event[1];
                try {
                    allTimeSales.put(id, allTimeSales.getOrDefault(id, 0) + Integer.parseInt(event[3]));
                } catch (Exception ignored) {}
            }
        }

        for (Product p : allProducts) {
            int totalSales = allTimeSales.getOrDefault(p.getId(), 0);

            // Criteria: Less than 3 total sales ever, and currently in stock
            if (totalSales < 3 && p.getQuantity() > 0) {
                listModel.addElement(" • " + p.getName() + " (" + totalSales + " total sales)");
            }
        }

        if (listModel.isEmpty()) {
            listModel.addElement("  No stagnant inventory found.");
        }

        JScrollPane scroll = new JScrollPane(slowList);
        scroll.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRestockRecommendationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        JLabel listTitle = new JLabel(" Restock Recommendations", SwingConstants.LEFT);
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        listTitle.setForeground(new Color(41, 128, 185)); // Blueish to distinguish from Slow Movers
        listTitle.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        panel.add(listTitle, BorderLayout.NORTH);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> recommendationList = new JList<>(listModel);
        recommendationList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        recommendationList.setBackground(new Color(248, 249, 250));

        List<Product> allProducts = inventoryService.getAllProducts();
        List<String[]> events = inventoryService.getFullEventHistory();

        Map<String, Integer> weeklySales = new HashMap<>();
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        for (String[] event : events) {
            try {
                LocalDateTime eventDate = LocalDateTime.parse(event[0]);
                if (eventDate.isAfter(sevenDaysAgo) && event[2].equalsIgnoreCase("PURCHASE")) {
                    String id = event[1];
                    int qty = Integer.parseInt(event[3]);
                    weeklySales.put(id, weeklySales.getOrDefault(id, 0) + qty);
                }
            } catch (Exception ignored) {}
        }

        for (Product p : allProducts) {
            int sales = weeklySales.getOrDefault(p.getId(), 0);
            double dailyBurnRate = sales / 7.0;

            if (dailyBurnRate > 0) {
                int daysLeft = (int) (p.getQuantity() / dailyBurnRate);
                if (daysLeft <= 5) {
                    String status = daysLeft == 0 ? "OUT OF STOCK" : daysLeft + " days stock left";
                    listModel.addElement(" • " + p.getName() + " (" + status + ")");
                }
            } else if (p.getQuantity() == 0) {
                listModel.addElement(" • " + p.getName() + " (OUT OF STOCK)");
            }
        }

        if (listModel.isEmpty()) listModel.addElement("  All stock levels healthy.");

        JScrollPane listScroller = new JScrollPane(recommendationList);
        listScroller.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        listScroller.setOpaque(false);
        listScroller.getViewport().setOpaque(false);
        panel.add(listScroller, BorderLayout.CENTER);
        return panel;
    }

    // --- CHART METHODS REMAIN SAME AS PREVIOUS ---
    private JPanel createStockLevelsChart() {
        List<Product> products = inventoryService.getAllProducts();
        products.sort(Comparator.comparingInt(Product::getQuantity));
        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < Math.min(3, products.size()); i++) {
            labels.add(products.get(i).getName() + " (Low)");
            values.add(products.get(i).getQuantity());
        }
        for (int i = Math.max(0, products.size() - 3); i < products.size(); i++) {
            labels.add(products.get(i).getName() + " (High)");
            values.add(products.get(i).getQuantity());
        }
        return new ChartPanel("Stock Extremes", labels, values, new Color(52, 152, 219));
    }

    private JPanel createSalesVolumeChart() {
        List<String[]> events = inventoryService.getFullEventHistory();
        Map<String, Integer> sales = new HashMap<>();
        for (String[] event : events) {
            if (event[2].equalsIgnoreCase("PURCHASE")) {
                String id = event[1];
                try {
                    sales.put(id, sales.getOrDefault(id, 0) + Integer.parseInt(event[3]));
                } catch (Exception ignored) {}
            }
        }
        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        sales.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).limit(4).forEach(e -> {
            Product p = inventoryService.getProductById(e.getKey());
            labels.add(p != null ? p.getName() : e.getKey());
            values.add(e.getValue());
        });
        return new ChartPanel("Top Selling (Volume)", labels, values, new Color(155, 89, 182));
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
        int[] ranges = new int[4];
        for (Product p : products) {
            double price = p.getPrice();
            if (price < 10) ranges[0]++;
            else if (price < 50) ranges[1]++;
            else if (price < 100) ranges[2]++;
            else ranges[3]++;
        }
        List<String> labels = Arrays.asList("<$10", "$10-$50", "$50-$100", ">$100");
        List<Integer> values = Arrays.stream(ranges).boxed().collect(Collectors.toList());
        return new ChartPanel("Price Distribution", labels, values, new Color(241, 196, 15));
    }

    private static class ChartPanel extends JPanel {
        private final String title;
        private final List<String> labels;
        private final List<Integer> values;
        private final Color barColor;

        public ChartPanel(String title, List<String> labels, List<Integer> values, Color barColor) {
            this.title = title; this.labels = labels; this.values = values; this.barColor = barColor;
            setPreferredSize(new Dimension(300, 250));
            setBackground(new Color(248, 249, 250));
            setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(); int h = getHeight(); int margin = 40;
            int chartW = w - 2 * margin; int chartH = h - 2 * margin - 30;
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.drawString(title, margin, margin - 10);
            if (values.isEmpty()) return;
            int max = values.stream().max(Integer::compare).orElse(1);
            int barW = (chartW / Math.max(1, labels.size())) - 10;
            for (int i = 0; i < labels.size(); i++) {
                int barH = (int) ((double) values.get(i) / max * chartH);
                int x = margin + i * (barW + 10);
                int y = h - margin - barH - 20;
                g2.setColor(barColor);
                g2.fillRect(x, y, barW, barH);
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                String label = labels.get(i);
                if (label.length() > 8) label = label.substring(0, 6) + "..";
                g2.drawString(label, x, h - margin);
                g2.setColor(Color.BLACK);
                g2.drawString(String.valueOf(values.get(i)), x + barW/2 - 5, y - 5);
            }
        }
    }
}