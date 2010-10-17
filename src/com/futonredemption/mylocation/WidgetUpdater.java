package com.futonredemption.mylocation;

import java.util.Locale;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.RemoteViews;

public class WidgetUpdater
{
	public static void UpdateAllWidgets_CustomMessage(final Context context, final CharSequence line1, final CharSequence line2)
	{
		final Bundle result = new Bundle();
		result.putInt(Constants.PARAM_State, Constants.STATE_UpdatingLocation);

		result.putCharSequence(Constants.PARAM_Line1, line1);
		result.putCharSequence(Constants.PARAM_Line2, line2);
		
		result.putParcelable(Constants.PARAM_IntentAction, Intents.actionCancel(context));
		result.putInt(Constants.PARAM_WidgetLayoutState, Constants.WIDGETLAYOUTSTATE_Loading);
		
		WidgetUpdater.UpdateAllWidgets(context, result);
	}
	
	public static void UpdateAllWidgets(final Context context, final Bundle location)
	{
		int i;
		final AppWidgetManager widget_manager = AppWidgetManager.getInstance(context);
		final int [] ids4x1 = widget_manager.getAppWidgetIds(new ComponentName(context, AppWidgetProvider4x1.class));
		
		final int len4x1 = ids4x1.length;
		for(i = 0; i < len4x1; i++)
		{
			AppWidgetProvider4x1.UpdateWidget(context, widget_manager, ids4x1[i], location);
		}
		
		final int [] ids2x1 = widget_manager.getAppWidgetIds(new ComponentName(context, AppWidgetProvider2x1.class));
		
		final int len2x1 = ids2x1.length;
		for(i = 0; i < len2x1; i++)
		{
			AppWidgetProvider2x1.UpdateWidget(context, widget_manager, ids2x1[i], location);
		}
		
		final int [] ids1x1 = widget_manager.getAppWidgetIds(new ComponentName(context, AppWidgetProvider1x1.class));
		
		final int len1x1 = ids1x1.length;
		for(i = 0; i < len1x1; i++)
		{
			AppWidgetProvider1x1.UpdateWidget(context, widget_manager, ids1x1[i], location);
		}
		
		if(len2x1 > 0 || len1x1 > 0)
		{
			PostViewMapNotification(context, location);
		}
		else
		{
			CancelViewMapNotification(context);
		}
	}

	public static void AttachPendingServiceIntent(final Context context, final RemoteViews views, final Bundle bundle, int id, String param_name)
	{
		if(bundle.containsKey(param_name))
		{
			final Intent intent = bundle.getParcelable(param_name);
			final PendingIntent pIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			views.setOnClickPendingIntent(id, pIntent);
		}
	}
	
	public static void AttachPendingActivityIntent(final Context context, final RemoteViews views, final Bundle bundle, int id, String param_name)
	{
		if(bundle.containsKey(param_name))
		{
			final Intent intent = bundle.getParcelable(param_name);
			final PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			views.setOnClickPendingIntent(id, pIntent);
		}
	}
	
	public static void CancelViewMapNotification(final Context context)
	{
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(Constants.NOTIFICATION_LocationUpdated);
	}
	
	public static void PostViewMapNotification(final Context context, final Bundle location)
	{
		if(location.containsKey(Constants.PARAM_IntentViewLocation))
		{
			final Intent intent = location.getParcelable(Constants.PARAM_IntentViewLocation);
			final PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			Notification notifier = null;
			notifier = new Notification();
			notifier.flags = Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;
			notifier.icon = R.drawable.icon;
			notifier.ledOffMS = Constants.INTERVAL_NotificationLed;
			notifier.ledOnMS = Constants.INTERVAL_NotificationLed;
			notifier.ledARGB = 0xff0000ff;
			
			final CharSequence lines[] = { Constants.BLANK, Constants.BLANK };
			
			if(location.containsKey(Constants.PARAM_Line1))
			{
				lines[0] = location.getCharSequence(Constants.PARAM_Line1);
			}
			if(location.containsKey(Constants.PARAM_Line2))
			{
				lines[1] = location.getCharSequence(Constants.PARAM_Line2);
			}
			notifier.tickerText = lines[0] + " " + lines[1];
			notifier.setLatestEventInfo(context, lines[0], lines[1], pIntent);
			nm.notify(Constants.NOTIFICATION_LocationUpdated, notifier);
		}
		else
		{
			CancelViewMapNotification(context);
		}
	}
	
