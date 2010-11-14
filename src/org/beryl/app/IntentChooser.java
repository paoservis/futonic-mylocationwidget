package org.beryl.app;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.View;

/**
 * Activity that appears as a dialog. Allows a user to pick an intent.
 * 
 * @author jeremyje
 *
 */
public class IntentChooser extends Activity implements OnDismissListener {
	private static final String EXTRA_TITLE = "chooserTitle";
	private static final String EXTRA_INTENTLIST = "choosables";
	private static final String EXTRA_ICON = "icon";
	
	private static final int NO_ICON_RESID = 0;


	public static Intent createChooserIntent(final Context context, final CharSequence title, final ArrayList<ChoosableIntent> intents) throws IllegalArgumentException {
		return createChooserIntent(context, title, NO_ICON_RESID, intents);
	}
	
	public static Intent createChooserIntent(final Context context, final CharSequence title, final int iconResId, final ArrayList<ChoosableIntent> intents) throws IllegalArgumentException {
		int i;
		ChoosableIntent test;
		final int len = intents.size();
		for(i = 0; i < len; i++) {
			test = intents.get(i);
			if(test == null || ! test.isValid()){
				throw new IllegalArgumentException("Choosable Intent list has NULL values.");
			}
		}
		
		final Intent intentChooserLauncher = new Intent(context, IntentChooser.class);
		intentChooserLauncher.putExtra(EXTRA_TITLE, title);
		intentChooserLauncher.putExtra(EXTRA_ICON, iconResId);
		intentChooserLauncher.putParcelableArrayListExtra(EXTRA_INTENTLIST, intents);
		return intentChooserLauncher;
	}
	
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new View(this));
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		try {
			final ArrayList<ChoosableIntent> choosables = getChoosables();
			final CharSequence title = getChooserTitle();
			final int iconResId = getChooserIconResId();
			showChooser(title, iconResId, choosables);
		} catch(IllegalArgumentException e) {
			
			finish();
		} finally {
			
		}
	}

	private ArrayList<ChoosableIntent> getChoosables() throws IllegalArgumentException {
		final Intent intent = getIntent();
		ArrayList<ChoosableIntent> choosables = null;
		
		if(intent != null) {
			choosables = intent.getParcelableArrayListExtra(EXTRA_INTENTLIST);
		}
		
		if(choosables == null) {
			throw new IllegalArgumentException("Choosable Intent list was NULL.");
		}
		
		return choosables;
	}
	
	private CharSequence getChooserTitle() throws IllegalArgumentException {
		final Intent intent = getIntent();
		CharSequence title = null;
		
		if(intent != null) {
			title = intent.getCharSequenceExtra(EXTRA_TITLE);
		}
		
		if(title == null) {
			throw new IllegalArgumentException("Chooser Title was NULL.");
		}
		
		return title;
	}

	private int getChooserIconResId() {
		final Intent intent = getIntent();
		int iconResId = NO_ICON_RESID;
		
		if(intent != null) {
			iconResId = intent.getIntExtra(EXTRA_ICON, NO_ICON_RESID);
		}
		return iconResId;
	}
	
	private void showChooser(final CharSequence title, final int iconResId, final ArrayList<ChoosableIntent> choosables) {
		
		final CharSequence[] labels = new CharSequence[choosables.size()];
		int i;
		final int numChoosables = choosables.size();
		for(i = 0; i < numChoosables; i++) {
			labels[i] = choosables.get(i).label;
		}
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		
		if(iconResId != NO_ICON_RESID) {
			builder.setIcon(iconResId);
		}
		builder.setItems(labels, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	IntentChooser.this.runChoosableIntent(choosables.get(item));
		    }
		});
		
		final AlertDialog alert = builder.create();
		alert.show();
		alert.setOnDismissListener(this);
	}
	
	protected void runChoosableIntent(final ChoosableIntent choosableIntent) {
		if(choosableIntent != null) {
			startActivity(choosableIntent.intent);
		}
    	finish();
	}

	public void onDismiss(DialogInterface dialog) {
		finish();
	}
}
