package inventory.service;

import inventory.model.InventoryEvent;
import inventory.model.Product;
import inventory.util.CSVHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryService {
    private Map<String, Product> productsById;
    private Map<String, Product> productsByBarcode;
    private String productsFile;
    private String eventsFile;

    public InventoryService() {
        this(CSVHandler.getDataPath() + "products.csv");
    }

    public InventoryService(String filePath) {
        this.productsFile = filePath;
        this.eventsFile = CSVHandler.getDataPath() + "inventory_events.csv";
        this.productsById = new HashMap<>();
        this.productsByBarcode = new HashMap<>();
        loadProductsFromCSV();
        initializeEventsFile();
    }

    private void loadProductsFromCSV() {
        List<String[]> data = CSVHandler.readCSV(productsFile);
        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            if (row.length == 8) {
                Product product = new Product(
                        row[0], row[1], row[2], row[3],
                        Double.parseDouble(row[4]),
                        Integer.parseInt(row[5]),
                        row[6], row[7]
                );
                productsById.put(product.getId(), product);
                productsByBarcode.put(product.getBarcode(), product);
            }
        }
    }

    private void saveProductsToCSV() {
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"id", "barcode", "name", "brand", "price", "quantity", "supplier", "storageCondition"});
        for (Product product : productsById.values()) {
            data.add(new String[]{
                    product.getId(), product.getBarcode(), product.getName(), product.getBrand(),
                    String.valueOf(product.getPrice()), String.valueOf(product.getQuantity()),
                    product.getSupplier(), product.getStorageCondition()
            });
        }
        CSVHandler.writeCSV(productsFile, data);
    }

    private void initializeEventsFile() {
        List<String[]> existing = CSVHandler.readCSV(eventsFile);
        if (existing.isEmpty()) {
            List<String[]> data = new ArrayList<>();
            data.add(new String[]{"timestamp", "productId", "eventType", "quantity", "notes"});
            CSVHandler.writeCSV(eventsFile, data);
        }
    }

    private void saveInventoryEvent(InventoryEvent event) {
        List<String[]> data = CSVHandler.readCSV(eventsFile);
        if (data.isEmpty()) {
            data.add(new String[]{"timestamp", "productId", "eventType", "quantity", "notes"});
        }
        data.add(new String[]{
                event.getTimestamp(),
                event.getProductId(),
                event.getEventType(),
                String.valueOf(event.getQuantity()),
                sanitizeCSVField(event.getNotes())
        });
        CSVHandler.writeCSV(eventsFile, data);
    }

    private String sanitizeCSVField(String value) {
        if (value == null) return "";
        return value.replace(",", ";").replace("\n", " ").replace("\r", " ");
    }

    public boolean increaseStock(String id, int amount, String reason) {
        Product product = productsById.get(id);
        if (product == null || amount <= 0) return false;

        product.increaseStock(amount);
        saveProductsToCSV();

        // LOGGING FOR ORDER HISTORY
        InventoryEvent event = new InventoryEvent(
                LocalDateTime.now().toString(),
                id,
                "RESTOCK",
                amount,
                reason == null ? "Shipment" : reason
        );
        saveInventoryEvent(event);
        return true;
    }

    public List<String[]> getOrderHistory() {
        List<String[]> allEvents = CSVHandler.readCSV(eventsFile);
        List<String[]> restockEvents = new ArrayList<>();
        for (int i = 1; i < allEvents.size(); i++) {
            String[] row = allEvents.get(i);
            if (row[2].equalsIgnoreCase("RESTOCK")) {
                restockEvents.add(row);
            }
        }
        restockEvents.sort((a, b) -> b[0].compareTo(a[0])); // Newest first
        return restockEvents;
    }

    public boolean recordProductEvent(String id, String eventType, int quantity, String notes) {
        Product product = productsById.get(id);
        if (product == null || quantity <= 0 || !isValidEventType(eventType)) return false;
        if (!product.decreaseStock(quantity)) return false;

        saveProductsToCSV();
        InventoryEvent event = new InventoryEvent(
                LocalDateTime.now().toString(), id, eventType.toUpperCase(),
                quantity, notes == null ? "" : notes
        );
        saveInventoryEvent(event);
        return true;
    }

    private boolean isValidEventType(String t) {
        return t != null && (t.equals("DAMAGED") || t.equals("RETURNED") || t.equals("EXPIRED"));
    }

    public boolean decreaseStock(String id, int amount, String reason) {
        Product p = productsById.get(id);
        if (p == null || amount <= 0) return false;
        if (p.decreaseStock(amount)) { saveProductsToCSV(); return true; }
        return false;
    }

    public boolean addProduct(Product p) {
        if (productsById.containsKey(p.getId()) || productsByBarcode.containsKey(p.getBarcode())) return false;
        productsById.put(p.getId(), p);
        productsByBarcode.put(p.getBarcode(), p);
        saveProductsToCSV();
        return true;
    }

    public Product getProductById(String id) { return productsById.get(id); }
    public List<Product> getAllProducts() { return new ArrayList<>(productsById.values()); }

    public boolean updateProduct(String id, String name, String brand, double price, String supplier, String storage) {
        Product p = productsById.get(id);
        if (p == null) return false;
        p.setName(name); p.setBrand(brand); p.setPrice(price);
        p.setSupplier(supplier); p.setStorageCondition(storage);
        saveProductsToCSV();
        return true;
    }
}