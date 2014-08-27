package com.inspiredo.inspiredo;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list.
 */
public class SettingsActivity extends PreferenceActivity implements AsyncJSON.JSONParser {



    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();

    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {


        addPreferencesFromResource(R.xml.pref_general);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        bindPreferenceSummaryToValue(findPreference("api_source"));

    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {

        private AsyncJSON mPendingJSON;

        @Override
        public boolean onPreferenceChange(final Preference preference, Object value) {
            final String stringValue = value.toString();


            AsyncJSON.JSONParser p = new AsyncJSON.JSONParser() {
                @Override
                public void parseJSON(JSONObject json) {
                    boolean connect;
                    if (json == null)
                        connect = false;
                    else {
                        Log.d("JSON", json.toString());
                        String status;
                        try {

                            connect = json.getString("status").equals("ok");
                        } catch (JSONException e) {
                            connect = false;
                        }

                    }

                    if (connect) {
                        Toast.makeText(preference.getContext(),
                                "Connected", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(preference.getContext(),
                                "No Connection", Toast.LENGTH_LONG).show();

                    }

                    mPendingJSON = null;

                }
            };

            if (preference.getKey().equals("api_source") && stringValue.length()> 0) {

                BasicNameValuePair[] params = {new BasicNameValuePair("connect", "test")};

                if (mPendingJSON != null)
                    mPendingJSON.cancel(true);
                mPendingJSON = new AsyncJSON(stringValue, AsyncJSON.METHOD_GET,
                        p, params);
                mPendingJSON.execute();
            }


            preference.setSummary(stringValue);

            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        String summary = PreferenceManager
                .getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(), "");
        preference.setSummary(summary);
    }

    @Override
    public void parseJSON(JSONObject json) {

    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("api_source"));
        }
    }


}
