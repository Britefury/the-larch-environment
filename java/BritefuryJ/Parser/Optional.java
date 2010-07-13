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
 * Optional
 * 
 * Optional:node( input )			->  result = Optional.subexp:node( input ); result.isValid()  ?  result  :  null_result
 * Optional:string( input, start )		->  result = Optional.subexp:string( input, start ); result.isValid()  ?  result  :  null_result
 * Optional:stream( input, start )	->  result = Optional.subexp:stream( input, start ); result.isValid()  ?  result  :  null_result
 * Optional:list( input, start )		->  result = Optional.subexp:list( input, start ); result.isValid()  ?  result  :  null_result
 */
public class Optional extends UnaryBranchExpression
{
	public Optional(ParserExpression subexp)
	{
		super( subexp );
	}
	
	public Optional(Object subexp) throws ParserCoerceException
	{
		super( subexp );
	}
	

	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		ParseResult res = subexp.handleNode( state, input );
		
		if ( res.isValid() )
		{
			return res;
		}
		else
		{
			return new ParseResult( null, 0, 0 );
		}
	}

	protected ParseResult evaluateStringChars(ParserState state, String input, int start)
	{
		ParseResult res = subexp.handleStringChars( state, input, start );
		
		if ( res.isValid() )
		{
			return res;
		}
		else
		{
			return new ParseResult( null, start, start );
		}
	}

	protected ParseResult evaluateStreamItems(ParserState state, StreamValueAccessor input, int start)
	{
		ParseResult res = subexp.handleStreamItems( state, input, start );
		
		if ( res.isValid() )
		{
			return res;
		}
		else
		{
			return new ParseResult( null, start, start );
		}
	}

	protected ParseResult evaluateListItems(ParserState state, List<Object> input, int start)
	{
		ParseResult res = subexp.handleListItems( state, input, start );
		
		if ( res.isValid() )
		{
			return res;
		}
		else
		{
			return new ParseResult( null, start, start );
		}
	}

	
	
	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof Optional )
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
		return "Optional( " + subexp.toString() + " )";
	}
}
