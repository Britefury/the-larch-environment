//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.List;

public class Condition extends UnaryBranchExpression
{
	protected ParseCondition cond;
	
	
	public Condition(String subexp, ParseCondition cond)
	{
		super( subexp );
		this.cond = cond;
	}
	
	public Condition(List<Object> subexp, ParseCondition cond) throws ParserCoerceException
	{
		super( subexp );
		this.cond = cond;
	}
		
	public Condition(ParserExpression subexp, ParseCondition cond)
	{
		super( subexp );
		this.cond = cond;
	}
	
	
	public ParseCondition getCondition()
	{
		return cond;
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
				return ParseResult.failure( res.end );
			}
		}
		else
		{
			return res;
		}
	}



	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof Condition )
		{
			Condition xc = (Condition)x;
			return super.compareTo( x )  &&  cond == xc.cond;
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "Condition( " + subexp.toString() + " when " + cond.toString() + " )";
	}
}
