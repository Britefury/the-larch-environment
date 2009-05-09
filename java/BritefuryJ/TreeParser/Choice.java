//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.TreeParser;

import java.util.List;

public class Choice extends BranchExpression
{
	public Choice(TreeParserExpression[] subexps)
	{
		super( subexps );
	}
	
	public Choice(Object[] subexps)
	{
		super( subexps );
	}
	
	public Choice(List<Object> subexps)
	{
		super( subexps );
	}
	
	
	protected TreeParseResult evaluateNode(TreeParserState state, Object input)
	{
		int maxErrorPos = 0;
		
		for (TreeParserExpression subexp: subexps)
		{
			TreeParseResult result = subexp.processNode( state, input );
			if ( result.isValid() )
			{
				return result;
			}
			else
			{
				maxErrorPos = Math.max( maxErrorPos, result.end );
			}
		}
		
		return TreeParseResult.failure( maxErrorPos );
	}
	
	protected TreeParseResult evaluateList(TreeParserState state, List<Object> input, int start, int stop)
	{
		int maxErrorPos = start;
		
		for (TreeParserExpression subexp: subexps)
		{
			TreeParseResult result = subexp.processList( state, input, start, stop );
			if ( result.isValid() )
			{
				return result;
			}
			else
			{
				maxErrorPos = Math.max( maxErrorPos, result.end );
			}
		}
		
		return TreeParseResult.failure( maxErrorPos );
	}
	
	

	public TreeParserExpression __or__(TreeParserExpression x)
	{
		return new Choice( appendToSubexps( x ) );
	}

	public TreeParserExpression __or__(Object x)
	{
		return new Choice( appendToSubexps( coerce( x ) ) );
	}


	public String toString()
	{
		return "Choice( " + subexpsToString() + " )";
	}
}
