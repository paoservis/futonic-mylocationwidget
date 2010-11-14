package com.futonredemption.mylocation.services;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.futonredemption.mylocation.Constants;
import com.futonredemption.mylocation.CustomMessage;
import com.futonredemption.mylocation.ILocationWidgetInfo;
import com.futonredemption.mylocation.MyLocation;
import com.futonredemption.mylocation.NoLocationAvailable;
import com.futonredemption.mylocation.Notifications;
import com.futonredemption.mylocation.R;
import com.futonredemption.mylocation.WidgetUpdater;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

public class WidgetUpdateService extends Service {
	public static final String TAG = "WidgetUpdateService";

	private final WidgetLocationListener _listener = new WidgetLocationListener();
	private LocationManager _lm = null;
	private Timer _watchdog = null;

	private TimerTask _watchexec = new TimerTask() {
		@Override
		public void run() {
			cancelNotification();
			endService(null);
		}
	};

	private void cancelNotification() {
		Notifications.cancelCustomMessage(this);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Thread.currentThread().setName(TAG);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		boolean lockacquired = false;

		final String action = intent.getStringExtra(Constants.ACTION);
		if (action.compareToIgnoreCase(Constants.ACTION_Refresh) == 0) {
			if (_lm == null) {
				_lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
				lockacquired = true;
			}

			if (lockacquired) {
				_watchdog = new Timer();
				_watchdog.schedule(_watchexec, Constants.INTERVAL_Timeout);
				showBeginFindingLocationNotification();
				getLocationInformation();
			}
		} else if (action.compareToIgnoreCase(Constants.ACTION_Cancel) == 0) {
			endService(null);
		} else {
			throw new RuntimeException("Clunched");
		}
	}

	private void showBeginFindingLocationNotification() {
		final CharSequence title = getText(R.string.finding_location);
		final CharSequence description = getText(R.string.tap_to_cancel);
		Notifications.customMessage(this, title, description, title);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void endService(MyLocation location) {
		boolean allow_refresh = false;

		synchronized (this) {
			if (_watchdog != null) {
				_watchdog.purge();
				_watchdog.cancel();
				_watchdog = null;
				allow_refresh = true;
			}

			if (_listener != null && _lm != null) {
				_lm.removeUpdates(_listener);
			}
		}

		// Sometimes multiple location updates come through. Only accept the
		// first one which can be determined by the killing of the watchdog.
		if (allow_refresh) {
			if (location == null) {
				WidgetUpdater.updateAll(this, new NoLocationAvailable(this));
			} else {
				final CharSequence title = this.getText(R.string.finding_location);
				final CharSequence description = this.getText(R.string.determining_address);
				final ILocationWidgetInfo widgetInfo = CustomMessage.create(this, title, description);
				WidgetUpdater.updateAll(this, widgetInfo);

				ResolveAddress(location);
				WidgetUpdater.updateAll(this, location);
			}
		}

		_lm = null;
		Notifications.cancelCustomMessage(this);
		this.stopSelf();
	}

	private void getLocationInformation() {
		// Attempt to first get GPS.
		// TODO: use location monitor.
		String provider = null;
		if (_lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			provider = LocationManager.GPS_PROVIDER;
		}
		if (provider == null) {
			final Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
			criteria.setCostAllowed(false);
			provider = _lm.getBestProvider(criteria, true);
		}

		if (provider != null) {
			final CharSequence title = this.getText(R.string.finding_location);
			final CharSequence description = String.format(Locale.ENGLISH, Constants.TEXT_UsingProviderPleaseWait,
					provider);
			ILocationWidgetInfo widgetInfo = CustomMessage.create(this, title, description);
			WidgetUpdater.updateAll(this, widgetInfo);
			beginAcquireLocation(provider);
		} else {
			onLocationAcquired(null);
		}
	}

	public class WidgetLocationListener implements LocationListener {
		private boolean _acquired_location = false;

		// private final WidgetUpdateService _parent;
		public WidgetLocationListener() {
		}

		public boolean hasLocation() {
			return _acquired_location;
		}

		public void onLocationChanged(Location location) {
			final MyLocation myLocation = new MyLocation(WidgetUpdateService.this, location);
			_acquired_location = true;
			WidgetUpdateService.this.onLocationAcquired(myLocation);
		}

		public void onProviderDisabled(String provider) {
			WidgetUpdateService.this.onLocationAcquired(null);
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

	public void ResolveAddress(MyLocation location) {
		Geocoder coder = new Geocoder(this);
		final double latitude = location.getLatitude();
		final double longitude = location.getLongitude();

		try {
			final List<Address> addresses = coder.getFromLocation(latitude, longitude, 1);
			if (addresses.size() > 0) {
				final Address address = addresses.get(0);
				location.attachAddress(address);
			}
		} catch (Exception e) {
		}
	}

	public void onLocationAcquired(final MyLocation location) {
		endService(location);
	}

	private void beginAcquireLocation(final String provider) {
		_lm.requestLocationUpdates(provider, Constants.INTERVAL_OneSecond, Constants.DISTANCE_ReallyReallyFarAway,
				_listener);
	}
}
