package zetbrush.com.testapp.fresco;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

/**
 * Created by Arman on 12/26/16.
 */

public class FrescoLoader {


	public FrescoLoader() {
	}


	public void loadWithParams(String url, DraweeView imageView, ControllerListener<ImageInfo> listener) {
		Uri uri = Uri.parse(url);

		ImageRequestBuilder imageRequestBuilder = ImageRequestBuilder.newBuilderWithSource(uri);

		if (UriUtil.isNetworkUri(uri)) {
			imageRequestBuilder.setProgressiveRenderingEnabled(true);
		} else {
			imageRequestBuilder.setResizeOptions(new ResizeOptions(
					imageView.getLayoutParams().width,
					imageView.getLayoutParams().height));
		}

		DraweeController draweeController = Fresco.newDraweeControllerBuilder()
				.setImageRequest(imageRequestBuilder.build())
				.setOldController(imageView.getController())
				.setControllerListener(listener)
				.setAutoPlayAnimations(true)
				.build();

		imageView.setController(draweeController);
	}

	public void loadWithParamsAsCircle(String url, SimpleDraweeView imageView, @Nullable ControllerListener<ImageInfo> listener) {
		if (imageView.getHierarchy() != null) {
			RoundingParams roundingParams = RoundingParams.asCircle();
			imageView.getHierarchy().setRoundingParams(roundingParams);
			loadWithParams(url, imageView, listener);
		} else
			Log.e("loadWithParamsAsCircle", " can't load as circle if no DraweeHierarchy is set");
	}

}
