//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser;

import java.util.List;

import BritefuryJ.Util.RichString.RichStringAccessor;

/*
 * Optional
 * 
 * Optional:node( input )			->  result = Optional.subexp:node( input ); result.isValid()  ?  result  :  null_result
 * Optional:string( input, start )		->  result = Optional.subexp:string( input, start ); result.isValid()  ?  result  :  null_result
 * Optional:richStr( input, start )		->  result = Optional.subexp:richStr( input, start ); result.isValid()  ?  result  :  null_result
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

	protected ParseResult evaluateRichStringItems(ParserState state, RichStringAccessor input, int start)
	{
		ParseResult res = subexp.handleRichStringItems( state, input, start );
		
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

	
	
	public boolean isEquivalentTo(ParserExpression x)
	{
		if ( x instanceof Optional )
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
		return "Optional( " + subexp.toString() + " )";
	}
}
