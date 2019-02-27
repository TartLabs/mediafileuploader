package com.tartlabs.mediafileupload.backgroudService;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.tartlabs.mediafileupload.R;
import com.tartlabs.mediafileupload.config.UploadNotificationConfig;
import com.tartlabs.mediafileupload.config.UploadNotificationStatusConfig;
import com.tartlabs.mediafileupload.eventBus.AppEventBus;
import com.tartlabs.mediafileupload.eventBus.OnCompletedEventReceiver;
import com.tartlabs.mediafileupload.eventBus.OnFailureEventReceiver;
import com.tartlabs.mediafileupload.eventBus.OnRetryEventReceiver;
import com.tartlabs.mediafileupload.model.Media;
import com.tartlabs.mediafileupload.model.UploadData;
import com.tartlabs.mediafileupload.model.UploadFiles;
import com.tartlabs.mediafileupload.receiver.UploadNotificationActionReceiver;
import com.tartlabs.mediafileupload.service.FileUploadService;
import com.tartlabs.mediafileupload.util.SharedPrefsUtils;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Date;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

import static com.tartlabs.mediafileupload.constant.Constants.ACCOUNT_PREFS;
import static com.tartlabs.mediafileupload.constant.Constants.UPLOAD_FILES;
import static com.tartlabs.mediafileupload.receiver.UploadNotificationActionReceiver.ACTION_CLEAR;
import static com.tartlabs.mediafileupload.receiver.UploadNotificationActionReceiver.ACTION_RETRY;
import static com.tartlabs.mediafileupload.service.FileUploadService.paramsName;
import static com.tartlabs.mediafileupload.util.ConversionUtils.getJsonFromString;
import static com.tartlabs.mediafileupload.util.ConversionUtils.getStringFromJson;
import static com.tartlabs.mediafileupload.util.FileUploadNotificationUtils.getNotificationConfig;
import static com.tartlabs.mediafileupload.util.FileUploadNotificationUtils.setStartTime;

public class FileUploaderBackgroundService extends Service {
    public static final String NOTIFICATION_CHANNEL_ID = "file_uploader_1002";
    public static final String NOTIFICATION_CHANNEL_NAME = "file_uploader";
    public int notificationId = 0;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;

