package zetbrush.com.testapp.fresco;

import android.content.Context;

import com.facebook.imagepipeline.core.ImagePipelineConfig;

import okhttp3.OkHttpClient;

/**
 * Created by Arman on 12/27/16.
 */
public class OkHttpImagePipelineConfigFactory {

	public static ImagePipelineConfig.Builder newBuilder(Context context, OkHttpClient okHttpClient) {
		return ImagePipelineConfig.newBuilder(context)
				.setNetworkFetcher(new OkHttpNetworkFetcher(okHttpClient));
	}
}
