//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.List;

import BritefuryJ.DocPresent.StreamValue.StreamValueAccessor;

/*
 * Choice
 * 
 * Choice:node( input )		->  first_success_of( [ s:node( input )   for s in Bind.subexps ] )
 * Choice:string( input, start )	->  first_success_of( [ s:string( input, start )   for s in Bind.subexps ] )
 * Choice:stream( input, start )	->  first_success_of( [ s:stream( input, start )   for s in Bind.subexps ] )
 * Choice:list( input, start )	->  first_success_of( [ s:list( input, start )   for s in Bind.subexps ] )
 */
public class Choice extends BranchExpression
{
	public Choice(ParserExpression[] subexps)
	{
		super( subexps );
	}
	
	public Choice(Object[] subexps) throws ParserCoerceException
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

	protected ParseResult evaluateStreamItems(ParserState state, StreamValueAccessor input, int start)
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

	public ParserExpression __or__(Object x) throws ParserCoerceException
	{
		return new Choice( appendToSubexps( coerce( x ) ) );
	}


	public boolean isEquivalentTo(ParserExpression x)
	{
		if ( x instanceof Choice )
		{
			return super.isEquivalentTo( x );
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
