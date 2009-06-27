//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ParserOld;

import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;


public class Peek extends UnaryBranchExpression
{
	public Peek(String subexp) throws ParserCoerceException
	{
		super( subexp );
	}
	
	public Peek(ParserExpression subexp)
	{
		super( subexp );
	}
	

	protected ParseResult parseStream(ParserState state, ItemStreamAccessor input, int start)
	{
		ParseResult res = subexp.evaluateStream( state, input, start );
		
		if ( res.isValid() )
		{
			return ParseResult.suppressedNoValue( start, start );
		}
		else
		{
			return ParseResult.failure( start );
		}
	}


	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof Peek )
		{
			return super.compareTo( x );
		}
		else
		{
			return false;
		}
	}
	

	public String toString()
	{
		return "Peek( " + subexp.toString() + " )";
	}
}
