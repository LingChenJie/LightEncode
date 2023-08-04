package com.light.encode.util;

import android.util.Log;

import java.util.Locale;

public class L {

    private static final String TAG = "LightEncode";
    public static int LEVEL = Log.VERBOSE;

    public static void setDebuggable(boolean isDebuggable) {
        LEVEL = isDebuggable ? Log.VERBOSE : Log.INFO;
    }

    public static void v(String TAG, String msg) {
        if (LEVEL <= Log.VERBOSE) {
            logWithLink(Log.VERBOSE, TAG, msg);
        }
    }

    public static void d(String TAG, String msg) {
        if (LEVEL <= Log.DEBUG) {
            logWithLink(Log.DEBUG, TAG, msg);
        }
    }

    public static void i(String TAG, String msg) {
        if (LEVEL <= Log.INFO) {
            logWithLink(Log.INFO, TAG, msg);
        }
    }

    public static void w(String TAG, String msg) {
        if (LEVEL <= Log.WARN) {
            logWithLink(Log.WARN, TAG, msg);
        }
    }

    public static void e(String TAG, String msg) {
        if (LEVEL <= Log.ERROR) {
            logWithLink(Log.ERROR, TAG, msg);
        }
    }

    public static void v(String msg) {
        if (LEVEL <= Log.VERBOSE) {
            logWithLink(Log.VERBOSE, TAG, msg);
        }
    }

    public static void d(String msg) {
        if (LEVEL <= Log.DEBUG) {
            logWithLink(Log.DEBUG, TAG, msg);
        }
    }

    public static void i(String msg) {
        if (LEVEL <= Log.INFO) {
            logWithLink(Log.INFO, TAG, msg);
        }
    }

    public static void w(String msg) {
        if (LEVEL <= Log.WARN) {
            logWithLink(Log.WARN, TAG, msg);
        }
    }

    public static void e(String msg) {
        if (LEVEL <= Log.ERROR) {
            logWithLink(Log.ERROR, TAG, msg);
        }
    }

    //Log输出的最大长度
    private static final int LOG_MAX_LENGTH = 2000;//2000

    /**
     * 循环打印log，解决msg过长不打印问题
     */
    @SuppressWarnings("DanglingJavadoc")
    private static void LogWrapperLoop(int logPriority, String tag, String msg) {
        int msgLength = msg.length();
        int index = 0;//输出字符的位置
        while (index < msgLength) {
            if (index + LOG_MAX_LENGTH > msgLength) {
                LogWrapper(logPriority, tag, msg.substring(index, msgLength));
            } else {
                LogWrapper(logPriority, tag, msg.substring(index, index + LOG_MAX_LENGTH));
            }
            index += LOG_MAX_LENGTH;
        }
    }

    private static void LogWrapper(int logPriority, String tag, String msg) {
        switch (logPriority) {
            case Log.VERBOSE:
                Log.v(tag, msg);
                break;
            case Log.DEBUG:
                Log.d(tag, msg);
                break;
            case Log.INFO:
                Log.i(tag, msg);
                break;
            case Log.WARN:
                Log.w(tag, msg);
                break;
            case Log.ERROR:
                Log.e(TAG, msg);
                break;
            default:
                break;
        }
    }

    /**
     * 带有链接的log打印
     */
    private static void logWithLink(int logPriority, String TAG, String msg) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int index = 4;
        String className = stackTrace[index].getFileName();
        String methodName = stackTrace[index].getMethodName();
        int lineNumber = stackTrace[index].getLineNumber();
        methodName = methodName.substring(0, 1).toUpperCase(Locale.getDefault()) + methodName.substring(1);
        String logStr = "[ (" + className + ")#" + methodName + " ] " + msg;
        LogWrapperLoop(logPriority, TAG, logStr);
    }


}
