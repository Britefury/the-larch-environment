//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.ArrayList;
import java.util.List;

public class Sequence extends BranchExpression
{
	public Sequence(ParserExpression[] subexps)
	{
		super( subexps );
	}
	
	public Sequence(Object[] subexps) throws ParserCoerceException
	{
		super( subexps );
	}
	
	public Sequence(List<Object> subexps) throws ParserCoerceException
	{
		super( subexps );
	}
	
	
	protected ParseResult parseString(ParserState state, String input, int start, int stop)
	{
		ArrayList<Object> value = new ArrayList<Object>();
		
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
					value.add( result.value );
				}
			}
		}
		
		return new ParseResult( value, start, pos );
	}

	
	
	public ParserExpression __add__(ParserExpression x)
	{
		return new Sequence( appendToSubexps( x ) );
	}

	public ParserExpression __add__(Object x) throws ParserCoerceException
	{
		return new Sequence( appendToSubexps( coerce( x ) ) );
	}


	public String toString()
	{
		return "Sequence( " + subexpsToString() + " )";
	}
}
