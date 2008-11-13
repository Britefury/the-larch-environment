//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;

import org.python.core.PyObject;

public class Production extends UnaryBranchExpression
{
	public Production(String subexp)
	{
		super( subexp );
	}
	
	public Production(MatchExpression subexp)
	{
		super( subexp );
	}
	
	public Production(MatchExpression subexp, String debugName)
	{
		super( subexp );
		debug( debugName );
	}
	
	
	protected MatchResult parseNode(MatcherState state, Object input, int start, int stop)
	{
		return state.memoisedMatch( subexp, input, start, stop ).clearBindings();
	}



	public MatchExpression action(MatchAction a)
	{
		return new Production( new Action( subexp, a ), debugName );
	}

	public MatchExpression action(PyObject a)
	{
		return new Production( new Action( subexp, a ), debugName );
	}

	public MatchExpression condition(MatchCondition cond)
	{
		return new Production( new Condition( subexp, cond ), debugName );
	}
	
	public MatchExpression bindTo(String name)
	{
		return new Production( new Bind( name, subexp ), debugName );
	}

	public MatchExpression clearBindings()
	{
		return new Production( new ClearBindings( subexp ), debugName );
	}

	public MatchExpression suppress()
	{
		return new Production( new Suppress( subexp ), debugName );
	}

	public MatchExpression optional()
	{
		return new Production( new Optional( subexp ), debugName );
	}

	public MatchExpression zeroOrMore()
	{
		return new Production( new ZeroOrMore( subexp ), debugName );
	}

	public MatchExpression oneOrMore()
	{
		return new Production( new OneOrMore( subexp ), debugName );
	}


	
	
	public boolean compareTo(MatchExpression x)
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
