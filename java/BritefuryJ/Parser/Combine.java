//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import BritefuryJ.Util.RichString.RichStringAccessor;

public class Combine extends BranchExpression
{
	public Combine(ParserExpression[] subexps)
	{
		super( subexps );
	}
	
	public Combine(Object[] subexps) throws ParserCoerceException
	{
		super( subexps );
	}
	
	

	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		return ParseResult.failure( 0 );
	}
	
	@SuppressWarnings("unchecked")
	protected ParseResult evaluateStringChars(ParserState state, String input, int start)
	{
		ArrayList<Object> values = new ArrayList<Object>();
		Map<String, Object> bindings = null;
		boolean bFinalValueIsString = true;
		
		int pos = start;
		for (int i = 0; i < subexps.length; i++)
		{
			ParseResult result = subexps[i].handleStringChars(  state, input, pos );
			pos = result.end;
			
			if ( !result.isValid() )
			{
				return ParseResult.failure( pos );
			}
			else
			{
				bindings = ParseResult.addBindings( bindings, result.bindings );

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
			StringBuilder value = new StringBuilder();
			for (Object v: values)
			{
				value.append( (String)v );
			}
			
			return new ParseResult( value.toString(), start, pos, bindings );
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

			return new ParseResult( value, start, pos, bindings );
		}
	}

	@SuppressWarnings("unchecked")
	protected ParseResult evaluateRichStringItems(ParserState state, RichStringAccessor input, int start)
	{
		ArrayList<Object> values = new ArrayList<Object>();
		Map<String, Object> bindings = null;
		boolean bFinalValueIsString = true;
		
		int pos = start;
		for (int i = 0; i < subexps.length; i++)
		{
			ParseResult result = subexps[i].handleRichStringItems(  state, input, pos );
			pos = result.end;
			
			if ( !result.isValid() )
			{
				return ParseResult.failure( pos );
			}
			else
			{
				bindings = ParseResult.addBindings( bindings, result.bindings );

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
			StringBuilder value = new StringBuilder();
			for (Object v: values)
			{
				value.append( (String)v );
			}
			
			return new ParseResult( value.toString(), start, pos, bindings );
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

			return new ParseResult( value, start, pos, bindings );
		}
	}

	@SuppressWarnings("unchecked")
	protected ParseResult evaluateListItems(ParserState state, List<Object> input, int start)
	{
		ArrayList<Object> values = new ArrayList<Object>();
		Map<String, Object> bindings = null;
		boolean bFinalValueIsString = true;
		
		int pos = start;
		for (int i = 0; i < subexps.length; i++)
		{
			ParseResult result = subexps[i].handleListItems(  state, input, pos );
			pos = result.end;
			
			if ( !result.isValid() )
			{
				return ParseResult.failure( pos );
			}
			else
			{
				bindings = ParseResult.addBindings( bindings, result.bindings );

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
			StringBuilder value = new StringBuilder();
			for (Object v: values)
			{
				value.append( (String)v );
			}
			
			return new ParseResult( value.toString(), start, pos, bindings );
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

			return new ParseResult( value, start, pos, bindings );
		}
	}

	
	
	
	public ParserExpression __sub__(ParserExpression x)
	{
		return new Combine( appendToSubexps( x ) );
	}

	public ParserExpression __sub__(Object x) throws ParserCoerceException
	{
		return new Combine( appendToSubexps( coerce( x ) ) );
	}


	public String toString()
	{
		return "Combine( " + subexpsToString() + " )";
	}
}
