package com.nothing.onlineconfig;

import android.content.Context;

import org.json.JSONArray;

public class ConfigGrabber {
    private final String mProjectName;

    public ConfigGrabber(Context context, String projectName) {
        this.mProjectName = projectName;
        // No real resolver needed in stub
    }

    public JSONArray grabConfig() {
        // Return empty config so observer doesn't crash
        return new JSONArray();
    }

    String getProjectName() {
        return this.mProjectName;
    }
}
