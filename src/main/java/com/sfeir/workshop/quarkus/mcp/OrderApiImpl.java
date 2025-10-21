package com.sfeir.workshop.quarkus.mcp;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import org.openapitools.model.NewOrderDto;
import org.openapitools.model.OrderDto;
import org.openapitools.model.UpdateOrderRequestDto;

@ApplicationScoped
@AllArgsConstructor
@JBossLog
public class OrderApiImpl implements OrderServiceService{

    private final Map<String, OrderDto> orders = new ConcurrentHashMap<>();

    @Override
    public Response createOrder(NewOrderDto newOrderDto, SecurityContext securityContext) throws NotFoundException {
        OrderDto createdOrder = new OrderDto();
        createdOrder.setId(UUID.randomUUID().toString());
        createdOrder.setItems(newOrderDto.getItems());
        createdOrder.setTotalAmount(newOrderDto.getItems().stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum());
        createdOrder.setStatus("CREATED");
        createdOrder.setCreatedAt(OffsetDateTime.now());
        orders.put(createdOrder.getId(), createdOrder);

        return Response.status(Response.Status.CREATED).entity(createdOrder).build();
    }

    @Override
    public Response deleteOrder(String orderId, SecurityContext securityContext) throws NotFoundException {
        if (orders.remove(orderId) != null) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Override
    public Response getOrderById(String orderId, SecurityContext securityContext) throws NotFoundException {
        OrderDto order = orders.get(orderId);
        if (order != null) {
            return Response.ok(order).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Override
    public Response listOrders(String status, SecurityContext securityContext) throws NotFoundException {
        List<OrderDto> filteredOrders = orders.values().stream()
                .filter(order -> status == null || order.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
        return Response.ok(filteredOrders).build();
    }

    @Override
    public Response updateOrder(String orderId, UpdateOrderRequestDto updateOrderRequestDto, SecurityContext securityContext) throws NotFoundException {
        OrderDto existingOrder = orders.get(orderId);
        if (existingOrder != null) {
            // Update only the status for now, as per the DTO
            if (updateOrderRequestDto.getStatus() != null) {
                existingOrder.setStatus(updateOrderRequestDto.getStatus());
            }
            // You might want to update other fields if they are in UpdateOrderRequestDto
            orders.put(orderId, existingOrder); // Re-put to ensure map is updated if it's not a direct reference
            return Response.ok(existingOrder).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
