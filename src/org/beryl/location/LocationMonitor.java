package org.beryl.location;

import java.util.ArrayList;
import java.util.List;

import org.beryl.app.ContextClonable;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Monitors multiple location providers at the same time and routes the readings to all listeners.
 * This type is ContextClonable but listeners that are not ContextClonable will need to be reattached.
 * @author jeremyje
 *
 */
public class LocationMonitor implements LocationListener, ContextClonable
{
	private final LocationManager _locationManager;
	private final Context _context;
	private final ArrayList<LocationMonitorController> _controllers = new ArrayList<LocationMonitorController>();
	private final ArrayList<LocationListener> _listeners = new ArrayList<LocationListener>();
	private final ArrayList<LocationListenerProxy> _proxies = new ArrayList<LocationListenerProxy>();

	public LocationMonitor(final Context context)
	{
		_context = context;
		_locationManager = (LocationManager)_context.getSystemService(Context.LOCATION_SERVICE);
	}
	
	public LocationMonitor(final Context context, final LocationMonitor cloneFrom)
	{
		_context = context;
		_locationManager = (LocationManager)_context.getSystemService(Context.LOCATION_SERVICE);
	}
	
	public Object clone(final Context context) {
		return new LocationMonitor(context, this);
	}
	
	public void beginGpsListening(final long minTime, final float minDistance) {
		beginListening(LocationManager.GPS_PROVIDER, minTime, minDistance);
	}
	
	public void beginNetworkListening(final long minTime, final float minDistance) {
		beginListening(LocationManager.NETWORK_PROVIDER, minTime, minDistance);
	}
	
	/** Begins listening to a specific location provider.
	 * If the provider is already running it will be restarted to use the new parameters. */
	public void beginListening(final String provider, final long minTime, final float minDistance) {
		final LocationListenerProxy existingProxy = getProxy(provider);

		if(existingProxy != null) {
			stopProxy(existingProxy);
		}

		startProxy(provider, minTime, minDistance);
	}

	/** Stops all location providers that are being listened to. */
	public void stopListening() {
		final int numProxies = _proxies.size();
		LocationListenerProxy proxy;
		
		for(int i = 0; i < numProxies; i++) {
			proxy = _proxies.get(i);
			proxy.dispose();
		}
		
		_proxies.clear();
	}
	
	/** Stops a specific location provider. */
	public void stopListening(final String provider) {
		final LocationListenerProxy proxy = getProxy(provider);
		if(proxy != null) {
			stopProxy(proxy);
		}
	}
	
	/** Attempts to get the most accurate last known location of the device. Based on time and accuracy. */
	public Location getBestStaleLocation() {
		Location location = null;
		
		final List<String> providers = _locationManager.getAllProviders();
		
		for(String provider : providers) {
			final Location testLocation = _locationManager.getLastKnownLocation(provider);
			
			if(testLocation != null) {
				if(location == null) {
					location = testLocation;
				}
				// Accuracy vs Time Ratio: New - Old > 0
				else if(testLocation.getTime() / testLocation.getAccuracy() - location.getAccuracy() / location.getTime() > 0) {
					location = testLocation;
				}
			}
		}
		
		return location;
	}
	
	public boolean isGpsEnabled() {
		return _locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
	
	public boolean isNetworkEnabled() {
		return _locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}
	
	public boolean isProviderEnabled(final String provider) {
		return _locationManager.isProviderEnabled(provider);
	}
	
	public boolean isNetworkOrGpsEnabled() {
		return isNetworkEnabled() || isGpsEnabled();
	}
	
	public List<String> getEnabledProviders() {
		return _locationManager.getProviders(true);
	}
	
	public boolean isAnyProviderEnabled() {
		return getEnabledProviders().size() > 0;
	}

	public String getBestEnabledProvider() {
		if(isGpsEnabled())
			return LocationManager.GPS_PROVIDER;
		else if(isNetworkEnabled()) {
			return LocationManager.NETWORK_PROVIDER;
		}
		else {
			final Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
			criteria.setCostAllowed(false);
			return _locationManager.getBestProvider(criteria, true);
		}
	}
	
	private void startProxy(final String provider, final long minTime, final float minDistance) {
		final LocationListenerProxy proxy = new LocationListenerProxy(_locationManager, provider, this, minTime, minDistance);
		_proxies.add(proxy);
	}
	
	private void stopProxy(LocationListenerProxy proxy) {
		proxy.dispose();
		_proxies.remove(proxy);
	}
	
	public boolean isListening() {
		return _proxies.size() > 0;
	}
	
	public boolean isListening(final String provider) {
		return getProxy(provider) != null;
	}
	
	private LocationListenerProxy getProxy(String provider) {
		final int numProxies = _proxies.size();
		LocationListenerProxy proxy;
		for(int i = 0; i < numProxies; i++) {
			
			proxy = _proxies.get(i);
			if(proxy.getProvider().equals(provider)) {
				return proxy;
			}
		}
		
		return null;
	}
	
	public void addListener(final LocationListener listener) {
		if(listener instanceof LocationMonitorController)
			throw new IllegalArgumentException("LocationMonitorController was passed to addListener() use addController() instead.");
		
		if(! _listeners.contains(listener)) {
			_listeners.add(listener);
		}
	}
	
	void addController(final LocationMonitorController controller) {
		if(! _controllers.contains(controller)) {
			_controllers.add(controller);
		}
	}
	
	public void removeController(final LocationMonitorController controller) {
		_controllers.remove(controller);
	}
	
	public void removeListener(final LocationListener listener) {
		_listeners.remove(listener);
	}

	public void onLocationChanged(final Location location) {
		final int numControllers = _controllers.size();
		final int numListeners = _listeners.size();
		int i;
		
		for(i = 0; i < numControllers; i++) {
			_controllers.get(i).onLocationChanged(location);
		}
		for(i = 0; i < numListeners; i++) {
			_listeners.get(i).onLocationChanged(location);
		}
	}

	public void onProviderDisabled(final String provider) {
		final int numControllers = _controllers.size();
		final int numListeners = _listeners.size();
		int i;
		
		for(i = 0; i < numControllers; i++) {
			_controllers.get(i).onProviderDisabled(provider);
		}
		for(i = 0; i < numListeners; i++) {
			_listeners.get(i).onProviderDisabled(provider);
		}
	}

	public void onProviderEnabled(String provider) {
		final int numControllers = _controllers.size();
		final int numListeners = _listeners.size();
		int i;
		
		for(i = 0; i < numControllers; i++) {
			_controllers.get(i).onProviderEnabled(provider);
		}
		for(i = 0; i < numListeners; i++) {
			_listeners.get(i).onProviderEnabled(provider);
		}
	}

	public void onStatusChanged(final String provider, final int status, final Bundle extras) {
		final int numControllers = _controllers.size();
		final int numListeners = _listeners.size();
		int i;
		
		for(i = 0; i < numControllers; i++) {
			_controllers.get(i).onStatusChanged(provider, status, extras);
		}
		for(i = 0; i < numListeners; i++) {
			_listeners.get(i).onStatusChanged(provider, status, extras);
		}
	}
	
	protected ArrayList<String> getListeningProviders() {
		final ArrayList<String> providers = new ArrayList<String>();
		final int numProxies = _proxies.size();
		providers.ensureCapacity(numProxies);
		
		for(int i = 0; i < numProxies; i++) {
			providers.add(_proxies.get(i).getProvider());
		}
		
		return providers;
	}
}
