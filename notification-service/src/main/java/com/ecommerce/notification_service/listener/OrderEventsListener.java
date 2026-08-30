package com.ecommerce.notification_service.listener;

import com.ecommerce.notification_service.event.OrderCancelledEvent;
import com.ecommerce.notification_service.event.OrderConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventsListener {

    private final JavaMailSender mailSender;

    @RabbitListener(queues = "notification-queue")
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event){
        log.info("Pedido confirmado para Orden: {}",event.orderNumber());

        // throw new RuntimeException("error");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("test@local.com");
        message.setTo(event.email());
        message.setSubject("Orden confirmada: "+event.orderNumber());
        message.setText("El pedido ha sido recibido exitosamente, gracias por su compra");
        mailSender.send(message);

        log.info("Enviando correo de conformación");
        log.info("Correo enviado");
    }

    @RabbitListener(queues = "notification-queue")
    public void handleOrderCancelledEvent(OrderCancelledEvent event){
        log.warn("Enviando correo de cancelacion para la orden: {}",event.orderNumber());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(event.email());
        message.setSubject("Actualización de tu pedido: "+event.orderNumber());
        message.setText("El pedido ha sido cancelado, motivo: "+event.reason());
        mailSender.send(message);

        log.info("Correo de cancelación enviado a: {}", event.email());
    }

}
