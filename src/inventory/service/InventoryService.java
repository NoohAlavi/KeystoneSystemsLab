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
        if (existing == null || existing.isEmpty()) {
            List<String[]> data = new ArrayList<>();
            data.add(new String[]{"timestamp", "productId", "eventType", "quantity", "notes"});
            CSVHandler.writeCSV(eventsFile, data);
        }
    }

    private void saveInventoryEvent(InventoryEvent event) {
        List<String[]> data = CSVHandler.readCSV(eventsFile);
        if (data == null || data.isEmpty()) {
            data = new ArrayList<>();
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

    public boolean addProduct(Product product) {
        if (productsById.containsKey(product.getId()) || productsByBarcode.containsKey(product.getBarcode())) return false;
        productsById.put(product.getId(), product);
        productsByBarcode.put(product.getBarcode(), product);
        saveProductsToCSV();
        return true;
    }

    public Product getProductById(String id) { return productsById.get(id); }
    public Product getProductByBarcode(String barcode) { return productsByBarcode.get(barcode); }
    public List<Product> getAllProducts() { return new ArrayList<>(productsById.values()); }

    public boolean updateProduct(String id, String name, String brand, double price, String supplier, String storageCondition) {
        Product product = productsById.get(id);
        if (product == null) return false;
        product.setName(name);
        product.setBrand(brand);
        product.setPrice(price);
        product.setSupplier(supplier);
        product.setStorageCondition(storageCondition);
        saveProductsToCSV();
        return true;
    }

    public List<String[]> getFullEventHistory() {
        List<String[]> allEvents = CSVHandler.readCSV(eventsFile);
        if (allEvents != null && allEvents.size() > 1) {
            List<String[]> dataOnly = new ArrayList<>(allEvents.subList(1, allEvents.size()));
            dataOnly.sort((a, b) -> b[0].compareTo(a[0]));
            return dataOnly;
        }
        return new ArrayList<>();
    }

    public boolean increaseStock(String id, int amount, String reason) {
        Product product = productsById.get(id);
        if (product == null || amount <= 0) return false;
        product.increaseStock(amount);
        saveProductsToCSV();
        saveInventoryEvent(new InventoryEvent(LocalDateTime.now().toString(), id, "INCREASE", amount, reason));
        return true;
    }

    public boolean decreaseStock(String id, int amount, String reason) {
        Product product = productsById.get(id);
        if (product == null || amount <= 0) return false;
        if (product.decreaseStock(amount)) {
            saveProductsToCSV();
            saveInventoryEvent(new InventoryEvent(LocalDateTime.now().toString(), id, "DECREASE", amount, reason));
            return true;
        }
        return false;
    }

    public boolean recordProductEvent(String id, String eventType, int quantity, String notes) {
        Product product = productsById.get(id);
        if (product == null || quantity <= 0) return false;
        if (product.decreaseStock(quantity)) {
            saveProductsToCSV();
            saveInventoryEvent(new InventoryEvent(LocalDateTime.now().toString(), id, eventType.toUpperCase(), quantity, notes));
            return true;
        }
        return false;
    }

    public List<Product> searchProductsByName(String searchTerm) {
        List<Product> results = new ArrayList<>();
        String lower = searchTerm.toLowerCase();
        for (Product product : productsById.values()) {
            if (product.getName().toLowerCase().contains(lower)) results.add(product);
        }
        return results;
    }

    public int getProductCount() { return productsById.size(); }
}
