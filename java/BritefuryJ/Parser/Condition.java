//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import org.python.core.Py;
import org.python.core.PyInteger;
import org.python.core.PyObject;

public class Condition extends UnaryBranchExpression
{
	private static class PyCondition implements ParseCondition
	{
		private PyObject callable;
		
		
		public PyCondition(PyObject callable)
		{
			this.callable = callable;
		}


		public boolean test(Object input, int begin, Object x)
		{
			return Py.py2boolean( callable.__call__( Py.java2py( input ), new PyInteger( begin ), Py.java2py( x ) ) );
		}
	}

	
	
	protected ParseCondition cond;
	
	
	public Condition(String subexp, ParseCondition cond)
	{
		super( subexp );
		this.cond = cond;
	}
	
	public Condition(ParserExpression subexp, ParseCondition cond)
	{
		super( subexp );
		this.cond = cond;
	}
	

	public Condition(String subexp, PyObject cond)
	{
		this( subexp, new PyCondition( cond ) );
	}
	
	public Condition(ParserExpression subexp, PyObject cond)
	{
		this( subexp, new PyCondition( cond ) );
	}
	

	
	public ParseCondition getCondition()
	{
		return cond;
	}
	

	protected ParseResult parseString(ParserState state, String input, int start, int stop)
	{
		ParseResult res = subexp.evaluateString( state, input, start, stop );
		
		if ( res.isValid() )
		{
			if ( cond.test( input, res.begin, res.value ) )
			{
				return res;
			}
			else
			{
				return ParseResult.failure( res.end );
			}
		}
		else
		{
			return res;
		}
	}


	protected ParseResult parseNode(ParserState state, Object input, int start, int stop)
	{
		ParseResult res = subexp.evaluateNode( state, input, start, stop );
		
		if ( res.isValid() )
		{
			if ( cond.test( input, res.begin, res.value ) )
			{
				return res;
			}
			else
			{
				return ParseResult.failure( res.end );
			}
		}
		else
		{
			return res;
		}
	}



	public boolean compareTo(ParserExpression x)
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
