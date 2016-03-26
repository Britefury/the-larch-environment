//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import BritefuryJ.Util.RichString.RichStringAccessor;

/*
 * RegEx
 * 
 * RegEx:node( input )			->  regex.match(input as string)  ?  input  :  fail
 * RegEx:string( input, start )		->  regex.match(input[start:])  ?  match.group() : fail
 * RegEx:richStr( input, start )		->  regex.match(input[start:])  ?  match.group() : fail
 * RegEx:list( input, start )			->  regex.match(input[start] as string)  ?  input[start]  :  fail
 */
public class RegEx extends TerminalString
{
	protected Pattern pattern;
	
	
	public RegEx(Pattern pattern)
	{
		this.pattern = pattern;
	}
	
	public RegEx(String re)
	{
		this( re, 0 );
	}
	
	public RegEx(String re, int flags)
	{
		this( Pattern.compile( re, flags ) );
	}
	
	
	public String getRE()
	{
		return pattern.pattern();
	}
	
	public int getREFlags()
	{
		return pattern.flags();
	}
	
	
	
	protected ParseResult consumeString(String input, int start)
	{
		Matcher m = pattern.matcher( input.subSequence( start, input.length() ) );
		
		boolean bFound = m.lookingAt();
		if ( bFound  &&  m.end() > 0 )
		{
			String match = m.group();
			return new ParseResult( match, start, start + match.length() );
		}
		
		return ParseResult.failure( start );
	}
	
	protected ParseResult consumeRichString(RichStringAccessor input, int start)
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

	

	public boolean isEquivalentTo(ParserExpression x)
	{
		if ( x instanceof RegEx )
		{
			RegEx xr = (RegEx)x;
			return pattern.pattern().equals( xr.pattern.pattern() )  &&  pattern.flags() == xr.pattern.flags();
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "RegEx( \"" + pattern + "\" )";
	}
}
