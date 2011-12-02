package com.twofivesix.pt.listAdapters;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.twofivesix.pt.R;
import com.twofivesix.pt.data.Partner;

public class PartnerListAdapter extends ArrayAdapter<Partner> {
	
	public PartnerListAdapter(Context context, ArrayList<Partner> partnerArrayList)
	{
		super(context, R.layout.edit_question_list_item, R.id.question, partnerArrayList);
//		Log.d("SPENCER", "PartnerListAdapter(...)");
	}
	
	/*public QuestionListAdapter(Context context, int resource, int textViewResourceId,
			ArrayList<Question> questions) {
		super(context, resource, textViewResourceId, questions);
	}*/
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Partner item = getItem(position);
		
		View row;
		if(item != null)
		{
			row = inflater.inflate(R.layout.partner_list_item, parent, false);
			TextView tvQuestion  = (TextView) row.findViewById(R.id.question);
			TextView tvAdded = (TextView) row.findViewById(R.id.added);
		
			tvQuestion.setText(item.getEmail());
			if(item.getState().equals(Partner.CONFIRMED))
			{
				tvAdded.setText(R.string.confirmed);
				tvAdded.setTextColor(Color.GREEN);
			}
			else if(item.getState().equals(Partner.UNCONFIRMED))
			{
				tvAdded.setText(R.string.unconfirmed);
				tvAdded.setTextColor(Color.YELLOW);
			}
			else
			{
				tvAdded.setText(R.string.denied);
				tvAdded.setTextColor(Color.RED);
			}
			tvAdded.setShadowLayer(1, 1, 1, Color.DKGRAY);
		}
		else
		{
			row = inflater.inflate(R.layout.add_partner_list_item, parent, false);
		}
		
				
		return row;
	}
}
