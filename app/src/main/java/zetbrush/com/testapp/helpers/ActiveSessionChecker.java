package zetbrush.com.testapp.helpers;

import android.app.Activity;
import android.content.Intent;

import com.twitter.sdk.android.core.TwitterCore;

import zetbrush.com.testapp.activities.LoginActivity;

/**
 * Created by Arman on 12/27/16.
 */

public final class ActiveSessionChecker {

	public static boolean isActiveSessionValid() {
		return TwitterCore.getInstance().getSessionManager().getActiveSession() != null;
	}

	public static boolean isActiveSessionValid(Activity activity, boolean finishifNo) {
		boolean isValid = TwitterCore.getInstance().getSessionManager().getActiveSession() != null;
		if (!isValid && finishifNo) {
			Intent intent = new Intent();
			intent.setClass(activity, LoginActivity.class);
			activity.startActivity(intent);
			activity.finish();
		}
		return isValid;
	}
}
