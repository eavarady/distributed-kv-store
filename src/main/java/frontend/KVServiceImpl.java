package frontend;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import io.grpc.stub.StreamObserver;
import kv.GetReply;
import kv.GetRequest;
import kv.KVServiceGrpc;
import kv.PutReply;
import kv.PutRequest;
import replica.PrimaryAPI;
import replica.ReplicaControl;

public class KVServiceImpl extends KVServiceGrpc.KVServiceImplBase {

    private volatile PrimaryAPI primaryStub;
    private final List<ReplicaControl> remainingBackups; // in promotion order

    public KVServiceImpl(PrimaryAPI initialPrimary, List<ReplicaControl> backupsInOrder) {
        this.primaryStub = initialPrimary;
        this.remainingBackups = new ArrayList<>(backupsInOrder);
    }

    /**
     * Helper to promote backup if primary died.
     */
    private synchronized void failoverToBackup(ReplicaControl backupStub) throws RemoteException {
        // TODO:
        // 1) Call promoteToPrimary() on the backupStub.
        backupStub.promoteToPrimary();
        // 2) Update this.primaryStub so that future requests go to the new primary.
        this.primaryStub = (PrimaryAPI) backupStub;
        // 3) Remove this backup from remainingBackups (it is no longer a backup).
        this.remainingBackups.remove(backupStub);
        // 4) Optionally: log what happened.
        System.out.println("FAILOVER: NEW PRIMARY PROMOTED");
        System.out.println("New primary is now: " + backupStub);
    }

    @Override
    public void put(PutRequest request, StreamObserver<PutReply> responseObserver) {
        boolean ok = false;

         try {
        // 1) call handleClientPut on the primary replica (primaryStub)
        // 2) update the boolean ok (if needed)
            ok = primaryStub.handleClientPut(request.getKey(), request.getValue());
        // 3) Implement a failure detector –
        //      a) Use try, catch (using appropriate exception) to detect failure
        } catch (RemoteException primaryFail) {
            System.err.println("PRIMARY FAILED: " + primaryFail.getMessage());
        //      b) If primary has failed, then failover to backup, using failoverToBackup()
            if (!remainingBackups.isEmpty()) {
                // Promote the first backup in the list
                ReplicaControl newBackup = remainingBackups.get(0);
                try {
                    // Promote the backup to primary
                    failoverToBackup(newBackup);
                    // Retry the put operation on the new primary
                    ok = primaryStub.handleClientPut(request.getKey(), request.getValue());
                } catch (RemoteException failoverFail) {
                    // If failover failed, log the error
                    System.err.println("FAILOVER FAILED: " + failoverFail.getMessage());
                }
            } else {
                // No backups available
                System.err.println("NO BACKUPS AVAILABLE FOR FAILOVER");
            }  
        }
        // Build a reply and send it back to the client
        PutReply reply = PutReply.newBuilder().setOk(ok).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void get(GetRequest request, StreamObserver<GetReply> responseObserver) {
        String value = null;
        boolean found = false;

        try {
            // 1) Call handleClientGet on the primary replica (primaryStub)
            value = primaryStub.handleClientGet(request.getKey());
            // 2) update value and found variables (if needed)
            if (value != null) {
                found = true;
            }
        // 3) Implement a failure detector –
        //      a) Use try, catch (using appropriate exception) to detect failure
        } catch (RemoteException primaryFail) {
            System.err.println("PRIMARY FAILED: " + primaryFail.getMessage());
            //      b) If primary has failed, then failover to backup, using failoverToBackup()
            if (!remainingBackups.isEmpty()) {
                // Promote the first backup in the list
                ReplicaControl newBackup = remainingBackups.get(0);
                try {
                    // Promote the backup to primary
                    failoverToBackup(newBackup);
                    // Retry the get operation on the new primary
                    value = primaryStub.handleClientGet(request.getKey());
                    // Update value and found variables (if needed)
                    if (value != null) {
                        found = true;
                    }
                } catch (RemoteException failoverFail) {
                    // If failover failed, log the error
                    System.err.println("FAILOVER FAILED: " + failoverFail.getMessage());
                }
            } else {
                // No backups available
                System.err.println("NO BACKUPS AVAILABLE FOR FAILOVER");
            }  
        }
        // Build a reply and send it back to the client
        GetReply reply = GetReply.newBuilder().setValue(value).setFound(found).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}

