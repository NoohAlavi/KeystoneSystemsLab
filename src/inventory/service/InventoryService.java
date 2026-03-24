package inventory.service;

import inventory.model.Product;
import inventory.util.CSVHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryService {
    private Map<String, Product> productsById;
    private Map<String, Product> productsByBarcode;
    private static final String PRODUCTS_FILE = CSVHandler.getDataPath() + "products.csv";

    public InventoryService() {
        this.productsById = new HashMap<>();
        this.productsByBarcode = new HashMap<>();
        loadProductsFromCSV();
    }

    /**
     * Load products from CSV file
     */
    private void loadProductsFromCSV() {
        List<String[]> data = CSVHandler.readCSV(PRODUCTS_FILE);
        // Skip header row
        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);
            if (row.length == 8) {
                Product product = new Product(
                    row[0], // id
                    row[1], // barcode
                    row[2], // name
                    row[3], // brand
                    Double.parseDouble(row[4]), // price
                    Integer.parseInt(row[5]), // quantity
                    row[6], // supplier
                    row[7]  // storageCondition
                );
                productsById.put(product.getId(), product);
                productsByBarcode.put(product.getBarcode(), product);
            }
        }
    }

    /**
     * Save all products to CSV file
     */
    private void saveProductsToCSV() {
        List<String[]> data = new ArrayList<>();
        // Add header
        data.add(new String[]{"id", "barcode", "name", "brand", "price", "quantity", "supplier", "storageCondition"});
        // Add all products
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
        CSVHandler.writeCSV(PRODUCTS_FILE, data);
    }

    /**
     * Add a new product to the inventory (manager only)
     */
    public boolean addProduct(Product product) {
        if (productsById.containsKey(product.getId()) ||
            productsByBarcode.containsKey(product.getBarcode())) {
            return false; // Product already exists
        }
        productsById.put(product.getId(), product);
        productsByBarcode.put(product.getBarcode(), product);
        saveProductsToCSV();
        return true;
    }

    /**
     * Get product by ID
     */
    public Product getProductById(String id) {
        return productsById.get(id);
    }

    /**
     * Get product by barcode
     */
    public Product getProductByBarcode(String barcode) {
        return productsByBarcode.get(barcode);
    }

    /**
     * Get all products in inventory
     */
    public List<Product> getAllProducts() {
        return new ArrayList<>(productsById.values());
    }

    /**
     * Update product details (manager only)
     */
    public boolean updateProduct(String id, String name, String brand, double price,
                                  String supplier, String storageCondition) {
        Product product = productsById.get(id);
        if (product == null) {
            return false;
        }
        product.setName(name);
        product.setBrand(brand);
        product.setPrice(price);
        product.setSupplier(supplier);
        product.setStorageCondition(storageCondition);
        saveProductsToCSV();
        return true;
    }
    /**
     * decrease stock with reason
     */
    public boolean decreaseStock(String id, int amount, String reason) {
        Product product = productsById.get(id);
        if (product == null) {
            return false;
        }
        boolean success = product.decreaseStock(amount);
        if (success) {
            System.out.println("Stock decreased for product: " + id);
            System.out.println("Amount: " + amount);
            System.out.println("Reason: " + reason);
            saveProductsToCSV();
        }
        return success;
    }

    /**
     * Increase stock when shipment arrives (manager only)
     */
    public boolean increaseStock(String id, int amount, String reason) {
        Product product = productsById.get(id);
        if (product == null) {
            return false;
        }

        product.increaseStock(amount);

        System.out.println("Stock increased for product: " + id);
        System.out.println("Amount: " + amount);
        System.out.println("Reason: " + reason);

        saveProductsToCSV();
        return true;
    }

    /**
     * Decrease stock when items are sold (employee can do this)
     */
    public boolean decreaseStock(String id, int amount) {
        Product product = productsById.get(id);
        if (product == null) {
            return false;
        }
        boolean success = product.decreaseStock(amount);
        if (success) {
            saveProductsToCSV();
        }
        return success;
    }

    /**
     * Search products by name (partial match)
     */
    public List<Product> searchProductsByName(String searchTerm) {
        List<Product> results = new ArrayList<>();
        String lowerSearchTerm = searchTerm.toLowerCase();
        for (Product product : productsById.values()) {
            if (product.getName().toLowerCase().contains(lowerSearchTerm)) {
                results.add(product);
            }
        }
        return results;
    }

    /**
     * Get count of products in inventory
     */
    public int getProductCount() {
        return productsById.size();
    }
}
