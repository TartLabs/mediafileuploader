package com.tartlabs.mediafileupload.service;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.tartlabs.mediafileupload.backgroudService.FileUploaderBackgroundService;
import com.tartlabs.mediafileupload.config.UploadNotificationConfig;
import com.tartlabs.mediafileupload.eventBus.AppEventBus;
import com.tartlabs.mediafileupload.eventBus.OnCompletedEventReceiver;
import com.tartlabs.mediafileupload.eventBus.OnFailureEventReceiver;
import com.tartlabs.mediafileupload.model.UploadData;
import com.tartlabs.mediafileupload.model.UploadFiles;
import com.tartlabs.mediafileupload.network.FileUploadApi;
import com.tartlabs.mediafileupload.util.GsonWrapper;
import com.tartlabs.mediafileupload.util.SharedPrefsUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.tartlabs.mediafileupload.constant.Constants.ACCOUNT_PREFS;
import static com.tartlabs.mediafileupload.constant.Constants.FAILURE;
import static com.tartlabs.mediafileupload.constant.Constants.SUCCESS;
import static com.tartlabs.mediafileupload.constant.Constants.SYNC;
import static com.tartlabs.mediafileupload.constant.Constants.UPLOAD_FILES;
import static com.tartlabs.mediafileupload.util.AppUtils.prepareFilePart;
import static com.tartlabs.mediafileupload.util.ConversionUtils.getJsonFromString;
import static com.tartlabs.mediafileupload.util.ConversionUtils.getStringFromJson;

public class FileUploadService {
    public static String paramsName = "";
    public static String path = "";
    private UploadNotificationConfig uploadNotificationConfig;
    static int maxRetries = 0;
    private static Context context;

    public static void postFiles(String _paramsName, String _path, final UploadData uploadData, final int notificationId) {

        List<MultipartBody.Part> list = new ArrayList<>();
        MultipartBody.Part file = null;

        //very important image[]
        Uri fileUri = Uri.parse(_path);
        file = prepareFilePart(_paramsName, fileUri);

        // initialize retrofit
        Retrofit retrofit = getApiClient(uploadData.getUrl());

        FileUploadApi fileUploadApi = retrofit.create(FileUploadApi.class);
        Call<ResponseBody> call = fileUploadApi.uploadMediaFiles(uploadData.getHeaderHashMap(), uploadData.getParamsHashMap(), file);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // send broadcast
                Intent intent = new Intent("android.intent.action.MAIN")
                        .putExtra("uploadId", notificationId)
                        .putExtra("status", SUCCESS);
                context.sendBroadcast(intent);
                AppEventBus.getInstance().post(new OnCompletedEventReceiver(uploadData, notificationId));
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                // send broadcast
                Intent intent = new Intent("android.intent.action.MAIN")
                        .putExtra("uploadId", notificationId)
                        .putExtra("status", FAILURE);
                context.sendBroadcast(intent);

                //context.registerReceiver(uploadData.getStatusReceiver(), new IntentFilter());
                AppEventBus.getInstance().post(new OnFailureEventReceiver(uploadData, notificationId, maxRetries));
            }
        });
    }


    private static HttpLoggingInterceptor getLoggingInterceptor() {
        return new HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY);
    }

    public FileUploadService setNotificationConfig(UploadNotificationConfig _uploadNotificationConfig) {
        uploadNotificationConfig = _uploadNotificationConfig;
        return this;
    }

    public FileUploadService setSyc(Context context, boolean syc) {
        SharedPrefsUtils.set(context, ACCOUNT_PREFS, SYNC, syc);
        return this;
    }

    public FileUploadService setMaxRetries(int _maxRetries) {
        maxRetries = _maxRetries;
        return this;
    }

    public void serviceCall(Context _context, String _paramsName, String _pathName,
                            final UploadData uploadData, final int uploadId)
            throws IllegalArgumentException {
        context = _context;
        String serverUrl = uploadData.getUrl();
        if (context == null)
            throw new IllegalArgumentException("Context MUST not be null!");

        if (serverUrl == null || "".equals(serverUrl)) {
            throw new IllegalArgumentException("Server URL cannot be null or empty");
        }


        // check valid url
        if (!uploadData.getUrl().startsWith("http://")
                && !uploadData.getUrl().startsWith("https://")) {
            throw new IllegalArgumentException("Specify either http:// or https:// as protocol");
        }

        paramsName = _paramsName;
        path = _pathName;

        String uploadFilesStr = null;
        UploadFiles uploadFiles = null;

        // get upload media files string to shared preference
        uploadFilesStr = SharedPrefsUtils.getString(context, ACCOUNT_PREFS, UPLOAD_FILES);
        if (uploadFilesStr != null) {
            // convert  string  to upload item object file
            uploadFiles = getJsonFromString(uploadFilesStr, UploadFiles.class);
        }

        if (uploadFiles == null) {
            // set uploaded item count
            uploadData.setUploadItemCount(uploadData.getMediaList().size());

            uploadFiles = new UploadFiles();
            //  add uploaded item
            ArrayList<UploadData> uploadDataList = new ArrayList<>();
            uploadDataList.add(uploadData);
            uploadFiles.setUploadDataList(uploadDataList);
            // convert  upload item object file to string
            uploadFilesStr = getStringFromJson(uploadFiles);
            // save upload media files string to shared preference
            SharedPrefsUtils.set(context, ACCOUNT_PREFS, UPLOAD_FILES, uploadFilesStr);
        } else {
            // set uploaded item count
            uploadData.setUploadItemCount(uploadData.getMediaList().size());

            ArrayList<UploadData> uploadDataList = uploadFiles.getUploadDataList();
            uploadDataList.add(uploadData);
            uploadFiles.setUploadDataList(uploadDataList);
            // convert  upload item object file to string
            uploadFilesStr = getStringFromJson(uploadFiles);
            // save upload media files string to shared preference
            SharedPrefsUtils.set(context, ACCOUNT_PREFS, UPLOAD_FILES, uploadFilesStr);
        }
        // start service
        Intent intentService = new Intent(context, FileUploaderBackgroundService.class);
        intentService.putExtra("notificationId", uploadId);
        if (uploadNotificationConfig != null) {
            String uploadNotificationConfigStr = getStringFromJson(uploadNotificationConfig);
            intentService.putExtra("uploadNotificationConfig", uploadNotificationConfigStr);
        }
        context.startService(intentService);
        FileUploadService.postFiles(paramsName, path, uploadData, uploadId);
    }


    private static Retrofit getApiClient(String _baseUploadUrl) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        // add logger
        builder.addInterceptor(getLoggingInterceptor());
        // build client
        OkHttpClient okHttpClient = builder
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(28, TimeUnit.SECONDS)
                .addInterceptor(new ApiInterceptor())
                .build();
        Gson gson = GsonWrapper.newInstance();

        return new Retrofit.Builder()
                .baseUrl(_baseUploadUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory
                        .createWithScheduler(Schedulers.io()))
                .client(okHttpClient)
                .build();
    }


    public static class ApiInterceptor implements Interceptor {
        @Override
        public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();


            // Adding token as Authorization header for every request
            request = request.newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer ")
                    .build();

            return chain.proceed(request);
        }
    }
}
