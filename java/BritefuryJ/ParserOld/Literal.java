//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ParserOld;

import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;

public class Literal extends ParserExpression
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
	
	
	
	protected ParseResult parseStream(ParserState state, ItemStreamAccessor input, int start)
	{
		start = state.skipJunkChars( input, start );
		
		int end = input.consumeString( start, matchString );
		
		if ( end != -1 )
		{
			return new ParseResult( matchString, start, end );
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
}
