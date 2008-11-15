//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;

import java.util.Arrays;
import java.util.List;

public class Forward extends MatchExpression
{
	protected MatchExpression subexp;
	
	
	public Forward()
	{
		this.subexp = null;
	}
	
	
	
	public MatchExpression setExpression(Object exp)
	{
		subexp = toMatchExpression( exp );
		return this;
	}
	
	public MatchExpression setExpression(MatchExpression exp)
	{
		subexp = exp;
		return this;
	}
	
	public MatchExpression getExpression()
	{
		return subexp;
	}
	

	
	protected MatchResult parseNode(MatchState state, Object input, int start, int stop)
	{
		return subexp.evaluateNode( state, input, start, stop );
	}



	public List<MatchExpression> getChildren()
	{
		MatchExpression[] children = { subexp };
		return Arrays.asList( children );
	}
	
	public boolean compareTo(MatchExpression x)
	{
		if ( x instanceof Forward )
		{
			Forward xf = (Forward)x;
			return subexp.compareTo( xf.subexp );
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "Forward( <" + subexp.getDebugName() + "> )";
	}
}
