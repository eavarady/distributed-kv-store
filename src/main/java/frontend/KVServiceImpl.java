package frontend;

import io.grpc.stub.StreamObserver;
import kv.KVServiceGrpc;
import kv.PutRequest;
import kv.PutReply;
import kv.GetRequest;
import kv.GetReply;
import replica.PrimaryAPI;
import replica.ReplicaControl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

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
        // 2) Update this.primaryStub so that future requests go to the new primary.
        // 3) Remove this backup from remainingBackups (it is no longer a backup).
        // 4) Optionally: log what happened.
    }

    @Override
    public void put(PutRequest request, StreamObserver<PutReply> responseObserver) {
        boolean ok = false;

        // TODO: 
        // 1) call handleClientPut on the primary replica (primaryStub)
        // 2) update the boolean ok (if needed)
        // 3) Implement a failure detector –
        //      a) Use try, catch (using appropriate exception) to detect failure
        //      b) If primary has failed, then failover to backup, using failoverToBackup()

        // TODO: 
        // Build a reply and send it back to the client
    }

    @Override
    public void get(GetRequest request, StreamObserver<GetReply> responseObserver) {
        String value = null;
        boolean found = false;

        // TODO: 
        // 1) Call handleClientGet on the primary replica (primaryStub)
        // 2) update value and found variables (if needed)
        // 3) Implement a failure detector –
        //      a) Use try, catch (using appropriate exception) to detect failure
        //      b) If primary has failed, then failover to backup, using failoverToBackup()


        // TODO: 
        // Build a reply and send it back to the client
    }
}

