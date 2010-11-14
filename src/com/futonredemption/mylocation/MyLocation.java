package com.futonredemption.mylocation;

import java.util.ArrayList;
import java.util.Locale;

import org.beryl.app.ChoosableIntent;
import org.beryl.app.IntentChooser;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.net.Uri;

public class MyLocation implements ILocationWidgetInfo {

	private final Location _location;
	private final Context _context;
	private Address _address = null;

	public MyLocation(final Context context, final Location location) {
		_location = location;
		_context = context;
	}

	public double getLatitude() {
		return _location.getLatitude();
	}
	
	public double getLongitude() {
		return _location.getLongitude();
	}
	
	public CharSequence getTitle() {
		CharSequence title = null;
		
		if(_address != null) {
			if(_address.getMaxAddressLineIndex() > 0) {
				title = _address.getAddressLine(0);
			}
		}
		else {
			title = _context.getText(R.string.coordinates);
		}
		
		return title;
	}
	
	public CharSequence getDescription() {
		CharSequence description = null;
		
		if(_address != null) {
			if(_address.getMaxAddressLineIndex() > 1) {
				description = _address.getAddressLine(1);
			}
			else {
				description = _address.getFeatureName();
			}
		}
		else {
			description = getOneLineCoordinates();
		}
		
		return description;
	}
	
	public void attachAddress(Address address) {
		_address = address;
	}
	
	public Intent getActionIntent() {
		return Intents.actionRefresh(_context);
	}

	private Intent getShareCoordinatesIntent() {
		final Intent coordinateShare = Intents.createSend(_context.getText(R.string.my_location), getOneLineCoordinates());
		return Intent.createChooser(coordinateShare, _context.getText(R.string.coordinates));
	}

	private Intent getShareAddressIntent() {
		final Intent addressShare = Intents.createSend(_context.getText(R.string.my_location), getOneLineAddress());
		return Intent.createChooser(addressShare, _context.getText(R.string.address));
	}
	
	private Intent getShareMapsLinkIntent() {
		final Intent addressShare = Intents.createSend(_context.getText(R.string.my_location), getGoogleMapsUrl(getOneLineAddress()));
		return Intent.createChooser(addressShare, _context.getText(R.string.maps_link));
	}
	
	public Intent getShareIntent() {
		final ArrayList<ChoosableIntent> intents = new ArrayList<ChoosableIntent>();
		intents.add(createChoosable(R.string.address, getShareAddressIntent()));
		intents.add(createChoosable(R.string.maps_link, getShareMapsLinkIntent()));
		intents.add(createChoosable(R.string.coordinates, getShareCoordinatesIntent()));
		
		return IntentChooser.createChooserIntent(_context, _context.getText(R.string.share_location), intents);
	}

	private ChoosableIntent createChoosable(final int stringId, final Intent intent) {
		final CharSequence title = _context.getText(stringId);
		return new ChoosableIntent(title, intent);
	}
	
	private String getOneLineAddress() {

		final StringBuilder sb = new StringBuilder();
		sb.append(getTitle());
		sb.append(" ");
		sb.append(getDescription());
		return sb.toString();
	}
	
	private CharSequence getOneLineCoordinates() {
		return String.format(Locale.ENGLISH, "Lat: %s Long: %s", getLatitude(), getLongitude());
	}
	
	public Intent getViewIntent() {
		return Intents.viewWebsite(getGoogleMapsUrl(getOneLineAddress()));
	}

	public int getWidgetState() {
		return Constants.WIDGETLAYOUTSTATE_Default;
	}
	
	private String getGoogleMapsUrl(String message) {
		return String.format(Locale.ENGLISH, Constants.URL_GmapsBase, getLatitude(), getLongitude(), Uri.encode(message));
	}

	public Intent getNotificationIntent() {
		return getViewIntent();
	}
	
