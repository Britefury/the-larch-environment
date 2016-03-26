//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import BritefuryJ.Util.RichString.RichStringAccessor;

public class Word extends TerminalString
{
	protected final String initChars, bodyChars;
	protected final Pattern pattern;
	
	
	public Word(String bodyChars)
	{
		initChars = "";
		this.bodyChars = bodyChars;
		pattern = Pattern.compile( "[" + Pattern.quote(  bodyChars ) + "]*" );
	}
	
	public Word(String initChars, String bodyChars)
	{
		this.initChars = initChars;
		this.bodyChars = bodyChars;
		pattern = Pattern.compile( "[" + Pattern.quote(  initChars ) + "][" + Pattern.quote(  bodyChars ) + "]*" );
	}
	
	
	public String getInitChars()
	{
		return initChars;
	}
	
	public String getBodyChars()
	{
		return bodyChars;
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
		if ( x instanceof Word )
		{
			Word xw = (Word)x;
			return initChars.equals( xw.initChars )  &&  bodyChars.equals( xw.bodyChars );
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "Word( \"" + initChars + "\", \"" + bodyChars + "\" )";
	}
}
