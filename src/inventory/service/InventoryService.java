package inventory.service;

import inventory.model.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryService {
    private Map<String, Product> productsById;
    private Map<String, Product> productsByBarcode;

    public InventoryService() {
        this.productsById = new HashMap<>();
        this.productsByBarcode = new HashMap<>();
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
        return true;
    }

    /**
     * Increase stock when shipment arrives (manager only)
     */
    public boolean increaseStock(String id, int amount) {
        Product product = productsById.get(id);
        if (product == null) {
            return false;
        }
        product.increaseStock(amount);
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
        return product.decreaseStock(amount);
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
