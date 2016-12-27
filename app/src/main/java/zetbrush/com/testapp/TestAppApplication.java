package zetbrush.com.testapp;

import android.app.Application;

import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.tweetui.TweetUi;

import io.fabric.sdk.android.Fabric;
import zetbrush.com.testapp.fresco.ImagePipelineConfigFactory;

/**
 * Created by Arman on 12/26/16.
 */

public class TestAppApplication extends Application {
	// shouldn't be exposed to public
	public static final String TWITTER_KEY = "yf9eaVG9riuZbReH7cyKI6BH8";
	public static final String TWITTER_SECRET = "toMb4HVr330A23vB33chtnVzC7o01ENUGWOBEg1RFK6BmhpFDA";
	//


	@Override
	public void onCreate() {
		super.onCreate();
		TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
		Fabric.with(this, new Twitter(authConfig), new TwitterCore(authConfig), new TweetUi());
		Fresco.initialize(this, ImagePipelineConfigFactory.getInstance().getImagePipelineConfig(this));
		FLog.setMinimumLoggingLevel(FLog.VERBOSE);


	}
}
