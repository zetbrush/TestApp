package zetbrush.com.testapp.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import twitter4j.MediaEntity;
import twitter4j.Status;
import zetbrush.com.testapp.R;
import zetbrush.com.testapp.fresco.FrescoLoader;

/**
 * Created by Arman on 12/27/16.
 */

public class UserHomeTimelineAdapter extends RecyclerView.Adapter<UserHomeTimelineAdapter.TimeLineViewHolder> {

	private static final int feedViewType = 1;
	private static final int loadingViewType = 2;
	private Context context;
	private ArrayList<Status> items;
	private FrescoLoader frescoLoader;
	private boolean noRealData = false;


	public UserHomeTimelineAdapter(Context context, ArrayList<Status> items) {
		this.context = context;
		this.items = (items == null) ? new ArrayList<Status>() : items;
		this.frescoLoader = new FrescoLoader();
	}


	@Override
	public int getItemViewType(int position) {
		return noRealData ? loadingViewType : feedViewType;
	}


	@Override
	public TimeLineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
			case loadingViewType: {
				return new TimeLineViewHolder(LayoutInflater.from(context).inflate(R.layout.feed_loading_layout, parent, false), true);
			}
			case feedViewType:
			default:
				return new TimeLineViewHolder(LayoutInflater.from(context).inflate(R.layout.card_skeleton, parent, false));
		}
	}


	@Override
	public void onBindViewHolder(TimeLineViewHolder holder, int position) {
		if (getItemViewType(position) == loadingViewType) {
			return;
		}
		Status status = items.get(position);
		MediaEntity mediaEntity = (status.getMediaEntities() != null && status.getMediaEntities().length > 0) ? status
				.getMediaEntities()[0] : null;

		//set tweet message
		holder.tweetText.setText(status.getText());

		/// load main image if exists
		if (mediaEntity != null && !"video".equals(mediaEntity.getType())) {
			holder.mainImage.setVisibility(View.VISIBLE);
			MediaEntity.Size size = new ArrayList<>(mediaEntity.getSizes().values()).get(0);
			float aspectRatio = size.getWidth() / (size.getHeight() * 1f);
			holder.mainImage.setAspectRatio(aspectRatio);
			frescoLoader.loadWithParams(mediaEntity.getMediaURL(), holder.mainImage, null);
		} else {
			holder.mainImage.setVisibility(View.GONE);
		}

		frescoLoader.loadWithParams(status.getUser().getProfileImageURL(), holder.icon, null);

		holder.title.setText(status.getUser().getScreenName());

		holder.subTitle.setText(status.getCreatedAt().toString());

	}


	@Override
	public int getItemCount() {
		noRealData = items.size() == 0;
		return items != null && !noRealData ? items.size() : 1;
	}


	public void addItems(List<Status> statuses) {
		for (Status status : statuses) {
			if (status != null) {
				items.add(status);
			}
		}
		notifyDataSetChanged();
	}


	class TimeLineViewHolder extends RecyclerView.ViewHolder {
		CardView cardContainer;
		View header;
		public SimpleDraweeView icon;
		public TextView title;
		TextView subTitle;
		SimpleDraweeView mainImage;
		TextView tweetText;

		LinearLayout body;

		TimeLineViewHolder(View itemView) {
			super(itemView);
			cardContainer = (CardView) itemView.findViewById(R.id.card_container);
			header = itemView.findViewById(R.id.header);
			icon = (SimpleDraweeView) header.findViewById(R.id.icon);
			title = (TextView) header.findViewById(R.id.title);
			subTitle = (TextView) header.findViewById(R.id.subtitle);
			body = (LinearLayout) itemView.findViewById(R.id.body);
			mainImage = (SimpleDraweeView) itemView.findViewById(R.id.main_image);
			tweetText = (TextView) itemView.findViewById(R.id.tweet_text);
		}

		TimeLineViewHolder(View itemView, boolean loadingView) {
			super(itemView);
		}
	}
}
