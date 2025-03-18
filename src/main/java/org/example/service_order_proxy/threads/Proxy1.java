package org.example.service_order_proxy.threads;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.HashMap;
import java.util.Map;

import org.example.service_order_proxy.RMI.InterfaceImpl;
import org.example.service_order_proxy.RMI.RMIService;

public class Proxy1 {

    private int port = 54321;
    private String appServerIp = "localhost";
    private int appServerPort = 54322;
    private File logFile;
    private Map<String, String> cache = new HashMap<>();
    private Map<String, String> proxies = new HashMap<>();

    public Proxy1() {
        this.logFile = new File("proxy1.log");
    }

    public void start() {
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            proxies.put("1200", "CacheUpdateProxy1");
            proxies.put("1201", "CacheUpdateProxy3");

            LocateRegistry.createRegistry(1199);
            RMIService rmiService = new InterfaceImpl(cache);
            Naming.rebind("rmi://localhost:1199/CacheUpdateProxy1", rmiService);

            System.out.println("Servidor RMI inciado no Proxy 1");

            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Proxy 1 iniciado na porta " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader clientInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter clientOutput = new PrintWriter(clientSocket.getOutputStream(), true);

                String request = clientInput.readLine();

                ProxyThread thread = new ProxyThread(
                        logFile,
                        appServerIp,
                        appServerPort,
                        request,
                        clientOutput,
                        clientInput,
                        clientSocket,
                        proxies
                );

                new Thread(thread).start();
            }
        } catch (IOException e) {
            System.out.println("Erro ao iniciar o Proxy 1: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro ao configurar RMI: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Proxy1 proxy = new Proxy1();
        proxy.start();
    }
}
