package org.beryl.app;

import android.content.Context;

/**
 * Contract for objects that can create a copy of themselves with a different Context.
 * @author jeremyje
 *
 */
public interface ContextClonable {
	Object clone(final Context context);
}
