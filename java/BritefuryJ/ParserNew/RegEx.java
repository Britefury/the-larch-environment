//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ParserNew;

import java.util.List;
import java.util.regex.Matcher;
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
public class RegEx extends ParserExpression
{
	protected String re;
	protected int flags;
	protected boolean bSkipJunkChars;
	protected Pattern pattern;
	
	
	public RegEx(String re)
	{
		this( re, 0, true );
	}
	
	public RegEx(String re, int flags)
	{
		this( re, flags, true );
	}
	
	public RegEx(String re, boolean bSkipJunkChars)
	{
		this( re, 0, bSkipJunkChars );
	}
	
	public RegEx(String re, int flags, boolean bSkipJunkChars)
	{
		pattern = Pattern.compile( re, flags );
		this.re = re;
		this.flags = flags;
		this.bSkipJunkChars = bSkipJunkChars;
	}
	
	
	public String getRE()
	{
		return re;
	}
	
	public int getREFlags()
	{
		return flags;
	}
	
	public boolean getSkipJunkChars()
	{
		return bSkipJunkChars;
	}
	
	
	
	private ParseResult matchNode(Object input, int start)
	{
		if ( input instanceof String )
		{
			String s = (String)input;
			Matcher m = pattern.matcher( s );
			
			boolean bFound = m.find();
			if ( bFound  &&  m.start() == 0  &&  m.end() > 0 )
			{
				if ( m.group().length() == s.length() )
				{
					return new ParseResult( s, start, start + 1 );
				}
			}
		}
		else if ( input instanceof ItemStreamAccessor )
		{
			ItemStreamAccessor stream = (ItemStreamAccessor)input;
			String match = stream.matchRegEx( start, pattern );
			if ( match != null  &&  match.length() == stream.length() )
			{
				return new ParseResult( input, start, start + 1 );
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
		if ( bSkipJunkChars )
		{
			start = state.skipJunkChars( input, start );
		}
		

		Matcher m = pattern.matcher( input.subSequence( start, input.length() ) );
		
		boolean bFound = m.find();
		if ( bFound  &&  m.start() == 0  &&  m.end() > 0 )
		{
			String match = m.group();
			return new ParseResult( match, start, start + match.length() );
		}
		
		return ParseResult.failure( start );
	}

	protected ParseResult evaluateStreamItems(ParserState state, ItemStreamAccessor input, int start)
	{
		if ( bSkipJunkChars )
		{
			start = state.skipJunkChars( input, start );
		}
		
		String match = input.matchRegEx( start, pattern );
		
		if ( match != null )
		{
			return new ParseResult( match, start, start + match.length() );
		}
		else
		{
			return ParseResult.failure( start );
		}
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
		if ( x instanceof RegEx )
		{
			RegEx xr = (RegEx)x;
			return re.equals( xr.re )  &&  flags == xr.flags  &&  bSkipJunkChars == xr.bSkipJunkChars;
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "RegEx( \"" + re + "\", " + String.valueOf( flags ) + ", " + String.valueOf( bSkipJunkChars ) + "  )";
	}
}
