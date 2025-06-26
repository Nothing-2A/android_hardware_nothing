package com.nothing.kafka;

import android.content.Context;
import android.os.Handler;

public class EventObserver {
    protected Context mContext;

    public EventObserver(Context context, Handler handler, String tag) {
        mContext = context;
    }

    public void register(EventCallback callback, boolean flag) {
        // Stub
    }

    public void unregister() {
        // Stub
    }

    public interface EventCallback {
        void updateEvent(String event);
    }
}
