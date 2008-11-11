//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.NodeParser;

import java.util.HashMap;
import java.util.Map;

public class ParseResult
{
	protected static class NameAlreadyBoundException extends Exception
	{
		private static final long serialVersionUID = 1L;
	};
	
	
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
	
	public ParseResult(Object value, int begin, int end, HashMap<String, Object> bindings)
	{
		this.value = value;
		this.begin = begin;
		this.end = end;
		bSuppressed = false;
		bValid = true;
		this.bindings = bindings;
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
	
	protected ParseResult(Object value, int begin, int end, boolean bSuppressed, boolean bValid, HashMap<String, Object> bindings)
	{
		this.value = value;
		this.begin = begin;
		this.end = end;
		this.bSuppressed = bSuppressed;
		this.bValid = bValid;
		this.bindings = bindings;
	}
	
	
	
	protected ParseResult withValidUnsuppressedValue(Object v)
	{
		return new ParseResult( v, begin, end, false, true, bindings );
	}
	
	
	protected ParseResult suppressed()
	{
		return new ParseResult( value, begin, end, true, bValid, bindings );
	}
	
	
	protected ParseResult bind(String name, Object bindingValue, int start)
	{
		HashMap<String, Object> b = new HashMap<String, Object>();
		
		if ( bindings != null )
		{
			if ( bindings.containsKey( name ) )
			{
				if ( !bindingValue.equals( bindings.get( name ) ) )
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
				b.putAll( bindings );
			}
		}
		
		b.put( name, bindingValue );
		
		return new ParseResult( value, begin, end, bSuppressed, bValid, b );
	}
	
	
	protected ParseResult clearBindings()
	{
		return new ParseResult( value, begin, end, bSuppressed, bValid );
	}
	
	
	
	protected HashMap<String, Object> addBindingsTo(HashMap<String, Object> joinedBindings) throws NameAlreadyBoundException
	{
		if ( bindings == null )
		{
			return joinedBindings;
		}
		else
		{
			if ( joinedBindings == null )
			{
				joinedBindings = new HashMap<String, Object>();
				joinedBindings.putAll( bindings );
			}
			else
			{
				for (Map.Entry<String, Object> entry: bindings.entrySet())
				{
					String name = entry.getKey();
					Object value = entry.getValue();
					if ( joinedBindings.containsKey( name ) )
					{
						Object existingValue = joinedBindings.get( name );
						if ( !value.equals( existingValue ) )
						{
							throw new NameAlreadyBoundException();
						}
					}
					else
					{
						joinedBindings.put( name, value );
					}
				}
			}

			return joinedBindings;
		}
	}
	
	
	protected DebugParseResult debug(DebugParseResult.DebugNode debugNode)
	{
		return new DebugParseResult( value, begin, end, bSuppressed, bValid, bindings, debugNode );
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
	
	public HashMap<String, Object> getBindings()
	{
		return bindings;
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
