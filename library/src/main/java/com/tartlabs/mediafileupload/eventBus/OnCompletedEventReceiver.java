package com.tartlabs.mediafileupload.eventBus;

public class OnCompletedEventReceiver<T> {
    private T object;
    private int notificationId;

    public OnCompletedEventReceiver(T object, int notificationId) {
        this.object = object;
        this.notificationId = notificationId;
    }

    public T getObject() {
        return object;
    }

    public int getNotificationId() {
        return notificationId;
    }
}