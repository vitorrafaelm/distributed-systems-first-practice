package org.example.service_order_proxy.RMI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

public class InterfaceImpl extends UnicastRemoteObject implements RMIService {

    private Map<String, String> cache; // Referência ao cache do proxy

    public InterfaceImpl(Map<String, String> cache) throws RemoteException {
        super();
        this.cache = cache;
    }

    @Override
    public String getCacheItem(String key) throws RemoteException {
        synchronized (cache) {
            return cache.get(key);
        }
    }

    @Override
    public void invalidateCacheItem(String key) throws RemoteException {
        synchronized (cache) {
            cache.remove(key);
        }
    }

    @Override
    public void invalidateAllCache() throws RemoteException {
        synchronized (cache) {
            cache.clear();
        }
    }

    @Override
    public void updateCacheItem(String key, String value) throws RemoteException {
        synchronized (cache) {
            cache.put(key, value);
        }
    }

    @Override
    public boolean isAlive() throws RemoteException {
        return true;
    }
}
