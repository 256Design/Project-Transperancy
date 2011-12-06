package com.twofivesix.pt.listAdapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.twofivesix.pt.R;
import com.twofivesix.pt.data.TileItem;

public class TileWithNameAdapter extends ArrayAdapter<TileItem> {

    public TileWithNameAdapter(Context c, ArrayList<TileItem> itemsArrayList) {
    	super(c, R.layout.home_tile_list_item, R.id.home_tile_label, itemsArrayList);
    }

    // create a new ImageView for each item referenced by the Adapter
    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
    	LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	TileItem item = getItem(position);
    	
    	View row;
        ImageView imageView;
        TextView label;
        row = inflater.inflate(R.layout.home_tile_list_item, parent, false);
        imageView = (ImageView) row.findViewById(R.id.home_tile_image);
        label = (TextView) row.findViewById(R.id.home_tile_label);

        imageView.setImageResource(item.getImageRef());
        label.setText(item.getLabelRef());
		return row;
    }
}