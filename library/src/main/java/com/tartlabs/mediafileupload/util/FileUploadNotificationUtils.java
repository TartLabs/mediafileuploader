package com.tartlabs.mediafileupload.util;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;

import com.tartlabs.mediafileupload.R;
import com.tartlabs.mediafileupload.config.UploadNotificationConfig;

import java.util.Date;

public class FileUploadNotificationUtils {
    public static final String NOTIFICATION_CHANNEL_ID = "file_uploader_1002";
    public static final String NOTIFICATION_CHANNEL_NAME = "file_uploader";
    public static int notificationId = 0;

    private static NotificationManager mNotificationManager;
    private static NotificationCompat.Builder mBuilder;

    static long startTime;


    /**
     * Gets the elapsed time as a string, expressed in seconds if the value is {@code < 60},
     * or expressed in minutes:seconds if the value is {@code >=} 60.
     *
     * @return string representation of the elapsed time
     */
    public static String getElapsedTimeString() {
        int elapsedSeconds = (int) (getElapsedTime() / 1000);

        if (elapsedSeconds == 0)
            return "0s";

        int minutes = elapsedSeconds / 60;
        elapsedSeconds -= (60 * minutes);

        if (minutes == 0) {
            return elapsedSeconds + "s";
        }

        return minutes + "m " + elapsedSeconds + "s";
    }

    /**
     * Gets upload task's start timestamp in milliseconds.
     *
     * @return long value
     */
    public static long getStartTime() {
        return startTime;
    }

    /**
     * Sets upload task's start timestamp in milliseconds.
     *
     * @return long value
     */
    public static void setStartTime(long startTime_) {
        startTime = startTime_;
    }

    /**
     * Gets upload task's elapsed time in milliseconds.
     *
     * @return long value
     */
    public static long getElapsedTime() {
        long currentTime = new Date().getTime();
        return (currentTime - getStartTime());
    }

