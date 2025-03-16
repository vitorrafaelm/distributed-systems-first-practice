package org.example.service_order_proxy.RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIService extends Remote {
    /**
     * Busca um item no cache do proxy
     * @param key Chave do item
     * @return O valor armazenado no cache ou null se não encontrado
     */
    String getCacheItem(String key) throws RemoteException;
    
    /**
     * Invalida um item no cache
     * @param key Chave do item a ser invalidado
     */
    void invalidateCacheItem(String key) throws RemoteException;
    
    // Invalida toda a cache
    void invalidateAllCache() throws RemoteException;
    
    /**
     * Adiciona ou atualiza um item no cache
     * @param key Chave do item
     * @param value Valor a ser armazenado
     */
    void updateCacheItem(String key, String value) throws RemoteException;
    
    /**
     * Verifica se o proxy está ativo (para detecção de falhas)
     * @return true se o proxy estiver ativo
     */
    boolean isAlive() throws RemoteException;
}