package zetbrush.com.testapp.consts;

import com.facebook.common.util.ByteConstants;

/**
 * Created by Arman on 12/27/16.
 */

public interface CommonConstants {
	int CAMERA_PERMISSION_REQUEST_CODE = 111;
	int EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 222;
	int MAX_HEAP_SIZE = (int) Runtime.getRuntime().maxMemory();

	int MAX_DISK_CACHE_SIZE = 40 * ByteConstants.MB;
	int MAX_MEMORY_CACHE_SIZE = MAX_HEAP_SIZE / 4;

}
