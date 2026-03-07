package inventory.model;

public class Product {
    private String id;
    private String barcode;
    private String name;
    private String brand;
    private double price;
    private int quantity;
    private String supplier;
    private String storageCondition;

    public Product(String id, String barcode, String name, String brand, double price,
                   int quantity, String supplier, String storageCondition) {
        this.id = id;
        this.barcode = barcode;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.quantity = quantity;
        this.supplier = supplier;
        this.storageCondition = storageCondition;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getName() {
        return name;
    }

    public String getBrand() {
        return brand;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getSupplier() {
        return supplier;
    }

    public String getStorageCondition() {
        return storageCondition;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public void setStorageCondition(String storageCondition) {
        this.storageCondition = storageCondition;
    }

    // Stock management methods
    public void increaseStock(int amount) {
        if (amount > 0) {
            this.quantity += amount;
        }
    }

    public boolean decreaseStock(int amount) {
        if (amount > 0 && this.quantity >= amount) {
            this.quantity -= amount;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("ID: %s | Barcode: %s | Name: %s | Brand: %s | Price: $%.2f | Quantity: %d | Supplier: %s | Storage: %s",
                id, barcode, name, brand, price, quantity, supplier, storageCondition);
    }
}
