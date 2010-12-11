package org.beryl.location;

import java.util.ArrayList;

import android.location.LocationListener;

public abstract class LocationMonitorController implements LocationListener {

	protected LocationMonitor Monitor;
	
	public LocationMonitorController(LocationMonitor monitor) {
		setMonitor(monitor);
	}
	
	void setMonitor(LocationMonitor monitor) {
		Monitor = monitor;
	}
	
	protected ArrayList<String> getListeningProviders() {
		return Monitor.getListeningProviders();
	}
}
