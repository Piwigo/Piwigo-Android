package org.piwigo.io.event;

public class SnackProgressEvent extends SimpleEvent
{
    private final int snackbarType;
    private final String snackbarDesc;
    private final int snackbarId;
    private SnackbarUpdateAction action;

    public SnackProgressEvent(int snackbarType, String snackbarDesc, int snackbarId, SnackbarUpdateAction action)
    {
        super(null);
        this.snackbarType = snackbarType;
        this.snackbarDesc = snackbarDesc;
        this.snackbarId = snackbarId;
        this.action = action;
    }

    public int getSnackbarType() {
        return (snackbarType);
    }

    public String getSnackbarDesc() {
        return (snackbarDesc);
    }

    public int getSnackbarId() {
        return (snackbarId);
    }

    public SnackbarUpdateAction getAction() {
        return (action);
    }

    public enum SnackbarUpdateAction {
        REFRESH, KILL;
    }
}
