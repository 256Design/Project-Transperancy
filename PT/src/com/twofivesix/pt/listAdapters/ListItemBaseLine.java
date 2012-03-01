package com.twofivesix.pt.listAdapters;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class ListItemBaseLine extends View
{
	public ListItemBaseLine(Context context)
	{
		super(context);
		LayoutParams params = new LayoutParams(
				LayoutParams.FILL_PARENT,
				1);
		setLayoutParams(params);
		setBackgroundColor(Color.GRAY);
		float density = context.getResources().getDisplayMetrics().density;
		setPadding(0, (int)(density* 10), 0, 0);
	}
}