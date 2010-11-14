package org.beryl.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class ChoosableIntent implements Parcelable {
	
	protected final CharSequence label;
	protected final Intent intent;
	
	public ChoosableIntent(final CharSequence label, final Intent intent) {
		this.label = label;
		this.intent = intent;
	}

	public ChoosableIntent(final Parcel in) {
		final Bundle bundle = in.readBundle();
		this.label = bundle.getCharSequence("label");
		this.intent = bundle.getParcelable("intent");
	}

	public boolean isValid() { 
		return label != null && intent != null;
	}
	
	public int describeContents() {
		return 0;
	}

	public void writeToParcel(final Parcel dest, final int flags) {
		final Bundle bundle = new Bundle();
		bundle.putCharSequence("label", this.label);
		bundle.putParcelable("intent", this.intent);
		dest.writeBundle(bundle);
	}
	
	@SuppressWarnings("unchecked")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ChoosableIntent createFromParcel(final Parcel in) {
            return new ChoosableIntent(in);
        }

        public ChoosableIntent[] newArray(final int size) {
            return new ChoosableIntent[size];
        }
    };
}