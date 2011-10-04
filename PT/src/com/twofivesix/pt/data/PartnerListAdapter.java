package com.twofivesix.pt.data;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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
		// TODO Change list item layout
		Partner item = getItem(position);
		
		View row;
		if(item != null)
		{
			row = inflater.inflate(R.layout.partner_list_item, parent, false);
			TextView tvQuestion  = (TextView) row.findViewById(R.id.question);
			TextView tvAdded = (TextView) row.findViewById(R.id.added);
		
			tvQuestion.setText(item.getEmail());
			tvAdded.setText(item.getDateAdded().toString());
		}
		else
		{
			row = inflater.inflate(R.layout.add_partner_list_item, parent, false);
		}
				
		return row;
	}
}
