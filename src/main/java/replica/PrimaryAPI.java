package replica;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Methods the FrontEnd calls on the current primary replica.
 */
public interface PrimaryAPI extends Remote {

    boolean handleClientPut(String key, String value) throws RemoteException;

    String handleClientGet(String key) throws RemoteException;

}

