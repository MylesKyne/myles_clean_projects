public class AuctionResult {
    private int itemID; // ID of the auctioned item
    private int winningBidder; // ID of the user with the highest bid
    private int winningBid; // Highest bid amount


    public AuctionResult(int itemID, int winningBidder, int winningBid) {
        this.itemID = itemID;
        this.winningBidder = winningBidder;
        this.winningBid = winningBid;
    }

    public int getItemID() {
        return itemID;
    }

    public int getWinningBidder() {
        return winningBidder;
    }

    public int getWinningBid() {
        return winningBid;
    }
}