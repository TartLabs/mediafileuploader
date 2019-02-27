package com.tartlabs.mediafileupload.config;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;

public class UploadNotificationStatusConfig implements Parcelable {

    /**
     * Notification title.
     */
    public String title;

    /**
     * Notification message.
     */
    public String message;

    /**
     * Clear the notification automatically.
     * This would not affect progress notification, as it's ongoing and managed by upload service.
     * It's used to be able to automatically dismiss cancelled, error or completed notifications.
     */
    public boolean autoClear = false;

    /**
     * Notification icon.
     */
    public int iconResourceID = android.R.drawable.ic_menu_upload;

    /**
     * Large notification icon.
     */
    public Bitmap largeIcon = null;

    /**
     * Icon color tint.
     */
    public int iconColorResourceID = NotificationCompat.COLOR_DEFAULT;

    /**
     * Intent to be performed when the user taps on the notification.
     */
    public PendingIntent clickIntent = null;

    /**
     * Clear the notification automatically when the clickIntent is performed.
     * This would not affect progress notification, as it's ongoing and managed by upload service.
     */
    public boolean clearOnAction = false;


    final PendingIntent getClickIntent(Context context) {
        if (clickIntent == null) {
            return PendingIntent.getBroadcast(context, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        }

        return clickIntent;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.message);
        dest.writeByte(this.autoClear ? (byte) 1 : (byte) 0);
        dest.writeByte(this.clearOnAction ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.largeIcon, flags);
        dest.writeInt(this.iconResourceID);
        dest.writeInt(this.iconColorResourceID);
        dest.writeParcelable(this.clickIntent, flags);
    }

    public UploadNotificationStatusConfig() {
    }

    protected UploadNotificationStatusConfig(Parcel in) {
        this.title = in.readString();
        this.message = in.readString();
        this.autoClear = in.readByte() != 0;
        this.clearOnAction = in.readByte() != 0;
        this.largeIcon = in.readParcelable(Bitmap.class.getClassLoader());
        this.iconResourceID = in.readInt();
        this.iconColorResourceID = in.readInt();
        this.clickIntent = in.readParcelable(PendingIntent.class.getClassLoader());
    }

    public static final Creator<UploadNotificationStatusConfig> CREATOR = new Creator<UploadNotificationStatusConfig>() {
        @Override
        public UploadNotificationStatusConfig createFromParcel(Parcel source) {
            return new UploadNotificationStatusConfig(source);
        }

        @Override
        public UploadNotificationStatusConfig[] newArray(int size) {
            return new UploadNotificationStatusConfig[size];
        }
    };
}