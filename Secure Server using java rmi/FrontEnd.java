import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.HashMap;

public class FrontEnd extends UnicastRemoteObject implements Auction {
    private Map<Integer, Auction> replicas; // maps replica ids to reference
    private int primaryReplicaID; // the id of the current replica

    public FrontEnd() throws RemoteException {
        super();
        replicas = new HashMap<>();
    }

    // dynamic allocation
    public void addReplica(int replicaID, String replicaURL) {
        try {
            Auction replica = (Auction) Naming.lookup(replicaURL);
            replicas.put(replicaID, replica);
            System.out.println("Replica " + replicaID + " added.");
            if (replicas.size() == 1) {
                // first replica is primary replica
                primaryReplicaID = replicaID;
                System.out.println("Replica " + replicaID + " set as primary.");
            }
        } catch (Exception e) {
            System.err.println("Failed to add replica: " + replicaURL + " - " + e.getMessage());
        }
    }

    // forwards register request to replica
    public int register(String email) throws RemoteException {
        try {
            return replicas.get(primaryReplicaID).register(email);
        } catch (Exception e) {
            handlePrimaryFailure();
            return replicas.get(primaryReplicaID).register(email);
        }
    }

    // forwards getspec request to the primary replica
    public AuctionItem getSpec(int itemID) throws RemoteException {
        try {
            return replicas.get(primaryReplicaID).getSpec(itemID);
        } catch (Exception e) {
            handlePrimaryFailure();
            return replicas.get(primaryReplicaID).getSpec(itemID);
        }
    }

    // forwards newauction request to primary replica
    public int newAuction(int userID, AuctionSaleItem item) throws RemoteException {
        try {
            return replicas.get(primaryReplicaID).newAuction(userID, item);
        } catch (Exception e) {
            handlePrimaryFailure();
            return replicas.get(primaryReplicaID).newAuction(userID, item);
        }
    }

    // forwards listitems request to primary replica
    public AuctionItem[] listItems() throws RemoteException {
        try {
            return replicas.get(primaryReplicaID).listItems();
        } catch (Exception e) {
            handlePrimaryFailure();
            return replicas.get(primaryReplicaID).listItems();
        }
    }

    // forwards closeauction request to primary replica
    public AuctionResult closeAuction(int userID, int itemID) throws RemoteException {
        try {
            return replicas.get(primaryReplicaID).closeAuction(userID, itemID);
        } catch (Exception e) {
            handlePrimaryFailure();
            return replicas.get(primaryReplicaID).closeAuction(userID, itemID);
        }
    }

    // forwards the bid request to primary replica
    public boolean bid(int userID, int itemID, int price) throws RemoteException {
        try {
            return replicas.get(primaryReplicaID).bid(userID, itemID, price);
        } catch (Exception e) {
            handlePrimaryFailure();
            return replicas.get(primaryReplicaID).bid(userID, itemID, price);
        }
    }

    // returns the primary replica ID
    public int getPrimaryReplicaID() throws RemoteException {
        return primaryReplicaID;
    }

    // handles failure as well as backup replicas
    private void handlePrimaryFailure() {
        System.err.println("Primary replica " + primaryReplicaID + " has failed.");
        replicas.remove(primaryReplicaID); // deletes the failed replica
        if (replicas.isEmpty()) {
            throw new IllegalStateException("No replicas available to promote.");
        }

        // first replica becomes the primary
        primaryReplicaID = replicas.keySet().iterator().next();
        System.out.println("Replica " + primaryReplicaID + " promoted to primary.");
    }

    // starts the frontend
    public static void main(String[] args) {
        try {
            // creates instance
            FrontEnd frontEnd = new FrontEnd();

            // binds to rmi registry
            Naming.rebind("FrontEnd", frontEnd);
            System.out.println("FrontEnd is running...");

            // dynamically adds the replicas
            frontEnd.addReplica(1, "rmi://localhost/Replica1");
            frontEnd.addReplica(2, "rmi://localhost/Replica2");
            frontEnd.addReplica(3, "rmi://localhost/Replica3");

        } catch (Exception e) {
            System.err.println("FrontEnd error: " + e.getMessage());
        }
    }
}
