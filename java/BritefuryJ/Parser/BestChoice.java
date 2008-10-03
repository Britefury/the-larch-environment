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
	public BestChoice(ParserExpression[] subexps)
	{
		super( subexps );
	}
	
	public BestChoice(Object[] subexps) throws ParserCoerceException
	{
		super( subexps );
	}
	
	public BestChoice(List<Object> subexps) throws ParserCoerceException
	{
		super( subexps );
	}
	
	
	protected ParseResult parse(ParserState state, String input, int start, int stop)
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
			return ParseResult.failure( maxErrorPos );
		}
	}
	
	

	public ParserExpression __xor__(ParserExpression x)
	{
		return new BestChoice( joinSubexp( x ) );
	}

	public ParserExpression __xor__(String x)
	{
		return new BestChoice( joinSubexp( coerce( x ) ) );
	}

	public ParserExpression __xor__(List<Object> x) throws ParserCoerceException
	{
		return new BestChoice( joinSubexp( coerce( x ) ) );
	}


	public String toString()
	{
		return "BestChoice( " + subexpsToString() + " )";
	}
}
