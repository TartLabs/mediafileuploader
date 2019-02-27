package com.tartlabs.mediafileupload.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.tartlabs.mediafileupload.backgroudService.FileUploaderBackgroundService;
import com.tartlabs.mediafileupload.model.UploadData;
import com.tartlabs.mediafileupload.model.UploadFiles;
import com.tartlabs.mediafileupload.service.FileUploadService;
import com.tartlabs.mediafileupload.util.SharedPrefsUtils;

import static com.tartlabs.mediafileupload.constant.Constants.ACCOUNT_PREFS;
import static com.tartlabs.mediafileupload.constant.Constants.SYNC;
import static com.tartlabs.mediafileupload.constant.Constants.UPLOAD_FILES;
import static com.tartlabs.mediafileupload.util.AppUtils.isInternetAvailable;
import static com.tartlabs.mediafileupload.util.AppUtils.isNetworkAvailable;
import static com.tartlabs.mediafileupload.util.ConversionUtils.getJsonFromString;

public class ConnectionReceiver extends BroadcastReceiver {

    private Context context;

    public ConnectionReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        showToast("Network Available " + isNetworkAvailable(context));
        showToast("Internet Available " + isInternetAvailable());
        if (isNetworkAvailable(context) && isInternetAvailable()) {
            boolean syc = SharedPrefsUtils.getBoolean(context, ACCOUNT_PREFS, SYNC, false);
            if (syc) {
                String uploadFilesStr = null;
                UploadFiles uploadFiles = null;

                // get upload media files string to shared preference
                uploadFilesStr = SharedPrefsUtils.getString(context, ACCOUNT_PREFS, UPLOAD_FILES);

                if (uploadFilesStr != null && !uploadFilesStr.isEmpty()) {

                    // convert  string  to upload item object file
                    uploadFiles = getJsonFromString(uploadFilesStr, UploadFiles.class);

                    for (UploadData uploadData : uploadFiles.getUploadDataList()) {
                        if (uploadData.getMediaList() != null && uploadData.getMediaList().size() > 0) {   // get notification id
                            int notificationId = uploadData.getUploadId();
                            // start service
                            Intent intentService = new Intent(context, FileUploaderBackgroundService.class);
                            intentService.putExtra("notificationId", notificationId);
                            context.startService(intentService);
                            FileUploadService.postFiles("image[]", uploadData.getMediaList().get(0).getUri(), uploadData, notificationId);
                        }
                    }
                }
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}