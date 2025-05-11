package edu.duke.ece651.hw2.simulation;

/**
 * Represents an item delivery that is scheduled to arrive at a future time.
 */
public class DelayedDelivery {
    private Building source;
    private Building destination;
    private String item;
    private int quantity;
    private int deliveryTime;
    
    /**
     * Constructs a DelayedDelivery.
     *
     * @param source the building that sends the item.
     * @param destination the building that will receive the item.
     * @param item the item type to be delivered.
     * @param quantity the quantity of the item.
     * @param deliveryTime the time step when the delivery should arrive.
     */
    public DelayedDelivery(Building source, Building destination, String item, int quantity, int deliveryTime) {
        this.source = source;
        this.destination = destination;
        this.item = item;
        this.quantity = quantity;
        this.deliveryTime = deliveryTime;
    }
    
    /**
     * Gets the source building.
     *
     * @return the source building.
     */
    public Building getSource() {
        return source;
    }
    
    /**
     * Gets the destination building.
     *
     * @return the destination building.
     */
    public Building getDestination() {
        return destination;
    }
    
    /**
     * Gets the item type.
     *
     * @return the item type.
     */
    public String getItem() {
        return item;
    }
    
    /**
     * Gets the quantity of the item.
     *
     * @return the quantity.
     */
    public int getQuantity() {
        return quantity;
    }
    
    /**
     * Gets the scheduled delivery time.
     *
     * @return the delivery time step.
     */
    public int getDeliveryTime() {
        return deliveryTime;
    }
}