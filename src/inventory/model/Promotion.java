package inventory.model;

public class Promotion {
    private String name;
    private PromotionType type;
    private double value;
    private int minimumQuantity;
    private boolean stackable;
    private boolean active;

    public Promotion(String name, PromotionType type, double value, int minimumQuantity, boolean stackable) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.minimumQuantity = minimumQuantity;
        this.stackable = stackable;
        this.active = true;
    }

    public String getName() {
        return name;
    }

    public PromotionType getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    public int getMinimumQuantity() {
        return minimumQuantity;
    }

    public boolean isStackable() {
        return stackable;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double apply(double basePrice, int quantityToBuy) {
        switch (type) {
            case SALE_PRICE:
                return value;

            case PERCENTAGE_OFF:
                return basePrice - (basePrice * (value / 100.0));

            case MULTI_BUY:
                if (quantityToBuy >= minimumQuantity) {
                    return basePrice - value;
                }
                return basePrice;

            case COUPON:
                return basePrice - value;

            default:
                return basePrice;
        }
    }

    @Override
    public String toString() {
        return name + " [" + type + ", value=" + value +
                ", minQty=" + minimumQuantity +
                ", stackable=" + stackable +
                ", active=" + active + "]";
    }
}
