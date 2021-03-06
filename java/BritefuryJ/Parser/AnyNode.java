//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser;

import java.util.List;

import BritefuryJ.Util.RichString.RichStringAccessor;

/*
 * AnyNode
 * 
 * AnyNode:node( input )			->  input
 * AnyNode:string( input, start )		->  fail
 * AnyNode:richStr( input, start )	->  item = input.consumeStructuralItem(); item != null  ?  item  :  fail
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

	protected ParseResult evaluateRichStringItems(ParserState state, RichStringAccessor input, int start)
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



	public boolean isEquivalentTo(ParserExpression x)
	{
		if ( x instanceof AnyNode )
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
		return "AnyNode()";
	}
}
