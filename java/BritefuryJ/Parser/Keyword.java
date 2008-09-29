//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.regex.Pattern;

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
	
	
	
	protected ParseResult evaluate(ParserState state, String input, int start, int stop)
	{
		start = state.skipJunkChars( input, start, stop );
		
		int end = start + keywordString.length();
		
		if ( end <= stop )
		{
			CharSequence sub = input.substring( start, end );
			if ( keywordString.equals( sub ) )
			{
				if ( end == stop   ||   !(postPattern.matcher( input.substring( end, end+1 ) ).matches()) )
				{
					return new ParseResult( keywordString, start, end );
				}
			}
		}
		
		return ParseResult.failure( start );
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
