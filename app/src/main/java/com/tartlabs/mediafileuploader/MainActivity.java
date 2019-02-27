package com.tartlabs.mediafileuploader;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.tartlabs.mediafileupload.config.UploadNotificationConfig;
import com.tartlabs.mediafileupload.model.Media;
import com.tartlabs.mediafileupload.model.UploadData;
import com.tartlabs.mediafileupload.service.FileUploadService;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static com.tartlabs.mediafileupload.util.AppUtils.getPath;
import static com.tartlabs.mediafileupload.util.AppUtils.isInternetAvailable;
import static com.tartlabs.mediafileupload.util.AppUtils.isNetworkAvailable;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private static final int RC_PERM_STORAGE = 900;
    private static final int SELECT_MULTIPLE_MEDIA_FILE = 1;
    String BASE_URL = "http://192.168.1.30:8000/api/image/upload/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPermissions();
            }
        });

    }

    private void galleryMultipleMediaChoose() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"});
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Multiple Picture"), SELECT_MULTIPLE_MEDIA_FILE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ArrayList<Media> mediaList = new ArrayList<>();
        UploadData uploadData = new UploadData();
        int uploadFileCount = 0;
        if (isNetworkAvailable(this) && isInternetAvailable()) {
            if (requestCode == SELECT_MULTIPLE_MEDIA_FILE && resultCode == RESULT_OK) {
                // Get the Image from data
                if (data.getData() != null) {
                    Uri selectedMediaUri = data.getData();
                    Media media = new Media();
                    media.setUri(getPath(this, selectedMediaUri));
                    if (selectedMediaUri.toString().contains("image")) {
                        //handle image
                        media.setType("image");
                    } else if (selectedMediaUri.toString().contains("video")) {
                        //handle video
                        media.setType("video");
                    }


                    mediaList.add(media);
                    int uploadId = (int) System.currentTimeMillis();
                    // set media list into object
                    uploadData.setUploadId(uploadId);
                    uploadData.setMediaList(mediaList);

                    // request upload
                    requestUpload(this, "image[]", mediaList.get(0).getUri(), uploadData, uploadId);
                } else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();

                        for (int i = 0; i < mClipData.getItemCount(); i++) {
                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri selectedMediaUri = item.getUri();
                            Media media = new Media();
                            media.setUri(getPath(this, selectedMediaUri));
                            if (selectedMediaUri.toString().contains("image")) {
                                //handle image
                                media.setType("image");
                            } else if (selectedMediaUri.toString().contains("video")) {
                                //handle video
                                media.setType("video");
                            }
                            mediaList.add(media);
                        }
                        int uploadId = (int) System.currentTimeMillis();
                        // set media list into object
                        uploadData.setUploadId(uploadId);
                        uploadData.setMediaList(mediaList);

                        // request upload
                        requestUpload(this, "image[]", mediaList.get(0).getUri(), uploadData, uploadId);
                    }
                }
            }
        } else {
            showToast("Check internet connection");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d("", "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d("", "onPermissionsDenied:" + requestCode + ":" + perms.size());
        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }


    @AfterPermissionGranted(RC_PERM_STORAGE)
    public void getPermissions() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {
            galleryMultipleMediaChoose();
        } else {
            // Request one permission
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_storage_camera), RC_PERM_STORAGE, perms);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //stopService(new Intent(this, FileUploaderBackgroundService.class));
    }


    private void requestUpload(Context context, String paramsName, String pathName, final UploadData uploadData, final int uploadId) {

        uploadData
                // set base upload url
                .setUrl(BASE_URL)
                // register status receiver
                .setStatusReceiver(registerStatusReceiver())
                // set header
                .setHeader("Content-Type", "multipart/form-data")
                .setHeader("Authorization", "Bearer" + " token")
                // set params
                .setParams("id", paramsName);

        // service call
        new FileUploadService()
                // set max retries
                .setMaxRetries(0)
                // set syc
                .setSyc(this, false)
                // set notification config
                .setNotificationConfig(getNotificationConfig(this, R.string.app_name))
                // create service call
                .serviceCall(context, paramsName, pathName, uploadData, uploadId);
    }


    public UploadNotificationConfig getNotificationConfig(Context context, @StringRes int title) {

//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, Main2Activity.class), 0);
        UploadNotificationConfig config = new UploadNotificationConfig();

        config.getProgress().title = "Progress";
        config.getProgress().message = context.getString(com.tartlabs.mediafileupload.R.string.in_progress);
        config.getProgress().iconResourceID = com.tartlabs.mediafileupload.R.drawable.ic_stat_notification;
        config.getProgress().iconColorResourceID = Color.BLUE;
        //config.getProgress().clickIntent = pendingIntent;
        //config.getProgress().clearOnAction = true;

        config.getCompleted().title = "Completed";
        config.getCompleted().message = context.getString(com.tartlabs.mediafileupload.R.string.upload_success);
        config.getCompleted().iconResourceID = com.tartlabs.mediafileupload.R.drawable.ic_stat_notification;
        config.getCompleted().iconColorResourceID = Color.GREEN;
        // config.getCompleted().clearOnAction = true;

        config.getError().title = "Error";
        config.getError().message = context.getString(com.tartlabs.mediafileupload.R.string.upload_error);
        config.getError().iconResourceID = com.tartlabs.mediafileupload.R.drawable.ic_stat_notification;
        config.getError().iconColorResourceID = Color.RED;
        config.getError().clearOnAction = false;
        //config.getError().clickIntent = PendingIntent.getActivity(this, 0, new Intent(this, Main2Activity.class), 0);


        config.getCancelled().title = "Cancelled";
        config.getCancelled().message = context.getString(com.tartlabs.mediafileupload.R.string.upload_cancelled);
        config.getCancelled().iconResourceID = com.tartlabs.mediafileupload.R.drawable.ic_stat_notification;
        config.getCancelled().iconColorResourceID = Color.YELLOW;
        // config.getCancelled().clearOnAction = true;

        return config;
    }

    private BroadcastReceiver registerStatusReceiver() {
        IntentFilter intentFilter = new IntentFilter("android.intent.action.MAIN");

        BroadcastReceiver receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent
                int uploadId = intent.getIntExtra("uploadId", 0);
                String status = intent.getStringExtra("status");
                //show our message value
                showToast(" uploadId " + uploadId);
                showToast(" status " + status);
            }
        };
        //registering our receiver
        this.registerReceiver(receiver, intentFilter);
        return receiver;
    }
}