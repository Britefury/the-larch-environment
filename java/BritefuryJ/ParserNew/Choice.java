//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ParserNew;

import java.util.List;

import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;

public class Choice extends BranchExpression
{
	public Choice(ParserExpression[] subexps)
	{
		super( subexps );
	}
	
	
	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		int maxErrorPos = 0;
		
		for (ParserExpression subexp: subexps)
		{
			ParseResult result = subexp.handleNode( state, input );
			if ( result.isValid() )
			{
				return result;
			}
			else
			{
				maxErrorPos = Math.max( maxErrorPos, result.end );
			}
		}
		
		return ParseResult.failure( maxErrorPos );
	}
	
	protected ParseResult evaluateStringChars(ParserState state, String input, int start)
	{
		int maxErrorPos = 0;
		
		for (ParserExpression subexp: subexps)
		{
			ParseResult result = subexp.handleStringChars( state, input, start );
			if ( result.isValid() )
			{
				return result;
			}
			else
			{
				maxErrorPos = Math.max( maxErrorPos, result.end );
			}
		}
		
		return ParseResult.failure( maxErrorPos );
	}

	protected ParseResult evaluateStreamItems(ParserState state, ItemStreamAccessor input, int start)
	{
		int maxErrorPos = 0;
		
		for (ParserExpression subexp: subexps)
		{
			ParseResult result = subexp.handleStreamItems( state, input, start );
			if ( result.isValid() )
			{
				return result;
			}
			else
			{
				maxErrorPos = Math.max( maxErrorPos, result.end );
			}
		}
		
		return ParseResult.failure( maxErrorPos );
	}

	protected ParseResult evaluateListItems(ParserState state, List<Object> input, int start)
	{
		int maxErrorPos = 0;
		
		for (ParserExpression subexp: subexps)
		{
			ParseResult result = subexp.handleListItems( state, input, start );
			if ( result.isValid() )
			{
				return result;
			}
			else
			{
				maxErrorPos = Math.max( maxErrorPos, result.end );
			}
		}
		
		return ParseResult.failure( maxErrorPos );
	}
	
	

	public ParserExpression __or__(ParserExpression x)
	{
		return new Choice( appendToSubexps( x ) );
	}


	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof Choice )
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
		return "Choice( " + subexpsToString() + " )";
	}
}
