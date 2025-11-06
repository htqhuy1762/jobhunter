package vn.hoidanit.jobservice.infrastructure.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.hoidanit.jobservice.domain.event.DomainEvent;

/**
 * Simple in-memory event publisher for demonstration
 * In production, this would integrate with RabbitMQ, Kafka, or Spring Events
 */
@Component
@Slf4j
public class SimpleEventPublisher implements DomainEventPublisher {

    @Override
    public void publish(DomainEvent event) {
        // Log the event
        log.info("Publishing Domain Event: {} - {}", event.eventType(), event);

        // In production, this would:
        // 1. Publish to message queue (RabbitMQ/Kafka)
        // 2. Or use Spring ApplicationEventPublisher
        // 3. Or store in outbox table for eventual consistency

        // For now, just logging to demonstrate the pattern
        // You can integrate with existing RabbitMQ setup here
    }
}


