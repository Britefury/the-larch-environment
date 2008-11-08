//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.List;

public class Suppress extends UnaryBranchExpression
{
	public Suppress(String subexp)
	{
		super( subexp );
	}
	
	public Suppress(List<Object> subexp) throws ParserCoerceException
	{
		super( subexp );
	}
		
	public Suppress(ParserExpression subexp)
	{
		super( subexp );
	}
	
	
	protected ParseResult parse(ParserState state, Object input, int start, int stop) throws ParserIncompatibleDataTypeException
	{
		return subexp.evaluate( state, input, start, stop ).suppressed();
	}



	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof Suppress )
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
		return "Suppress( " + subexp.toString() + " )";
	}
}
