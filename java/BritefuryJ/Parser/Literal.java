//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

public class Literal extends Terminal
{
	protected String matchString;
	
	
	public Literal(String matchString)
	{
		this.matchString = matchString;
	}
	
	
	public String getMatchString()
	{
		return matchString;
	}
	
	
	
	protected ParseResult parseString(ParserState state, String input, int start, int stop)
	{
		start = state.skipJunkChars( input, start, stop );
		
		int end = start + matchString.length();
		
		if ( end <= stop )
		{
			if ( matchString.equals( input.substring( start, end ) ) )
			{
				return new ParseResult( matchString, start, end );
			}
		}
		
		return ParseResult.failure( start );
	}
	
	
	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof Literal )
		{
			Literal xl = (Literal)x;
			return matchString.equals( xl.matchString );
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "Literal( \"" + matchString + "\" )";
	}


	protected boolean isTerminal()
	{
		return true;
	}
}
