package vn.hoidanit.jobservice.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for Aggregates in DDD
 * Manages domain events that will be published when aggregate is saved
 */
public abstract class AggregateRoot {

    private final List<vn.hoidanit.jobservice.domain.event.DomainEvent> domainEvents = new ArrayList<>();

    /**
     * Register a domain event to be published
     */
    protected void registerEvent(vn.hoidanit.jobservice.domain.event.DomainEvent event) {
        this.domainEvents.add(event);
    }

    /**
     * Get all registered domain events
     */
    public List<vn.hoidanit.jobservice.domain.event.DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * Clear all domain events (usually called after publishing)
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}