    private UploadNotificationConfig uploadNotificationConfig;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        AppEventBus.getInstance().asObservable().observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object event) throws Exception {
                        onEvent(event);
                    }
                });
        if (intent != null && intent.getExtras() != null) {
            notificationId = intent.getExtras().getInt("notificationId");
            String uploadNotificationConfigStr = intent.getExtras().getString("uploadNotificationConfig");
            if (uploadNotificationConfigStr != null) {
                uploadNotificationConfig = getJsonFromString(uploadNotificationConfigStr, UploadNotificationConfig.class);
            }
            uploadNotificationConfig = getNotificationConfig(getBaseContext(), getBaseContext().getString(R.string.media_upload), uploadNotificationConfig);

            // set upload start time
            setStartTime(new Date().getTime());

            notificationId = intent.getExtras().getInt("notificationId");
            // create notification
            createNotification(this);
        }


        return super.onStartCommand(intent, flags, startId);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Subscribe
    public void onEvent(Object event) {
        if (event instanceof OnCompletedEventReceiver) {

            UploadNotificationStatusConfig statusConfig = uploadNotificationConfig.getCompleted();
            int notificationId = ((OnCompletedEventReceiver) event).getNotificationId();
            Object object = ((OnCompletedEventReceiver) event).getObject();

            UploadData uploadData = (UploadData) object;
            ArrayList<Media> mediaList = uploadData.getMediaList();

            if (mediaList.size() == 1) {
                // remove uploaded item
                uploadData.getMediaList().remove(0);

                AppEventBus.getInstance().post(new OnCompletedEventReceiver(uploadData, notificationId));

                mBuilder.setOngoing(false);
                mBuilder.setProgress(100, 100, false);
                mBuilder.setContentTitle(statusConfig.title);
                mBuilder.setSmallIcon(statusConfig.iconResourceID);
                mBuilder.setColor(statusConfig.iconColorResourceID);

                //mBuilder.addAction(null);
                //mBuilder.addAction(null);
                mBuilder.setContentText(statusConfig.message);
                mNotificationManager.notify(notificationId, mBuilder.build());

                // remove uploaded file
                removeUploadedFile(notificationId);
            } else if (mediaList.size() > 1) {
                // remove uploaded item
                uploadData.getMediaList().remove(0);

                AppEventBus.getInstance().post(new OnCompletedEventReceiver(uploadData, notificationId));

                mBuilder.setContentTitle(statusConfig.title);
                mBuilder.setSmallIcon(statusConfig.iconResourceID);
                mBuilder.setColor(statusConfig.iconColorResourceID);
                //mBuilder.addAction(null);
                // mBuilder.addAction(null);
                mBuilder.setContentText(statusConfig.message);
                mNotificationManager.notify(notificationId, mBuilder.build());

                int index = 0;
                if (!mediaList.isEmpty()) {
                    for (Media media : mediaList) {
                        if (index == 0)
                            FileUploadService.postFiles(paramsName, media.getUri(), uploadData, notificationId);
                        index += 1;
                    }
                }
                // remove uploaded file
                removeUploadedFile(notificationId);
            }
        } else if (event instanceof OnFailureEventReceiver) {

            final FileUploadService fileUploadService = new FileUploadService();
            UploadNotificationStatusConfig statusConfig = uploadNotificationConfig.getError();
            notificationId = ((OnFailureEventReceiver) event).getNotificationId();
            int maxRetries = ((OnFailureEventReceiver) event).getMaxRetries();
            Object object = ((OnFailureEventReceiver) event).getObject();

            final UploadData uploadData = (UploadData) object;
            final ArrayList<Media> mediaList = uploadData.getMediaList();

            if (mediaList.size() > 0 && maxRetries == 0) {

                String uploadItemStr = getStringFromJson(uploadData);
                Intent retryIntent = new Intent(getBaseContext(), UploadNotificationActionReceiver.class);
                retryIntent.putExtra("notificationId", notificationId);
                retryIntent.putExtra("uploadData", uploadItemStr);
                retryIntent.setAction(ACTION_RETRY);

                Intent clearIntent = new Intent(getBaseContext(), UploadNotificationActionReceiver.class);
                clearIntent.putExtra("notificationId", notificationId);
                clearIntent.putExtra("uploadData", uploadItemStr);
                clearIntent.setAction(ACTION_CLEAR);

                PendingIntent retryPendingIntent = PendingIntent.getBroadcast(getBaseContext(), 0, retryIntent, 0);
                PendingIntent clearPendingIntent = PendingIntent.getBroadcast(getBaseContext(), 0, clearIntent, 0);

                mBuilder.setContentTitle(statusConfig.title);
                mBuilder.setSmallIcon(statusConfig.iconResourceID);
                mBuilder.setColor(statusConfig.iconColorResourceID);
                if (statusConfig.clearOnAction) {
                    mBuilder.addAction(android.R.drawable.ic_menu_revert, getString(R.string.retry), retryPendingIntent);
                    mBuilder.addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.cancel), clearPendingIntent);
                }
                mBuilder.setOngoing(false);
                mBuilder.setProgress(0, 0, false);
                mBuilder.setContentText("Uploaded " + (uploadData.getUploadItemCount() - mediaList.size()) + " of " + uploadData.getUploadItemCount() + " Failed " + mediaList.size());
                mNotificationManager.notify(notificationId, mBuilder.build());

            } else if (mediaList.size() > 0 && maxRetries > 0) {

                maxRetries = maxRetries - 1;
                final int finalMaxRetries = maxRetries;

                mBuilder.setContentTitle(statusConfig.title);
                mBuilder.setSmallIcon(statusConfig.iconResourceID);
                mBuilder.setColor(statusConfig.iconColorResourceID);
                //mBuilder.addAction(null);
                //mBuilder.addAction(null);

                mBuilder.setOngoing(false);
                mBuilder.setProgress(0, 0, true);
                mBuilder.setContentText("Retries ..... " + maxRetries);
                mNotificationManager.notify(notificationId, mBuilder.build());

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fileUploadService.setMaxRetries(finalMaxRetries);
                        int index = 0;
                        for (Media media : mediaList) {
                            if (index == 0)
                                fileUploadService.postFiles(paramsName, media.getUri(), uploadData, notificationId);
                            index += 1;
                        }
                    }
                }, 3000);
            }
        } else if (event instanceof OnRetryEventReceiver) {
            final FileUploadService fileUploadService = new FileUploadService();
            UploadNotificationStatusConfig statusConfig = uploadNotificationConfig.getError();
            notificationId = ((OnRetryEventReceiver) event).getNotificationId();
            Object object = ((OnRetryEventReceiver) event).getObject();

            final UploadData uploadData = (UploadData) object;
            final ArrayList<Media> mediaList = uploadData.getMediaList();
            int index = 0;
            for (Media media : mediaList) {
                if (index == 0)
                    fileUploadService.postFiles(paramsName, media.getUri(), uploadData, notificationId);
                index += 1;
            }
            createNotification(this);
        }
    }

    public void createNotification(Context context) {
        UploadNotificationStatusConfig statusConfig = uploadNotificationConfig.getProgress();

        Intent resultIntent = new Intent();
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        mBuilder.setSmallIcon(statusConfig.iconResourceID);
        mBuilder.setColor(statusConfig.iconColorResourceID);
        mBuilder.setContentTitle(statusConfig.title)
                .setOngoing(true)
                .setContentText(statusConfig.message)
                .setContentIntent(resultPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        mBuilder.setSound(null);
        mBuilder.setVibrate(new long[]{0L});
        mBuilder.build().flags |= Notification.FLAG_ONGOING_EVENT;
        mBuilder.setWhen(System.currentTimeMillis());
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance);
            notificationChannel.setDescription("no sound");
            notificationChannel.setSound(null, null);
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }

        mBuilder.setProgress(100, 0, true);
        mNotificationManager.notify(notificationId, mBuilder.build());
    }


    private void removeUploadedFile(int notificationId) {
        String uploadFilesStr = null;
        UploadFiles uploadFiles = null;

        // get upload media files string to shared preference
        uploadFilesStr = SharedPrefsUtils.getString(getBaseContext(), ACCOUNT_PREFS, UPLOAD_FILES);

        if (uploadFilesStr != null && !uploadFilesStr.isEmpty()) {

            // convert  string  to upload item object file
            uploadFiles = getJsonFromString(uploadFilesStr, UploadFiles.class);
            ArrayList<UploadData> uploadDataList = new ArrayList<>();

            for (UploadData uploadData : uploadFiles.getUploadDataList()) {
                if (uploadData.getUploadId() == notificationId && uploadData.getMediaList().size() > 0) {
                    uploadData.getMediaList().remove(0);
                }
                uploadDataList.add(uploadData);
            }

            uploadFiles.setUploadDataList(uploadDataList);
            // convert  upload item object file to string
            uploadFilesStr = getStringFromJson(uploadFiles);
            // save upload media files string to shared preference
            SharedPrefsUtils.set(getBaseContext(), ACCOUNT_PREFS, UPLOAD_FILES, uploadFilesStr);
        }
    }
}