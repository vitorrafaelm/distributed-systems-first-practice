package org.example.service_order_proxy;


import org.example.service_order_proxy.server.Server;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Proxy3 {
    public static void main(String[] args) {
        int AplicationServerPort = 54322;

        String proxyName = args[0];
        String rmiPort = args[1];
        String rmiName = args[2];
        int proxyPort = 54331;
        String AplicationServerIp = "localhost";
        Map<String, String> proxies = new HashMap<>();
        
        try {
            proxies.put("1201", "CacheUpdateProxy1");
            proxies.put("1202", "CacheUpdateProxy2");
            proxies.put("1203", "CacheUpdateProxy3");

            proxies.entrySet().removeIf(entry -> Objects.equals(entry.getValue(), rmiName));
            // Inicia o servidor proxy com as informações obtidas
            new Server(proxyPort, AplicationServerIp, AplicationServerPort, proxies, rmiPort, proxyName, rmiName);

        } catch (NumberFormatException e) {
            System.err.println("Formato inválido para porta do servidor de aplicação: " + e.getMessage());
            e.printStackTrace();
        }
    }
}