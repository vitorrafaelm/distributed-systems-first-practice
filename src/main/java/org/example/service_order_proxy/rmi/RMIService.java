package org.example.service_order_proxy.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIService extends Remote {

    String getCacheItem(String key) throws RemoteException;

    void invalidateCacheItem(String key) throws RemoteException;

    // Invalida toda a cache
    void invalidateAllCache() throws RemoteException;

    void updateCacheItem(String key, String value) throws RemoteException;

    boolean isAlive() throws RemoteException;
}