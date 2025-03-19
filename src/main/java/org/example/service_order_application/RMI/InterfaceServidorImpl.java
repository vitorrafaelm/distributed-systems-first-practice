package org.example.service_order_application.RMI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.example.service_order_application.server.RequestRedirect;

public class InterfaceServidorImpl extends UnicastRemoteObject implements RMIServidor {
    private RequestRedirect requestRedirect;

    public InterfaceServidorImpl(RequestRedirect requestRedirect) throws RemoteException {
        super();
        this.requestRedirect = requestRedirect;
    }

    @Override
    public String processRequest(String request) throws RemoteException {
        try {
            return requestRedirect.redirect(request);
        } catch (Exception e) {
            throw new RemoteException("Erro ao processar requisição", e);
        }
    }

    @Override
    public boolean isAlive() throws RemoteException {
        return true;
    }
}