//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.Arrays;
import java.util.List;

public class Production extends ParserExpression
{
	protected ParserExpression subexp;
	
	
	public Production(String subexp)
	{
		this( coerce( subexp ) );
	}
	
	public Production(List<Object> subexp) throws ParserCoerceException
	{
		this( coerce( subexp ) );
	}
		
	public Production(ParserExpression subexp)
	{
		this.subexp = subexp;
	}
	
	public Production(ParserExpression subexp, String debugName)
	{
		this.subexp = subexp;
		debug( debugName );
	}
	
	
	protected ParseResult evaluate(ParserState state, String input, int start, int stop)
	{
		return state.memoisedMatch( subexp, input, start, stop );
	}



	public ParserExpression action(ParseAction a)
	{
		return new Production( new Action( subexp, a ), debugName );
	}

	public ParserExpression bindTo(String name)
	{
		return new Production( new Bind( subexp, name ), debugName );
	}

	public ParserExpression condition(ParseCondition cond)
	{
		return new Production( new Condition( subexp, cond ), debugName );
	}
	
	public ParserExpression suppress()
	{
		return new Production( new Suppress( subexp ), debugName );
	}


	
	
	public List<ParserExpression> getChildren()
	{
		ParserExpression[] children = { subexp };
		return Arrays.asList( children );
	}
	
	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof Production )
		{
			Production xp = (Production)x;
			return subexp.compareTo( xp.subexp );
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "Production( " + subexp.toString() + " )";
	}
}
