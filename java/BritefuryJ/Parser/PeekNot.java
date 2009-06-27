//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.List;

import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;



/*
 * PeekNot
 * 
 * PeekNot:node( input )			->  result = PeekNot.subexp:node( input ); result.isValid()  ?  fail  :  suppressed
 * PeekNot:string( input, start )		->  result = PeekNot.subexp:string( input, start ); result.isValid()  ?  fail  :  suppressed
 * PeekNot:stream( input, start )	->  result = PeekNot.subexp:stream( input, start ); result.isValid()  ?  fail  :  suppressed
 * PeekNot:list( input, start )		->  result = PeekNot.subexp:list( input, start ); result.isValid()  ?  fail  :  suppressed
 */
public class PeekNot extends UnaryBranchExpression
{
	public PeekNot(ParserExpression subexp)
	{
		super( subexp );
	}
	
	public PeekNot(Object subexp) throws ParserCoerceException
	{
		super( subexp );
	}
	

	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		ParseResult res = subexp.handleNode( state, input );
		
		if ( res.isValid() )
		{
			return ParseResult.failure( 0 );
		}
		else
		{
			return ParseResult.suppressedNoValue( 0, 0 );
		}
	}

	protected ParseResult evaluateStringChars(ParserState state, String input, int start)
	{
		ParseResult res = subexp.handleStringChars( state, input, start );
		
		if ( res.isValid() )
		{
			return ParseResult.failure( start );
		}
		else
		{
			return ParseResult.suppressedNoValue( start, start );
		}
	}

	protected ParseResult evaluateStreamItems(ParserState state, ItemStreamAccessor input, int start)
	{
		ParseResult res = subexp.handleStreamItems( state, input, start );
		
		if ( res.isValid() )
		{
			return ParseResult.failure( start );
		}
		else
		{
			return ParseResult.suppressedNoValue( start, start );
		}
	}

	protected ParseResult evaluateListItems(ParserState state, List<Object> input, int start)
	{
		ParseResult res = subexp.handleListItems( state, input, start );
		
		if ( res.isValid() )
		{
			return ParseResult.failure( start );
		}
		else
		{
			return ParseResult.suppressedNoValue( start, start );
		}
	}

	
	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof PeekNot )
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
		return "PeekNot( " + subexp.toString() + " )";
	}
}
