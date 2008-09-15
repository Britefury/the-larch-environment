//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.HashMap;
import java.util.Map;

public class ParseResult
{
	public Object value;
	public int begin, end;
	public Map<String, Object> bindings;
	private boolean bSuppressed, bValid;
	
	
	public ParseResult()
	{
		value = null;
		begin = end = 0;
		bindings = new HashMap<String, Object>();
		bSuppressed = false;
		bValid = false;
	}
	
	
	public ParseResult(int end)
	{
		this.value = null;
		this.begin = 0;
		this.end = end;
		bindings = new HashMap<String, Object>();
		bSuppressed = false;
		bValid = false;
	}

	
	public ParseResult(Object value, int begin, int end)
	{
		this.value = value;
		this.begin = begin;
		this.end = end;
		bindings = new HashMap<String, Object>();
		bSuppressed = false;
		bValid = true;
	}
	
	public ParseResult(Object value, int begin, int end, HashMap<String, Object> bindings)
	{
		this.value = value;
		this.begin = begin;
		this.end = end;
		this.bindings = bindings;
		bSuppressed = false;
		bValid = true;
	}
	
	public ParseResult(Object value, int begin, int end, boolean bSuppressed)
	{
		this.value = value;
		this.begin = begin;
		this.end = end;
		this.bindings = new HashMap<String, Object>();
		this.bSuppressed = bSuppressed;
		this.bValid = true;
	}
	
	private ParseResult(Object value, int begin, int end, Map<String, Object> bindings, boolean bSuppressed, boolean bValid)
	{
		this.value = value;
		this.begin = begin;
		this.end = end;
		this.bindings = bindings;
		this.bSuppressed = bSuppressed;
		this.bValid = bValid;
	}
	
	
	
	public ParseResult suppressed()
	{
		return new ParseResult( value, begin, end, bindings, true, bValid );
	}
	
	
	public boolean isSuppressed()
	{
		return bSuppressed;
	}

	public boolean isValid()
	{
		return bValid;
	}
}
