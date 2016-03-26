//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser;

import java.util.List;

import BritefuryJ.Util.RichString.RichStringAccessor;



/*
 * PeekNot
 * 
 * PeekNot:node( input )			->  result = PeekNot.subexp:node( input ); result.isValid()  ?  fail  :  suppressed
 * PeekNot:string( input, start )		->  result = PeekNot.subexp:string( input, start ); result.isValid()  ?  fail  :  suppressed
 * PeekNot:richStr( input, start )		->  result = PeekNot.subexp:richStr( input, start ); result.isValid()  ?  fail  :  suppressed
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

	protected ParseResult evaluateRichStringItems(ParserState state, RichStringAccessor input, int start)
	{
		ParseResult res = subexp.handleRichStringItems( state, input, start );
		
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

	
	public boolean isEquivalentTo(ParserExpression x)
	{
		if ( x instanceof PeekNot )
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
		return "PeekNot( " + subexp.toString() + " )";
	}
}
