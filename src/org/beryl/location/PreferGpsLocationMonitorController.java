package org.beryl.location;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

/**
 * Configures a LocationMonitor to prefer GPS over all other methods of acquiring locations.
 * 
 * This provides the following features:
 * 1. When GPS is reporting locations, all other providers are disabled to conserve battery.
 * 2. When GPS is not available or not reporting locations. Other providers are wakened to
 *    compensate.
 * 
 * @author jeremyje
 *
 */
public class PreferGpsLocationMonitorController extends LocationMonitorController {

	private float _defaultRestartProviderMinDistance = Constants.DEFAULT_INTERVAL_DISTANCE;
	private long _defaultRestartProviderMinTime = Constants.DEFAULT_INTERVAL_TIME;
	
	private ArrayList<String> _providersIStopped = new ArrayList<String>();
	
	public PreferGpsLocationMonitorController(LocationMonitor monitor) {
		super(monitor);
	}

	public void startMonitor() {
		List<String> providers = Monitor.getEnabledProviders();
		
		for(String provider : providers) {
			Monitor.beginListening(provider, _defaultRestartProviderMinTime, _defaultRestartProviderMinDistance);
		}
	}
	
	public void setLocationTimeAndDistanceIntervals(long minTime, float minDistance) {
		_defaultRestartProviderMinTime = minTime;
		_defaultRestartProviderMinDistance = minDistance;
	}

	public void onLocationChanged(Location location) {
		if(location.equals(LocationManager.GPS_PROVIDER)) {
			disableNonGpsProviders();
		}
	}

	public void onProviderDisabled(String provider) {
		if(provider.equals(LocationManager.GPS_PROVIDER)) {
			enableNonGpsProviders();
		}
	}

	private void disableNonGpsProviders() {
		final ArrayList<String> activeProviders = getListeningProviders();
		final int numProviders = activeProviders.size();
		String provider;
		
		_providersIStopped.ensureCapacity(numProviders);

		for(int i = 0; i < numProviders; i++) {
			provider = activeProviders.get(i);
			Monitor.stopListening(provider);
			_providersIStopped.add(provider);
		}
	}

	private void enableNonGpsProviders() {
		final int numProviders = _providersIStopped.size();
		String provider;
		
		for(int i = 0; i < numProviders; i++) {
			provider = _providersIStopped.get(i);
			Monitor.beginListening(provider, _defaultRestartProviderMinTime, _defaultRestartProviderMinDistance);
		}
	}
	
	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		if((status == LocationProvider.OUT_OF_SERVICE || status == LocationProvider.TEMPORARILY_UNAVAILABLE) && provider.equals(LocationManager.GPS_PROVIDER)) {
			enableNonGpsProviders();
		}
	}
}
