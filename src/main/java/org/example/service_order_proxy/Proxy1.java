package org.example.service_order_proxy;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.example.service_order_proxy.server.Server;

public class Proxy1 {

    public static void main(String[] args) {
        int AplicationServerPort = 54322;

        String proxyName = "Proxy1";
        String rmiPort = "1201";
        String rmiName = "CacheUpdateProxy1";
        int proxyPort = 54329;
        String AplicationServerIp = "192.168.172.134";
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
