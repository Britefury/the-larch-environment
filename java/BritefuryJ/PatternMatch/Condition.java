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

public class Condition extends UnaryBranchExpression
{
	private static class PyCondition implements MatchCondition
	{
		private PyObject callable;
		
		
		public PyCondition(PyObject callable)
		{
			this.callable = callable;
		}


		public boolean test(Object input, Object x, Map<String, Object> bindings, Object arg)
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
				return Py.py2boolean( callable.__call__( values ) );
			}
			else
			{
				return Py.py2boolean( callable.__call__( Py.java2py( input ), Py.java2py( x ), Py.java2py( bindings ) ) );
			}
		}
	}

	
	
	protected MatchCondition cond;
	
	
	public Condition(Object subexp, MatchCondition cond)
	{
		super( subexp );
		this.cond = cond;
	}
	
	public Condition(MatchExpression subexp, MatchCondition cond)
	{
		super( subexp );
		this.cond = cond;
	}
	

	public Condition(String subexp, PyObject cond)
	{
		this( subexp, new PyCondition( cond ) );
	}
	
	public Condition(MatchExpression subexp, PyObject cond)
	{
		this( subexp, new PyCondition( cond ) );
	}
	

	
	public MatchCondition getCondition()
	{
		return cond;
	}
	

	protected MatchResult parseNode(MatchState state, Object input, int start, int stop)
	{
		MatchResult res = subexp.evaluateNode( state, input, start, stop );
		
		if ( res.isValid() )
		{
			if ( cond.test( input, res.value, res.bindings, state.arg ) )
			{
				return res;
			}
			else
			{
				return MatchResult.failure( res.end );
			}
		}
		else
		{
			return res;
		}
	}



	public boolean compareTo(MatchExpression x)
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
