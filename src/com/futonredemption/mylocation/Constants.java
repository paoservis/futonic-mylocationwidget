package com.futonredemption.mylocation;

import android.app.PendingIntent;

public class Constants
{
	public static final String PARAM_Altitude = "PARAM_Altitude";
	public static final String PARAM_Latitude = "PARAM_Latitude";
	public static final String PARAM_Longitude = "PARAM_Longitude";
	public static final String PARAM_Bearing = "PARAM_Bearing";
	public static final String PARAM_Speed = "PARAM_Speed";
	public static final String PARAM_Address = "PARAM_Address";
	public static final String PARAM_Line1 = "PARAM_Line1";
	public static final String PARAM_Line2 = "PARAM_Line2";
	public static final String PARAM_IntentViewLocation = "PARAM_IntentViewLocation";
	public static final String PARAM_IntentShareLocation = "PARAM_IntentShareLocation";
	public static final String PARAM_IntentAction = "PARAM_IntentAction";
	public static final String PARAM_WidgetLayoutState = "PARAM_WidgetLayoutState";
	
	public static final int DISTANCE_ReallyReallyFarAway = 100000;
	
	public static final int INTERVAL_Timeout = 60000;
	public static final int INTERVAL_OneSecond = 1000;
	public static final int INTERVAL_NotificationLed = 1000;
	
	public static final String PARAM_State = "PARAM_State";
	public static final int STATE_HaveLocation = 0;
	public static final int STATE_UpdatingLocation = 1;
	public static final int STATE_NoLocation = 2;
	
	public static final String BLANK = "";
	public static final String SPACE = " ";
	
	public static final String URL_GmapsBase = "http://maps.google.com/maps?q=%f,+%f+(%s)&iwloc=A&hl=en&z=13";
	
	public static final String FORMAT_Latitude = "Lat: %f";
	public static final String FORMAT_Longitude = "Long: %f";
	public static final String TEXT_YouAreHere = "You+are+here";
	public static final String TEXT_IAmHere = "I+am+here";
	public static final String TEXT_UsingProviderPleaseWait = "Finding with %s...";
	
	public static final String MIME_Email = "text/plain";
	
	public static final String ACTION = "action";
	public static final String ACTION_Refresh = "refresh";
	public static final String ACTION_Cancel = "cancel";
	
	public static final int NOTIFICATION_LocationUpdated = 1;
	public static final int NOTIFICATION_CustomMessage = 2;
	public static final int NOTIFICATION_ShareLocation = 3;
	
	public static final int WIDGETLAYOUTSTATE_NotAvailable = 1;
	public static final int WIDGETLAYOUTSTATE_Loading = 2;
	public static final int WIDGETLAYOUTSTATE_Default = 3;
	
	public static final int PENDINGINTENT_FLAG = PendingIntent.FLAG_UPDATE_CURRENT;
	
	public static final String NEWLINE = "\n";
}
