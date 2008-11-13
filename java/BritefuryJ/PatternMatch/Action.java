//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;

import java.util.Map;

import org.python.core.Py;
import org.python.core.PyInteger;
import org.python.core.PyObject;

public class Action extends UnaryBranchExpression
{
	private static class PyAction implements MatchAction
	{
		private PyObject callable;
		
		
		public PyAction(PyObject callable)
		{
			this.callable = callable;
		}


		public Object invoke(Object input, int begin, Object x, Map<String, Object> bindings)
		{
			return callable.__call__( Py.java2py( input ), new PyInteger( begin ), Py.java2py( x ) );
		}
	}
	
	
	protected MatchAction a;
	
	
	public Action(String subexp, MatchAction a)
	{
		super( subexp );
		this.a = a;
	}
	
	public Action(MatchExpression subexp, MatchAction a)
	{
		super( subexp );
		this.a = a;
	}
	
	
	public Action(String subexp, PyObject a)
	{
		this( subexp, new PyAction( a ) );
	}
	
	public Action(MatchExpression subexp, PyObject a)
	{
		this( subexp, new PyAction( a ) );
	}
	
	
	public MatchAction getAction()
	{
		return a;
	}
	

	protected MatchResult parseNode(MatchState state, Object input, int start, int stop)
	{
		MatchResult res = subexp.evaluateNode( state, input, start, stop );
		
		if ( res.isValid() )
		{
			return res.withValidUnsuppressedValue( this.a.invoke( input, res.begin, res.value, res.bindings ) );
		}
		else
		{
			return res;
		}
	}



	public boolean compareTo(MatchExpression x)
	{
		if ( x instanceof Action )
		{
			Action ax = (Action)x;
			return super.compareTo( x )  &&  a == ax.a;
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "Action( " + subexp.toString() + " -> " + a.toString() + " )";
	}
}
