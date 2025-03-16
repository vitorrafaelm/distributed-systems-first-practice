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
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

import org.example.service_order_proxy.RMI.InterfaceImpl;
import org.example.service_order_proxy.RMI.RMIService;

public class Proxy2 {
    private int port = 54331; 
    private String appServerIp = "localhost";
    private int appServerPort = 5322;
    private File logFile;
    private Map<String, String> cache = new HashMap<>();
    private RMIService[] otherProxies = new RMIService[2];

    public Proxy2() {
        this.logFile = new File("proxy2.log"); // Arquivo de log 
    }

    public void start() {
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            // Iniciar o servidor RMI
            Registry registry = LocateRegistry.createRegistry(1100); // Porta RMI do Proxy2
            RMIService rmiService = new InterfaceImpl(cache);
            Naming.rebind("//localhost/Proxy2", rmiService); // Nome do serviço RMI para o Proxy2

            // Obter referências para os outros proxys
            otherProxies[0] = (RMIService) Naming.lookup("//localhost/Proxy1"); 
            otherProxies[1] = (RMIService) Naming.lookup("//localhost/Proxy3");

            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Proxy 2 iniciado na porta " + port);

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
                    clientSocket);

                new Thread(thread).start();
            }
        } catch (IOException e) {
            System.out.println("Erro ao iniciar o Proxy 2: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro ao configurar RMI: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Proxy2 proxy = new Proxy2();
        proxy.start();
    }
}