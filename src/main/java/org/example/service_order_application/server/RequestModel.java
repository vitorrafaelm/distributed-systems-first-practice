package org.example.service_order_application.server;

public class RequestModel {
    public String method;
    public String router;
    public String operation;

    public RequestModel(String method, String router, String operation) {
        this.method = method;
        this.router = router;
        this.operation = operation;
    }
}
