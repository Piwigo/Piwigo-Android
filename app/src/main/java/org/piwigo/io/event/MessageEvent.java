package org.piwigo.io.event;

public class MessageEvent
{
    private final int titleId;
    private final int contentId;

    public MessageEvent(int titleId, int contentId) {
        this.titleId = titleId;
        this.contentId = contentId;
    }

    public int getTitleId() {
        return (titleId);
    }

    public int getContentId() {
        return (contentId);
    }
}
