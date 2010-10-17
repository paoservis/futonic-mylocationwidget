package com.futonredemption.mylocation;

import android.content.Context;
import android.content.Intent;

public class Intents
{
	public static final Intent actionRefresh(final Context context)
	{
		final Intent intent = new Intent(context, WidgetUpdateService.class);
		intent.putExtra(Constants.ACTION, Constants.ACTION_Refresh);
		return intent;
	}

	public static final Intent actionCancel(final Context context)
	{
		final Intent intent = new Intent(context, WidgetUpdateService.class);
		intent.putExtra(Constants.ACTION, Constants.ACTION_Cancel);
		return intent;
	}
	public static final Intent actionNothing(final Context context)
	{
		final Intent intent = new Intent(context, WidgetUpdateService.class);
		intent.putExtra(Constants.ACTION, Constants.ACTION_Nothing);
		return intent;
	}
}
