package zetbrush.com.testapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.content.PermissionChecker;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.PopupWindow;
import android.widget.Toast;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.view.SimpleDraweeView;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;

import java.io.File;
import java.io.IOException;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterListener;
import twitter4j.TwitterMethod;
import twitter4j.conf.Configuration;
import zetbrush.com.testapp.R;
import zetbrush.com.testapp.adapters.UserHomeTimelineAdapter;
import zetbrush.com.testapp.consts.CommonConstants;
import zetbrush.com.testapp.fresco.FrescoLoader;
import zetbrush.com.testapp.helpers.ActiveSessionChecker;
import zetbrush.com.testapp.helpers.CameraCaptureHelper;
import zetbrush.com.testapp.popuputils.PopupBuilder;
import zetbrush.com.testapp.receivers.NetworkStateReceiver;
import zetbrush.com.testapp.twitterconfig.TwitterConfigurationBuilder;

import static zetbrush.com.testapp.consts.CommonConstants.CONNECTIVITY_CHANGE_ACTION;
import static zetbrush.com.testapp.consts.CommonConstants.REQUEST_IMAGE_CAPTURE;
import static zetbrush.com.testapp.consts.CommonConstants.REQUEST_PICK_IMAGE;

public class FeedActivity extends AppCompatActivity {

	private ImageButton attachGalleryImageBtn;
	private ImageButton attachCameraImageBtn;
	private EditText tweetTextView;
	private ImageButton tweetButton;
	private FrameLayout attachedImageContainer;
	private SimpleDraweeView attachedImagePreview;
	private ImageButton deleteAttachmentBtn;

	private StringBuilder tweetText = new StringBuilder();
	private String attachedImagePath;
	private String attachedCapturedImagePath;

	private FrescoLoader frescoLoader;
	private ViewGroup progressContainer;
	private RecyclerView recyclerViewFeedContent;
	private UserHomeTimelineAdapter homeTimelineAdapter;
	private SwipeRefreshLayout swipeRefreshLayout;
	private NetworkStateReceiver networkStateReceiver;
	private PopupWindow popupWindow;
	private boolean isActive;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_bar_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		initViews();

		initListeners();

		initNetworkAnalyzer();

		frescoLoader = new FrescoLoader();

		initAndLoadFeed();

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

