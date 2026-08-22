package com.ecommerce.order_service.service.impl;

import com.ecommerce.order_service.dto.OrderRequest;
import com.ecommerce.order_service.dto.OrderResponse;
import com.ecommerce.order_service.exception.ResourceNotFoundException;
import com.ecommerce.order_service.mapper.OrderMapper;
import com.ecommerce.order_service.model.Order;
import com.ecommerce.order_service.model.OrderLineItems;
import com.ecommerce.order_service.repository.OrderRepository;
import com.ecommerce.order_service.service.OrderService;
import com.ecommerce.order_service.service.client.InventoryClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@RefreshScope
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final InventoryClient inventoryClient;

    @Value("${order.enabled:true}")
    private boolean ordersEnabled;

    public OrderResponse fallbackMethod(OrderRequest orderRequest, String userId, Throwable throwable) {
        log.error("Circuit breaker activado, causa: {}", throwable.getMessage());
        throw new RuntimeException("El servicio de inventario no responde, intente mas tarde");
    }

    @Override
    @Transactional
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @Retry(name = "inventory")
    public OrderResponse placeOrder(OrderRequest orderRequest, String userId) {
        if(!ordersEnabled){
            log.warn("Pedido rechazado, servicio deshabilitado por configuración");
            throw new RuntimeException("El servicio de pedidos esta actualmente en mantenimiento");
        }
        log.info("Colocando nuevo pedido");
        Order order = orderMapper.toOrder(orderRequest);
        order.setUserId(userId);

        for(OrderLineItems item: order.getOrderLineItemsList()){
            String sku = item.getSku();
            Integer quantity = item.getQuantity();
            try {
                inventoryClient.reduceStock(sku, quantity);
            }catch (Exception e){
                log.error("Error al reducir stock para el producto {}: {}", sku, e.getMessage());
                throw new IllegalArgumentException("No se pudo procesar la orden: Stock insuficiente/Error de inventario");
            }
        }

        order.setOrderNumber(UUID.randomUUID().toString());
        Order saved = orderRepository.save(order);
        log.info("Orden guardanda con exito");
        return orderMapper.toOrderResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(String userId, boolean isAdmin) {
        List<Order> orders;
        if(isAdmin){
            orders = orderRepository.findAll();
        }else{
            orders = orderRepository.findByUserId(userId);
        }
        return orders.stream().map(orderMapper::toOrderResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Orden","id",id)
        );
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        if(!orderRepository.existsById(id)){
            throw new ResourceNotFoundException("Orden","id",id);
        }
        orderRepository.deleteById(id);
        log.info("Orden eliminada. ID: {}", id);
    }
}
