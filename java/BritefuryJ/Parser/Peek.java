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
 * Peek
 * 
 * Peek:node( input )			->  result = Peek.subexp:node( input ); result.isValid()  ?  suppressed  :  fail
 * Peek:string( input, start )	->  result = Peek.subexp:string( input, start ); result.isValid()  ?  suppressed  :  fail
 * Peek:stream( input, start )	->  result = Peek.subexp:stream( input, start ); result.isValid()  ?  suppressed  :  fail
 * Peek:list( input, start )		->  result = Peek.subexp:list( input, start ); result.isValid()  ?  suppressed  :  fail
 */
public class Peek extends UnaryBranchExpression
{
	public Peek(ParserExpression subexp)
	{
		super( subexp );
	}
	
	public Peek(Object subexp) throws ParserCoerceException
	{
		super( subexp );
	}
	

	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		ParseResult res = subexp.handleNode( state, input );
		
		if ( res.isValid() )
		{
			return ParseResult.suppressedNoValue( 0, 0 );
		}
		else
		{
			return ParseResult.failure( 0 );
		}
	}

	protected ParseResult evaluateStringChars(ParserState state, String input, int start)
	{
		ParseResult res = subexp.handleStringChars( state, input, start );
		
		if ( res.isValid() )
		{
			return ParseResult.suppressedNoValue( start, start );
		}
		else
		{
			return ParseResult.failure( start );
		}
	}

	protected ParseResult evaluateStreamItems(ParserState state, StreamValueAccessor input, int start)
	{
		ParseResult res = subexp.handleStreamItems( state, input, start );
		
		if ( res.isValid() )
		{
			return ParseResult.suppressedNoValue( start, start );
		}
		else
		{
			return ParseResult.failure( start );
		}
	}

	protected ParseResult evaluateListItems(ParserState state, List<Object> input, int start)
	{
		ParseResult res = subexp.handleListItems( state, input, start );
		
		if ( res.isValid() )
		{
			return ParseResult.suppressedNoValue( start, start );
		}
		else
		{
			return ParseResult.failure( start );
		}
	}


	
	public boolean isEquivalentTo(ParserExpression x)
	{
		if ( x instanceof Peek )
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
		return "Peek( " + subexp.toString() + " )";
	}
}
