//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.Arrays;
import java.util.List;

public class Suppress extends ParserExpression
{
	protected ParserExpression subexp;
	
	
	public Suppress(String subexp)
	{
		this( coerce( subexp ) );
	}
	
	public Suppress(List<Object> subexp) throws ParserCoerceException
	{
		this( coerce( subexp ) );
	}
		
	public Suppress(ParserExpression subexp)
	{
		this.subexp = subexp;
	}
	
	
	protected ParseResult evaluate(ParserState state, String input, int start, int stop)
	{
		return subexp.evaluate( state, input, start, stop ).suppressed();
	}



	public List<ParserExpression> getChildren()
	{
		ParserExpression[] children = { subexp };
		return Arrays.asList( children );
	}
	
	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof Suppress )
		{
			Suppress xs = (Suppress)x;
			return subexp.compareTo( xs.subexp );
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
