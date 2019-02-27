package com.tartlabs.mediafileupload.model;

import android.content.BroadcastReceiver;

import java.util.ArrayList;
import java.util.HashMap;

public class UploadData {
    private String url;
    private HashMap<String, String> headerHashMap = new HashMap<String, String>();
    private HashMap<String, Object> paramsHashMap = new HashMap<String, Object>();
    private int uploadId;
    private int uploadItemCount;
    private ArrayList<Media> mediaList;
    private BroadcastReceiver statusReceiver;

    public String getUrl() {
        return url;
    }

    public UploadData setUrl(String url) {
        this.url = url;
        return this;
    }

    public HashMap<String, String> getHeaderHashMap() {
        return headerHashMap;
    }

    public UploadData setHeader(String key, String value) {
        this.headerHashMap.put(key, value);
        return this;
    }

    public HashMap<String, Object> getParamsHashMap() {
        return paramsHashMap;
    }

    public UploadData setParams(String key, Object value) {
        this.paramsHashMap.put(key, value);
        return this;
    }

    public int getUploadId() {
        return uploadId;
    }

    public UploadData setUploadId(int uploadId) {
        this.uploadId = uploadId;
        return this;
    }

    public int getUploadItemCount() {
        return uploadItemCount;
    }

    public UploadData setUploadItemCount(int uploadItemCount) {
        this.uploadItemCount = uploadItemCount;
        return this;
    }

    public ArrayList<Media> getMediaList() {
        return mediaList;
    }

    public UploadData setMediaList(ArrayList<Media> mediaList) {
        this.mediaList = mediaList;
        return this;
    }

    public BroadcastReceiver getStatusReceiver() {
        return statusReceiver;
    }

    public UploadData setStatusReceiver(BroadcastReceiver statusReceiver) {
        this.statusReceiver = statusReceiver;
        return this;
    }
}