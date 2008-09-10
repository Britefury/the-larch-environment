//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.Arrays;
import java.util.List;

public class Condition extends ParserExpression
{
	protected ParserExpression subexp;
	protected ParseCondition cond;
	
	
	public Condition(ParserExpression subexp, ParseCondition cond)
	{
		this.subexp = subexp;
		this.cond = cond;
	}
	

	protected ParseResult evaluate(ParserState state, String input, int start, int stop)
	{
		ParseResult res = subexp.evaluate( state, input, start, stop );
		
		if ( res.isValid() )
		{
			if ( cond.test( input, res.begin, res.value, res.bindings ) )
			{
				return res;
			}
			else
			{
				return new ParseResult( res.end );
			}
		}
		else
		{
			return res;
		}
	}



	public List<ParserExpression> getChildren()
	{
		ParserExpression[] children = { subexp };
		return Arrays.asList( children );
	}
	
	public boolean compareTo(Condition x)
	{
		return subexp.compareTo( x.subexp )  &&  cond == x.cond;
	}
	
	public String toString()
	{
		return "Condition( " + subexp.toString() + " when " + cond.toString() + " )";
	}
}
