package com.tartlabs.mediafileupload.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tartlabs.mediafileupload.eventBus.AppEventBus;
import com.tartlabs.mediafileupload.eventBus.OnRetryEventReceiver;
import com.tartlabs.mediafileupload.model.UploadData;

import static com.tartlabs.mediafileupload.util.ConversionUtils.getJsonFromString;

public class UploadNotificationActionReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationActionRecei";
    public static final String ACTION_RETRY = "com.tartlabs.library.ACTION_RETRY";
    public static final String ACTION_CLEAR = "com.tartlabs.library.ACTION_CLEAR";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            String uploadItemStr = null;
            UploadData uploadData = null;

            // get upload media files string to shared preference
            uploadItemStr = intent.getStringExtra("uploadData");
            if (uploadItemStr != null) {
                // convert  string  to upload item object file
                uploadData = getJsonFromString(uploadItemStr, UploadData.class);
            }
            int notificationId = intent.getIntExtra("notificationId", 0);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            switch (intent.getAction()) {
                case ACTION_RETRY:
                    if (notificationManager != null) {
                        notificationManager.cancel(notificationId);
                    }
                    AppEventBus.getInstance().post(new OnRetryEventReceiver(uploadData, notificationId));
                    break;
                case ACTION_CLEAR:
                    if (notificationManager != null) {
                        notificationManager.cancel(notificationId);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}