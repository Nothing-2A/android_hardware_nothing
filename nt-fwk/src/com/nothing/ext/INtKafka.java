package com.nothing.ext;

import android.content.ContentResolver;

public interface INtKafka {
    public static final INtKafka DEFAULT = new INtKafka() {
    };

    default String getAuthority() {
        return "";
    }

    default void fire(ContentResolver resolver, String eventTag, String eventValue) {
    }
}
