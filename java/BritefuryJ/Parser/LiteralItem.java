//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.List;

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
	
	
	
	private ParseResult parse(ParserState state, List<Object> input, int start, int stop) throws ParserIncompatibleDataTypeException
	{
		if ( matchValue.equals( input.get( start ) ) )
		{
			return new ParseResult( input.get( start ), start, start + 1 );
		}
		
		return ParseResult.failure( start );
	}
	
	
	@SuppressWarnings("unchecked")
	protected ParseResult parse(ParserState state, Object input, int start, int stop) throws ParserIncompatibleDataTypeException
	{
		try
		{
			return parse( state, (List<Object>)input, start, stop );
		}
		catch (ClassCastException e)
		{
			throw new ParserIncompatibleDataTypeException();
		}
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
}
