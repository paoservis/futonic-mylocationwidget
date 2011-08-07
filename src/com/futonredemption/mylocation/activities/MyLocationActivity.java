package com.futonredemption.mylocation.activities;

import android.os.Bundle;

import com.futonredemption.mylocation.R;
import com.google.android.maps.MapActivity;

public class MyLocationActivity extends MapActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mylocation);
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

}
