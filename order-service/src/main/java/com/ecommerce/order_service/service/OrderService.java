package com.ecommerce.order_service.service;

import com.ecommerce.order_service.dto.OrderRequest;
import com.ecommerce.order_service.dto.OrderResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface OrderService {

    CompletableFuture<OrderResponse> placeOrder(OrderRequest orderRequest, String userId);
    //List<OrderResponse> getAllOrders();
    List<OrderResponse> getOrders(String userId, boolean isAdmin);
    OrderResponse getOrderById(Long id);
    void deleteOrder(Long id);

}
