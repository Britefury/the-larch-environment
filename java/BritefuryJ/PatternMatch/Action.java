//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch;

import java.util.Map;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PySequenceList;

public class Action extends UnaryBranchExpression
{
	protected static class PyAction implements MatchAction
	{
		private PyObject callable;
		
		
		public PyAction(PyObject callable)
		{
			this.callable = callable;
		}


		public Object invoke(Object input, Object x, Map<String, Object> bindings, Object arg)
		{
			if ( arg != null  &&  arg instanceof PySequenceList )
			{
				PySequenceList args = (PySequenceList)arg;
				int numArgs = args.size();
				PyObject[] values = new PyObject[numArgs + 3];
				
				for (int i = 0; i < numArgs; i++)
				{
					values[i] = Py.java2py( args.get( i ) );
				}
				values[numArgs] = Py.java2py( input );
				values[numArgs] = Py.java2py( x );
				values[numArgs] = Py.java2py( bindings );
				return callable.__call__( values );
			}
			else
			{
				return callable.__call__( Py.java2py( input ), Py.java2py( x ), Py.java2py( bindings ) );
			}
		}
	}
	
	
	protected MatchAction a;
	
	
	public Action(Object subexp, MatchAction a)
	{
		super( subexp );
		this.a = a;
	}
	
	public Action(MatchExpression subexp, MatchAction a)
	{
		super( subexp );
		this.a = a;
	}
	
	
	public Action(Object subexp, PyObject a)
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
			return res.withValidUnsuppressedValue( this.a.invoke( input, res.value, res.bindings, state.arg ) );
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
