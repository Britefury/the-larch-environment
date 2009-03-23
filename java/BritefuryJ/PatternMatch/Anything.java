//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;

import java.util.List;

public class Anything extends MatchExpression
{
	public Anything()
	{
		super();
	}
	
	

	protected MatchResult evaluateNode(MatchState state, Object input)
	{
		return new MatchResult( input, 0, 1 );
	}


	protected MatchResult evaluateList(MatchState state, List<Object> input, int start, int stop)
	{
		if ( stop > start )
		{
			Object x = input.get( start );
			return new MatchResult( x, start, start + 1 );
		}
		else
		{
			return MatchResult.failure( start );
		}
	}
}
