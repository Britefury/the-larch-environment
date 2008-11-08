//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

public class LiteralItem extends ParserExpression
{
	protected Object matchValue;
	
	
	public LiteralItem(Object matchValue)
	{
		this.matchValue = matchValue;
	}
	
	
	public Object getMatchValue()
	{
		return matchValue;
	}
	
	
	
	protected ParseResult parseString(ParserState state, String input, int start, int stop)
	{
		return ParseResult.failure( start );
	}

	protected ParseResult parseNode(ParserState state, Object input, int start, int stop)
	{
		if ( matchValue.equals( input ) )
		{
			return new ParseResult( input, start, stop );
		}
		
		return ParseResult.failure( start );
	}


	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof LiteralItem )
		{
			LiteralItem xl = (LiteralItem)x;
			return matchValue.equals( xl.matchValue );
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "LiteralItem( \"" + matchValue.toString() + "\" )";
	}


	protected boolean isTerminal()
	{
		return true;
	}
}
