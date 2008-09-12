//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.List;

public class BestChoice extends BranchExpression
{
	public BestChoice(Object[] subexps) throws ParserCoerceException
	{
		super( subexps );
	}
	
	public BestChoice(List<Object> subexps) throws ParserCoerceException
	{
		super( subexps );
	}
	
	
	protected ParseResult evaluate(ParserState state, String input, int start, int stop)
	{
		ParseResult bestResult = null;
		int bestPos = -1;
		int maxErrorPos = start;
		
		for (ParserExpression subexp: subexps)
		{
			ParseResult result = subexp.evaluate(  state, input, start, stop );
			if ( result.isValid()  &&  result.end > bestPos )
			{
				bestResult = result;
				bestPos = result.end;
			}
			else
			{
				maxErrorPos = Math.max( maxErrorPos, result.end );
			}
		}
		
		if ( bestResult != null )
		{
			return bestResult;
		}
		else
		{
			return new ParseResult( maxErrorPos );
		}
	}
	
	

	public ParserExpression __xor__(ParserExpression x)
	{
		try
		{
			return new BestChoice( joinSubexp( x ) );
		}
		catch (ParserCoerceException e)
		{
			throw new RuntimeException();
		}
	}

	public ParserExpression __xor__(String x)
	{
		try
		{
			return new BestChoice( joinSubexp( coerce( x ) ) );
		}
		catch (ParserCoerceException e)
		{
			throw new RuntimeException();
		}
	}

	public ParserExpression __xor__(List<Object> x)
	{
		try
		{
			return new BestChoice( joinSubexp( coerce( x ) ) );
		}
		catch (ParserCoerceException e)
		{
			throw new RuntimeException();
		}
	}


	public String toString()
	{
		return "BestChoice( " + subexpsToString() + " )";
	}
}
