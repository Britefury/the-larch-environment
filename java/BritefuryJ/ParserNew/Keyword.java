//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ParserNew;

import java.util.List;
import java.util.regex.Pattern;

import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;

/*
 * Keyword
 * 
 * Keyword:node( input )			->  input == Keyword.keywordString  ?  input  :  fail
 * Keyword:string( input, start )		->  input[start:start+Keyword.keywordString.length()] == Keyword.keywordString  &&
 * 									input[start+Keyword.keywordString.length()] not in Keyword.disallowedSubsequentChars  ?  input[start:start+Keyword.keywordString.length()] : fail
 * Keyword:stream( input, start )	->  input[start:start+Keyword.keywordString.length()] == Keyword.keywordString  &&
 * 								input[start+Keyword.keywordString.length()] not in Keyword.disallowedSubsequentChars  ?  input[start:start+Keyword.keywordString.length()] : fail
 * Keyword:list( input, start )		->  input[start] == Keyword.keywordString  ?  input[start]  :  fail
 */
public class Keyword extends ParserExpression
{
	protected String keywordString, disallowedSubsequentChars;
	private Pattern postPattern;
	
	
	public Keyword(String keywordString)
	{
		this( keywordString, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_" );
	}
	
	public Keyword(String keywordString, String disallowedSubsequentChars)
	{
		this.keywordString = keywordString;
		this.disallowedSubsequentChars = disallowedSubsequentChars;
		postPattern = Pattern.compile( "[" + Pattern.quote( disallowedSubsequentChars ) + "]*" );
	}
	
	
	public String getKeywordString()
	{
		return keywordString;
	}
	
	public String getDisallowedSubsequentChars()
	{
		return disallowedSubsequentChars;
	}
	
	
	
	private ParseResult matchNode(Object input, int start)
	{
		if ( input instanceof String )
		{
			if ( input.equals( keywordString ) )
			{
				return new ParseResult( input, start, start + 1 );
			}
		}
		else if ( input instanceof ItemStreamAccessor )
		{
			ItemStreamAccessor stream = (ItemStreamAccessor)input;
			if ( stream.consumeString( start, keywordString ) == stream.length() )
			{
				return new ParseResult( keywordString, start, start + 1 );
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
		
		int end = start + keywordString.length();
		
		if ( end <= input.length() )
		{
			if ( input.subSequence( start, end ).equals( keywordString ) )
			{
				if ( end == input.length()  ||  !postPattern.matcher( input.subSequence( end, end + 1 ) ).matches() )
				{
					return new ParseResult( keywordString, start, start + end );
				}
			}
		}
		
		return ParseResult.failure( start );
	}

	protected ParseResult evaluateStreamItems(ParserState state, ItemStreamAccessor input, int start)
	{
		start = state.skipJunkChars( input, start );
		
		CharSequence itemText = input.getItemTextFrom( start );
		int end = keywordString.length();
		
		if ( end <= itemText.length() )
		{
			if ( itemText.subSequence( 0, end ).equals( keywordString ) )
			{
				if ( end == itemText.length()  ||  !postPattern.matcher( itemText.subSequence( end, end + 1 ) ).matches() )
				{
					return new ParseResult( keywordString, start, start + end );
				}
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

	
	
	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof Keyword )
		{
			Keyword xk = (Keyword)x;
			return keywordString.equals( xk.keywordString )  &&  disallowedSubsequentChars.equals(  xk.disallowedSubsequentChars );
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "Keyword( \"" + keywordString + "\", \"" + disallowedSubsequentChars + "\" )";
	}
}
