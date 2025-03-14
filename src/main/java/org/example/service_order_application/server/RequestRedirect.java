package org.example.service_order_application.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.service_order_application.controller.ServiceOrderController;
import org.example.service_order_application.controller.dto.ServiceOrderDTO;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RequestRedirect {
    List<RequestModel> requests_allowed = new ArrayList<>();

    public void setUpAllowedOperations() {
        RequestModel[] request_models = {
                new RequestModel("GET", "/service-order", "list"),
                new RequestModel("POST", "/service-order", "add"),
                new RequestModel("PUT", "/service-order", "update"),
                new RequestModel("GET", "/service-order", "list_quantity"),
                new RequestModel("DELETE", "/service-order", "delete"),
                new RequestModel("GET", "/service-order/:id", "search"),
        };
        Collections.addAll(requests_allowed, request_models);
    }

    public String redirect(String request_json) throws SQLException {
        final JsonObject jsonObject = JsonParser.parseString(request_json).getAsJsonObject();

        final String operation = jsonObject.get("operation").getAsString();

        List<RequestModel> request = requests_allowed.stream()
                .filter(model -> Objects.equals(model.operation, operation))
                .collect(Collectors.toList());

        return selectController(request.get(0), jsonObject);
    }

    public String selectController(RequestModel request_model, JsonObject jsonObject) throws SQLException {
        ServiceOrderController serviceOrderController = new ServiceOrderController();

        switch (request_model.operation) {
            case "list":
                return serviceOrderController.index();
            case "update":
                String code_update = jsonObject.get("code").getAsString();
                String description_update = jsonObject.get("description").getAsString();
                String name_update = jsonObject.get("name").getAsString();
                int id = jsonObject.get("id").getAsInt();

                ServiceOrderDTO serviceOrderDTOupdate =  new ServiceOrderDTO();
                serviceOrderDTOupdate.setCode(code_update);
                serviceOrderDTOupdate.setDescription(description_update);
                serviceOrderDTOupdate.setName(name_update);
                serviceOrderDTOupdate.setId(id);

                return String.valueOf(serviceOrderController.update(serviceOrderDTOupdate));
            case "delete":
                int service_order_id = jsonObject.get("id").getAsInt();
                ServiceOrderDTO serviceOrderDTO =  new ServiceOrderDTO();
                serviceOrderDTO.setId(service_order_id);

                boolean deleted = serviceOrderController.destroy(serviceOrderDTO);
                return String.valueOf(deleted);
            case "add":
                String code = jsonObject.get("code").getAsString();
                String description = jsonObject.get("description").getAsString();
                String name = jsonObject.get("name").getAsString();

                ServiceOrderDTO serviceOrderDTOCreate =  new ServiceOrderDTO();
                serviceOrderDTOCreate.setCode(code);
                serviceOrderDTOCreate.setDescription(description);
                serviceOrderDTOCreate.setName(name);

                return serviceOrderController.create(serviceOrderDTOCreate);
            case "list_quantity":
                return serviceOrderController.countServiceOrder();
            case "search":
                int service_order_id_search = jsonObject.get("id").getAsInt();
                ServiceOrderDTO serviceOrderDTOsearch =  new ServiceOrderDTO();
                serviceOrderDTOsearch.setId(service_order_id_search);

                return serviceOrderController.search(serviceOrderDTOsearch);
        }

        return null;
    }
}
