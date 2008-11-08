//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.ArrayList;
import java.util.List;

public class Combine extends BranchExpression
{
	public Combine(ParserExpression[] subexps)
	{
		super( subexps );
	}
	
	public Combine(Object[] subexps)
	{
		super( subexps );
	}
	
	public Combine(List<Object> subexps)
	{
		super( subexps );
	}
	
	
	@SuppressWarnings("unchecked")
	protected ParseResult parseString(ParserState state, String input, int start, int stop)
	{
		ArrayList<Object> values = new ArrayList<Object>();
		boolean bFinalValueIsString = true;
		
		int pos = start;
		for (int i = 0; i < subexps.length; i++)
		{
			if ( pos > stop )
			{
				return ParseResult.failure( pos );
			}
			
			ParseResult result = subexps[i].evaluateString(  state, input, pos, stop );
			pos = result.end;
			
			if ( !result.isValid() )
			{
				return ParseResult.failure( pos );
			}
			else
			{
				if ( !result.isSuppressed() )
				{
					values.add( result.value );
					
					if ( !(result.value instanceof String) )
					{
						bFinalValueIsString = false;
					}
				}
			}
		}
		
		
		if ( bFinalValueIsString )
		{
			String value = "";
			for (Object v: values)
			{
				String s = (String)v;
				value += s;
			}
			
			return new ParseResult( value, start, pos );
		}
		else
		{
			ArrayList<Object> value = new ArrayList<Object>();
			
			for (Object v: values)
			{
				if ( v instanceof List )
				{
					List<Object> l = (List<Object>)v;
					value.addAll( l );
				}
				else
				{
					value.add( v );
				}
			}

			return new ParseResult( value, start, pos );
		}
	}
	
	
	@SuppressWarnings("unchecked")
	protected ParseResult parseNode(ParserState state, Object input, int start, int stop)
	{
		ArrayList<Object> values = new ArrayList<Object>();
		
		int pos = start;
		for (int i = 0; i < subexps.length; i++)
		{
			if ( pos > stop )
			{
				return ParseResult.failure( pos );
			}
			
			ParseResult result = subexps[i].evaluateNode(  state, input, pos, stop );
			pos = result.end;
			
			if ( !result.isValid() )
			{
				return ParseResult.failure( pos );
			}
			else
			{
				if ( !result.isSuppressed() )
				{
					values.add( result.value );
				}
			}
		}
		
		
		ArrayList<Object> value = new ArrayList<Object>();
		
		for (Object v: values)
		{
			if ( v instanceof List )
			{
				List<Object> l = (List<Object>)v;
				value.addAll( l );
			}
			else
			{
				value.add( v );
			}
		}

		return new ParseResult( value, start, pos );
	}
	
	

	public ParserExpression __sub__(ParserExpression x)
	{
		return new Combine( appendToSubexps( x ) );
	}

	public ParserExpression __sub__(Object x)
	{
		return new Combine( appendToSubexps( coerce( x ) ) );
	}


	public String toString()
	{
		return "Combine( " + subexpsToString() + " )";
	}

	
	protected boolean isSequence()
	{
		return true;
	}
}
