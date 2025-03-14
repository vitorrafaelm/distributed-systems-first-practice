package org.example.service_order_proxy.threads;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProxyThread implements Runnable {
    private BufferedReader clientInput;
    private PrintWriter clientOutput;
    private String line;
    Socket clientSocket;

    private String appServerIp;           // IP do servidor de aplicação
    private int appServerPort;            // Porta do servidor de aplicação
    private File logFile;

    // Cache FIFO
    private static final int CACHE_SIZE = 30;
    private static final Map<String, String> cache = new LinkedHashMap<String, String>(CACHE_SIZE + 1, 0.75f, false) {
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > CACHE_SIZE;
        }
    };

    public ProxyThread(File file, String appServerIp, int appServerPort, String readLine, PrintWriter clientOutput, BufferedReader clientInput, Socket clientSocket) {
        this.logFile = file;
        this.appServerIp = appServerIp;
        this.appServerPort = appServerPort;
        
        this.clientInput = clientInput;
        this.clientOutput = clientOutput;
        this.line = readLine;
        this.clientSocket = clientSocket;

    }

    @Override
    public void run() {
        try {
            String request = this.line;
            String response;

            JsonObject requestJson = JsonParser.parseString(request).getAsJsonObject();
            String operation = requestJson.get("operation").getAsString();

            boolean isReadOperation = "search".equals(operation);

            if (isReadOperation) {
                String id_item = requestJson.get("id").getAsString();
                synchronized (cache) {
                    if (cache.containsKey(id_item)) {
                        registrarLog("CACHE HIT para operação: " + operation + " [Chave: " + id_item + "]");
                        response = cache.get(id_item);
                    } else {
                        registrarLog("CACHE MISS para operação: " + operation + " [Chave: " + id_item + "]");
                        response = processarRequisicao(request);

                        cache.put(id_item, response);
                    }
                }
            } else {
                if ("add".equals(operation) || "update".equals(operation) || "delete".equals(operation)) {
                    invalidateCache();
                    registrarLog("Cache invalidada após operação de escrita: " + operation);
                }

                response = processarRequisicao(request);
            }

            printCacheStatus();
            this.clientOutput.println(response);

            clientOutput.close();
            clientInput.close();
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private String generateCacheKey(JsonObject requestJson) {
        String operation = requestJson.get("operation").getAsString();
        StringBuilder key = new StringBuilder(operation);

        // Adicionar parâmetros específicos na chave dependendo da operação
        if (requestJson.has("id")) {
            key.append("_id_").append(requestJson.get("id").getAsString());
        }
        if (requestJson.has("code") && ("list".equals(operation) || "list_quantity".equals(operation))) {
            key.append("_code_").append(requestJson.get("code").getAsString());
        }

        return key.toString();
    }

    // Invalida entradas da cache relacionadas a operações de listagem
    private synchronized void invalidateCache() {
        synchronized (cache) {
            // Remover todas as entradas que começam com "list" ou "list_quantity"
            cache.entrySet().removeIf(entry ->
                    entry.getKey().startsWith("list") || entry.getKey().startsWith("list_quantity"));
        }
    }

    private void printCacheStatus() {
        StringBuilder sb = new StringBuilder("Estado atual da cache:\n");

        synchronized (cache) {
            sb.append("Tamanho: ").append(cache.size()).append("/").append(CACHE_SIZE).append("\n");
            int count = 0;
            for (Map.Entry<String, String> entry : cache.entrySet()) {
                sb.append(count++).append(": ").append(entry.getKey()).append("\n");
            }
        }

        registrarLog(sb.toString());
    }

    private String processarRequisicao(String requisicao) {
        registrarLog("Requisição: " + requisicao + " - " + new Date());
        return comunicarComServidor(requisicao);
    }

    // Método para comunicar com o servidor de aplicação
    private String comunicarComServidor(String requisicao) {
        Socket serverSocket = null;
        PrintWriter serverOutput = null;
        BufferedReader serverInput = null;
        String resposta = "";

        try {
            // Conexão com o servidor de aplicação
            serverSocket = new Socket(appServerIp, appServerPort);

            /*
            * Troquei clientSocket por serverSocket
            * O clientSocket é a conexão com o cliente, e o serverSocket é a conexão com o servidor de aplicação.
            * A comunicação com o servidor de aplicação é feita através do serverSocket, que é a conexão estabelecida com o servidor de aplicação. */
            serverOutput = new PrintWriter(serverSocket.getOutputStream(), true);
            serverInput = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

            // Envia requisição para o servidor de aplicação
            serverOutput.println(requisicao);
            serverOutput.flush();

            // Recebe resposta do servidor de aplicação
            resposta = serverInput.readLine();

            // Log da operação
            registrarLog("Servidor de aplicação: " + requisicao + " - Response: " + resposta);

        } catch (IOException e) {
            registrarLog("ERRO: " + e.getMessage());
            resposta = "ERRO: " + e.getMessage(); // Retorna erro para o cliente
        } finally {
            // Fechar conexão
            try {
                if (serverInput != null) serverInput.close();
                if (serverOutput != null) serverOutput.close();
                if (serverSocket != null) serverSocket.close();
            } catch (IOException e) {
                System.out.println("Erro ao fechar conexão com servidor: " + e.getMessage());
            }
        }

        return resposta;
    }

    private synchronized void registrarLog(String mensagem) {
        try (FileWriter fw = new FileWriter(logFile, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter pw = new PrintWriter(bw)) {

            pw.println(new Date() + " - " + mensagem);

        } catch (IOException e) {
            System.out.println("Erro ao escrever no log: " + e.getMessage());
        }
    }
}