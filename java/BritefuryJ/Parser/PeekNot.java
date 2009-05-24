//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;



public class PeekNot extends UnaryBranchExpression
{
	public PeekNot(String subexp) throws ParserCoerceException
	{
		super( subexp );
	}
	
	public PeekNot(ParserExpression subexp)
	{
		super( subexp );
	}
	

	protected ParseResult parseStream(ParserState state, ItemStreamAccessor input, int start)
	{
		ParseResult res = subexp.evaluateStream( state, input, start );
		
		if ( res.isValid() )
		{
			return ParseResult.failure( start );
		}
		else
		{
			return ParseResult.suppressedNoValue( start, start );
		}
	}


	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof PeekNot )
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
		return "PeekNot( " + subexp.toString() + " )";
	}
}
