//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;

import java.util.List;


public class Peek extends UnaryBranchExpression
{
	public Peek(Object subexp)
	{
		super( subexp );
	}
	
	public Peek(MatchExpression subexp)
	{
		super( subexp );
	}
	

	protected MatchResult evaluateNode(MatchState state, Object input)
	{
		return MatchResult.failure( 0 );
	}

	protected MatchResult evaluateList(MatchState state, List<Object> input, int start, int stop)
	{
		MatchResult res = subexp.processList( state, input, start, stop );
		
		if ( res.isValid() )
		{
			return res.peek();
		}
		else
		{
			return MatchResult.failure( start );
		}
	}


	public boolean compareTo(MatchExpression x)
	{
		if ( x instanceof Peek )
		{
			return super.compareTo( x );
		}
		else
		{
			return false;
		}
	}
	

	public String toString()
	{
		return "Peek( " + subexp.toString() + " )";
	}
}
