//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.TreeParser;

import java.util.List;

public class Anything extends TreeParserExpression
{
	public Anything()
	{
		super();
	}
	
	

	protected TreeParseResult evaluateNode(TreeParserState state, Object input)
	{
		return new TreeParseResult( input, 0, 1 );
	}


	protected TreeParseResult evaluateList(TreeParserState state, List<Object> input, int start, int stop)
	{
		if ( stop > start )
		{
			Object x = input.get( start );
			return new TreeParseResult( x, start, start + 1 );
		}
		else
		{
			return TreeParseResult.failure( start );
		}
	}
}
