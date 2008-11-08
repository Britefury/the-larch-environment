//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.List;

public abstract class Terminal extends ParserExpression
{
	@SuppressWarnings("unchecked")
	protected ParseResult parseNode(ParserState state, Object input, int start, int stop)
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
					ParseResult res = parseString( state, s, 0, s.length() );
					if ( res.isValid() )
					{
						return new ParseResult( res.getValue(), start, start + 1 );
					}
				}
			}
		}
		

		return ParseResult.failure( start );
	}


	protected ParseResult parseRootNode(ParserState state, Object input, int start, int stop)
	{
		if ( input instanceof String )
		{
			String s = (String)input;
			return parseString( state, s, start, stop );
		}

	
		return ParseResult.failure( start );
	}
}
