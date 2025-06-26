package com.nothing.onlineconfig;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;

import org.json.JSONArray;

public class ConfigObserver extends ContentObserver {
    private final ConfigGrabber mConfigGrabber;
    private final ConfigUpdater mConfigUpdater;

    public interface ConfigUpdater {
        void updateConfig(JSONArray jSONArray);
    }

    public ConfigObserver(Context context, Handler handler, ConfigUpdater configUpdater, String projectName) {
        super(handler);
        this.mConfigGrabber = new ConfigGrabber(context, projectName);
        this.mConfigUpdater = configUpdater;
    }

    public void register() {
        // No-op
    }

    public void unregister() {
        // No-op
    }

    @Override
    public void onChange(boolean selfChange) {
        mConfigUpdater.updateConfig(mConfigGrabber.grabConfig());
    }
}
