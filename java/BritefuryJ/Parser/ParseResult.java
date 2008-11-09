//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.ArrayList;
import java.util.HashMap;

public class ParseResult
{
	protected Object value;
	protected int begin, end;
	protected boolean bSuppressed, bValid;
	protected HashMap<String, Object> bindings;
	
	
	public ParseResult()
	{
		value = null;
		begin = end = 0;
		bSuppressed = false;
		bValid = false;
	}
	
	
	
	public ParseResult(Object value, int begin, int end)
	{
		this.value = value;
		this.begin = begin;
		this.end = end;
		bSuppressed = false;
		bValid = true;
	}
	
	
	private ParseResult(int end)
	{
		this.value = null;
		this.begin = 0;
		this.end = end;
		bSuppressed = false;
		bValid = false;
	}

	private ParseResult(Object value, int begin, int end, boolean bSuppressed)
	{
		this.value = value;
		this.begin = begin;
		this.end = end;
		this.bSuppressed = bSuppressed;
		this.bValid = true;
	}
	
	private ParseResult(Object value, int begin, int end, boolean bSuppressed, boolean bValid)
	{
		this.value = value;
		this.begin = begin;
		this.end = end;
		this.bSuppressed = bSuppressed;
		this.bValid = bValid;
	}
	
	private ParseResult(Object value, int begin, int end, boolean bSuppressed, boolean bValid, HashMap<String, Object> bindings)
	{
		this.value = value;
		this.begin = begin;
		this.end = end;
		this.bSuppressed = bSuppressed;
		this.bValid = bValid;
		this.bindings = bindings;
	}
	
	
	
	public ParseResult suppressed()
	{
		return new ParseResult( value, begin, end, true, bValid );
	}
	
	
	@SuppressWarnings("unchecked")
	public ParseResult bind(String name, Object value, int start)
	{
		HashMap<String, Object> b;
		
		if ( bindings != null )
		{
			if ( bindings.containsKey( name ) )
			{
				if ( !value.equals( bindings.get( name ) ) )
				{
					return failure( start );
				}
				else
				{
					return this;
				}
			}
			else
			{
				b = (HashMap<String, Object>)bindings.clone();
			}
		}
		else
		{
			b = new HashMap<String, Object>();			
		}
		
		b.put( name, value );
		
		return new ParseResult( value, begin, end, bSuppressed, bValid, b );
	}
	
	
	@SuppressWarnings("unchecked")
	public ParseResult rebind(String name, Object value, int start)
	{
		HashMap<String, Object> b;
		
		if ( bindings != null )
		{
			b = (HashMap<String, Object>)bindings.clone();
		}
		else
		{
			b = new HashMap<String, Object>();			
		}
		
		b.put( name, value );
		
		return new ParseResult( value, begin, end, bSuppressed, bValid, b );
	}
	
	
	@SuppressWarnings("unchecked")
	public ParseResult bindMulti(String name, Object value, int start)
	{
		HashMap<String, Object> b;
		ArrayList<Object> xs;

		if ( bindings != null )
		{
			b = (HashMap<String, Object>)bindings.clone();
			
			if ( bindings.containsKey( name ) )
			{
				Object x = bindings.get( name );
			
				if ( x != null  &&  x instanceof ArrayList )
				{
					xs = (ArrayList<Object>)x;
				}
				else
				{
					xs = new ArrayList<Object>();
					xs.add( x );
				}
			}
			else
			{
				xs = new ArrayList<Object>();
			}
		}
		else
		{
			b = new HashMap<String, Object>();		
			xs = new ArrayList<Object>();
		}

		xs.add( value );
		b.put( name, xs );
		return new ParseResult( value, begin, end, bSuppressed, bValid, b );
	}
	
	
	public DebugParseResult debug(DebugParseResult.DebugNode debugNode)
	{
		return new DebugParseResult( value, begin, end, bSuppressed, bValid, debugNode );
	}
	
	
	
	public Object getValue()
	{
		return value;
	}
	
	public int getBegin()
	{
		return begin;
	}
	
	public int getEnd()
	{
		return end;
	}
	
	public boolean isSuppressed()
	{
		return bSuppressed;
	}

	public boolean isValid()
	{
		return bValid;
	}
	
	
	
	public static ParseResult failure(int end)
	{
		return new ParseResult( end );
	}
	
	public static ParseResult suppressedNoValue(int begin, int end)
	{
		return new ParseResult( null, begin, end, true );
	}
}
