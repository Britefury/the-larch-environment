//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;

import java.util.List;

abstract class StringTerminal extends MatchExpression
{
	protected abstract MatchResult parseString(MatchState state, String input);
	
	@SuppressWarnings("unchecked")
	protected MatchResult parseNode(MatchState state, Object input, int start, int stop)
	{
		if ( input instanceof List )
		{
			List<Object> xs = (List<Object>)input;
			if ( stop > start )
			{
				Object x = xs.get( start );
				if ( x instanceof String )
				{
					String s = (String)x;
					MatchResult res = parseString( state, s );
					if ( res.isValid()  &&  res.end == s.length() )
					{
						return new MatchResult( res.getValue(), start, start + 1 );
					}
				}
			}
		}
		else if ( input instanceof String )
		{
			String s = (String)input;
			MatchResult res = parseString( state, s );
			if ( res.isValid()  &&  res.end == s.length() )
			{
				return new MatchResult( res.getValue(), start, start + 1 );
			}
		}
		

		return MatchResult.failure( start );
	}
}
