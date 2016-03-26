//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser;

import java.util.List;

import BritefuryJ.Util.RichString.RichStringAccessor;

/*
 * AnyString
 * 
 * AnyString:node( input )			->  input instanceof RichString | String  ?  input  :  fail
 * AnyString:string( input, start )	->  fail
 * AnyString:richStr( input, start )	->  fail
 * AnyString:list( input, start )		->  input[start] instanceof RichString | String  ?  input[start]  :  fail
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

	protected ParseResult evaluateRichStringItems(ParserState state, RichStringAccessor input, int start)
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
