package server.android.paying.com.payingmobileserver;

/**
 * Created by Eren on 2.11.2014.
 */
public class Item {

    private String name;
    private int quantity;
    private double price;

    @Override
    public String toString() {
        return getName() + " " + getQuantity() + " " + getPrice();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
