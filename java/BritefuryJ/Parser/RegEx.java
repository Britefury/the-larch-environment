//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	
	
	protected ParseResult parse(ParserState state, String input, int start, int stop) throws ParserIncompatibleDataTypeException
	{
		if ( bSkipJunkChars )
		{
			start = state.skipJunkChars( input, start, stop );
		}
		
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
	
	
	protected ParseResult parse(ParserState state, Object input, int start, int stop) throws ParserIncompatibleDataTypeException
	{
		if ( input instanceof String )
		{
			return parse( state, (String)input, start, stop );
		}
		else
		{
			throw new ParserIncompatibleDataTypeException();
		}
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
