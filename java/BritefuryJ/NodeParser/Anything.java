//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.NodeParser;

import java.util.List;

public class Anything extends ParserExpression
{
	public Anything()
	{
		super();
	}
	
	

	@SuppressWarnings("unchecked")
	protected ParseResult parseNode(ParserState state, Object input, int start, int stop)
	{
		if ( input instanceof List )
		{
			List<Object> xs = (List<Object>)input;
			if ( stop > start )
			{
				Object x = xs.get( start );
				return new ParseResult( x, start, start + 1 );
			}
			else
			{
				return ParseResult.failure( start );
			}
		}
		

		return new ParseResult( input, start, stop );
	}
}
