package com.twofivesix.pt.listAdapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
		
		View row = convertView;
		Question item = super.getItem(position);
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// Reuse if possible
		if (null == row)
		{
			row = inflater.inflate(R.layout.reqort_question_list_item, parent, false);
		}
		else {
			if(item.getType().equals(Question.TYPE_YES_NO) &&
				row.findViewById(R.id.report_question_response_spinner) == null)
			{
				row = inflater.inflate(R.layout.reqort_question_list_item, parent, false);
			}
			else if (item.getType().equals(Question.TYPE_SHORT_ANSWER) &&
					row.findViewById(R.id.report_question_response_edittext) == null)
			{
				row = inflater.inflate(R.layout.reqort_question_list_item, parent, false);
			}
		}
		inflater = null;
		if (item != null)
		{
			View responseView = null;
			LinearLayout layout = (LinearLayout) row.findViewById(R.id.report_layout);
			EditText responseEditText = (EditText) row.findViewById(R.id.report_question_response_edittext);
			Spinner responseSpinner = (Spinner)row.findViewById(R.id.report_question_response_spinner);

			TextView questionText = (TextView) row.findViewById(R.id.question);
			questionText.setText(item.getQuestion());
			
			if(item.getType().equals(Question.TYPE_YES_NO))
			{
				responseView = responseSpinner;
				
				if(responseView != null)
				{
					Spinner spinner = (Spinner) responseView;
					ArrayAdapter<CharSequence> qTypeAdapter = ArrayAdapter.createFromResource(
							getContext(), R.array.pos_response_array, android.R.layout.simple_spinner_item);
					qTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					spinner.setAdapter(qTypeAdapter);
					String selectionString = item.getResponse();
					if(selectionString == null)
						selectionString = item.getPositive();
					int selectionIndex = qTypeAdapter.getPosition(selectionString);
					spinner.setSelection(selectionIndex);
					spinner.setPromptId(R.string.pick_one);
				}
				
				if(responseEditText != null)
					layout.removeView(responseEditText);
			}
			else if(item.getType().equals(Question.TYPE_SHORT_ANSWER))
			{
				responseView = responseEditText;
				String responseString = item.getResponse();
				if(responseString != null)
					responseEditText.setText(responseString);
				if(responseSpinner != null)
					layout.removeView(responseSpinner);
			}
			if(position < responseViewList.size())
				responseViewList.set(position, responseView);
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

	public Question[] getItemsWithResponses() {
		Question[] questions = new Question[getCount()];
		
		for(int i = 0; i < getCount(); i++)
		{
			Question q = getItem(i);
			q.setResponse(getResponse(i));
			questions[i] = q;
		}
		
		return questions;
	}
}
