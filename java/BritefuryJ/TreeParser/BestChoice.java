//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.TreeParser;

import java.util.List;

public class BestChoice extends BranchExpression
{
	public BestChoice(TreeParserExpression[] subexps)
	{
		super( subexps );
	}
	
	public BestChoice(Object[] subexps)
	{
		super( subexps );
	}
	
	public BestChoice(List<Object> subexps)
	{
		super( subexps );
	}
	
	
	protected TreeParseResult evaluateNode(TreeParserState state, Object input)
	{
		TreeParseResult bestResult = null;
		int bestPos = -1;
		int maxErrorPos = 0;
		
		for (TreeParserExpression subexp: subexps)
		{
			TreeParseResult result = subexp.processNode( state, input );
			if ( result.isValid()  &&  result.end > bestPos )
			{
				bestResult = result;
				bestPos = result.end;
			}
			else
			{
				maxErrorPos = Math.max( maxErrorPos, result.end );
			}
		}
		
		if ( bestResult != null )
		{
			return bestResult;
		}
		else
		{
			return TreeParseResult.failure( maxErrorPos );
		}
	}
	
	protected TreeParseResult evaluateList(TreeParserState state, List<Object> input, int start, int stop)
	{
		TreeParseResult bestResult = null;
		int bestPos = -1;
		int maxErrorPos = start;
		
		for (TreeParserExpression subexp: subexps)
		{
			TreeParseResult result = subexp.processList( state, input, start, stop );
			if ( result.isValid()  &&  result.end > bestPos )
			{
				bestResult = result;
				bestPos = result.end;
			}
			else
			{
				maxErrorPos = Math.max( maxErrorPos, result.end );
			}
		}
		
		if ( bestResult != null )
		{
			return bestResult;
		}
		else
		{
			return TreeParseResult.failure( maxErrorPos );
		}
	}
	

	public TreeParserExpression __xor__(TreeParserExpression x)
	{
		return new BestChoice( appendToSubexps( x ) );
	}

	public TreeParserExpression __xor__(Object x)
	{
		return new BestChoice( appendToSubexps( coerce( x ) ) );
	}


	public String toString()
	{
		return "BestChoice( " + subexpsToString() + " )";
	}
}
