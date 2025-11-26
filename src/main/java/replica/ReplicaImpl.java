package replica;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
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
        // 2) Update local state (store) by adding the key, value pair
        // 3) Push full state to backups 
        // 4) Return true (success), if all goes well. 
        

        return true;
    }

    @Override
    public synchronized String handleClientGet(String key) throws RemoteException {
        //TODO:
        // 1) Retrieve the value corresponding to the key
        // 2) Return null if the key does not exist; otherwise, return value
        
        return null;
    }

    @Override
    public synchronized void pushFullState(Map<String,String> newState) throws RemoteException {

        //TODO:
        // Replace the store with the newState
        System.out.println("[Replica " + myId + "] pushFullState applied. Store now: " + store);
    }
    @Override
    public synchronized void promoteToPrimary() throws RemoteException {
        // TODO:
        // 1) Set this replica to act as primary.
        // 2) Discover current backups in the RMI registry.
        // 3) Call setBackups(...) with the discovered list.
        // 4) Log useful information.
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
        // 2) Skip your own name ("replica" + myId).
        // 3) For each, look it up, cast to ReplicaControl, and only include if ping() returns true.
        // 4) Return the list.
    }
}