	private void initViews() {
		tweetButton = (ImageButton) findViewById(R.id.tweet_btn);
		tweetTextView = (EditText) findViewById(R.id.tweet_text);
		attachGalleryImageBtn = (ImageButton) findViewById(R.id.attach_image_gallery_btn);
		attachCameraImageBtn = (ImageButton) findViewById(R.id.attach_image_camera_btn);
		attachedImageContainer = (FrameLayout) findViewById(R.id.image_container);
		attachedImagePreview = (SimpleDraweeView) attachedImageContainer.findViewById(R.id.attached_image);
		deleteAttachmentBtn = (ImageButton) attachedImageContainer.findViewById(R.id.delete_image);
		progressContainer = (ViewGroup) findViewById(R.id.progress_container);
		recyclerViewFeedContent = (RecyclerView) findViewById(R.id.feed_content);
		swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_feed);
	}

	private void initNetworkAnalyzer() {
		networkStateReceiver = new NetworkStateReceiver();

		networkStateReceiver.setCallback(new NetworkStateReceiver.Callback() {
			@Override
			public void onNetworkAvailable() {
				if (popupWindow != null && popupWindow.isShowing()) {
					popupWindow.dismiss();
				}
			}

			@Override
			public void onNetworkLost() {
				if (isActive) {
					if (popupWindow != null && popupWindow.isShowing()) {
						return;
					}
					attachedImageContainer.post(new Runnable() {
						@Override
						public void run() {
							popupWindow = PopupBuilder.createNoNetworkPopupWindow(FeedActivity.this, (ViewGroup) ((ViewGroup)
									findViewById(android.R.id.content)).getChildAt(0));
							popupWindow.showAsDropDown(attachedImageContainer);
						}
					});

				}
			}
		});
		IntentFilter filter = new IntentFilter();
		filter.addAction(CONNECTIVITY_CHANGE_ACTION);
		registerReceiver(networkStateReceiver, filter);
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
						if (tweetText.length() == 0 && TextUtils.isEmpty(attachedImagePath)) {
							return;
						}
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

		swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				initAndLoadFeed();
			}
		});

	}


	private void executeTweet() {
		if (!ActiveSessionChecker.isActiveSessionValid(this, true)) {
			return;
		}

		try {
			updateStatus(TwitterCore.getInstance().getSessionManager().getActiveSession().getAuthToken(),
					TextUtils.isEmpty(attachedImagePath) ? null : new File(attachedImagePath), tweetText.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void updateStatus(TwitterAuthToken token, File file, String message) throws Exception {
		Configuration configuration = new TwitterConfigurationBuilder().setAccessToken(token.token, token.secret).build();

		TwitterListener listener = new TwitterAdapter() {
			@Override
			public void updatedStatus(Status status) {
				FeedActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						progressContainer.setVisibility(View.GONE);
						removeAttachment();
						Handler handler = new Handler();
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								if (!FeedActivity.this.isFinishing()) {
									swipeRefreshLayout.setRefreshing(true);
									initAndLoadFeed();
								}
							}
						}, 400);

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
								Toast.makeText(FeedActivity.this, " Tweet update failed:( with message: " +
										e.getErrorMessage(), Toast.LENGTH_SHORT).show();
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
			asyncTwitter.getHomeTimeline();
		} else {
			asyncTwitter.updateStatus(message);
		}
	}

	private void initAndLoadFeed() {
		if (!ActiveSessionChecker.isActiveSessionValid(this, true)) {
			return;
		}

		TwitterAuthToken token = TwitterCore.getInstance().getSessionManager().getActiveSession().getAuthToken();
		Configuration configuration = new TwitterConfigurationBuilder().setAccessToken(token.token, token.secret).build();
		if (homeTimelineAdapter == null) {
			homeTimelineAdapter = new UserHomeTimelineAdapter(this, null);
		}

		if (recyclerViewFeedContent.getLayoutManager() == null) {
			LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
			recyclerViewFeedContent.setAdapter(homeTimelineAdapter);
			recyclerViewFeedContent.setLayoutManager(layoutManager);
			recyclerViewFeedContent.addItemDecoration(new RecyclerView.ItemDecoration() {
				@Override
				public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
					super.getItemOffsets(outRect, view, parent, state);
					outRect.top = getResources().getDimensionPixelSize(R.dimen.space_4dp);
				}
			});
		}
		TwitterListener listener = new TwitterAdapter() {
			@Override
			public void gotHomeTimeline(final ResponseList<Status> statuses) {
				FeedActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (swipeRefreshLayout.isRefreshing()) {
							swipeRefreshLayout.setRefreshing(false);
							initAndLoadFeed();
						}
						homeTimelineAdapter.addItems(statuses);

					}
				});

			}

			@Override
			public void onException(final TwitterException e, final TwitterMethod method) {
				FeedActivity.this.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (swipeRefreshLayout.isRefreshing()) {
							swipeRefreshLayout.setRefreshing(false);
						}
						if (method == TwitterMethod.HOME_TIMELINE) {
							e.printStackTrace();
							if (!isFinishing()) {
								Toast.makeText(FeedActivity.this, " Couldn't update feed:( with message: " + e.getErrorMessage()
										, Toast.LENGTH_SHORT).show();
							}

						}
					}
				});
			}
		};

		AsyncTwitterFactory factory = new AsyncTwitterFactory(configuration);
		AsyncTwitter asyncTwitter = factory.getInstance();
		asyncTwitter.addListener(listener);
		asyncTwitter.getHomeTimeline();


	}


	private void attachImagefromCamera() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
				&& PermissionChecker.checkCallingOrSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
				!= PackageManager.PERMISSION_GRANTED) {
			requestPermissions(new String[]{Manifest.permission.CAMERA}, CommonConstants.CAMERA_PERMISSION_REQUEST_CODE);
			return;
		}

		File photoFile = null;
		try {
			photoFile = CameraCaptureHelper.createImageFile(this);
		} catch (IOException ex) {
			ex.printStackTrace();
			return;
		}

		if (photoFile != null) {
			attachedCapturedImagePath = photoFile.getAbsolutePath();
			Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
			Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			if (takePicture.resolveActivity(getPackageManager()) != null) {
				takePicture.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
				startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
			}
		}
	}

	private void removeAttachment() {
		File tmpCapturedImg = null;
		if (attachedCapturedImagePath != null && (tmpCapturedImg = new File(attachedCapturedImagePath)).exists()) {
			tmpCapturedImg.delete();
		}
		attachedCapturedImagePath = null;
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
		startActivityForResult(pickPhoto, REQUEST_PICK_IMAGE);
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
	protected void onStop() {
		super.onStop();
		isActive = false;
		if (networkStateReceiver != null) {
			unregisterReceiver(networkStateReceiver);
			networkStateReceiver = null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		isActive = true;
		if (networkStateReceiver == null) {
			initNetworkAnalyzer();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (networkStateReceiver != null) {
			unregisterReceiver(networkStateReceiver);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_PICK_IMAGE) {
			if (resultCode == RESULT_OK) {
				Uri selectedImage = data.getData();
				attachedImagePath = UriUtil.getRealPathFromUri(getContentResolver(), selectedImage);
				if (attachedImagePath != null && attachedImageContainer != null && !isFinishing()) {
					attachedImageContainer.setVisibility(View.VISIBLE);
					frescoLoader.loadWithParams(attachedImagePath, attachedImagePreview, null);
				}
			}
		} else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && !TextUtils.isEmpty
				(attachedCapturedImagePath) && !isFinishing()) {
			attachedImageContainer.setVisibility(View.VISIBLE);
			attachedImagePath = attachedCapturedImagePath;
			frescoLoader.loadWithParams(attachedImagePath, attachedImagePreview, null);
		}

	}
}
