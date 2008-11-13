//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;

import java.util.Arrays;
import java.util.List;

public abstract class UnaryBranchExpression extends MatchExpression
{
	protected MatchExpression subexp;
	
	
	public UnaryBranchExpression(MatchExpression subexp)
	{
		this.subexp = subexp;
	}
	
	public UnaryBranchExpression(Object subexp)
	{
		this( toParserExpression( subexp ) );
	}
	
	
	
	public MatchExpression getSubexpression()
	{
		return subexp;
	}
	

	public List<MatchExpression> getChildren()
	{
		MatchExpression[] children = { subexp };
		return Arrays.asList( children );
	}
	
	public boolean compareTo(MatchExpression x)
	{
		if ( x instanceof UnaryBranchExpression )
		{
			UnaryBranchExpression ux = (UnaryBranchExpression)x;
			return subexp.compareTo( ux.subexp );
		}
		else
		{
			return false;
		}
	}
	

	public String toString()
	{
		return "UnaryBranchExpression( " + subexp.toString() + " )";
	}
}
