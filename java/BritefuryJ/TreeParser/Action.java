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

public class Action extends UnaryBranchExpression
{
	protected static class PyAction implements TreeParseAction
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
	
	
	protected TreeParseAction a;
	protected boolean bMergeUp;
	
	
	public Action(Object subexp, TreeParseAction a)
	{
		this( subexp, a, false );
	}
	
	public Action(Object subexp, TreeParseAction a, boolean bMergeUp)
	{
		super( subexp );
		this.a = a;
		this.bMergeUp = bMergeUp;
	}
	
	public Action(TreeParserExpression subexp, TreeParseAction a)
	{
		this( subexp, a, false );
	}
	
	public Action(TreeParserExpression subexp, TreeParseAction a, boolean bMergeUp)
	{
		super( subexp );
		this.a = a;
		this.bMergeUp = bMergeUp;
	}
	
	
	public Action(Object subexp, PyObject a)
	{
		this( subexp, new PyAction( a ) );
	}
	
	public Action(Object subexp, PyObject a, boolean bMergeUp)
	{
		this( subexp, new PyAction( a ), bMergeUp );
	}
	
	public Action(TreeParserExpression subexp, PyObject a)
	{
		this( subexp, new PyAction( a ) );
	}
	
	public Action(TreeParserExpression subexp, PyObject a, boolean bMergeUp)
	{
		this( subexp, new PyAction( a ), bMergeUp );
	}
	
	
	public TreeParseAction getAction()
	{
		return a;
	}
	
	public boolean getMergeUp()
	{
		return bMergeUp;
	}
	

	protected TreeParseResult evaluateNode(TreeParserState state, Object input)
	{
		TreeParseResult res = subexp.processNode( state, input );
		
		if ( res.isValid() )
		{
			return res.actionValue( this.a.invoke( input, res.value, res.bindings, state.arg ), bMergeUp );
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
			return res.actionValue( this.a.invoke( input, res.value, res.bindings, state.arg ), bMergeUp );
		}
		else
		{
			return res;
		}
	}



	public boolean compareTo(TreeParserExpression x)
	{
		if ( x instanceof Action )
		{
			Action ax = (Action)x;
			return super.compareTo( x )  &&  a == ax.a  &&  bMergeUp == ax.bMergeUp;
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "Action( " + subexp.toString() + " -> " + a.toString() + ", " + bMergeUp + " )";
	}
	
	
	
	static Action mergeUpAction(Object subexp, TreeParseAction a)
	{
		return new Action( subexp, a, true );
	}

	static Action mergeUpAction(TreeParserExpression subexp, TreeParseAction a)
	{
		return new Action( subexp, a, true );
	}

	static Action mergeUpAction(Object subexp, PyObject a)
	{
		return new Action( subexp, a, true );
	}

	static Action mergeUpAction(TreeParserExpression subexp, PyObject a)
	{
		return new Action( subexp, a, true );
	}
}
