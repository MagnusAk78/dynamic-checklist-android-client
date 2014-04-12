package com.ma.dc;

import com.ma.dc.util.LogHelper;
import com.ma.dc.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    private final static String KEY_CLOUDANT_NAME = "pref_key_cloudant_name";
    private final static String KEY_DATABASE_NAME = "pref_key_database_name";
    private final static String KEY_CHECKPOINT_SORT_ORDER = "pref_key_checkpoint_sort_order";

    private static CHECKPOINT_SORT_ORDER_TYPE currentSortOrder = CHECKPOINT_SORT_ORDER_TYPE.FIXED;

    public enum CHECKPOINT_SORT_ORDER_TYPE {
        FIXED, DYMANIC;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_CHECKPOINT_SORT_ORDER))
            switch (Integer.parseInt(sharedPreferences.getString(key, "1"))) {
            case 1:
                LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onSharedPreferenceChanged",
                        "CHECKPOINT_SORT_ORDER_TYPE.FIXED");
                currentSortOrder = CHECKPOINT_SORT_ORDER_TYPE.FIXED;
                break;
            case 2:
                LogHelper.logDebug(this, Common.LOG_TAG_MAIN, "onSharedPreferenceChanged",
                        "CHECKPOINT_SORT_ORDER_TYPE.DYMANIC");
                currentSortOrder = CHECKPOINT_SORT_ORDER_TYPE.DYMANIC;
                break;
            }
    }

    public static String getCloudantName(final Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(KEY_CLOUDANT_NAME, "");
    }

    public static String getDatabaseName(final Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(KEY_DATABASE_NAME, "");
    }

    public static CHECKPOINT_SORT_ORDER_TYPE getCheckpointSortOrder() {
        return currentSortOrder;
    }
}