    public static UploadNotificationConfig getNotificationConfig(Context context, String title, UploadNotificationConfig uploadNotificationConfig) {
        UploadNotificationConfig config = new UploadNotificationConfig();
        if (uploadNotificationConfig != null) {

           /* PendingIntent clickIntent = PendingIntent.getActivity(
                    this, 1, new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

            config.setTitleForAllStatuses(getString(title))
                    .setRingToneEnabled(true)
                    .setClickIntentForAllStatuses(clickIntent)
                    .setClearOnActionForAllStatuses(true);*/

            // inprogress status
            if (uploadNotificationConfig.getProgress().title != null) {
                config.getProgress().title = uploadNotificationConfig.getProgress().title;
            } else {
                config.getProgress().title = title;
            }
            if (uploadNotificationConfig.getProgress().message != null) {
                config.getProgress().message = uploadNotificationConfig.getProgress().message;
            } else {
                config.getProgress().message = context.getString(R.string.uploading);
            }
            if (uploadNotificationConfig.getProgress().iconResourceID != 0) {
                config.getProgress().iconResourceID = uploadNotificationConfig.getProgress().iconResourceID;
            } else {
                config.getProgress().iconResourceID = R.drawable.ic_stat_notification;
            }
            if (uploadNotificationConfig.getProgress().iconColorResourceID != 0) {
                config.getProgress().iconColorResourceID = uploadNotificationConfig.getProgress().iconColorResourceID;
            } else {
                config.getProgress().iconColorResourceID = Color.GREEN;
            }
            if (uploadNotificationConfig.getProgress().clearOnAction) {
                uploadNotificationConfig.getProgress().clearOnAction = uploadNotificationConfig.getCancelled().clearOnAction;
            } else {
                uploadNotificationConfig.getProgress().clearOnAction = uploadNotificationConfig.getCancelled().clearOnAction;
            }


            // completed status
            if (uploadNotificationConfig.getCompleted().title != null) {
                config.getCompleted().title = uploadNotificationConfig.getCompleted().title;
            } else {
                config.getCompleted().title = title;
            }
            if (uploadNotificationConfig.getCompleted().message != null) {
                config.getCompleted().message = uploadNotificationConfig.getCompleted().message;
            } else {
                config.getCompleted().message = context.getString(R.string.upload_success);
            }
            if (uploadNotificationConfig.getCompleted().iconResourceID != 0) {
                config.getCompleted().iconResourceID = uploadNotificationConfig.getCompleted().iconResourceID;
            } else {
                config.getCompleted().iconResourceID = R.drawable.ic_stat_notification;
            }
            if (uploadNotificationConfig.getCompleted().iconColorResourceID != 0) {
                config.getCompleted().iconColorResourceID = uploadNotificationConfig.getCompleted().iconColorResourceID;
            } else {
                config.getCompleted().iconColorResourceID = Color.GREEN;
            }
            if (uploadNotificationConfig.getCompleted().clearOnAction) {
                uploadNotificationConfig.getCompleted().clearOnAction = uploadNotificationConfig.getCompleted().clearOnAction;
            } else {
                uploadNotificationConfig.getCompleted().clearOnAction = uploadNotificationConfig.getCompleted().clearOnAction;
            }

            // error status
            if (uploadNotificationConfig.getError().title != null) {
                config.getError().title = uploadNotificationConfig.getError().title;
            } else {
                config.getError().title = title;
            }
            if (uploadNotificationConfig.getError().message != null) {
                config.getError().message = uploadNotificationConfig.getError().message;
            } else {
                config.getError().message = context.getString(R.string.upload_error);
            }
            if (uploadNotificationConfig.getError().iconResourceID != 0) {
                config.getError().iconResourceID = uploadNotificationConfig.getError().iconResourceID;
            } else {
                config.getError().iconResourceID = R.drawable.ic_stat_notification;
            }
            if (uploadNotificationConfig.getError().iconColorResourceID != 0) {
                config.getError().iconColorResourceID = uploadNotificationConfig.getError().iconColorResourceID;
            } else {
                config.getError().iconColorResourceID = Color.RED;
            }
            if (uploadNotificationConfig.getError().clearOnAction) {
                uploadNotificationConfig.getError().clearOnAction = uploadNotificationConfig.getError().clearOnAction;
            } else {
                uploadNotificationConfig.getError().clearOnAction = uploadNotificationConfig.getError().clearOnAction;
            }

            // cancel status
            if (uploadNotificationConfig.getCancelled().title != null) {
                config.getCancelled().title = uploadNotificationConfig.getCancelled().title;
            } else {
                config.getCancelled().title = title;
            }
            if (uploadNotificationConfig.getCancelled().message != null) {
                config.getCancelled().message = uploadNotificationConfig.getCancelled().message;
            } else {
                config.getCancelled().message = context.getString(R.string.upload_cancelled);
            }
            if (uploadNotificationConfig.getCancelled().iconColorResourceID != 0) {
                config.getCancelled().iconResourceID = uploadNotificationConfig.getCancelled().iconColorResourceID;
            } else {
                config.getCancelled().iconResourceID = R.drawable.ic_stat_notification;
            }

            if (uploadNotificationConfig.getCancelled().iconColorResourceID != 0) {
                config.getCancelled().iconColorResourceID = uploadNotificationConfig.getCancelled().iconColorResourceID;
            } else {
                config.getCancelled().iconColorResourceID = Color.YELLOW;
            }

            if (uploadNotificationConfig.getCancelled().clearOnAction) {
                uploadNotificationConfig.getCancelled().clearOnAction = uploadNotificationConfig.getCancelled().clearOnAction;
            } else {
                uploadNotificationConfig.getCancelled().clearOnAction = uploadNotificationConfig.getCancelled().clearOnAction;
            }


            return config;
        } else {

            // PendingIntent clickIntent = PendingIntent.getActivity(
            //        context, 1, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

            config.setTitleForAllStatuses(title)
                    .setRingToneEnabled(true)
                    //.setClickIntentForAllStatuses(clickIntent)
                    .setClearOnActionForAllStatuses(true);
            config.getProgress().message = context.getString(R.string.uploading);
            config.getProgress().iconResourceID = R.drawable.ic_stat_notification;
            config.getProgress().iconColorResourceID = Color.GREEN;

            config.getCompleted().message = context.getString(R.string.upload_success);
            config.getCompleted().iconResourceID = R.drawable.ic_stat_notification;
            config.getCompleted().iconColorResourceID = Color.GREEN;

            config.getError().message = context.getString(R.string.upload_error);
            config.getError().iconResourceID = R.drawable.ic_stat_notification;
            config.getError().iconColorResourceID = Color.RED;

            config.getCancelled().message = context.getString(R.string.upload_cancelled);
            config.getCancelled().iconResourceID = R.drawable.ic_stat_notification;
            config.getCancelled().iconColorResourceID = Color.YELLOW;

            return config;
        }
    }
}
