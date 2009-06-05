//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ParserNew;

import java.util.List;
import java.util.Map;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.DocModel.DMObjectInterface;
import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;

public class Action extends UnaryBranchExpression
{
	protected static class PyAction implements ParseAction
	{
		private PyObject callable;
		
		
		public PyAction(PyObject callable)
		{
			this.callable = callable;
		}


		public Object invoke(Object input, int pos, int end, Object value, Map<String, Object> bindings)
		{
			return callable.__call__( new PyObject[] { Py.java2py( input ), Py.java2py( pos ), Py.java2py( end ), Py.java2py( value ), Py.java2py( bindings ) } );
		}
	}
	
	
	protected ParseAction a;
	protected boolean bMergeUp;
	
	
	public Action(ParserExpression subexp, ParseAction a)
	{
		this( subexp, a, false );
	}
	
	public Action(ParserExpression subexp, ParseAction a, boolean bMergeUp)
	{
		super( subexp );
		this.a = a;
		this.bMergeUp = bMergeUp;
	}
	
	public Action(ParserExpression subexp, PyObject a)
	{
		this( subexp, new PyAction( a ) );
	}
	
	public Action(ParserExpression subexp, PyObject a, boolean bMergeUp)
	{
		this( subexp, new PyAction( a ), bMergeUp );
	}
	
	
	public ParseAction getAction()
	{
		return a;
	}
	
	public boolean getMergeUp()
	{
		return bMergeUp;
	}
	

	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		ParseResult res = subexp.handleNode( state, input );
		
		if ( res.isValid() )
		{
			return res.actionValue( this.a.invoke( input, 0, 1, res.value, res.bindings ), bMergeUp );
		}
		else
		{
			return res;
		}
	}

	protected ParseResult evaluateString(ParserState state, String input, int start)
	{
		ParseResult res = subexp.handleString( state, input, start );
		
		if ( res.isValid() )
		{
			return res.actionValue( this.a.invoke( input, start, res.end, res.value, res.bindings ), bMergeUp );
		}
		else
		{
			return res;
		}
	}

	protected ParseResult evaluateStream(ParserState state, ItemStreamAccessor input, int start)
	{
		ParseResult res = subexp.handleStream( state, input, start );
		
		if ( res.isValid() )
		{
			return res.actionValue( this.a.invoke( input, start, res.end, res.value, res.bindings ), bMergeUp );
		}
		else
		{
			return res;
		}
	}
	
	protected ParseResult evaluateList(ParserState state, List<Object> input, int start)
	{
		ParseResult res = subexp.handleList( state, input, start );
		
		if ( res.isValid() )
		{
			return res.actionValue( this.a.invoke( input, start, res.end, res.value, res.bindings ), bMergeUp );
		}
		else
		{
			return res;
		}
	}

	protected ParseResult evaluateObject(ParserState state, DMObjectInterface input)
	{
		ParseResult res = subexp.handleObject( state, input );
		
		if ( res.isValid() )
		{
			return res.actionValue( this.a.invoke( input, 0, 1, res.value, res.bindings ), bMergeUp );
		}
		else
		{
			return res;
		}
	}


	public boolean compareTo(ParserExpression x)
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
	
	
	
	public static Action mergeUpAction(ParserExpression subexp, ParseAction a)
	{
		return new Action( subexp, a, true );
	}

	public static Action mergeUpAction(ParserExpression subexp, PyObject a)
	{
		return new Action( subexp, a, true );
	}
}
