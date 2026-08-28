package com.ecommerce.notification_service.listener;

import com.ecommerce.notification_service.event.OrderPlacedEvent;
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
    public void handleOrderPlacedEvent(OrderPlacedEvent event){
        log.info("Evento recibido en Inventario para Orden: {}",event.orderNumber());
        try {

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("test@local.com");
            message.setTo(event.email());
            message.setSubject("Orden confirmada: "+event.orderNumber());
            message.setText("El pedido ha sido recibido exitosamente, gracias por su compra");
            mailSender.send(message);

            log.info("Enviando correo de conformación");
            log.info("Correo enviado");
        }catch (Exception e){
            log.error("Error al envio correo: {}", e.getMessage());
        }
    }

}
