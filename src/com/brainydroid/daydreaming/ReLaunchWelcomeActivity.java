package com.brainydroid.daydreaming;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class ReLaunchWelcomeActivity extends Activity {

	SharedPreferences mFLPrefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mFLPrefs = getSharedPreferences(getString(R.pref.firstLaunchPrefs), MODE_PRIVATE);

		setContentView(R.layout.activity_re_launch_welcome);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_re_launch_welcome, menu);
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		checkFirstRun();
	}

	public void onClick_start(View view) {
		Intent intent = new Intent(this, FirstLaunchDescriptionActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	}

	private void checkFirstRun() {
		if (mFLPrefs.getBoolean(getString(R.pref.firstLaunchCompleted), false)) {
			finish();
		}
	}
}
