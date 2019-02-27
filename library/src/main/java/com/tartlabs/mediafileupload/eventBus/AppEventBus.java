package com.tartlabs.mediafileupload.eventBus;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class AppEventBus {
    private static AppEventBus appEventBus;
    private PublishSubject<Object> subject = PublishSubject.create();

    public static AppEventBus getInstance() {
        if (appEventBus == null) {
            appEventBus = new AppEventBus();
        }
        return appEventBus;
    }

    public void post(Object object) {
        subject.onNext(object);
    }

    public Observable<Object> asObservable() {
        return subject;
    }
}
