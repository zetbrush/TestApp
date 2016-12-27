package zetbrush.com.testapp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Arman on 12/27/16.
 */

public class NetworkStateReceiver extends BroadcastReceiver {
	private Callback callback;

	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null && activeNetInfo.isConnectedOrConnecting()) {
			if (callback != null) {
				callback.onNetworkAvailable();
			}
		} else {
			if (callback != null) {
				callback.onNetworkLost();
			}
		}
	}


	public NetworkStateReceiver setCallback(Callback callback) {
		this.callback = callback;
		return this;
	}

	public interface Callback {
		void onNetworkAvailable();

		void onNetworkLost();
	}
}
