//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;

import java.util.HashMap;
import java.util.Map;

import BritefuryJ.ParserHelpers.DebugNode;
import BritefuryJ.ParserHelpers.ParseResultInterface;

public class MatchResult implements ParseResultInterface
{
	protected static class NameAlreadyBoundException extends Exception
	{
		private static final long serialVersionUID = 1L;
	};
	
	
	protected Object value;
	protected int begin, end;
	protected boolean bSuppressed, bValid, bMerge;
	protected HashMap<String, Object> bindings;
	
	
	public MatchResult()
	{
		value = null;
		begin = end = 0;
		bSuppressed = false;
		bValid = false;
	}
	
	public MatchResult(Object value, int begin, int end)
	{
		this.value = value;
		this.begin = begin;
		this.end = end;
		bSuppressed = false;
		bValid = true;
	}
	
	public MatchResult(Object value, int begin, int end, HashMap<String, Object> bindings)
	{
		this.value = value;
		this.begin = begin;
		this.end = end;
		bSuppressed = false;
		bValid = true;
		this.bindings = bindings;
	}
	
	
	private MatchResult(int end)
	{
		this.value = null;
		this.begin = 0;
		this.end = end;
		bSuppressed = false;
		bValid = false;
	}

	private MatchResult(Object value, int begin, int end, boolean bSuppressed)
	{
		this.value = value;
		this.begin = begin;
		this.end = end;
		this.bSuppressed = bSuppressed;
		this.bValid = true;
	}
	
	private MatchResult(Object value, int begin, int end, boolean bSuppressed, boolean bValid)
	{
		this.value = value;
		this.begin = begin;
		this.end = end;
		this.bSuppressed = bSuppressed;
		this.bValid = bValid;
	}
	
	protected MatchResult(Object value, int begin, int end, boolean bSuppressed, boolean bValid, boolean bMerge, HashMap<String, Object> bindings)
	{
		this.value = value;
		this.begin = begin;
		this.end = end;
		this.bSuppressed = bSuppressed;
		this.bValid = bValid;
		this.bMerge = bMerge;
		this.bindings = bindings;
	}
	
	
	
	protected MatchResult actionValue(Object v, boolean bMergeUp)
	{
		return new MatchResult( v, begin, end, false, true, bMergeUp, bindings );
	}
	
	protected MatchResult withRange(int begin, int end)
	{
		return new MatchResult( value, begin, end, false, true, bMerge, bindings );
	}
	
	
	
	protected MatchResult suppressed()
	{
		return new MatchResult( value, begin, end, true, bValid, bMerge, bindings );
	}
	
	protected MatchResult peek()
	{
		return new MatchResult( null, begin, begin, true, true, bMerge, bindings );
	}
	
	
	protected MatchResult bind(String name, Object bindingValue, int start)
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
		
		return new MatchResult( value, begin, end, bSuppressed, bValid, bMerge, b );
	}
	
	
	protected MatchResult clearBindings()
	{
		if ( bindings == null )
		{
			return this;
		}
		else
		{
			return new MatchResult( value, begin, end, bSuppressed, bValid );
		}
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
	
	
	protected DebugMatchResult debug(DebugNode debugNode)
	{
		return new DebugMatchResult( value, begin, end, bSuppressed, bValid, bMerge, bindings, debugNode );
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
	
	public boolean isMergeable()
	{
		return bMerge;
	}
	
	public HashMap<String, Object> getBindings()
	{
		return bindings;
	}
	
	
	
	public static MatchResult failure(int end)
	{
		return new MatchResult( end );
	}
	
	public static MatchResult suppressedNoValue(int begin, int end)
	{
		return new MatchResult( null, begin, end, true );
	}
	
	public static MatchResult mergeableValue(Object value, int begin, int end, HashMap<String, Object> bindings)
	{
		return new MatchResult( value, begin, end, false, true, true, bindings );
	}
}
