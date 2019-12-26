package com.qx.level0.lang.xml.handler.type;

public class XML_TypeCompilationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6328195916635403775L;
	
	
	public XML_TypeCompilationException(Class<?> type, String message) {
		super(message+" for type "+type.getName());
	}

}