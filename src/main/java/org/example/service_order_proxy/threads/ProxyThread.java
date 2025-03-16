package org.example.service_order_proxy.threads;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.Map;

import org.example.service_order_proxy.RMI.RMIService;

public class ProxyThread implements Runnable {
    private File logFile;
    private String appServerIp;
    private int appServerPort;
    private String request;
    private PrintWriter clientOutput;
    private BufferedReader clientInput;
    private Socket clientSocket;
    private Map<String, String> cache;
    private RMIService[] otherProxies;

    public ProxyThread(File logFile, String appServerIp, int appServerPort, String request, PrintWriter clientOutput, BufferedReader clientInput, Socket clientSocket) {
        this.logFile = logFile;
        this.appServerIp = appServerIp;
        this.appServerPort = appServerPort;
        this.request = request;
        this.clientOutput = clientOutput;
        this.clientInput = clientInput;
        this.clientSocket = clientSocket;
        this.cache = cache;
        this.otherProxies = otherProxies;
    }

    @Override
    public void run() {
        try {
            // Verifica o cache local primeiro
            String cachedValue = cache.get(request);
            if (cachedValue != null) {
                clientOutput.println(cachedValue);
                return;
            }

            // Se não encontrado no cache local, consulta os outros proxys
            for (RMIService proxy : otherProxies) {
                try {
                    cachedValue = proxy.getCacheItem(request);
                    if (cachedValue != null) {
                        // Atualiza o cache local
                        cache.put(request, cachedValue);
                        clientOutput.println(cachedValue);
                        return;
                    }
                } catch (RemoteException e) {
                    System.err.println("Erro ao consultar proxy: " + e.getMessage());
                }
            }

            // Se não encontrado em nenhum proxy, busca no servidor de aplicação
            try (Socket appServerSocket = new Socket(appServerIp, appServerPort);
                 PrintWriter appServerOutput = new PrintWriter(appServerSocket.getOutputStream(), true);
                 BufferedReader appServerInput = new BufferedReader(new InputStreamReader(appServerSocket.getInputStream()))) {

                appServerOutput.println(request);
                String response = appServerInput.readLine();

                // Atualiza o cache local e nos outros proxys
                cache.put(request, response);
                for (RMIService proxy : otherProxies) {
                    try {
                        proxy.updateCacheItem(request, response);
                    } catch (RemoteException e) {
                        System.err.println("Erro ao atualizar cache no proxy: " + e.getMessage());
                    }
                }

                clientOutput.println(response);
            }
        } catch (IOException e) {
            System.err.println("Erro ao processar requisição: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Erro ao fechar socket: " + e.getMessage());
            }
        }
    }
}