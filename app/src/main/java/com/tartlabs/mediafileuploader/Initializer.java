package com.tartlabs.mediafileuploader;

import android.app.Application;
import android.content.Context;

public class Initializer extends Application {

    private static Initializer initializer;

    @Override
    public void onCreate() {
        super.onCreate();
        initializer = this;

    }

    public static Initializer getInstance() {
        return initializer;
    }

    public static Context getStaticContext() {
        return Initializer.getInstance().getApplicationContext();
    }
}