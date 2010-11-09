package com.futonredemption.mylocation;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

public class WidgetUpdateService extends Service
{
	public static final String TAG = "WidgetUpdateService";

	private final WidgetLocationListener _listener = new WidgetLocationListener(this);
	private LocationManager _lm = null;
	private Timer _watchdog = null;
	private NotificationManager _nm = null;
	private TimerTask _watchexec = new TimerTask()
	{
		@Override
		public void run()
		{
			cancelNotification();
			endService(null);
		}
	};
	
	private void cancelNotification()
	{
		if(_nm != null)
		{
			_nm.cancel(Constants.NOTIFICATION_FindingLocation);
		}
	}

	public void PostFindingLocationNotification(final CharSequence title, final CharSequence description, final CharSequence ticker_text)
	{
		Notification notifier = new Notification();
		notifier.flags = Notification.FLAG_ONGOING_EVENT;
		notifier.icon = R.drawable.stat_icon;
		if(ticker_text != null)
		{
			notifier.tickerText = ticker_text;
		}
		PendingIntent pintent = PendingIntent.getService(this, 0, Intents.actionCancel(this), PendingIntent.FLAG_UPDATE_CURRENT);
		notifier.setLatestEventInfo(this, title, description, pintent);
		_nm.notify(Constants.NOTIFICATION_FindingLocation, notifier);
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		Thread.currentThread().setName("WidgetUpdateService Service");
		
		if(_nm == null)
		{
			_nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		}
	}
	
	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
		boolean lockacquired = false;
		
		final String action = intent.getStringExtra(Constants.ACTION);
		if(action.compareToIgnoreCase(Constants.ACTION_Refresh) == 0)
		{
			if(_lm == null)
			{
				_lm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
				lockacquired = true;
			}
			
			if(lockacquired)
			{
				_watchdog = new Timer();
				_watchdog.schedule(_watchexec, Constants.INTERVAL_Timeout);
				PostFindingLocationNotification(getText(R.string.finding_location), getText(R.string.tap_to_cancel), getText(R.string.finding_location));
				GetLocationInformation();
			}
		}
		else if(action.compareToIgnoreCase(Constants.ACTION_Nothing) == 0)
		{
			final Bundle result = new Bundle();
			endService(result);
		}
		else if(action.compareToIgnoreCase(Constants.ACTION_Cancel) == 0)
		{
			endService(null);
		}
		else
		{
			throw new RuntimeException ("Clunched");
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	public void endService(Bundle location)
	{
		boolean allow_refresh = false;
		
		synchronized(this)
		{
			if(_watchdog != null)
			{
				_watchdog.purge();
				_watchdog.cancel();
				_watchdog = null;
				allow_refresh = true;
			}

			if(_listener != null && _lm != null)
			{
				_lm.removeUpdates(_listener);
			}
		}

		// Sometimes multiple location updates come through. Only accept the first one which can be determined by the killing of the watchdog.
		if(allow_refresh)
		{	
			if(location == null)
			{
				WidgetUpdater.UpdateAllWidgets(WidgetUpdateService.this, WidgetUpdater.GetLocationNotFoundBundle(this));
			}
			else
			{
				final CharSequence title = this.getText(R.string.finding_location);
				final CharSequence description = this.getText(R.string.determining_address);
				WidgetUpdater.UpdateAllWidgets_CustomMessage(this, title, description);
				ResolveAddress(location);
				WidgetUpdater.UpdateAllWidgets(this, location);
			}
		}
		
		_lm = null;
		synchronized(this)
		{
			if(_nm != null)
			{
				_nm.cancel(Constants.NOTIFICATION_FindingLocation);
			}
		}
		_nm = null;
		this.stopSelf();
	}

	public void GetLocationInformation()
	{
		// Attempt to first get GPS.
		
		String provider = null;
		if(_lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
		{
			provider = LocationManager.GPS_PROVIDER;
		}
		if(provider == null)
		{
			final Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
			criteria.setCostAllowed(false);
			provider = _lm.getBestProvider(criteria, true);
		}
		
		if(provider != null)
		{
			WidgetUpdater.UpdateAllWidgets_CustomMessage(this, this.getText(R.string.finding_location), String.format(Locale.ENGLISH, Constants.TEXT_UsingProviderPleaseWait, provider));
			BeginAcquireLocation(provider);
		}
		else
		{
			OnLocationAcquired(WidgetUpdater.GetLocationNotFoundBundle(this));
		}
	}
	
	public static class WidgetLocationListener implements LocationListener
	{
		private boolean _acquired_location = false;
		
		private final WidgetUpdateService _parent;
		public WidgetLocationListener(WidgetUpdateService parent)
		{
			_parent = parent;
		}
		
		public boolean hasLocation()
		{
			return _acquired_location;
		}
		
		public void onLocationChanged(Location location)
		{
			final Bundle bundle = WidgetUpdater.GetAcquiredLocationStateBundle(_parent, location.getLatitude(), location.getLongitude());
			
			if(location.hasAltitude())
			{
				bundle.putDouble(Constants.PARAM_Altitude, location.getAltitude());
			}
			
			if(location.hasBearing())
			{
				bundle.putFloat(Constants.PARAM_Bearing, location.getBearing());
			}
			
			if(location.hasSpeed())
			{
				bundle.putFloat(Constants.PARAM_Speed, location.getSpeed());
			}

			_acquired_location = true;
			_parent.OnLocationAcquired(bundle);
		}

		public void onProviderDisabled(String provider)
		{
			_parent.OnLocationAcquired(WidgetUpdater.GetLocationNotFoundBundle(_parent));
		}

		public void onProviderEnabled(String provider)
		{
		}

		public void onStatusChanged(String provider, int status, Bundle extras)
		{
		}
	}
	
	public void ResolveAddress(Bundle location)
	{
		Geocoder coder = new Geocoder(this);
		final double latitude = location.getDouble(Constants.PARAM_Latitude);
		final double longitude = location.getDouble(Constants.PARAM_Longitude);
		
		try
		{
			final List<Address> addresses = coder.getFromLocation(latitude, longitude, 1);
			if(addresses.size() > 0)
			{
				final Address address = addresses.get(0);
				location.putParcelable(Constants.PARAM_Address, address);
				WidgetUpdater.AttachAddress(this, location, address);
			}
		}
		catch (Exception e)
		{
		}
	}
	
	public void OnLocationAcquired(Bundle location)
	{
		endService(location);
	}
	
	private void BeginAcquireLocation(String provider)
	{
		_lm.requestLocationUpdates(provider, Constants.INTERVAL_OneSecond, Constants.DISTANCE_ReallyReallyFarAway, _listener);
	}
	
	public static void InitialWidgetAppearance(Context context)
	{
		WidgetUpdater.UpdateAllWidgets(context, WidgetUpdater.GetInitialAppearanceBundle(context));
	}
	
	public static void RefreshLocationToWidgets(Context context)
	{
		Intent intent = new Intent(context, WidgetUpdateService.class);
		intent.putExtra(Constants.ACTION, Constants.ACTION_Refresh);
		context.startService(intent);
	}
}
