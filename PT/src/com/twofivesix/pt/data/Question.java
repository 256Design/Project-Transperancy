package com.twofivesix.pt.data;

import java.io.Serializable;
import java.sql.Date;
import java.util.Calendar;

public class Question implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static public String TYPE_YES_NO = "yesno";
	static public String RESP_YES = "respYes";
	static public String RESP_NO = "respNo";
	
	private int id = -1;
	private String question;
	private String type;
	private String positive;
	private Date dateAdded;
	
	public Question(String question, String type, String positive, Date dateAdded)
	{
		this.question = question;
		this.type = type;
		this.positive = positive;
		this.dateAdded = dateAdded;
	}
	
	public Question(int id, String question, String type, String positive, Date dateAdded)
	{
		this.id = id;
		this.question = question;
		this.type = type;
		this.positive = positive;
		this.dateAdded = dateAdded;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		if(this.id == -1)
			this.id = id;
		else
			throw new RuntimeException("Can not override id: " + this.id + ".");
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPositive() {
		return positive;
	}

	public void setPositive(String positive) {
		this.positive = positive;
	}

	public Date getDateAdded() {
		return dateAdded;
	}
	
	public static java.sql.Date getNow()
	{
		return new java.sql.Date(Calendar.getInstance().getTime().getTime());
	}
}
