package org.example.service_order_proxy.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.service_order_proxy.rmi.InterfaceImpl;
import org.example.service_order_proxy.rmi.RMIService;
import org.example.service_order_proxy.threads.ProxyThread;

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

public class Server {
    private int proxyPort;    // Socket para receber conexões dos clientes
    private String appServerIp;           // IP do servidor de aplicação
    private int appServerPort;            // Porta do servidor de aplicação
    static private File logFile;
    Map<String, String> proxies;
    String rmiPort;
    String proxyName;
    String rmiName;
    private Map<String, String> cache = new HashMap<>();

    static final String PROXY_API_KEY = "90e476f3-5ccc-4143-8e30-f4b82b6dd131";

    public Server(int proxyPort, String appServerIp, int appServerPort, Map<String, String> proxies, String rmiPort, String proxyName, String rmiName) {
        this.proxyPort = proxyPort;
        this.appServerIp = appServerIp;
        this.appServerPort = appServerPort;
        this.proxies = proxies;
        this.rmiPort = rmiPort;
        this.proxyName = proxyName;
        this.rmiName = rmiName;
        this.logFile = new File(proxyName + ".txt");
        initializeProxyServer();
    }

    public void initializeProxyServer() {
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            LocateRegistry.createRegistry(Integer.parseInt(this.rmiPort));
            RMIService rmiService = new InterfaceImpl(cache, proxyName, logFile);
            Naming.rebind("rmi://localhost:" + rmiPort + "/" + rmiName, rmiService);

            System.out.println("Servidor RMI inciado no" + proxyName + " na porta: " + rmiPort);

            ServerSocket serverSocket = new ServerSocket(proxyPort);
            System.out.println("Proxy iniciado na porta " + proxyPort);
            System.out.println("Conectado ao servidor de aplicação em " + appServerIp + ":" + appServerPort);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nova conexão de cliente: " + clientSocket.getInetAddress().getHostAddress());

                BufferedReader clientInput =
                    new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter clientOutput =
                        new PrintWriter(clientSocket.getOutputStream());

                String readLine = clientInput.readLine();
                JsonObject jsonObjectTemp = JsonParser.parseString(readLine).getAsJsonObject();
                String authToken = jsonObjectTemp.get("Authorization").getAsString();

                if (!authToken.equals(PROXY_API_KEY)) {
                    clientOutput.println("Unauthorized Error: Key does not match");
                    clientOutput.close();
                    clientInput.close();
                    clientSocket.close();
                }

                if (readLine.equals("GET_APPLICATION_SERVER")) {
                    clientOutput.println(clientSocket.getInetAddress().getHostAddress() + ":" + appServerPort);

                    System.out.println("Endereço do servidor de aplicação obtido: " + appServerIp + ":" + appServerPort);

                    clientOutput.close();
                    clientInput.close();
                    clientSocket.close();
                    continue;
                }
                
                // Criar uma thread para tratar este cliente
                ProxyThread proxyThread = new ProxyThread(logFile, appServerIp, appServerPort, readLine, clientOutput, clientInput, clientSocket, proxies);
                Thread thread = new Thread(proxyThread);
                thread.start();
            }
        } catch (IOException e) {
            System.out.println("Erro ao iniciar o proxy: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
