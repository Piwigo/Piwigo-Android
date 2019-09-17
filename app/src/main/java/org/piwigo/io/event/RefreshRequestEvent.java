package org.piwigo.io.event;

public class RefreshRequestEvent {
    private final int categoryId;

    public RefreshRequestEvent(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getCategoryId() {
        return (categoryId);
    }
}
