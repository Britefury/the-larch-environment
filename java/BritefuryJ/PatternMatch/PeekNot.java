//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;


public class PeekNot extends UnaryBranchExpression
{
	public PeekNot(String subexp)
	{
		super( subexp );
	}
	
	public PeekNot(MatchExpression subexp)
	{
		super( subexp );
	}
	

	protected MatchResult parseNode(MatcherState state, Object input, int start, int stop)
	{
		MatchResult res = subexp.evaluateNode( state, input, start, stop );
		
		if ( res.isValid() )
		{
			return MatchResult.failure( start );
		}
		else
		{
			return MatchResult.suppressedNoValue( start, start );
		}
	}


	public boolean compareTo(MatchExpression x)
	{
		if ( x instanceof PeekNot )
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
		return "PeekNot( " + subexp.toString() + " )";
	}
}
