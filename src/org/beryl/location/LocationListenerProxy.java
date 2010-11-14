package org.beryl.location;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/** A proxy for {@link android.location.LocationListener} so that a parent listener can listen to multiple sources. */
class LocationListenerProxy implements LocationListener
{
	private final String _provider;
	private final LocationListener _listener;
	private final LocationManager _lm;
	
	public LocationListenerProxy(final LocationManager lm, final String provider, final LocationListener listener, final long min_time, final float min_distance)
	{
		_provider = provider;
		_listener = listener;
		
		_lm = lm;
		_lm.requestLocationUpdates(_provider, min_time, min_distance, this);
	}
	
	public void dispose()
	{
		_lm.removeUpdates(this);
	}

	public void onLocationChanged(Location location)
	{
		_listener.onLocationChanged(location);
	}

	public void onProviderDisabled(String provider)
	{
		_listener.onProviderDisabled(provider);
	}

	public void onProviderEnabled(String provider)
	{
		_listener.onProviderEnabled(provider);
	}

	public void onStatusChanged(String provider, int status, Bundle extras)
	{
		_listener.onStatusChanged(provider, status, extras);
	}
}
