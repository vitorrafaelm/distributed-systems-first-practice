package org.proxy.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.service_order_proxy.threads.ProxyThread;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private int proxyPort;    // Socket para receber conexões dos clientes
    private String appServerIp;           // IP do servidor de aplicação
    private int appServerPort;            // Porta do servidor de aplicação
    static private File logFile;

    static final String PROXY_API_KEY = "90e476f3-5ccc-4143-8e30-f4b82b6dd131";

    public Server(int proxyPort, String appServerIp, int appServerPort) {
        this.proxyPort = proxyPort;
        this.appServerIp = appServerIp;
        this.appServerPort = appServerPort;
        this.logFile = new File("proxy_log.txt");
        initializeProxyServer();
    }

    public void initializeProxyServer() {
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

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
                ProxyThread proxyThread = new ProxyThread(logFile, appServerIp, appServerPort, readLine, clientOutput, clientInput, clientSocket);
                Thread thread = new Thread(proxyThread);
                thread.start();
            }
        } catch (IOException e) {
            System.out.println("Erro ao iniciar o proxy: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
