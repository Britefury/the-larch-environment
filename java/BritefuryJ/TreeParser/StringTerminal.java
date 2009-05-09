//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.TreeParser;

import java.util.List;

import org.python.core.PyString;
import org.python.core.PyUnicode;

abstract class StringTerminal extends TreeParserExpression
{
	protected abstract TreeParseResult matchString(TreeParserState state, String input);
	
	@SuppressWarnings("unchecked")
	protected TreeParseResult evaluateNode(TreeParserState state, Object input)
	{
		if ( input instanceof List )
		{
			List<Object> xs = (List<Object>)input;
			return evaluateList( state, xs, 0, xs.size() );
		}
		else if ( input instanceof String )
		{
			String s = (String)input;
			TreeParseResult res = matchString( state, s );
			if ( res.isValid()  &&  res.end == s.length() )
			{
				return new TreeParseResult( res.getValue(), 0, 1 );
			}
		}
		else if ( input instanceof PyString  ||  input instanceof PyUnicode )
		{
			String s = input.toString();
			TreeParseResult res = matchString( state, s );
			if ( res.isValid()  &&  res.end == s.length() )
			{
				return new TreeParseResult( res.getValue(), 0, 1 );
			}
		}
		

		return TreeParseResult.failure( 0 );
	}

	protected TreeParseResult evaluateList(TreeParserState state, List<Object> input, int start, int stop)
	{
		if ( stop > start )
		{
			Object x = input.get( start );
			if ( x instanceof String )
			{
				String s = (String)x;
				TreeParseResult res = matchString( state, s );
				if ( res.isValid()  &&  res.end == s.length() )
				{
					return new TreeParseResult( res.getValue(), start, start + 1 );
				}
			}
		}

		return TreeParseResult.failure( start );
	}
}
