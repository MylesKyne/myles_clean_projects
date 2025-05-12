public class AuctionItem {
    private int itemID; // Unique ID for the auction item
    private String description; // Description of the item
    private int startingPrice; // Starting price for the auction
    private int highestBid; // Current highest bid
    private int highestBidder; // id of user with highest bid

    public AuctionItem(int itemID, AuctionSaleItem saleItem) {
        this.itemID = itemID;
        this.description = saleItem.getDescription();
        this.startingPrice = saleItem.getStartingPrice();
        this.highestBid = 0; // no bid initialisation
        this.highestBidder = -1; // no bidder
    }

    // getter and setter
    public int getItemID() {
        return itemID;
    }

    public String getDescription() {
        return description;
    }

    public int getStartingPrice() {
        return startingPrice;
    }

    public int getHighestBid() {
        return highestBid;
    }

    public void setHighestBid(int highestBid) {
        this.highestBid = highestBid;
    }

    public int getHighestBidder() {
        return highestBidder;
    }

    public void setHighestBidder(int highestBidder) {
        this.highestBidder = highestBidder;
    }
}