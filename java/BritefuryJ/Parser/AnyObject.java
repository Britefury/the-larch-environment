//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser;

import java.util.List;

import BritefuryJ.DocModel.DMObject;
import BritefuryJ.Util.RichString.RichStringAccessor;

/*
 * AnyObject
 * 
 * AnyObject:node( input )			->  input instanceof DMObject  ?  input  :  fail
 * AnyObject:string( input, start )	->  fail
 * AnyObject:richStr( input, start )	->  item = input.consumeStructuralItem(); item instanceof DMObject  ?  item  :  fail
 * AnyObject:list( input, start )		->  input[start] instanceof DMObject  ?  input[start]  :  fail
 */
public class AnyObject extends ParserExpression
{
	public AnyObject()
	{
	}
	
	
	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		if ( input instanceof DMObject )
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
				if ( valueArray[0] instanceof DMObject )
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
			
			if ( x instanceof DMObject )
			{
				return new ParseResult( x, start, start + 1 );
			}
		}

		return ParseResult.failure( start );
	}



	public boolean isEquivalentTo(ParserExpression x)
	{
		if ( x instanceof AnyObject )
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
		return "AnyObject()";
	}
}
