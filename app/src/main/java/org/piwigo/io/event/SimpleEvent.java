package org.piwigo.io.event;

public class SimpleEvent
{
    private final String message;

    public SimpleEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return (message);
    }
}
