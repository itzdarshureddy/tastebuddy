package com.example;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.activity.NearbyPlacesActivity;
import com.example.constants.Constants;

public class PreLoginActivity extends Activity{
	private SharedPreferences mPrefs;
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPrefs = getSharedPreferences(Constants.PREFERENCE_FILENAME, MODE_PRIVATE);
		String authtoken = mPrefs.getString("access_token", null);
		if(authtoken == null){
			Intent loginPageActivity = new Intent(getApplicationContext(), LoginActivity.class);
			startActivity(loginPageActivity);
		}
		else {
			Intent homePageActivity = new Intent(getApplicationContext(), NearbyPlacesActivity.class);
			startActivity(homePageActivity);
			//Toast.makeText(getBaseContext(), "Hi there!"+authtoken, Toast.LENGTH_LONG).show();
		}
	}

}
