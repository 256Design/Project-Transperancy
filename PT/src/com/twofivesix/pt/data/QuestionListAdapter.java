package com.twofivesix.pt.data;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class QuestionListAdapter extends ArrayAdapter<Question> {
	
	public QuestionListAdapter(Context context, ArrayList<Question> questions)
	{
		super(context, R.layout.edit_question_list_item, R.id.question, questions);
	}
	
	/*public QuestionListAdapter(Context context, int resource, int textViewResourceId,
			ArrayList<Question> questions) {
		super(context, resource, textViewResourceId, questions);
	}*/
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = inflater.inflate(R.layout.edit_question_list_item, parent, false);
		Question item = getItem(position);
//		String[] items = getResources().getStringArray(R.array.countries);
		
		//ImageView iv = (ImageView) row.findViewById(R.id.imageView1);
		TextView tvQuestion  = (TextView) row.findViewById(R.id.question);
		TextView tvAdded = (TextView) row.findViewById(R.id.added);
		
		tvQuestion.setText(item.getQuestion());
		tvAdded.setText(item.getDateAdded().toString());
				
		return row;
	}
}
