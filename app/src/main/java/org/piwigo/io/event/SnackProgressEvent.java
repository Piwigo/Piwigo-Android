package org.piwigo.io.event;

public class SnackProgressEvent extends SimpleEvent
{
    private int snackbarType;
    private String snackbarDesc;
    private int snackbarId;
    private int snackbarDuration;
    private SnackbarUpdateAction action;

    public SnackProgressEvent()
    {
        super(null);
    }

    public void setSnackbarType(int snackbarType) {
        this.snackbarType = snackbarType;
    }

    public void setSnackbarDesc(String snackbarDesc) {
        this.snackbarDesc = snackbarDesc;
    }

    public void setSnackbarId(int snackbarId) {
        this.snackbarId = snackbarId;
    }

    public void setSnackbarDuration(int duration)
    {
        this.snackbarDuration = duration;
    }

    public void setAction(SnackbarUpdateAction action) {
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

    public int getSnackbarDuration()
    {
        return (snackbarDuration);
    }

    public SnackbarUpdateAction getAction() {
        return (action);
    }

    public enum SnackbarUpdateAction {
        REFRESH, KILL;
    }
}
