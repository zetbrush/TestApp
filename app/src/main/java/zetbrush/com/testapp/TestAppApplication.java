package zetbrush.com.testapp;

import android.app.Application;

import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Arman on 12/26/16.
 */

public class TestAppApplication extends Application {

	private static final String TWITTER_KEY = "yf9eaVG9riuZbReH7cyKI6BH8";
	private static final String TWITTER_SECRET = "toMb4HVr330A23vB33chtnVzC7o01ENUGWOBEg1RFK6BmhpFDA";


	@Override
	public void onCreate() {
		super.onCreate();
		TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
		Fabric.with(this, new TwitterCore(authConfig));


	}
}
