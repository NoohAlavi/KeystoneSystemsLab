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
    private String productsFile;

    // Default constructor (REAL app)
    public InventoryService() {
        this(CSVHandler.getDataPath() + "products.csv");
    }

    // Constructor for testing (inject custom file)
    public InventoryService(String filePath) {
        this.productsFile = filePath;
        this.productsById = new HashMap<>();
        this.productsByBarcode = new HashMap<>();
        loadProductsFromCSV();
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
        product.setBrand(brand);
        product.setPrice(price);
        product.setSupplier(supplier);
        product.setStorageCondition(storageCondition);

        saveProductsToCSV();
        return true;
    }

    public boolean increaseStock(String id, int amount) {
        Product product = productsById.get(id);
        if (product == null) return false;

        product.increaseStock(amount);
        saveProductsToCSV();
        return true;
    }
    
    // Overloaded method to support legacy calls if any (e.g. tests) that provide a reason
    public boolean increaseStock(String id, int amount, String reason) {
        return increaseStock(id, amount);
    }

    public boolean decreaseStock(String id, int amount) {
        Product product = productsById.get(id);
        if (product == null) return false;

        boolean success = product.decreaseStock(amount);
        if (success) saveProductsToCSV();

        return success;
    }

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