package com.ma.dc;

import android.content.Context;
import android.util.Log;

//Used for common static constants and features.
public final class Common {

    // Strings
    public final static String HTTP_STRING = "http://";
    public final static String CLOUDANT_END = ".cloudant.com";

    public final static String CHECKPOINTS_VIEW_END = "/_design/dc_server_app/_view/checkpoints";
    public final static String MEASUREMENTS_VIEW_END = "/_design/dc_server_app/_view/measurements";

    public final static String CHECKPOINT_TYPE_STRING = "checkpoint";
    public final static String MEASUREMENT_TYPE_STRING = "measurement";

    // Limits
    public final static int MAX_SIZE_OF_CATEGORY_STRING = 20;
    public final static int START_BUFFER_SIZE_URL_READ = 4000;

    // Logging
    public final static String LOG_TAG_MAIN = "DC-MAIN";
    public final static String LOG_TAG_DB = "DC-DB";
    public final static String LOG_TAG_NETWORK = "DC-NETWORK";
    public final static String LOG_TAG_CONTENT_PROVIDER = "DC-CONTENT_PROVIDER";
    
    public final static int LOG_LEVEL = Log.WARN;
    
    //Ugly singleton way but quick
    public static Context listActivity = null;
}
