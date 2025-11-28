package replica;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class ReplicaImpl extends UnicastRemoteObject
        implements PrimaryAPI, ReplicaControl {

    // in-memory key-value store
    private final Map<String,String> store = new HashMap<>();

    // am I currently the primary?
    private boolean isPrimary;

    // my ID for naming (e.g., "1" for name "replica1")
    private final String myId;

    // stubs to the other replicas that act as backups
    private final List<ReplicaControl> backups;

    // pattern to recognize replica bindings in the registry
    private static final Pattern REPLICA_NAME = Pattern.compile("^replica\\d+$");

    protected ReplicaImpl(String myId,
                          boolean startAsPrimary,
                          List<ReplicaControl> backupsList)
            throws RemoteException {

        super();
        this.myId = Objects.requireNonNull(myId, "myId");
        this.isPrimary = startAsPrimary;
        this.backups = new ArrayList<>(backupsList);
    }

    /**
     * ========== PrimaryAPI ==========
     */

    @Override
    public synchronized boolean handleClientPut(String key, String value) throws RemoteException {
        
        //TODO:
        // 1) Check if I am currently the primary. If not, print an error message and return false
        if (!isPrimary) {
            System.err.println("[Replica " + myId + "] ERROR: NOT PRIMARY, CANNOT HANDLE PUT");
            return false;
        }
        // 2) Update local state (store) by adding the key, value pair
        store.put(key, value);
        // 3) Push full state to backups 
        for (ReplicaControl backup : backups) {
            try {
                backup.pushFullState(new HashMap<>(store));
            } catch (RemoteException e) {
                System.err.println("[Replica " + myId + "] ERROR: FAILED TO PUSH STATE TO FOLLOWING BACKUP: " + e.getMessage());
            }
        }
        // 4) Return true (success), if all goes well.
        System.out.println("[Replica " + myId + "] PUT key=" + key + ", value=" + value);
        System.out.println("[Replica " + myId + "] Pushed state to backups: " + store);
        return true;
    }

    @Override
    public synchronized String handleClientGet(String key) throws RemoteException {
        //TODO:
        // 1) Retrieve the value corresponding to the key
        String value = store.get(key);
        System.out.println("[Replica " + myId + "] GET key=" + key + ", value=" + value);
        // 2) Return null if the key does not exist; otherwise, return value
        if (value != null) {
            return value;
        }
        else {
            return null;
        }
    }

    @Override
    public synchronized void pushFullState(Map<String,String> newState) throws RemoteException {

        //TODO:
        // Replace the store with the newState
        store.clear();
        store.putAll(newState);
        System.out.println("[Replica " + myId + "] pushFullState applied. Store now: " + store);
    }
    @Override
    public synchronized void promoteToPrimary() throws RemoteException {
        // TODO:
        // 1) Set this replica to act as primary.
        isPrimary = true;
        System.out.println("[Replica " + myId + "] Promoted to PRIMARY.");
        // 2) Discover current backups in the RMI registry.
        List<ReplicaControl> discoveredBackups = discoverBackups();
        System.out.println("[Replica " + myId + "] Discovered " + discoveredBackups.size() + " backups.");
        // 3) Call setBackups(...) with the discovered list.
        setBackups(discoveredBackups);
        // 4) Log useful information.
        System.out.println("[Replica " + myId + "] Backups set.");
        System.out.println("Current Backups:");
        //For backup in discoveredBackups, print its id
        for (ReplicaControl backup : discoveredBackups) {
            System.out.println(" - " + backup);
        }
    }

    @Override
    public boolean ping() throws RemoteException {
        return true;
    }

    // ========== Helpers ==========

    public synchronized void setBackups(List<ReplicaControl> newBackups) {
        this.backups.clear();
        this.backups.addAll(newBackups);
    }

    private List<ReplicaControl> discoverBackups() {
        // TODO:
        // 1) Query the RMI registry for all bindings whose names match "replica<id>".
        List<ReplicaControl> result = new ArrayList<>();
        
        // 2) Skip your own name ("replica" + myId).

        // 3) For each, look it up, cast to ReplicaControl, and only include if ping() returns true.

        // 4) Return the list.
        return result;
            
    } 
}
