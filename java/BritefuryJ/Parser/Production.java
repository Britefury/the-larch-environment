//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import org.python.core.PyObject;

public class Production extends UnaryBranchExpression
{
	public Production(String subexp) throws ParserCoerceException
	{
		super( subexp );
	}
	
	public Production(ParserExpression subexp)
	{
		super( subexp );
	}
	
	public Production(ParserExpression subexp, String debugName)
	{
		super( subexp );
		debug( debugName );
	}
	
	
	protected ParseResult parseString(ParserState state, String input, int start, int stop)
	{
		return state.memoisedMatchString( subexp, input, start, stop );
	}



	public ParserExpression action(ParseAction a)
	{
		return new Production( new Action( subexp, a ), debugName );
	}

	public ParserExpression action(PyObject a)
	{
		return new Production( new Action( subexp, a ), debugName );
	}

	public ParserExpression condition(ParseCondition cond)
	{
		return new Production( new Condition( subexp, cond ), debugName );
	}
	
	public ParserExpression suppress()
	{
		return new Production( new Suppress( subexp ), debugName );
	}

	public ParserExpression optional()
	{
		return new Production( new Optional( subexp ), debugName );
	}


	
	
	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof Production )
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
		return "Production( " + subexp.toString() + " )";
	}
}
