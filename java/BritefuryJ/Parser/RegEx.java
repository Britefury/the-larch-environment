//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

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
public class RegEx extends TerminalString
{
	protected String re;
	protected int flags;
	protected Pattern pattern;
	
	
	public RegEx(String re)
	{
		this( re, 0 );
	}
	
	public RegEx(String re, int flags)
	{
		pattern = Pattern.compile( re, flags );
		this.re = re;
		this.flags = flags;
	}
	
	
	public String getRE()
	{
		return re;
	}
	
	public int getREFlags()
	{
		return flags;
	}
	
	
	
	protected ParseResult consumeString(String input, int start)
	{
		Matcher m = pattern.matcher( input.subSequence( start, input.length() ) );
		
		boolean bFound = m.find();
		if ( bFound  &&  m.start() == 0  &&  m.end() > 0 )
		{
			String match = m.group();
			return new ParseResult( match, start, start + match.length() );
		}
		
		return ParseResult.failure( start );
	}
	
	protected ParseResult consumeStream(ItemStreamAccessor input, int start)
	{
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

	

	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof RegEx )
		{
			RegEx xr = (RegEx)x;
			return re.equals( xr.re )  &&  flags == xr.flags;
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "RegEx( \"" + re + "\", " + String.valueOf( flags ) + "  )";
	}
}
