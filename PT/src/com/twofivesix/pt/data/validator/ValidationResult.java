package com.twofivesix.pt.data.validator;

import android.view.View;

/**
 * Validation result class
 * value holder.
 * this class contains true/false (validation status)
 * and a message.
 * 
 * @author http://nl.linkedin.com/in/marcdekwant
 *
 */
public class ValidationResult {
	
	private boolean _ok = false;
	private String _message = "";
	private View _source;
	
	public ValidationResult(boolean ok, String message) {
		_ok = ok;
		_message = message;
		_source = null;
	}
	
	public ValidationResult(boolean ok, String message, View source) {
		_ok = ok;
		_message = message;
		_source = source;
	}
	
	public boolean isValid() {
		return _ok;
	}
	
	public String getMessage() {
		return _message;
	}
	
	public View getSource() {
		return _source;
	}

}
