//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.NodeParser;

import org.python.core.PyObject;

public class Production extends UnaryBranchExpression
{
	public Production(String subexp)
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
	
	
	protected ParseResult parseNode(ParserState state, Object input, int start, int stop)
	{
		return state.memoisedMatch( subexp, input, start, stop ).clearBindings();
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
	
	public ParserExpression bindTo(String name)
	{
		return new Production( new Bind( name, subexp ), debugName );
	}

	public ParserExpression clearBindings()
	{
		return new Production( new ClearBindings( subexp ), debugName );
	}

	public ParserExpression suppress()
	{
		return new Production( new Suppress( subexp ), debugName );
	}

	public ParserExpression optional()
	{
		return new Production( new Optional( subexp ), debugName );
	}

	public ParserExpression zeroOrMore()
	{
		return new Production( new ZeroOrMore( subexp ), debugName );
	}

	public ParserExpression oneOrMore()
	{
		return new Production( new OneOrMore( subexp ), debugName );
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
