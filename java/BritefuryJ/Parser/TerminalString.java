//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.List;

import BritefuryJ.DocPresent.StreamValue.StreamValue;
import BritefuryJ.DocPresent.StreamValue.StreamValueAccessor;

/*
	 * TerminalString
	 * 
	 * TerminalString:node( input )				->  TerminalString.matchNode( input, 0 )
	 * TerminalString:string( input, start )		->  TerminalString.consumeString( input, start )
	 * TerminalString:stream( input, start )		->  structural = input[start].matchStructural(); structural != null  ?  TerminalString.matchNode( structural )  :  TerminalString.consumeStream( input, start )
	 * TerminalString:list( input, start )			->  TerminalString.matchNode( input[start], start )
	 * 
	 * TerminalString.matchNode(input, start)		->	input instanceof String  ->  res = TerminalString.consumeString( input, 0 ); res.end == input.length()  ?  success  :  fail 
	 * 										input instanceof Stream  ->  res = TerminalString.consumeStream( input, 0 ); res.end == input.length()  ?  success  :  fail
	 * TerminalString.consumeString(input, start)	->	defined in subclass
	 * TerminalString.consumeStream(input, start)	->	defined in subclass
 */
public abstract class TerminalString extends ParserExpression
{
	protected abstract ParseResult consumeString(String input, int start);
	protected abstract ParseResult consumeStream(StreamValueAccessor input, int start);
	
	
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
		else if ( input instanceof StreamValue )
		{
			StreamValue s = (StreamValue)input;
			StreamValueAccessor accessor = s.accessor();
			
			ParseResult res = consumeStream( accessor, 0 );
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
	
	protected ParseResult evaluateStreamItems(ParserState state, StreamValueAccessor input, int start)
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
				return consumeStream( input, start );
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
