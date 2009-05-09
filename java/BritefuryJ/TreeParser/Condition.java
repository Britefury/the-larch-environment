//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.TreeParser;

import java.util.List;
import java.util.Map;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PySequenceList;

public class Condition extends UnaryBranchExpression
{
	private static class PyCondition implements TreeParseCondition
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

	
	
	protected TreeParseCondition cond;
	
	
	public Condition(Object subexp, TreeParseCondition cond)
	{
		super( subexp );
		this.cond = cond;
	}
	
	public Condition(TreeParserExpression subexp, TreeParseCondition cond)
	{
		super( subexp );
		this.cond = cond;
	}
	

	public Condition(String subexp, PyObject cond)
	{
		this( subexp, new PyCondition( cond ) );
	}
	
	public Condition(TreeParserExpression subexp, PyObject cond)
	{
		this( subexp, new PyCondition( cond ) );
	}
	

	
	public TreeParseCondition getCondition()
	{
		return cond;
	}
	

	protected TreeParseResult evaluateNode(TreeParserState state, Object input)
	{
		TreeParseResult res = subexp.processNode( state, input );
		
		if ( res.isValid() )
		{
			if ( cond.test( input, res.value, res.bindings, state.arg ) )
			{
				return res;
			}
			else
			{
				return TreeParseResult.failure( res.end );
			}
		}
		else
		{
			return res;
		}
	}

	protected TreeParseResult evaluateList(TreeParserState state, List<Object> input, int start, int stop)
	{
		TreeParseResult res = subexp.processList( state, input, start, stop );
		
		if ( res.isValid() )
		{
			if ( cond.test( input, res.value, res.bindings, state.arg ) )
			{
				return res;
			}
			else
			{
				return TreeParseResult.failure( res.end );
			}
		}
		else
		{
			return res;
		}
	}



	public boolean compareTo(TreeParserExpression x)
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
