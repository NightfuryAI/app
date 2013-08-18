package com.brainydroid.daydreaming.ui.FirstLaunchSequence;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.brainydroid.daydreaming.R;
import com.brainydroid.daydreaming.background.Logger;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Activity at first launch
 * Checking if App Settings are correctly set (connectivity, location)
 *
 * In first launch sequence of apps
 *
 * Previous activity :  FirstLaunch04PersonalityQuestionnaireActivity
 * This activity     :  FirstLaunch05MeasuresActivity2
 * Next activity     :  FirstLaunch06PullActivity
 *
 */
@ContentView(R.layout.activity_first_launch_measures)
public class FirstLaunch05MeasuresActivity extends FirstLaunchActivity {

    private static String TAG = "FirstLaunch05MeasuresActivity";

    @InjectView(R.id.firstLaunchMeasures_textNetworkLocation) TextView textNetworkLocation;
    @InjectView(R.id.firstLaunchMeasures_textSettings) TextView textSettings;
    @InjectView(R.id.firstLaunchMeasures_buttonSettings) Button buttonSettings;
    @InjectView(R.id.firstLaunchMeasures_buttonNext) Button buttonNext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Logger.v(TAG, "Creating");
        super.onCreate(savedInstanceState);

        if (statusManager.isNetworkLocEnabled()) {
            Logger.i(TAG, "Network location is enabled -> jumping to " +
                    "dashboard");
            launchDashboard();
        } else {
            Logger.v(TAG, "Network location not enabled");
        }
    }

    @Override
    public void onStart() {
        Logger.v(TAG, "Starting");
        super.onStart();
        updateView();
    }

    @Override
    public void onResume() {
        Logger.v(TAG, "Resuming");
        super.onResume();
        updateView();
    }

    private void updateView() {
        Logger.d(TAG, "Updating view of settings");

        textNetworkLocation.setCompoundDrawablesWithIntrinsicBounds(
                statusManager.isNetworkLocEnabled() ? R.drawable.ic_check : R.drawable.ic_cross, 0, 0, 0);

        updateRequestAdjustSettings();
    }

    private void updateRequestAdjustSettings() {
        if ((statusManager.isNetworkLocEnabled()) || (Build.FINGERPRINT.startsWith("generic"))) {
            Logger.i(TAG, "Settings are good");
            setAdjustSettingsOff();
        } else {
            Logger.i(TAG, "Settings are bad");
            setAdjustSettingsNecessary();
        }
    }

    @TargetApi(11)
    private void setAdjustSettingsNecessary() {
        Logger.i(TAG, "Disabling button to move on");
        textSettings.setText(R.string.firstLaunchMeasures_text_settings_necessary);
        textSettings.setVisibility(View.VISIBLE);
        buttonSettings.setVisibility(View.VISIBLE);
        buttonSettings.setClickable(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            buttonNext.setAlpha(0.3f);
        } else {
            buttonNext.setVisibility(View.INVISIBLE);
        }

        buttonNext.setClickable(false);
    }

    @TargetApi(11)
    private void setAdjustSettingsOff() {
        Logger.i(TAG, "Allowing button to move on");
        textSettings.setVisibility(View.INVISIBLE);
        buttonSettings.setVisibility(View.INVISIBLE);
        buttonSettings.setClickable(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            buttonNext.setAlpha(1f);
        } else {
            buttonNext.setVisibility(View.VISIBLE);
        }

        buttonNext.setClickable(true);
    }

    public void onClick_buttonSettings(@SuppressWarnings("UnusedParameters") View view) {
        Logger.v(TAG, "Settings button clicked");
        launchSettings();
    }

    private void launchSettings() {
        Logger.d(TAG, "Launching settings");
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        settingsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(settingsIntent);
    }

    public void onClick_buttonNext(@SuppressWarnings("UnusedParameters") View view) {
        Logger.v(TAG, "Next button clicked");
        launchDashboard();
    }

    private void launchDashboard() {
        Logger.d(TAG, "Launching settings");
        finishFirstLaunch(); // when everything is ok, first launch is set to completed
        Intent dashboardIntent = new Intent(this, FirstLaunch06PullActivity.class);
        startActivity(dashboardIntent);
        Logger.d(TAG, "Finishing this activity");
        finish();
    }

}
