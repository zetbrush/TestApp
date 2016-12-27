package zetbrush.com.testapp.twitterconfig;

import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import zetbrush.com.testapp.TestAppApplication;

/**
 * Created by Arman on 12/27/16.
 */

public final class TwitterConfigurationBuilder {


	private final ConfigurationBuilder builder;

	public TwitterConfigurationBuilder() {
		builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(TestAppApplication.TWITTER_KEY);
		builder.setOAuthConsumerSecret(TestAppApplication.TWITTER_SECRET);
	}


	public TwitterConfigurationBuilder setAccessToken(String token, String secret) {
		builder.setOAuthAccessToken(token);
		builder.setOAuthAccessTokenSecret(secret);
		return this;
	}

	public Configuration build() {
		return builder.build();
	}


}
