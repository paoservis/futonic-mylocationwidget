package com.futonredemption.mylocation;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Bundle;
import android.widget.RemoteViews;

public class AppWidgetProvider1x1 extends AppWidgetProvider
{
	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		
		WidgetUpdateService.InitialWidgetAppearance(context);
	}
	
	public static final int ConvertToLayoutId1x1(final int layoutstate_id)
	{
		switch(layoutstate_id)
		{
			case Constants.WIDGETLAYOUTSTATE_NotAvailable: { return R.layout.appwidget_1x1_notavailable; }
			case Constants.WIDGETLAYOUTSTATE_Loading: { return R.layout.appwidget_1x1_loading; }
			case Constants.WIDGETLAYOUTSTATE_Default: { return R.layout.appwidget_1x1_default; }
		}
		
		return R.layout.appwidget_2x1_notavailable;
	}
	
	public static void UpdateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, final Bundle location)
	{
		RemoteViews views = null;

		final CharSequence lines[] = { Constants.BLANK, Constants.BLANK };
		final int layout_id = ConvertToLayoutId1x1(location.getInt(Constants.PARAM_WidgetLayoutState));
		
		if(location.containsKey(Constants.PARAM_Line1))
		{
			lines[0] = location.getCharSequence(Constants.PARAM_Line1);
		}
		if(location.containsKey(Constants.PARAM_Line2))
		{
			lines[1] = location.getCharSequence(Constants.PARAM_Line2);
		}
		
		views = new RemoteViews(context.getPackageName(), layout_id);

		WidgetUpdater.AttachPendingServiceIntent(context, views, location, R.id.btnAction, Constants.PARAM_IntentAction);
		WidgetUpdater.AttachPendingActivityIntent(context, views, location, R.id.btnShare, Constants.PARAM_IntentShareLocation);
		
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}
}
