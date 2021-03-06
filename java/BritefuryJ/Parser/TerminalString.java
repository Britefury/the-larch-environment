//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser;

import java.util.List;

import BritefuryJ.Util.RichString.RichString;
import BritefuryJ.Util.RichString.RichStringAccessor;

/*
	 * TerminalString
	 * 
	 * TerminalString:node( input )				->  TerminalString.matchNode( input, 0 )
	 * TerminalString:string( input, start )		->  TerminalString.consumeString( input, start )
	 * TerminalString:richStr( input, start )		->  structural = input[start].matchStructural(); structural != null  ?  TerminalString.matchNode( structural )  :  TerminalString.consumeRichString( input, start )
	 * TerminalString:list( input, start )			->  TerminalString.matchNode( input[start], start )
	 * 
	 * TerminalString.matchNode(input, start)		->	input instanceof String  ->  res = TerminalString.consumeString( input, 0 ); res.end == input.length()  ?  success  :  fail 
	 * 										input instanceof RichString  ->  res = TerminalString.consumeRichString( input, 0 ); res.end == input.length()  ?  success  :  fail
	 * TerminalString.consumeString(input, start)	->	defined in subclass
	 * TerminalString.consumeRichString(input, start) ->	defined in subclass
 */
public abstract class TerminalString extends ParserExpression
{
	protected abstract ParseResult consumeString(String input, int start);
	protected abstract ParseResult consumeRichString(RichStringAccessor input, int start);
	
	
	private ParseResult matchNode(Object input, int start)
	{
		if ( input instanceof String )
		{
			String s = (String)input;
			
			ParseResult res = consumeString( s, 0 );
			if ( res.getEnd() == s.length() )
			{
				return res.withRange( start, start + 1 );
			}
		}
		else if ( input instanceof RichString )
		{
			RichString s = (RichString)input;
			RichStringAccessor accessor = s.accessor();
			
			ParseResult res = consumeRichString( accessor, 0 );
			if ( res.getEnd() == s.length() )
			{
				return res.withRange( start, start + 1 );
			}
		}
		
		return ParseResult.failure( start );
	}
	
	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		return matchNode( input, 0 );
	}

	protected ParseResult evaluateStringChars(ParserState state, String input, int start)
	{
		start = state.skipJunkChars( input, start );
		
		if ( start < input.length() )
		{
			return consumeString( input, start );
		}

		return ParseResult.failure( start );
	}
	
	protected ParseResult evaluateRichStringItems(ParserState state, RichStringAccessor input, int start)
	{
		start = state.skipJunkChars( input, start );
		
		if ( start < input.length() )
		{
			Object structural[] = input.matchStructuralNode( start );
			if ( structural != null )
			{
				return matchNode( structural[0], start );
			}
			else
			{
				return consumeRichString( input, start );
			}
		}
		
		return ParseResult.failure( start );
	}

	protected ParseResult evaluateListItems(ParserState state, List<Object> input, int start)
	{
		if ( start < input.size() )
		{
			return matchNode( input.get( start ), start );
		}
		return ParseResult.failure( 0 );
	}
}
