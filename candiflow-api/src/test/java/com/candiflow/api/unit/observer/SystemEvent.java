package com.candiflow.api.unit.observer;

import com.candiflow.api.observer.Event;
import com.candiflow.api.observer.EventType;

import java.time.Instant;

/**
 * Événement système pour les tests
 */
public class SystemEvent implements Event {
    private final EventType type;
    private final String source;
    private final Instant timestamp;
    private final String message;

    public SystemEvent(EventType type, String source, String message) {
        this.type = type;
        this.source = source;
        this.timestamp = Instant.now();
        this.message = message;
    }

    @Override
    public EventType getType() {
        return type;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String getSource() {
        return source;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }
}
