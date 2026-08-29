package com.ecommerce.order_service.service;

import com.ecommerce.order_service.dto.OrderRequest;
import com.ecommerce.order_service.dto.OrderResponse;
import com.ecommerce.order_service.model.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponse placeOrder(OrderRequest orderRequest, String userId);
    //List<OrderResponse> getAllOrders();
    List<OrderResponse> getOrders(String userId, boolean isAdmin);
    OrderResponse getOrderById(Long id);
    void deleteOrder(Long id);
    void updateOrderStatus(String orderNumber, OrderStatus newStatus);
}
