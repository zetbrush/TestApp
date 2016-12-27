package zetbrush.com.testapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.view.SimpleDraweeView;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

import java.io.File;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterListener;
import twitter4j.TwitterMethod;
import twitter4j.conf.Configuration;
import zetbrush.com.testapp.R;
import zetbrush.com.testapp.consts.CommonConstants;
import zetbrush.com.testapp.fresco.FrescoLoader;
import zetbrush.com.testapp.twitterconfig.TwitterConfigurationBuilder;

public class FeedActivity extends AppCompatActivity
		implements NavigationView.OnNavigationItemSelectedListener {

	private ActionBarDrawerToggle toggle;
	private DrawerLayout drawer;

	private ImageButton attachGalleryImageBtn;
	private ImageButton attachCameraImageBtn;
	private EditText tweetTextView;
	private ImageButton tweetButton;
	private FrameLayout attachedImageContainer;
	private SimpleDraweeView attachedImagePreview;
	private ImageButton deleteAttachmentBtn;

	private StringBuilder tweetText = new StringBuilder();
	private String attachedImagePath;

	private FrescoLoader frescoLoader;
	private TwitterApiClient apiClient;
	private TwitterAuthClient authClient;
	private ViewGroup progressContainer;
	private Twitter mTwitter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		initViews();

		initListeners();

		frescoLoader = new FrescoLoader();

		apiClient = TwitterCore.getInstance().getApiClient();
		authClient = new TwitterAuthClient();
		progressContainer.setVisibility(View.VISIBLE);

		drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		toggle = new ActionBarDrawerToggle(
				this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		progressContainer.setVisibility(View.GONE);
		drawer.addDrawerListener(toggle);
		toggle.syncState();

	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.nav_feed) {
			// Handle the feed action
		}
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (toggle != null && drawer != null) {
			drawer.removeDrawerListener(toggle);
		}
	}

	private void initViews() {
		tweetButton = (ImageButton) findViewById(R.id.tweet_btn);
		tweetTextView = (EditText) findViewById(R.id.tweet_text);
		attachGalleryImageBtn = (ImageButton) findViewById(R.id.attach_image_gallery_btn);
		attachCameraImageBtn = (ImageButton) findViewById(R.id.attach_image_camera_btn);
		attachedImageContainer = (FrameLayout) findViewById(R.id.image_container);
		attachedImagePreview = (SimpleDraweeView) attachedImageContainer.findViewById(R.id.attached_image);
		deleteAttachmentBtn = (ImageButton) attachedImageContainer.findViewById(R.id.delete_image);
		progressContainer = (ViewGroup) findViewById(R.id.progress_container);
	}

	private void initListeners() {

		tweetTextView.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				tweetText.setLength(0);
				tweetText.append(s.toString());
			}
		});

		View.OnClickListener groupedClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.tweet_btn: {
						executeTweet();
						break;
					}
					case R.id.attach_image_gallery_btn: {
						attachImagefromGallery();
						break;
					}
					case R.id.attach_image_camera_btn: {
						attachImagefromCamera();
						break;
					}
					case R.id.delete_image: {
						removeAttachment();
						break;
					}


				}
			}
		};

		attachGalleryImageBtn.setOnClickListener(groupedClickListener);
		attachCameraImageBtn.setOnClickListener(groupedClickListener);
		deleteAttachmentBtn.setOnClickListener(groupedClickListener);
		tweetButton.setOnClickListener(groupedClickListener);

	}


	private void executeTweet() {
		if (apiClient == null) {
			apiClient = TwitterCore.getInstance().getApiClient();
			if (TwitterCore.getInstance().getSessionManager().getActiveSession() == null) {
				// our session  has expired routing to login
				// FIXME: 12/27/16
				finish();
			}

		}

		try {
			updateStatus(TwitterCore.getInstance().getSessionManager().getActiveSession().getAuthToken(),
					TextUtils.isEmpty(attachedImagePath) ? null : new File(attachedImagePath), tweetText.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void updateStatus(TwitterAuthToken token, File file, String message) throws Exception {
		Configuration configuration = TwitterConfigurationBuilder.getInstance().setAccessToken(token.token, token.secret).build();

		TwitterListener listener = new TwitterAdapter() {
			@Override
			public void updatedStatus(Status status) {
				FeedActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						progressContainer.setVisibility(View.GONE);
						removeAttachment();
						if (!isFinishing()) {
							Toast.makeText(FeedActivity.this, " Tweeted successfully!", Toast.LENGTH_SHORT).show();
						}
					}
				});

			}

			@Override
			public void onException(final TwitterException e, final TwitterMethod method) {
				FeedActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						progressContainer.setVisibility(View.GONE);
						if (method == TwitterMethod.UPDATE_STATUS) {
							e.printStackTrace();
							if (!isFinishing()) {
								Toast.makeText(FeedActivity.this, " Tweet update failed:(", Toast.LENGTH_SHORT).show();
							}

						}
					}
				});
			}
		};

		AsyncTwitterFactory factory = new AsyncTwitterFactory(configuration);
		AsyncTwitter asyncTwitter = factory.getInstance();
		asyncTwitter.addListener(listener);
		progressContainer.setVisibility(View.VISIBLE);
		if (file != null) {
			StatusUpdate status = new StatusUpdate(message);
			status.setMedia(file);
			asyncTwitter.updateStatus(status);
		} else {
			asyncTwitter.updateStatus(message);
		}
	}


	private void attachImagefromCamera() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
				&& PermissionChecker.checkCallingOrSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
				!= PackageManager.PERMISSION_GRANTED) {
			requestPermissions(new String[]{Manifest.permission.CAMERA}, CommonConstants.CAMERA_PERMISSION_REQUEST_CODE);
			return;
		}
		Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(takePicture, 0);
	}

	private void removeAttachment() {
		attachedImagePath = null;
		tweetTextView.setText("");
		AlphaAnimation fadeAnimation = new AlphaAnimation(1f, 0f);
		fadeAnimation.setDuration(300);
		fadeAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				attachedImageContainer.setVisibility(View.GONE);
				attachedImageContainer.setAlpha(1f);
				attachedImagePreview.getHierarchy().reset();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
		attachedImageContainer.startAnimation(fadeAnimation);


	}

	private void attachImagefromGallery() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
				&& PermissionChecker.checkCallingOrSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {
			requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
					CommonConstants.EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
			return;
		}
		Intent pickPhoto = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(pickPhoto, 1);
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case CommonConstants.EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE: {
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					attachImagefromGallery();
				}
				break;
			}
			case CommonConstants.CAMERA_PERMISSION_REQUEST_CODE: {
				if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					attachImagefromCamera();
				}
				break;
			}
		}
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0 || requestCode == 1) {
			if (resultCode == RESULT_OK) {
				Uri selectedImage = data.getData();
				attachedImagePath = UriUtil.getRealPathFromUri(getContentResolver(), selectedImage);
				if (attachedImagePath != null && attachedImageContainer != null && !isFinishing()) {
					attachedImageContainer.setVisibility(View.VISIBLE);
					frescoLoader.loadWithParams(attachedImagePath, attachedImagePreview, null);
				}
			}
		} else {
			authClient.onActivityResult(requestCode, resultCode, data);
		}

	}
}
