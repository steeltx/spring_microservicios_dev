package com.ecommerce.notification_service.listener;

import com.ecommerce.notification_service.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class OrderEventsListener {

    @RabbitListener(queues = "notification-queue")
    public void handleOrderPlacedEvent(OrderPlacedEvent event){
        log.info("Evento recibido en Inventario para Orden: {}",event.orderNumber());
        event.items().forEach(item -> {
            try {
                log.info("Enviando correo de conformación");
                log.info("Correo enviado");
            }catch (Exception e){
                log.error("Error al envio correo: {}", e.getMessage());
            }
        });
    }

}
