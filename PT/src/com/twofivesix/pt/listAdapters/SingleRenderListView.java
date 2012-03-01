package com.twofivesix.pt.listAdapters;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class SingleRenderListView extends ScrollView 
{
	protected LinearLayout layout;
	private View footerView;
	private ArrayAdapter<?> adapter;

	public SingleRenderListView(Context context) {
		super(context);
		initLayout();
	}
	
	public SingleRenderListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initLayout();
	}

	public SingleRenderListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		initLayout();
	}

	private void initLayout() {
		layout = new LinearLayout(getContext());
		layout.setOrientation(LinearLayout.VERTICAL);
		LayoutParams params = new LayoutParams(
				LayoutParams.FILL_PARENT, 
				LayoutParams.WRAP_CONTENT);
		layout.setLayoutParams(params);
		addView(layout);
	}

	public void addFooterView(View footerView) {
		this.footerView = footerView;
		layout.addView(footerView);
	}

	public void setAdapter(ArrayAdapter<?> adapter)
	{
		layout.removeAllViews();
		
		// add items from adapter here
		this.adapter = adapter; 
		int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			View item = adapter.getView(i, null, this);
			if(ViewGroup.class.isInstance(item))
			{
				ListItemBaseLine line = new ListItemBaseLine(getContext());
				((ViewGroup)item).addView(line);
			}
			layout.addView(item);
		}
		
		addFooterView(footerView);
	}
	
	public ArrayAdapter<?> getAdapter()
	{
		return adapter;
	}
}
