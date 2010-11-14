package org.beryl.location;

import java.util.ArrayList;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

public class LocationMonitor implements LocationListener {
	private final LocationManager _lm;
	private final Context _context;
	private LocationListenerProxy _gps = null;
	private LocationListenerProxy _network = null;
	private boolean _stopListeningToNetworkOnFirstGpsResult = false;
	private int _numSatellites = 0;
	private final ArrayList<LocationListener> _listeners = new ArrayList<LocationListener>();
	private long _minTime;
	private float _minDistance;

	public LocationMonitor(final Context context) {
		_context = context;
		_lm = (LocationManager) _context
				.getSystemService(Context.LOCATION_SERVICE);
	}

	public void beginListening(final long min_time, final float min_distance) {
		stopListening();
		_minTime = min_time;
		_minDistance = min_distance;
		beginGpsListening(_minTime, _minDistance);
		beginNetworkListening(_minTime, _minDistance);
	}

	public void attachListener(LocationListener listener) {
		if (!_listeners.contains(listener)) {
			_listeners.add(listener);
		}
	}

	public void detachListener(LocationListener listener) {
		_listeners.remove(listener);
	}

	private void beginGpsListening(final long min_time, final float min_distance) {
		_gps = new LocationListenerProxy(_lm, LocationManager.GPS_PROVIDER,
				this, min_time, min_distance);
	}

	private void beginNetworkListening(final long min_time,
			final float min_distance) {
		_network = new LocationListenerProxy(_lm,
				LocationManager.NETWORK_PROVIDER, this, min_time, min_distance);
	}

	public void stopListeningToNetworkOnFirstGpsResult(boolean param) {
		_stopListeningToNetworkOnFirstGpsResult = param;
	}

	public int numSatellitesInLastFix() {
		return _numSatellites;
	}

	public boolean isListening() {
		return isGpsListening() || isNetworkListening();
	}

	public boolean isGpsListening() {
		return _gps != null;
	}

	public boolean isNetworkListening() {
		return _network != null;
	}

	public void dispose() {
		_listeners.clear();
		stopListening();
	}

	public void stopListening() {
		disposeGps();
		disposeNetwork();
	}

	private void disposeGps() {
		if (_gps != null) {
			_numSatellites = 0;
			_gps.dispose();
			_gps = null;
		}
	}

	private void disposeNetwork() {
		if (_network != null) {
			_network.dispose();
			_network = null;
		}
	}

	public boolean shouldStopListeningToNetworkOnFirstGpsResult() {
		return _stopListeningToNetworkOnFirstGpsResult;
	}

	public void onLocationChanged(Location location) {
		// Stop the network service if we aren't using it any more.
		if (isNetworkListening()
				&& shouldStopListeningToNetworkOnFirstGpsResult()
				&& location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
			disposeNetwork();
		}
		
		for (LocationListener listener : _listeners) {
			listener.onLocationChanged(location);
		}
	}

	public void onProviderDisabled(String provider) {
		for (LocationListener listener : _listeners) {
			listener.onProviderDisabled(provider);
		}
	}

	public void onProviderEnabled(String provider) {
		for (LocationListener listener : _listeners) {
			listener.onProviderEnabled(provider);
		}
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		boolean is_available = LocationProvider.AVAILABLE == status;

		if (is_available && provider.equals(LocationManager.GPS_PROVIDER)) {
			_numSatellites = extras.getInt("satellites");
		}

		// TODO This needs to be handled better.

		for (LocationListener listener : _listeners) {
			listener.onStatusChanged(provider, status, extras);
		}
	}
}
