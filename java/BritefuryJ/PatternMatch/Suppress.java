//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;

import java.util.List;


public class Suppress extends UnaryBranchExpression
{
	public Suppress(Object subexp)
	{
		super( subexp );
	}
	
	public Suppress(MatchExpression subexp)
	{
		super( subexp );
	}
	
	
	protected MatchResult evaluateNode(MatchState state, Object input)
	{
		return MatchResult.failure( 0 );
	}

	protected MatchResult evaluateList(MatchState state, List<Object> input, int start, int stop)
	{
		return subexp.processList( state, input, start, stop ).suppressed();
	}



	public boolean compareTo(MatchExpression x)
	{
		if ( x instanceof Suppress )
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
		return "Suppress( " + subexp.toString() + " )";
	}
}
