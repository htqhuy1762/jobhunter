package vn.hoidanit.notificationservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EMAIL_QUEUE = "email.queue";
    public static final String EMAIL_EXCHANGE = "email.exchange";
    public static final String EMAIL_ROUTING_KEY = "email.routing.key";

    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.routing.key";

    @Bean
    public Queue emailQueue() {
        return new Queue(EMAIL_QUEUE, true); // durable = true
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    @Bean
    public TopicExchange emailExchange() {
        return new TopicExchange(EMAIL_EXCHANGE);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, TopicExchange emailExchange) {
        return BindingBuilder
                .bind(emailQueue)
                .to(emailExchange)
                .with(EMAIL_ROUTING_KEY);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange notificationExchange) {
        return BindingBuilder
                .bind(notificationQueue)
                .to(notificationExchange)
                .with(NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}