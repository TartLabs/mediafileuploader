package com.tartlabs.mediafileupload.eventBus;

public class OnFailureEventReceiver<T> {
    private T object;
    private int notificationId, maxRetries;

    public OnFailureEventReceiver(T object, int notificationId, int maxRetries) {
        this.object = object;
        this.notificationId = notificationId;
        this.maxRetries = maxRetries;
    }

    public T getObject() {
        return object;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public int getMaxRetries() {
        return maxRetries;
    }
}
