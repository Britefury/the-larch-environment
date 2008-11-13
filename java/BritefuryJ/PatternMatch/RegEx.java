//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegEx extends StringTerminal
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
	
	
	
	protected MatchResult parseString(MatchState state, String input)
	{
		Matcher m = pattern.matcher( input );
		
		boolean bFound = m.find();
		if ( bFound  &&  m.start() == 0  &&  m.end() == input.length() )
		{
			String matchString = m.group();
			return new MatchResult( matchString, 0, input.length() );
		}
		else
		{
			return MatchResult.failure( 0 );
		}
	}


	public boolean compareTo(MatchExpression x)
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
