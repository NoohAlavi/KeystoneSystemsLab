package inventory.model;

public class InventoryEvent {
    private String timestamp;
    private String productId;
    private String eventType;
    private int quantity;
    private String notes;

    public InventoryEvent(String timestamp, String productId, String eventType, int quantity, String notes) {
        this.timestamp = timestamp;
        this.productId = productId;
        this.eventType = eventType;
        this.quantity = quantity;
        this.notes = notes;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getProductId() {
        return productId;
    }

    public String getEventType() {
        return eventType;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getNotes() {
        return notes;
    }
}