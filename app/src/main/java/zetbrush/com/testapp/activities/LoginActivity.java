package zetbrush.com.testapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import zetbrush.com.testapp.R;

/**
 * Created by Arman on 12/26/16.
 */

public class LoginActivity extends AppCompatActivity {
	private TwitterLoginButton loginButton;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);

		loginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
		loginButton.setCallback(new Callback<TwitterSession>() {
			@Override
			public void success(Result<TwitterSession> result) {
				// The TwitterSession is also available through:
				// Twitter.getInstance().core.getSessionManager().getActiveSession()
				TwitterSession session = result.data;
				// with your app's user model
//				String msg = "@" + session.getUserName() + " logged in! (#" + session.getUserId() + ")";
//				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
			}

			@Override
			public void failure(TwitterException exception) {
				Log.d("TwitterKit", "Login with Twitter failure", exception);
			}
		});
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// Make sure that the loginButton hears the result from any
		// Activity that it triggered.
		loginButton.onActivityResult(requestCode, resultCode, data);
	}
}