	/*
	public static void AttachAddress(final Context context, final Bundle bundle, Address address) {
		

		final double latitude = bundle.getDouble(Constants.PARAM_Latitude);
		final double longitude = bundle.getDouble(Constants.PARAM_Longitude);

		final String uriaddress = lines[0] + Constants.SPACE + lines[1];
		final String gmaps_url = String.format(Locale.ENGLISH, Constants.URL_GmapsBase, latitude, longitude, Uri
				.encode(uriaddress));
		final String gmaps_share_url = String.format(Locale.ENGLISH, Constants.URL_GmapsBase, latitude, longitude, Uri.encode(uriaddress));
		final String address_data = lines[0] + " " + lines[1];

		Intent sendMapLink = new Intent(Intent.ACTION_SEND);
		sendMapLink.setType(Constants.MIME_Email);
		sendMapLink.putExtra(Intent.EXTRA_EMAIL, new String[] { Constants.BLANK });
		sendMapLink.putExtra(Intent.EXTRA_SUBJECT, context.getText(R.string.my_location));
		sendMapLink.putExtra(Intent.EXTRA_TEXT, address_data + " " + gmaps_share_url);

		final Intent openMapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(gmaps_url));

		bundle.putParcelable(Constants.PARAM_IntentViewLocation, openMapIntent);
		bundle.putParcelable(Constants.PARAM_IntentShareLocation, );
	}
	
	public static Bundle GetAcquiredLocationStateBundle(Context context, double latitude, double longitude) {
		final Bundle result = new Bundle();
		result.putInt(Constants.PARAM_State, Constants.STATE_HaveLocation);

		result.putDouble(Constants.PARAM_Latitude, latitude);
		result.putDouble(Constants.PARAM_Longitude, longitude);

		final String gmaps_url = String.format(Locale.ENGLISH, Constants.URL_GmapsBase, latitude, longitude,
				Constants.TEXT_YouAreHere);
		final String gmaps_share_url = String.format(Locale.ENGLISH, Constants.URL_GmapsBase, latitude, longitude,
				Constants.TEXT_IAmHere);

		Intent sendMapLink = new Intent(Intent.ACTION_SEND);
		sendMapLink.setType(Constants.MIME_Email);
		sendMapLink.putExtra(Intent.EXTRA_EMAIL, new String[] { Constants.BLANK });
		sendMapLink.putExtra(Intent.EXTRA_SUBJECT, context.getText(R.string.my_location));
		sendMapLink.putExtra(Intent.EXTRA_TEXT, gmaps_share_url);

		final Intent openMapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(gmaps_url));

		result.putParcelable(Constants.PARAM_IntentViewLocation, openMapIntent);
		result.putParcelable(Constants.PARAM_IntentShareLocation, Intent.createChooser(sendMapLink, context
				.getText(R.string.share_location)));

		result.putCharSequence(Constants.PARAM_Line1, String
				.format(Locale.ENGLISH, Constants.FORMAT_Latitude, latitude));
		result.putCharSequence(Constants.PARAM_Line2, String.format(Locale.ENGLISH, Constants.FORMAT_Longitude,
				longitude));

		result.putParcelable(Constants.PARAM_IntentAction, Intents.actionRefresh(context));
		result.putInt(Constants.PARAM_WidgetLayoutState, Constants.WIDGETLAYOUTSTATE_Default);

		return result;
	}

	public static Bundle GetLocationNotFoundBundle(final Context context) {
		final Bundle result = new Bundle();
		result.putInt(Constants.PARAM_State, Constants.STATE_NoLocation);
		
		final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		result.putParcelable(Constants.PARAM_IntentViewLocation, intent);
		
		
		result.putParcelable(Constants.PARAM_IntentAction, Intents.actionRefresh(context));
		result.putInt(Constants.PARAM_WidgetLayoutState, Constants.WIDGETLAYOUTSTATE_NotAvailable);

		return result;
	}
	*/
}