	public static void AttachAddress(final Context context, final Bundle bundle, Address address)
	{
		String lines[] = { context.getText(R.string.unknown_location).toString(), Constants.BLANK };
		int len = 0;
		int i;
		if(address != null)
		{
			len = address.getMaxAddressLineIndex();
			for(i = 0; i < len && i < 2; i++)
			{
				lines[i] = address.getAddressLine(i);
			}
			
			if(i == 1)
			{
				lines[1] = address.getFeatureName();
			}
		}
		
		final double latitude = bundle.getDouble(Constants.PARAM_Latitude);
		final double longitude = bundle.getDouble(Constants.PARAM_Longitude);
		
		final String uriaddress = lines[0] + Constants.SPACE + lines[1];
		final String gmaps_url = String.format(Locale.ENGLISH, Constants.URL_GmapsBase, latitude, longitude, Uri.encode(uriaddress));
		final String gmaps_share_url = String.format(Locale.ENGLISH, Constants.URL_GmapsBase, latitude, longitude, Uri.encode(uriaddress));
		final String address_data = lines[0] + " " + lines[1];
		
		Intent sendMapLink = new Intent(Intent.ACTION_SEND);
		sendMapLink.setType(Constants.MIME_Email);
		sendMapLink.putExtra(Intent.EXTRA_EMAIL, new String[] {Constants.BLANK});
		sendMapLink.putExtra(Intent.EXTRA_SUBJECT, context.getText(R.string.my_location));
		sendMapLink.putExtra(Intent.EXTRA_TEXT, address_data + " " + gmaps_share_url);
		
		final Intent openMapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(gmaps_url));
		
		bundle.putParcelable(Constants.PARAM_IntentViewLocation, openMapIntent);
		bundle.putParcelable(Constants.PARAM_IntentShareLocation, Intent.createChooser(sendMapLink, context.getText(R.string.share_location)));
		
		bundle.putCharSequence(Constants.PARAM_Line1, lines[0]);
		bundle.putCharSequence(Constants.PARAM_Line2, lines[1]);
	}
	
	public static Bundle GetAcquiredLocationStateBundle(Context context, double latitude, double longitude)
	{
		final Bundle result = new Bundle();
		result.putInt(Constants.PARAM_State, Constants.STATE_HaveLocation);
		
		result.putDouble(Constants.PARAM_Latitude, latitude);
		result.putDouble(Constants.PARAM_Longitude, longitude);
		
		final String gmaps_url = String.format(Locale.ENGLISH, Constants.URL_GmapsBase, latitude, longitude, Constants.TEXT_YouAreHere);
		final String gmaps_share_url = String.format(Locale.ENGLISH, Constants.URL_GmapsBase, latitude, longitude, Constants.TEXT_IAmHere);
		
		Intent sendMapLink = new Intent(Intent.ACTION_SEND);
		sendMapLink.setType(Constants.MIME_Email);
		sendMapLink.putExtra(Intent.EXTRA_EMAIL, new String[] {Constants.BLANK});
		sendMapLink.putExtra(Intent.EXTRA_SUBJECT, context.getText(R.string.my_location));
		sendMapLink.putExtra(Intent.EXTRA_TEXT, gmaps_share_url);
		
		final Intent openMapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(gmaps_url));
		
		result.putParcelable(Constants.PARAM_IntentViewLocation, openMapIntent);
		result.putParcelable(Constants.PARAM_IntentShareLocation, Intent.createChooser(sendMapLink, context.getText(R.string.share_location)));

		result.putCharSequence(Constants.PARAM_Line1, String.format(Locale.ENGLISH, Constants.FORMAT_Latitude, latitude));
		result.putCharSequence(Constants.PARAM_Line2, String.format(Locale.ENGLISH, Constants.FORMAT_Longitude, longitude));
		
		result.putParcelable(Constants.PARAM_IntentAction, Intents.actionRefresh(context));
		result.putInt(Constants.PARAM_WidgetLayoutState, Constants.WIDGETLAYOUTSTATE_Default);
		
		return result;
	}
	
	public static Bundle GetLocationNotFoundBundle(final Context context)
	{
		final Bundle result = new Bundle();
		result.putInt(Constants.PARAM_State, Constants.STATE_NoLocation);
		result.putCharSequence(Constants.PARAM_Line1, context.getText(R.string.location_not_found));
		result.putCharSequence(Constants.PARAM_Line2, context.getText(R.string.tap_here_to_fix_that));
		final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		result.putParcelable(Constants.PARAM_IntentViewLocation, intent);
		
		result.putParcelable(Constants.PARAM_IntentAction, Intents.actionRefresh(context));
		result.putInt(Constants.PARAM_WidgetLayoutState, Constants.WIDGETLAYOUTSTATE_NotAvailable);
		
		return result;
	}
	
	public static Bundle GetInitialAppearanceBundle(Context context)
	{
		final Bundle result = new Bundle();
		result.putInt(Constants.PARAM_State, Constants.STATE_NoLocation);
		result.putCharSequence(Constants.PARAM_Line1, context.getText(R.string.my_location));
		result.putCharSequence(Constants.PARAM_Line2, context.getText(R.string.tap_to_get_location));
		
		result.putParcelable(Constants.PARAM_IntentAction, Intents.actionRefresh(context));
		result.putInt(Constants.PARAM_WidgetLayoutState, Constants.WIDGETLAYOUTSTATE_NotAvailable);
		
		return result;
	}
}
