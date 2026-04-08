package inventory.model;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private String id;
    private String barcode;
    private String name;
    private String brand;
    private double price;
    private int quantity;
    private String supplier;
    private String storageCondition;

    // New fields
    private int lowStockThreshold;
    private List<Promotion> promotions;

    public Product(String id, String barcode, String name, String brand, double price,
                   int quantity, String supplier, String storageCondition) {
        this(id, barcode, name, brand, price, quantity, supplier, storageCondition, 5);
    }

    public Product(String id, String barcode, String name, String brand, double price,
                   int quantity, String supplier, String storageCondition, int lowStockThreshold) {
        this.id = id;
        this.barcode = barcode;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.quantity = quantity;
        this.supplier = supplier;
        this.storageCondition = storageCondition;
        this.lowStockThreshold = lowStockThreshold;
        this.promotions = new ArrayList<>();
    }

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

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public List<Promotion> getPromotions() {
        return new ArrayList<>(promotions);
    }

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

    public void setLowStockThreshold(int lowStockThreshold) {
        if (lowStockThreshold >= 0) {
            this.lowStockThreshold = lowStockThreshold;
        }
    }

    public void addPromotion(Promotion promotion) {
        if (promotion != null) {
            promotions.add(promotion);
        }
    }

    public void clearPromotions() {
        promotions.clear();
    }

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

    public boolean isLowStock() {
        return quantity <= lowStockThreshold;
    }

    public double getEffectivePrice(int quantityToBuy) {
        double currentPrice = price;
        boolean hasAppliedNonStackable = false;

        for (Promotion promotion : promotions) {
            if (!promotion.isActive()) continue;

            if (!promotion.isStackable() && hasAppliedNonStackable) {
                continue;
            }

            currentPrice = promotion.apply(currentPrice, quantityToBuy);

            if (!promotion.isStackable()) {
                hasAppliedNonStackable = true;
            }
        }

        return Math.max(0.0, currentPrice);
    }

    @Override
    public String toString() {
        return String.format(
                "ID: %s | Barcode: %s | Name: %s | Brand: %s | Price: $%.2f | Quantity: %d | Supplier: %s | Storage: %s | Low Stock Threshold: %d",
                id, barcode, name, brand, price, quantity, supplier, storageCondition, lowStockThreshold
        );
    }
}
