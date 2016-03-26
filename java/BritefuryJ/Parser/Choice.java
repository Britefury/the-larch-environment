//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser;

import java.util.List;

import BritefuryJ.Util.RichString.RichStringAccessor;

/*
 * Choice
 * 
 * Choice:node( input )		->  first_success_of( [ s:node( input )   for s in Choice.subexps ] )
 * Choice:string( input, start )	->  first_success_of( [ s:string( input, start )   for s in Choice.subexps ] )
 * Choice:richStr( input, start )	->  first_success_of( [ s:richStr( input, start )   for s in Choice.subexps ] )
 * Choice:list( input, start )	->  first_success_of( [ s:list( input, start )   for s in Choice.subexps ] )
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
		int maxErrorPos = start;
		
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

	protected ParseResult evaluateRichStringItems(ParserState state, RichStringAccessor input, int start)
	{
		int maxErrorPos = start;
		
		for (ParserExpression subexp: subexps)
		{
			ParseResult result = subexp.handleRichStringItems( state, input, start );
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
		int maxErrorPos = start;
		
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
