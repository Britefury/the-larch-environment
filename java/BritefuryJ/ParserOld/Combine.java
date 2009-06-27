//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ParserOld;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;

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
	
	public Combine(List<Object> subexps) throws ParserCoerceException
	{
		super( subexps );
	}
	
	
	@SuppressWarnings("unchecked")
	protected ParseResult parseStream(ParserState state, ItemStreamAccessor input, int start)
	{
		ArrayList<Object> values = new ArrayList<Object>();
		boolean bFinalValueIsString = true;
		
		int pos = start;
		for (int i = 0; i < subexps.length; i++)
		{
			ParseResult result = subexps[i].evaluateStream(  state, input, pos );
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
