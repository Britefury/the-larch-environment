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
 * AnyNode
 * 
 * AnyNode:node( input )			->  input
 * AnyNode:string( input, start )		->  fail
 * AnyNode:stream( input, start )	->  item = input.consumeStructuralItem(); item != null  ?  item  :  fail
 * AnyNode:list( input, start )		->  input[start]
 */
public class AnyNode extends ParserExpression
{
	public AnyNode()
	{
	}
	
	
	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		return new ParseResult( input, 0, 1 );
	}

	protected ParseResult evaluateStringChars(ParserState state, String input, int start)
	{
		return ParseResult.failure( start );
	}

	protected ParseResult evaluateStreamItems(ParserState state, StreamValueAccessor input, int start)
	{
		if ( start < input.length() )
		{
			start = state.skipJunkChars( input, start );
			
			Object valueArray[] = input.matchStructuralNode( start );
			
			if ( valueArray != null )
			{
				return new ParseResult( valueArray[0], 0, 1 );
			}
		}
		
		return ParseResult.failure( start );
	}

	protected ParseResult evaluateListItems(ParserState state, List<Object> input, int start)
	{
		if ( start < input.size() )
		{
			return new ParseResult( input.get( start ), start, start + 1 );
		}

		return ParseResult.failure( start );
	}



	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof AnyNode )
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
		return "AnyNode()";
	}
}
