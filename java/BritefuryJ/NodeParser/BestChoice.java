//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.NodeParser;

import java.util.List;

public class BestChoice extends BranchExpression
{
	public BestChoice(ParserExpression[] subexps)
	{
		super( subexps );
	}
	
	public BestChoice(Object[] subexps)
	{
		super( subexps );
	}
	
	public BestChoice(List<Object> subexps)
	{
		super( subexps );
	}
	
	
	protected ParseResult parseNode(ParserState state, Object input, int start, int stop)
	{
		ParseResult bestResult = null;
		int bestPos = -1;
		int maxErrorPos = start;
		
		for (ParserExpression subexp: subexps)
		{
			ParseResult result = subexp.evaluateNode(  state, input, start, stop );
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
		return new BestChoice( appendToSubexps( x ) );
	}

	public ParserExpression __xor__(Object x)
	{
		return new BestChoice( appendToSubexps( coerce( x ) ) );
	}


	public String toString()
	{
		return "BestChoice( " + subexpsToString() + " )";
	}
}
