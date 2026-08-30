package com.ecommerce.notification_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter messageConverter(){
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public Queue notificationQueue(){
        return QueueBuilder.durable("notification-queue")
                .withArgument("x-dead-letter-exchange","notification-dlx")
                .withArgument("x-dead-letter-routing-key","notification.dead")
                .build();
    }

    @Bean
    public TopicExchange orderEventsExchange(){
        return new TopicExchange("order-events");
    }

    @Bean
    public Binding binding(Queue notificationQueue, TopicExchange orderEventsExchange){
        return BindingBuilder.bind(notificationQueue).to(orderEventsExchange).with("order.confirmed");
    }

    @Bean
    public Binding cancelledBinding(Queue notificationQueue, TopicExchange orderEventsExchange){
        return BindingBuilder.bind(notificationQueue).to(orderEventsExchange).with("order.cancelled");
    }

    @Bean
    public DirectExchange deadLetterExchange(){
        return new DirectExchange("notification-dlx");
    }

    @Bean
    public Queue deadLetterQueue(){
        return new Queue("notification-dlq", true);
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange){
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with("notification.dead");
    }

}
