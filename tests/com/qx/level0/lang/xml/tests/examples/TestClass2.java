package com.qx.level0.lang.xml.tests.examples;


import com.s8.lang.xml.api.XML_GetAttribute;
import com.s8.lang.xml.api.XML_SetAttribute;
import com.s8.lang.xml.api.XML_Type;

@XML_Type(name="test2", sub={})
public class TestClass2 extends Wrapper {

	public int hiddenValue;
	
	private double b = 5.0;
	
	@XML_SetAttribute(name="b2")
	public void setFactor2(double b){
		this.b = b;
	}
	
	@XML_GetAttribute(tag="b2")
	public double getFactor2(){
		return b;
	}
}