package org.example.service_order_proxy.rmi;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.Map;

public class InterfaceImpl extends UnicastRemoteObject implements RMIService {

    private Map<String, String> cache; // Referência ao cache do proxy
    String proxyName;
    int rmiPort;
    File file;

    public InterfaceImpl(Map<String, String> cache, String proxyName, File file) throws RemoteException {
        super();
        this.cache = cache;
        this.proxyName = proxyName;
        this.file = file;
    }

    @Override
    public String getCacheItem(String key) throws RemoteException {
        registrarLog("CACHE MISS para operação: getCacheItem [Chave: " + key + "] on proxy: " + proxyName);
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
        registrarLog("CACHE MISS para operação: updateCacheItem [Chave: " + key + "] on proxy: " + proxyName);
        synchronized (cache) {
            cache.put(key, value);
        }
    }

    @Override
    public boolean isAlive() throws RemoteException {
        return true;
    }

    private synchronized void registrarLog(String mensagem) {
        try (FileWriter fw = new FileWriter(file, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {

            pw.println(new Date() + " - " + mensagem);

        } catch (IOException e) {
            System.out.println("Erro ao escrever no log: " + e.getMessage());
        }
    }
}