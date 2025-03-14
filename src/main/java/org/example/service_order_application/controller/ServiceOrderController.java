package org.example.service_order_application.controller;

import org.example.service_order_application.controller.dto.ServiceOrderDTO;
import org.example.service_order_application.database.entities.ServiceOrder;
import org.example.service_order_application.database.services.ServiceOrderService;
import com.google.gson.Gson;

import java.sql.SQLException;
import java.util.List;

public class ServiceOrderController {
    private static Gson gson = new Gson();

    // Index
    public String index() {
        ServiceOrderService service_order_service = new ServiceOrderService();
        List<ServiceOrder> service_order = service_order_service.list();
        return gson.toJson(service_order);
    }

    // Create
    public String create(ServiceOrderDTO serviceOrderDTO) throws SQLException {
        ServiceOrderService service_order_service = new ServiceOrderService();
        ServiceOrder service_order = service_order_service.add(serviceOrderDTO);
        System.out.println(service_order);
        return gson.toJson(service_order);
    }

    // Update
    public boolean update(ServiceOrderDTO serviceOrderDTO) throws SQLException {
        ServiceOrderService service_order_service = new ServiceOrderService();
        return service_order_service.update(serviceOrderDTO);
    }

    // destroy
    public boolean destroy(ServiceOrderDTO serviceOrderDTO) {
        ServiceOrderService service_order_service = new ServiceOrderService();
        return service_order_service.delete(serviceOrderDTO);
    }


    // Count
    public String countServiceOrder() {
        ServiceOrderService service_order_service = new ServiceOrderService();
        int service_order_count = service_order_service.get_registers_quantity();
        return gson.toJson(service_order_count);
    }

    // Search
    public String search(ServiceOrderDTO serviceOrderDTO) throws SQLException {
        ServiceOrderService service_order_service = new ServiceOrderService();
        ServiceOrder service_order = service_order_service.search(serviceOrderDTO);
        return gson.toJson(service_order);
    }
}
