package com.twofivesix.pt.listAdapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.twofivesix.pt.R;
import com.twofivesix.pt.data.Question;

public class ReportQuestionListAdapter extends ArrayAdapter<Question> {
	
	protected ArrayList<View> responseViewList;
	protected boolean followUp;
	
	public ReportQuestionListAdapter(Context context, ArrayList<Question> questionArrayList)
	{
		super(context, R.layout.reqort_question_list_item, R.id.question, questionArrayList);
		responseViewList = new ArrayList<View>(questionArrayList.size());
		for (Question question : questionArrayList) {
			if(question != null)
				responseViewList.add(null);
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
				View responseView = null;
				LinearLayout layout = (LinearLayout) row.findViewById(R.id.report_layout);
				EditText responseEditText = (EditText) row.findViewById(R.id.report_question_response_edittext);
				Spinner responseSpinner = (Spinner)row.findViewById(R.id.report_question_response_spinner);

				TextView questionText = (TextView) row.findViewById(R.id.question);
				questionText.setText(item.getQuestion());
				
				if(item.getType().equals(Question.TYPE_YES_NO))
				{
					responseView = responseSpinner;
					Spinner spinner = (Spinner) responseView;
					
					ArrayAdapter<CharSequence> qTypeAdapter = ArrayAdapter.createFromResource(
							getContext(), R.array.pos_response_array, android.R.layout.simple_spinner_item);
					qTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					spinner.setAdapter(qTypeAdapter);
					String positive = item.getPositive();
					int positiveIndex = qTypeAdapter.getPosition(positive);
					spinner.setSelection(positiveIndex);
					spinner.setPromptId(R.string.pick_one);
					
					layout.removeView(responseEditText);
				}
				else if(item.getType().equals(Question.TYPE_SHORT_ANSWER))
				{
					responseView = responseEditText;
					layout.removeView(responseSpinner);
				}
				if(position < responseViewList.size())
					responseViewList.set(position, responseView);
			} 
			else 
			{
				row = convertView;
			}
			
		}
		else
		{
			row = inflater.inflate(R.layout.follow_up_with_me_list_item, parent, false);
			CheckBox followUpCB = (CheckBox) row.findViewById(R.id.follow_up_cb);
			followUpCB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					followUp = isChecked;
				}
			});
		}
				
		return row;
	}

	public int getQuestionId(int i) {
		if(i < getCount())
			return getItem(i).getId();
		return -1;
	}

	public String getResponse(int i) {
		if(i < responseViewList.size() && responseViewList.get(i) != null)
		{
			View v = responseViewList.get(i);
			if (v instanceof Spinner)
				return ((Spinner)responseViewList.get(i)).getSelectedItem().toString();
			else if (v instanceof EditText)
				return ((EditText)responseViewList.get(i)).getText().toString();
			else
				return null;
		}
		else
			return getItem(i).getPositive();
	}
	
	public boolean getFollowUp()
	{
		return followUp;
	}
}
