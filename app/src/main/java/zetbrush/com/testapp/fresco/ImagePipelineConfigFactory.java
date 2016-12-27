package zetbrush.com.testapp.fresco;


import android.content.Context;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.listener.RequestListener;
import com.facebook.imagepipeline.listener.RequestLoggingListener;

import java.util.HashSet;
import java.util.Set;

import okhttp3.OkHttpClient;
import zetbrush.com.testapp.consts.CommonConstants;

/**
 * Created by Arman on 12/27/16.
 */
public class ImagePipelineConfigFactory {
	private static final String IMAGE_PIPELINE_CACHE_DIR = "imagepipeline_cache";

	private ImagePipelineConfig sImagePipelineConfig;
	private ImagePipelineConfig sOkHttpImagePipelineConfig;

	private static ImagePipelineConfigFactory INSTANSE;

	private ImagePipelineConfigFactory() {
	}

	public static ImagePipelineConfigFactory getInstance() {
		if (INSTANSE == null) {
			synchronized (ImagePipelineConfigFactory.class) {
				if (INSTANSE == null) {
					INSTANSE = new ImagePipelineConfigFactory();
				}
			}
		}
		return INSTANSE;
	}

	public ImagePipelineConfig getImagePipelineConfig(Context context) {
		if (INSTANSE.sImagePipelineConfig == null) {
			ImagePipelineConfig.Builder configBuilder = ImagePipelineConfig.newBuilder(context);
			configureCaches(configBuilder, context);
			configureLoggingListeners(configBuilder);
			configureOptions(configBuilder);
			INSTANSE.sImagePipelineConfig = configBuilder.build();
		}
		return INSTANSE.sImagePipelineConfig;
	}


	public ImagePipelineConfig getOkHttpImagePipelineConfig(Context context) {
		if (INSTANSE.sOkHttpImagePipelineConfig == null) {
			OkHttpClient okHttpClient = new OkHttpClient.Builder()
					.build();
			ImagePipelineConfig.Builder configBuilder =
					OkHttpImagePipelineConfigFactory.newBuilder(context, okHttpClient);
			configureCaches(configBuilder, context);
			configureLoggingListeners(configBuilder);
			INSTANSE.sOkHttpImagePipelineConfig = configBuilder.build();
		}
		return INSTANSE.sOkHttpImagePipelineConfig;
	}

	/**
	 * Configures disk and memory cache not to exceed common limits
	 */
	private void configureCaches(
			ImagePipelineConfig.Builder configBuilder,
			Context context) {
		final MemoryCacheParams bitmapCacheParams = new MemoryCacheParams(
				CommonConstants.MAX_MEMORY_CACHE_SIZE, // Max total size of elements in the cache
				Integer.MAX_VALUE,                     // Max entries in the cache
				CommonConstants.MAX_MEMORY_CACHE_SIZE, // Max total size of elements in eviction queue
				Integer.MAX_VALUE,                     // Max length of eviction queue
				Integer.MAX_VALUE);                    // Max cache entry size
		configBuilder
				.setBitmapMemoryCacheParamsSupplier(
						new Supplier<MemoryCacheParams>() {
							public MemoryCacheParams get() {
								return bitmapCacheParams;
							}
						})
				.setMainDiskCacheConfig(
						DiskCacheConfig.newBuilder(context)
								.setBaseDirectoryPath(context.getApplicationContext().getCacheDir())
								.setBaseDirectoryName(IMAGE_PIPELINE_CACHE_DIR)
								.setMaxCacheSize(CommonConstants.MAX_DISK_CACHE_SIZE)
								.build());
	}

	private static void configureLoggingListeners(ImagePipelineConfig.Builder configBuilder) {
		Set<RequestListener> requestListeners = new HashSet<>();
		requestListeners.add(new RequestLoggingListener());
		configBuilder.setRequestListeners(requestListeners);
	}

	private static void configureOptions(ImagePipelineConfig.Builder configBuilder) {
		configBuilder.setDownsampleEnabled(true);
	}
}
