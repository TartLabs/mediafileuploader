package com.tartlabs.mediafileupload.network;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public interface FileUploadApi {

    @Multipart
    @POST("/")
    Call<ResponseBody> uploadMediaFiles(@HeaderMap Map<String, String> headers, @PartMap() Map<String, Object> partMap, @Part MultipartBody.Part file);
}
