package server.android.paying.com.payingmobileserver;



import java.util.List;

/**
 * Created by Eren on 2.11.2014.
 */
public class Table {
    private String id;
    private String restaurantID;
    private double totalAmount;
    private double paidAmount;
    private List<Item> nameQuantityPriceList;

    public Table(String id, String restaurantID, double totalAmount, double paidAmount){
        this.setId(id);
        this.setRestaurantID(restaurantID);
        this.setTotalAmount(totalAmount);
        this.setPaidAmount(paidAmount);
    }

    @Override
    public String toString(){
        String result = "id: " + getId() + " resID: " + getRestaurantID() + " total Amount: " + getTotalAmount() + " paidAmount: " + getPaidAmount() + "\n";
        for (Item i: getItemList())
            result+= i.toString() + "\n";
        return result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRestaurantID() {
        return restaurantID;
    }

    public void setRestaurantID(String restaurantID) {
        this.restaurantID = restaurantID;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public List<Item> getItemList() {
        return nameQuantityPriceList;
    }

    public void setItemList(List<Item> itemList) {
        this.nameQuantityPriceList = itemList;
    }
}
