package vn.hoidanit.jobservice.infrastructure.event;

import vn.hoidanit.jobservice.domain.event.DomainEvent;

/**
 * Interface for publishing domain events
 * This abstracts the actual event publishing mechanism (RabbitMQ, Kafka, etc.)
 */
public interface DomainEventPublisher {

    /**
     * Publish a domain event
     * @param event The domain event to publish
     */
    void publish(DomainEvent event);

    /**
     * Publish multiple domain events
     * @param events The domain events to publish
     */
    default void publishAll(Iterable<DomainEvent> events) {
        events.forEach(this::publish);
    }
}