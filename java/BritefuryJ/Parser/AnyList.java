//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.List;

import BritefuryJ.Util.RichString.RichStringAccessor;

/*
 * AnyList
 * 
 * AnyList:node( input )		->  input instanceof List  ?  input  :  fail
 * AnyList:string( input, start )	->  fail
 * AnyList:richStr( input, start )	->  item = input.consumeStructuralItem(); item instanceof List  ?  item  :  fail
 * AnyList:list( input, start )	->  input[start] instanceof List  ?  input[start]  :  fail
 */
public class AnyList extends ParserExpression
{
	public AnyList()
	{
	}
	
	
	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		if ( input instanceof List<?> )
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

	protected ParseResult evaluateRichStringItems(ParserState state, RichStringAccessor input, int start)
	{
		if ( start < input.length() )
		{
			Object valueArray[] = input.matchStructuralNode( start );
			
			if ( valueArray != null )
			{
				if ( valueArray[0] instanceof List<?> )
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
			
			if ( x instanceof List<?> )
			{
				return new ParseResult( x, start, start + 1 );
			}
		}

		return ParseResult.failure( start );
	}



	public boolean isEquivalentTo(ParserExpression x)
	{
		if ( x instanceof AnyList )
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
		return "AnyList()";
	}
}
