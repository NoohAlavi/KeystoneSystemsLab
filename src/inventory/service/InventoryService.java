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

    // Default constructor (REAL app)
    public InventoryService() {
        this(CSVHandler.getDataPath() + "products.csv");
    }

    // Constructor for testing (inject custom file)
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
                        row[0],
                        row[1],
                        row[2],
                        row[3],
                        Double.parseDouble(row[4]),
                        Integer.parseInt(row[5]),
                        row[6],
                        row[7]
                );
                productsById.put(product.getId(), product);
                productsByBarcode.put(product.getBarcode(), product);
            }
        }
    }

    private void saveProductsToCSV() {
        List<String[]> data = new ArrayList<>();

        data.add(new String[]{
                "id", "barcode", "name", "brand",
                "price", "quantity", "supplier", "storageCondition"
        });

        for (Product product : productsById.values()) {
            data.add(new String[]{
                    product.getId(),
                    product.getBarcode(),
                    product.getName(),
                    product.getBrand(),
                    String.valueOf(product.getPrice()),
                    String.valueOf(product.getQuantity()),
                    product.getSupplier(),
                    product.getStorageCondition()
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

    public boolean addProduct(Product product) {
        if (productsById.containsKey(product.getId()) ||
                productsByBarcode.containsKey(product.getBarcode())) {
            return false;
        }

        productsById.put(product.getId(), product);
        productsByBarcode.put(product.getBarcode(), product);
        saveProductsToCSV();
        return true;
    }

    public Product getProductById(String id) {
        return productsById.get(id);
    }

    public Product getProductByBarcode(String barcode) {
        return productsByBarcode.get(barcode);
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(productsById.values());
    }

    public boolean updateProduct(String id, String name, String brand, double price,
                                 String supplier, String storageCondition) {
        Product product = productsById.get(id);
        if (product == null) return false;

        product.setName(name);
        product.setBrand(name == null ? product.getBrand() : brand);
        product.setPrice(price);
        product.setSupplier(supplier);
        product.setStorageCondition(storageCondition);

        saveProductsToCSV();
        return true;
    }

    /**
     * Increase stock when shipment arrives (manager only)
     * @param id The product ID
     * @param amount The amount to increase
     * @param reason The reason for the increase (e.g., "Shipment", "Correction")
     */
    public boolean increaseStock(String id, int amount, String reason) {
        Product product = productsById.get(id);
        if (product == null || amount <= 0) return false;

        product.increaseStock(amount);
        saveProductsToCSV();
        return true;
    }

    /**
     * Decrease stock when items are sold (employee can do this)
     * @param id The product ID
     * @param amount The amount to decrease
     * @param reason The reason for the decrease (e.g., "Sale", "Damage", "Theft")
     */
    public boolean decreaseStock(String id, int amount, String reason) {
        Product product = productsById.get(id);
        if (product == null || amount <= 0) return false;

        boolean success = product.decreaseStock(amount);
        if (success) {
            saveProductsToCSV();
        }
        return success;
    }

    /**
     * Record manager-only inventory loss events such as DAMAGED, RETURNED, EXPIRED.
     * This decreases stock and stores an audit record in inventory_events.csv.
     */
    public boolean recordProductEvent(String id, String eventType, int quantity, String notes) {
        Product product = productsById.get(id);
        if (product == null || quantity <= 0 || !isValidEventType(eventType)) {
            return false;
        }

        boolean success = product.decreaseStock(quantity);
        if (!success) {
            return false;
        }

        saveProductsToCSV();

        InventoryEvent event = new InventoryEvent(
                LocalDateTime.now().toString(),
                id,
                eventType.toUpperCase(),
                quantity,
                notes == null ? "" : notes
        );
        saveInventoryEvent(event);

        return true;
    }

    private boolean isValidEventType(String eventType) {
        if (eventType == null) return false;
        String type = eventType.toUpperCase();
        return type.equals("DAMAGED") || type.equals("RETURNED") || type.equals("EXPIRED");
    }

    /**
     * Search products by name (partial match)
     */
    public List<Product> searchProductsByName(String searchTerm) {
        List<Product> results = new ArrayList<>();
        String lower = searchTerm.toLowerCase();

        for (Product product : productsById.values()) {
            if (product.getName().toLowerCase().contains(lower)) {
                results.add(product);
            }
        }

        return results;
    }

    public int getProductCount() {
        return productsById.size();
    }
}