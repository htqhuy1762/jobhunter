package vn.hoidanit.jobservice.domain.event;

import java.time.Instant;

/**
 * Base interface for all Domain Events in DDD
 * Domain Events represent something important that happened in the domain
 */
public interface DomainEvent {

    /**
     * When the event occurred
     */
    Instant occurredOn();

    /**
     * Type of the event
     */
    String eventType();
}