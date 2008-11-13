//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;


public class Suppress extends UnaryBranchExpression
{
	public Suppress(String subexp)
	{
		super( subexp );
	}
	
	public Suppress(MatchExpression subexp)
	{
		super( subexp );
	}
	
	
	protected MatchResult parseNode(MatchState state, Object input, int start, int stop)
	{
		return subexp.evaluateNode( state, input, start, stop ).suppressed();
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
