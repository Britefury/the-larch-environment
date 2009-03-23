//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;

import java.util.List;

public class Delegate extends UnaryBranchExpression
{
	public Delegate()
	{
		this( new Anything() );
	}
	
	public Delegate(Object subexp)
	{
		super( subexp );
	}
	
	public Delegate(MatchExpression subexp, MatchAction a)
	{
		super( subexp );
	}
	
	
	protected MatchResult evaluateNode(MatchState state, Object input)
	{
		MatchResult res = subexp.processNode( state, input );
		
		if ( res.isValid() )
		{
			return res.actionValue( state.delegateAction.invoke( input, res.value, res.bindings, state.arg ), false );
		}
		else
		{
			return res;
		}
	}

	protected MatchResult evaluateList(MatchState state, List<Object> input, int start, int stop)
	{
		MatchResult res = subexp.processList( state, input, start, stop );
		
		if ( res.isValid() )
		{
			return res.actionValue( state.delegateAction.invoke( input, res.value, res.bindings, state.arg ), false );
		}
		else
		{
			return res;
		}
	}



	public boolean compareTo(MatchExpression x)
	{
		if ( x instanceof Delegate )
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
		return "Delegate( " + subexp.toString() + " )";
	}
}
