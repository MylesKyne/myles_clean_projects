public class AuctionSaleItem {
    private String description; // auction description
    private int startingPrice; // auction starting price

    public AuctionSaleItem(String description, int startingPrice) {
        this.description = description;
        this.startingPrice = startingPrice;
    }

    public String getDescription() {
        return description;
    }

    public int getStartingPrice() {
        return startingPrice;
    }

    // debugging assistance
    @Override
    public String toString() {
        return "AuctionSaleItem{" +
                "description='" + description + '\'' +
                ", startingPrice=" + startingPrice +
                '}';
    }
}
