package com.ma.dc.util;

import com.ma.dc.Common;

import android.util.Log;

public abstract class LogHelper {
    
    private final static String BEFORE_CLASS = "[";
    private final static String AFTER_CLASS = "], method: (";
    private final static String AFTER_METHOD_EMPTY = ")";
    private final static String AFTER_METHOD_MESSAGE = "), message: ";
    
    public static void logDebug(final Object callingClass, final String tag, final String method) {
        if (Common.LOG_LEVEL >= Log.DEBUG) {
            final String callingClassName = callingClass.getClass().getName();
            StringBuilder sb = new StringBuilder(callingClassName.length() + method.length() + BEFORE_CLASS.length() + AFTER_CLASS.length() + AFTER_METHOD_EMPTY.length());
            sb.append(BEFORE_CLASS);
            sb.append(callingClassName);
            sb.append(AFTER_CLASS);
            sb.append(method);
            sb.append(AFTER_METHOD_EMPTY);
            Log.d(tag, sb.toString());
        }
    }

    public static void logDebug(final Object callingClass, final String tag, final String method, final String message) {
        if (Common.LOG_LEVEL >= Log.DEBUG) {
            final String callingClassName = callingClass.getClass().getName();
            StringBuilder sb = new StringBuilder(callingClassName.length() + message.length() + method.length() + BEFORE_CLASS.length() + AFTER_CLASS.length() + AFTER_METHOD_MESSAGE.length());
            sb.append(BEFORE_CLASS);
            sb.append(callingClassName);
            sb.append(AFTER_CLASS);
            sb.append(method);
            sb.append(AFTER_METHOD_MESSAGE);
            sb.append(message);
            Log.d(tag, sb.toString());
        }
    }
    
    public static void logWarning(final Object callingClass, final String tag, final String method, final Throwable throwable) {
        if (Common.LOG_LEVEL >= Log.WARN) {
            final String callingClassName = callingClass.getClass().getName();
            StringBuilder sb = new StringBuilder(callingClassName.length() + method.length() + BEFORE_CLASS.length() + AFTER_CLASS.length() + AFTER_METHOD_EMPTY.length());
            sb.append(BEFORE_CLASS);
            sb.append(callingClassName);
            sb.append(AFTER_CLASS);
            sb.append(method);
            sb.append(AFTER_METHOD_EMPTY);
            Log.w(tag, sb.toString(), throwable);
        }
    }
}
