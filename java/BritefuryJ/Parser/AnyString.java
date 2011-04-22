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
 * AnyString
 * 
 * AnyString:node( input )			->  input instanceof Stream | String  ?  input  :  fail
 * AnyString:string( input, start )	->  fail
 * AnyString:stream( input, start )	->  fail
 * AnyString:list( input, start )		->  input[start] instanceof Stream | String  ?  input[start]  :  fail
 */
public class AnyString extends ParserExpression
{
	public AnyString()
	{
	}
	
	
	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		if ( input instanceof String )
		{
			return new ParseResult( input, 0, 1 );
		}
		else
		{
			return ParseResult.failure( 0 );
		}
	}

	protected ParseResult evaluateStringChars(ParserState state, String input, int start)
	{
		return ParseResult.failure( start );
	}

	protected ParseResult evaluateStreamItems(ParserState state, StreamValueAccessor input, int start)
	{
		if ( start < input.length() )
		{
			Object valueArray[] = input.matchStructuralNode( start );
			
			if ( valueArray != null )
			{
				if ( valueArray[0] instanceof String )
				{
					return new ParseResult( valueArray[0], 0, 1 );
				}
			}
		}
		
		return ParseResult.failure( start );
	}

	protected ParseResult evaluateListItems(ParserState state, List<Object> input, int start)
	{
		if ( start < input.size() )
		{
			Object x = input.get( start );
			
			if ( x instanceof String )
			{
				return new ParseResult( x, start, start + 1 );
			}
		}

		return ParseResult.failure( start );
	}



	public boolean isEquivalentTo(ParserExpression x)
	{
		if ( x instanceof AnyString )
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
		return "AnyString()";
	}
}
