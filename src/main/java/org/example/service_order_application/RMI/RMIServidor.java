package org.example.service_order_application.RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIServidor extends Remote {

    String processRequest(String request) throws RemoteException;

    // mudar para o ID
    boolean isAlive() throws RemoteException;
}
