//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;

import java.util.List;

public class BestChoice extends BranchExpression
{
	public BestChoice(MatchExpression[] subexps)
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
	
	
	protected MatchResult parseNode(MatchState state, Object input, int start, int stop)
	{
		MatchResult bestResult = null;
		int bestPos = -1;
		int maxErrorPos = start;
		
		for (MatchExpression subexp: subexps)
		{
			MatchResult result = subexp.evaluateNode(  state, input, start, stop );
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
			return MatchResult.failure( maxErrorPos );
		}
	}
	

	public MatchExpression __xor__(MatchExpression x)
	{
		return new BestChoice( appendToSubexps( x ) );
	}

	public MatchExpression __xor__(Object x)
	{
		return new BestChoice( appendToSubexps( toParserExpression( x ) ) );
	}


	public String toString()
	{
		return "BestChoice( " + subexpsToString() + " )";
	}
}
