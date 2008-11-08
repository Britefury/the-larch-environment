//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Word extends Terminal
{
	protected String initChars, bodyChars;
	protected Pattern pattern;
	
	
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
	
	
	
	protected ParseResult parseString(ParserState state, String input, int start, int stop)
	{
		start = state.skipJunkChars( input, start, stop );
		
		Matcher m = pattern.matcher( input.substring( start, stop ) );
		
		boolean bFound = m.find();
		if ( bFound  &&  m.start() == 0  &&  m.end() > 0 )
		{
			String matchString = m.group();
			int end = start + matchString.length();
			return new ParseResult( matchString, start, end );
		}
		else
		{
			return ParseResult.failure( start );
		}
	}


	public boolean compareTo(ParserExpression x)
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


	protected boolean isTerminal()
	{
		return true;
	}
}
