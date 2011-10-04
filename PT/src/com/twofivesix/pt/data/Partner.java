package com.twofivesix.pt.data;

import java.io.Serializable;
import java.sql.Date;
import java.util.Calendar;

public class Partner implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int id = -1;
	private String email;
	private Date dateAdded;
	
	public Partner(String email, Date dateAdded)
	{
		this.email = email;
		this.dateAdded = dateAdded;
	}
	
	public Partner(int id, String email, Date dateAdded)
	{
		this.id = id;
		this.email = email;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getDateAdded() {
		return dateAdded;
	}
	
	public static java.sql.Date getNow()
	{
		return new java.sql.Date(Calendar.getInstance().getTime().getTime());
	}
}
