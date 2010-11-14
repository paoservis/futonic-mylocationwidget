package com.futonredemption.mylocation;

import android.content.Intent;

public interface ILocationWidgetInfo {
	CharSequence getTitle();
	CharSequence getDescription();
	Intent getActionIntent();
	Intent getShareIntent();
	Intent getViewIntent();
	int getWidgetState();
}
