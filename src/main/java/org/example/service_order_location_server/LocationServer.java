package org.example.service_order_location_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LocationServer {

    private static final int PORT = 8000;
    private static final Logger logger = Logger.getLogger("LocationServerLog");

    // Lista de proxies disponíveis
    private static final List<String> proxyServers = new ArrayList<>();
    private static int currentProxyIndex = 0;
    private static final boolean useRandomBalancing = false; // true para random, false para round-robin
    private static final Random random = new Random();

    public static void main(String[] args) {
        setupLogger();

        // Inicia os proxies
        proxyServers.add("12.0.0.1:54321"); // Proxy 1
        proxyServers.add("12.0.0.1:54331"); // Proxy 2
        proxyServers.add("12.0.0.1:54341"); // Proxy 3

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor de Localização iniciado na porta " + PORT);
            logger.info("Servidor de Localização iniciado na porta " + PORT);
            System.out.println("Proxies registrados: " + proxyServers);
            logger.info("Proxys registrados: " + proxyServers);

            // Loop para aceitar conexões
            while (true) {
                try {
                    // Aceita uma conexão de cliente
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Nova conexão de cliente: " + clientSocket.getInetAddress().getHostAddress());
                    logger.info("Nova conexão de cliente: " + clientSocket.getInetAddress().getHostAddress());

                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    new Thread(clientHandler).start();
                } catch (IOException e) {
                    logger.severe("Erro ao aceitar conexão: " + e.getMessage());
                    System.err.println("Erro ao aceitar conexão: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.severe("Erro ao iniciar o servidor: " + e.getMessage());
            System.err.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }

    private static void setupLogger() {
        try {
            FileHandler fileHandler = new FileHandler("location_server_log.txt", true);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false); // Desativa o log no console
        } catch (IOException e) {
            System.err.println("Erro ao configurar logger: " + e.getMessage());
        }
    }

    // Método para selecionar o próximo proxy usando balanceamento de carga
    private static synchronized String getNextProxyAddress() {
        if (useRandomBalancing) {
            // Balanceamento aleatório
            return proxyServers.get(random.nextInt(proxyServers.size()));
        } else {
            // Balanceamento Round-Robin
            String proxy = proxyServers.get(currentProxyIndex);
            currentProxyIndex = (currentProxyIndex + 1) % proxyServers.size();
            return proxy;
        }
    }

    static class ClientHandler implements Runnable {

        private final Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true); BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                String request = in.readLine();
                logger.info("Requisição recebida: " + request);

                // Obtem o endereço do próximo proxy disponível
                String proxyAddress = getNextProxyAddress();

                // Envia a resposta para o cliente
                out.println(proxyAddress);
                logger.info("Cliente direcionado para o proxy: " + proxyAddress);
                System.out.println("Cliente direcionado para o proxy: " + proxyAddress);

                // Fecha a conexão
                out.close();
                in.close();
                clientSocket.close();

            } catch (IOException e) {
                logger.severe("Erro ao processar requisição do cliente: " + e.getMessage());
                System.err.println("Erro ao processar requisição do cliente: " + e.getMessage());
            }
        }
    }
}
