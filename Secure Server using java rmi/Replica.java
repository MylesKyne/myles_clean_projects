import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class Replica extends UnicastRemoteObject implements Auction {
    private int replicaID; // for Replica identification
    private boolean isPrimary; // boolean fto check for primary replica
    private Map<Integer, AuctionItem> auctionItems; // tracking auction items
    private Map<Integer, AuctionResult> auctionResults; // tracking auction results

    public Replica(int replicaID) throws RemoteException {
        super();
        this.replicaID = replicaID;
        this.isPrimary = false; 
        this.auctionItems = new HashMap<>();
        this.auctionResults = new HashMap<>();
    }

    public int register(String email) throws RemoteException {
        int userID = email.hashCode(); // generates ID for user
        System.out.println("User registered with ID: " + userID);
        return userID;
    }

    public AuctionItem getSpec(int itemID) throws RemoteException {
        return auctionItems.get(itemID);
    }

    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException {
        int itemID = item.hashCode(); // generates ID for items
        auctionItems.put(itemID, new AuctionItem(itemID, item));
        System.out.println("New auction created for item ID: " + itemID);
        return itemID;
    }

    public AuctionItem[] listItems() throws RemoteException {
        return auctionItems.values().toArray(new AuctionItem[0]);
    }

    public AuctionResult closeAuction(int userID, int itemID) throws RemoteException {
        AuctionItem item = auctionItems.get(itemID);
        if (item == null) {
            throw new RemoteException("Item not found.");
        }

        AuctionResult result = new AuctionResult(itemID, item.getHighestBidder(), item.getHighestBid());
        auctionResults.put(itemID, result);
        auctionItems.remove(itemID);
        System.out.println("Auction closed for item ID: " + itemID);
        return result;
    }

    public boolean bid(int userID, int itemID, int price) throws RemoteException {
        AuctionItem item = auctionItems.get(itemID);
        if (item == null || price <= item.getHighestBid()) {
            return false; // bid is invalid
        }
        item.setHighestBid(price);
        item.setHighestBidder(userID);
        System.out.println("Bid placed by user " + userID + " for item ID: " + itemID + " with price: " + price);
        return true;
    }

    public int getPrimaryReplicaID() throws RemoteException {
        if (isPrimary) {
            return replicaID;
        }
        throw new RemoteException("This replica is not the primary.");
    }

    public void promoteToPrimary() {
        isPrimary = true;
        System.out.println("Replica " + replicaID + " is now the primary.");
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Replica <ReplicaID>");
            System.exit(1);
        }

        try {
            int replicaID = Integer.parseInt(args[0]);
            Replica replica = new Replica(replicaID);

            // registers the replica in RMI
            Naming.rebind("Replica" + replicaID, replica);
            System.out.println("Replica " + replicaID + " is running...");
        } catch (Exception e) {
            System.err.println("Error starting Replica: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
