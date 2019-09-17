package org.piwigo.io.event;

public class SnackbarShowEvent extends SimpleEvent
{

    private final int duration;

    public SnackbarShowEvent(String message, int duration)
    {
        super(message);
        this.duration = duration;
    }

    public int getDuration() {
        return (duration);
    }
}
