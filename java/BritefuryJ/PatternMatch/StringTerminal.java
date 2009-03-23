//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;

import java.util.List;

import org.python.core.PyString;
import org.python.core.PyUnicode;

abstract class StringTerminal extends MatchExpression
{
	protected abstract MatchResult matchString(MatchState state, String input);
	
	@SuppressWarnings("unchecked")
	protected MatchResult evaluateNode(MatchState state, Object input)
	{
		if ( input instanceof List )
		{
			List<Object> xs = (List<Object>)input;
			return evaluateList( state, xs, 0, xs.size() );
		}
		else if ( input instanceof String )
		{
			String s = (String)input;
			MatchResult res = matchString( state, s );
			if ( res.isValid()  &&  res.end == s.length() )
			{
				return new MatchResult( res.getValue(), 0, 1 );
			}
		}
		else if ( input instanceof PyString  ||  input instanceof PyUnicode )
		{
			String s = input.toString();
			MatchResult res = matchString( state, s );
			if ( res.isValid()  &&  res.end == s.length() )
			{
				return new MatchResult( res.getValue(), 0, 1 );
			}
		}
		

		return MatchResult.failure( 0 );
	}

	protected MatchResult evaluateList(MatchState state, List<Object> input, int start, int stop)
	{
		if ( stop > start )
		{
			Object x = input.get( start );
			if ( x instanceof String )
			{
				String s = (String)x;
				MatchResult res = matchString( state, s );
				if ( res.isValid()  &&  res.end == s.length() )
				{
					return new MatchResult( res.getValue(), start, start + 1 );
				}
			}
		}

		return MatchResult.failure( start );
	}
}
