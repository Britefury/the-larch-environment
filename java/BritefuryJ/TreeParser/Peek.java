//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.TreeParser;

import java.util.List;


public class Peek extends UnaryBranchExpression
{
	public Peek(Object subexp)
	{
		super( subexp );
	}
	
	public Peek(TreeParserExpression subexp)
	{
		super( subexp );
	}
	

	protected TreeParseResult evaluateNode(TreeParserState state, Object input)
	{
		return TreeParseResult.failure( 0 );
	}

	protected TreeParseResult evaluateList(TreeParserState state, List<Object> input, int start, int stop)
	{
		TreeParseResult res = subexp.processList( state, input, start, stop );
		
		if ( res.isValid() )
		{
			return res.peek();
		}
		else
		{
			return TreeParseResult.failure( start );
		}
	}


	public boolean compareTo(TreeParserExpression x)
	{
		if ( x instanceof Peek )
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
		return "Peek( " + subexp.toString() + " )";
	}
}
