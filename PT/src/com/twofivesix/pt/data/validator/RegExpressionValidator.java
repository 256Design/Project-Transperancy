package com.twofivesix.pt.data.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.widget.EditText;

/**
 * A regular expression validator
 * implementation of the validator.
 * With this validator EditText can be validated
 * against a regular expression.
 * 
 * @author http://nl.linkedin.com/in/marcdekwant
 *
 */
public class RegExpressionValidator extends AbstractValidator {
	
	
	public static final String PASSWORD_REGEX = "((?=.*[a-z]).{6,20})";
	public static final String EMAIL_REGEX = ".+@.+\\.[a-z]+";
	public static final String BIRTH_YEAR_REGEX = "(19|20)\\d\\d";
	public static final String FULL_NAME_REGEX = "^[A-Za-z\\W]+\\s[A-Za-z\\W]+";	
	
	private EditText _source;
	private String _expression = null;
	private Pattern _pattern = null;
	private String _faultExpressionMessage = "No valid expression found";
	private String _faultPatternMessage = "";
	private String _faultNoStringSourceMessage = "The field is not a valid String object";
	
	/** CONSTRUCTORS */

	public RegExpressionValidator() {}
	
	public RegExpressionValidator(EditText source, String expression) {
		_source = source;
		_expression = expression;
	}

	public RegExpressionValidator(EditText source, String expression, String faultMessage, String faultExpressionMessage) {
		_source = source;
		setExpression(expression);
		_faultMessage = faultMessage;
		_faultExpressionMessage = faultExpressionMessage;
	}

	@Override
	public ValidationResult validate() {
		ValidationResult _v = super.validate();
		if (_v == null) {
			
			// here starts the custom Regexpression validator implementation
			
			if (_source!=null) {
				if (!(_source instanceof EditText)) {
					_v = new ValidationResult(false,
							_faultNoStringSourceMessage,_source);
				} else if (_expression == null || _expression.length() == 0
						|| _pattern == null) {
					if (_pattern == null) {
						_v = new ValidationResult(false, _faultPatternMessage,_source);
					} else {
						_v = new ValidationResult(false,
								_faultExpressionMessage,_source);
					}
				} else {
					Matcher _matcher = _pattern.matcher(_source.getText().toString());
					if (!_matcher.find()) {
						_v = new ValidationResult(false, _faultMessage,_source);
					} else {
						_v = new ValidationResult(true, "",_source);
					}
				}
			} else if (_source==null) {
				// No validation is required...
				_v = new ValidationResult(true, "",_source);
			}
		}
		return _v;
	}
	
	/** CUSTOM SETTERS */
	
	public void setExpression(String expression) {
		_expression = expression;
		try {
		    _pattern = Pattern.compile(_expression);
		} catch (PatternSyntaxException e) {
			_faultPatternMessage = e.getMessage();
			_pattern = null;
		}
	}
	
	public void setFaultExpressionMessage(String message) {
		_faultExpressionMessage = message;
	}
	
	@Override
	public Object getSource() {
		return _source;
	}

}
