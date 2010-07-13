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
 * LiteralValue
 * 
 * LiteralValue:node( input )			->  input == LiteralValue.matchValue  ?  input  :  fail
 * LiteralValue:string( input, start )		->  fail
 * LiteralValue:stream( input, start )		->  item = input.structuralItem(); item == LiteralValue.matchValue  ?  item  :  fail
 * LiteralValue:list( input, start )			->  input[start] == LiteralValue.matchValue  ?  input[start]  :  fail
 */
public class LiteralNode extends ParserExpression
{
	protected Object matchValue;
	
	
	public LiteralNode(Object matchValue)
	{
		this.matchValue = matchValue;
	}
	
	
	public Object getMatchValue()
	{
		return matchValue;
	}
	
	
	
	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		if ( input == matchValue  ||  input.equals( matchValue ) )
		{
			return new ParseResult( input, 0, 1 );
		}
		
		return ParseResult.failure( 0 );
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
				if ( valueArray[0] == matchValue  ||  valueArray[0].equals( matchValue ) )
				{
					return new ParseResult( valueArray[0], start, start + 1 );
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
			
			if ( x == matchValue  ||  x.equals( matchValue ) )
			{
				return new ParseResult( x, start, start + 1 );
			}
		}

		return ParseResult.failure( start );
	}
	
	
	
	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof LiteralNode )
		{
			LiteralNode xl = (LiteralNode)x;
			return matchValue.equals( xl.matchValue );
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "LiteralValue( " + matchValue.toString() + " )";
	}
}
