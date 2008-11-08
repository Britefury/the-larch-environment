//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.List;

public class Choice extends BranchExpression
{
	public Choice(ParserExpression[] subexps)
	{
		super( subexps );
	}
	
	public Choice(Object[] subexps)
	{
		super( subexps );
	}
	
	public Choice(List<Object> subexps)
	{
		super( subexps );
	}
	
	
	protected ParseResult parse(ParserState state, Object input, int start, int stop) throws ParserIncompatibleDataTypeException
	{
		int maxErrorPos = start;
		
		for (ParserExpression subexp: subexps)
		{
			ParseResult result = subexp.evaluate(  state, input, start, stop );
			if ( result.isValid() )
			{
				return result;
			}
			else
			{
				maxErrorPos = Math.max( maxErrorPos, result.end );
			}
		}
		
		return ParseResult.failure( maxErrorPos );
	}
	
	

	public ParserExpression __or__(ParserExpression x)
	{
		return new Choice( appendToSubexps( x ) );
	}

	public ParserExpression __or__(Object x)
	{
		return new Choice( appendToSubexps( coerce( x ) ) );
	}


	public String toString()
	{
		return "Choice( " + subexpsToString() + " )";
	}
}
