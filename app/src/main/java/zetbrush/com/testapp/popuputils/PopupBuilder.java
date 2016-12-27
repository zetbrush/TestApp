package zetbrush.com.testapp.popuputils;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import zetbrush.com.testapp.R;

/**
 * Created by Arman on 12/28/16.
 */

public class PopupBuilder {

	public static PopupWindow createNoNetworkPopupWindow(Context context, ViewGroup parent) {

		View v = LayoutInflater.from(context).inflate(R.layout.no_network_state, parent, false);
		PopupWindow popupWindow = new PopupWindow(v, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			popupWindow.setElevation(2f);
		}

		popupWindow.setContentView(v);
		popupWindow.setOutsideTouchable(false);
		return popupWindow;
	}
}
