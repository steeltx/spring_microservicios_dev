package com.ecommerce.order_service.service.impl;

import com.ecommerce.order_service.dto.OrderRequest;
import com.ecommerce.order_service.dto.OrderResponse;
import com.ecommerce.order_service.event.OrderPlacedEvent;
import com.ecommerce.order_service.exception.ResourceNotFoundException;
import com.ecommerce.order_service.mapper.OrderMapper;
import com.ecommerce.order_service.model.Order;
import com.ecommerce.order_service.model.OrderLineItems;
import com.ecommerce.order_service.model.OrderStatus;
import com.ecommerce.order_service.repository.OrderRepository;
import com.ecommerce.order_service.service.OrderService;
import com.ecommerce.order_service.service.OutboxService;
import com.ecommerce.order_service.service.client.InventoryClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    private final RabbitTemplate rabbitTemplate;
    private final OutboxService outboxService;

    @Value("${order.enabled:true}")
    private boolean ordersEnabled;

    public OrderResponse fallbackMethod(OrderRequest orderRequest, String userId, Throwable throwable) {
        log.error("Circuit breaker activado, causa: {}", throwable.getMessage());
        throw new RuntimeException("El servicio de inventario no responde, intente mas tarde");
    }

    @Override
    @Transactional
    public OrderResponse placeOrder(OrderRequest orderRequest, String userId) {
        if(!ordersEnabled){
            log.warn("Pedido rechazado, servicio deshabilitado por configuración");
            throw new RuntimeException("El servicio de pedidos esta actualmente en mantenimiento");
        }
        log.info("Colocando nuevo pedido");
        Order order = orderMapper.toOrder(orderRequest);
        order.setUserId(userId);
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setStatus(OrderStatus.PLACED);
        Order saved = orderRepository.save(order);
        log.info("Orden guardanda con exito");

        List<OrderPlacedEvent.OrderItemEvent> orderItems = order.getOrderLineItemsList()
                .stream()
                .map(item -> new OrderPlacedEvent.OrderItemEvent(
                        item.getSku(), item.getPrice().toString(), item.getQuantity()
                )).toList();
        OrderPlacedEvent event = new OrderPlacedEvent(saved.getOrderNumber(), orderRequest.getEmail(), orderItems);

        boolean sentToRabbit = false;

        try {
            rabbitTemplate.convertAndSend("order-events","order.placed", event);
            sentToRabbit = true;
            log.info("Mensaje enviado a rabbit: {}", saved.getOrderNumber());
        } catch (AmqpException e) {
            log.error("Rabbit caido, se guarda para envio posterior la orden : {}", saved.getOrderNumber());
        }

        outboxService.saveOrderPlacedEvent(event,sentToRabbit);
        log.info("Evento enviado a RabbitMQ para la orden: {}",saved.getOrderNumber());

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

    @Override
    @Transactional
    public void updateOrderStatus(String orderNumber, OrderStatus newStatus) {
        orderRepository.findByOrderNumber(orderNumber).ifPresentOrElse(
                order -> {
                    order.setStatus(newStatus);
                    orderRepository.save(order);
                    log.info("Estado actualizado en DB para la orden: {}", orderNumber);
                },
                () -> log.error("No se encontro la orden para actualizar : {}", orderNumber)
        );
    }
}
