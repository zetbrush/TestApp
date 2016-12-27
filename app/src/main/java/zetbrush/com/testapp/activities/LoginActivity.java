package zetbrush.com.testapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import zetbrush.com.testapp.R;

/**
 * Created by Arman on 12/26/16.
 */

public class LoginActivity extends AppCompatActivity {
	private TwitterLoginButton loginButton;
	private TwitterAuthClient authClient;
	private Intent mainPageIntent;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);
		authClient = new TwitterAuthClient();

		final TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();

		if (session != null) {
			openFeed();
		}

		loginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
		loginButton.setCallback(new Callback<TwitterSession>() {
			@Override
			public void success(Result<TwitterSession> result) {
				TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();
				TwitterAuthToken authToken = session.getAuthToken();
				openFeed();

			}

			@Override
			public void failure(TwitterException exception) {
				Log.d("TwitterKit", "Login with Twitter failure", exception);
			}
		});
	}


	private void openFeed() {
		mainPageIntent = new Intent();
		mainPageIntent.setClass(LoginActivity.this, FeedActivity.class);
		startActivity(mainPageIntent);
		LoginActivity.this.finish();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		loginButton.onActivityResult(requestCode, resultCode, data);
		authClient.onActivityResult(requestCode, resultCode, data);
	}
}

