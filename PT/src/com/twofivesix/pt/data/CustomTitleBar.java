package com.twofivesix.pt.data;

import android.app.Activity;
import android.view.Window;
import android.widget.TextView;

import com.twofivesix.pt.R;

public class CustomTitleBar {
	public static void customTitleBar(Activity context, String right) {
        if (right.length() > 20) right = right.substring(0, 20);
        // set up custom title
        //boolean customTitleSupported = getWindow().requestFeature(Window.FEATURE_CUSTOM_TITLE);
        context.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
               R.layout.custom_title_bar);
        TextView titleTvRight = (TextView) context.findViewById(R.id.titleTvRight);

        titleTvRight.setText(right);
	}
}
