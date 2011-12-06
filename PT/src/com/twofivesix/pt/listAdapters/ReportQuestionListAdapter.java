package com.twofivesix.pt.listAdapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.twofivesix.pt.R;
import com.twofivesix.pt.data.Question;

public class ReportQuestionListAdapter extends ArrayAdapter<Question> {
	
	protected ArrayList<Spinner> spinnerList;
	
	public ReportQuestionListAdapter(Context context, ArrayList<Question> questionArrayList)
	{
		super(context, R.layout.reqort_question_list_item, R.id.question, questionArrayList);
		spinnerList = new ArrayList<Spinner>(questionArrayList.size());
		for (int i = 0; i < questionArrayList.size(); i++) {
			spinnerList.add(null);
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Question item = getItem(position);
		
		View row;
		if(item != null)
		{
			// Reuse if possible
			if (null == convertView)
			{
				row = inflater.inflate(R.layout.reqort_question_list_item, parent, false);
				Spinner spinner = (Spinner) row.findViewById(R.id.report_question_response);
				if(position < spinnerList.size())
					spinnerList.set(position, spinner);
				
				TextView questionText = (TextView) row.findViewById(R.id.question);
				questionText.setText(item.getQuestion());
				
				ArrayAdapter<CharSequence> qTypeAdapter = ArrayAdapter.createFromResource(
						getContext(), R.array.pos_response_array, android.R.layout.simple_spinner_item);
				qTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spinner.setAdapter(qTypeAdapter);
				String positive = item.getPositive();
				int positiveIndex = qTypeAdapter.getPosition(positive);
				spinner.setSelection(positiveIndex);
				spinner.setPromptId(R.string.pick_one);
			} 
			else 
			{
				row = convertView;
			}
			
		}
		else
		{
			row = inflater.inflate(R.layout.add_partner_list_item, parent, false);
		}
				
		return row;
	}

	public int getQuestionId(int i) {
		if(i < getCount())
			return getItem(i).getId();
		return -1;
	}

	public String getResponse(int i) {
		if(i < spinnerList.size() && spinnerList.get(i) != null)
			return spinnerList.get(i).getSelectedItem().toString();
		else
			return getItem(i).getPositive();
	}
}
