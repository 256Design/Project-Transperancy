package com.twofivesix.pt.listAdapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

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
		Question currentQuestion = super.getItem(position);
		
		if(null == row || row.getClass() != ReportingListItem.class)
			row = new ReportingListItem(getContext(), currentQuestion);
		else
			((ReportingListItem)row).setQuestion(currentQuestion);
				
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
			questions[i] = q;
		}
		
		return questions;
	}
}
