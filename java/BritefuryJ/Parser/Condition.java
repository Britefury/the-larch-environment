//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser;

import java.util.List;
import java.util.Map;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.Util.RichString.RichStringAccessor;

/*
 * Condition
 * 
 * Condition:node( input )			->  res = Condition.subexp:node( input );  Condition.cond.test( res )  ?  res  :  fail
 * Condition:string( input, start )	->  res = Condition.subexp:string( input, start );  Condition.cond.test( res )  ?  res  :  fail
 * Condition:richStr( input, start )	->  res = Condition.subexp:richStr( input, start );  Condition.cond.test( res )  ?  res  :  fail
 * Condition:list( input, start )		->  res = Condition.subexp:list( input, start );  Condition.cond.test( res )  ?  res  :  fail
 */
public class Condition extends UnaryBranchExpression
{
	private static class PyCondition implements ParseCondition
	{
		private final PyObject callable;
		
		
		public PyCondition(PyObject callable)
		{
			this.callable = callable;
		}


		public boolean test(Object input, int pos, int end, Object value, Map<String, Object> bindings)
		{
			return Py.py2boolean( callable.__call__( new PyObject[] { Py.java2py( input ), Py.java2py( pos ), Py.java2py( end ), Py.java2py( value ), Py.java2py( bindings ) } ) );
		}
	}

	
	
	protected ParseCondition cond;
	
	
	public Condition(ParserExpression subexp, ParseCondition cond)
	{
		super( subexp );
		this.cond = cond;
	}

	public Condition(ParserExpression subexp, PyObject cond)
	{
		this( subexp, new PyCondition( cond ) );
	}
	

	public Condition(Object subexp, ParseCondition cond) throws ParserCoerceException
	{
		super( subexp );
		this.cond = cond;
	}

	public Condition(Object subexp, PyObject cond) throws ParserCoerceException
	{
		this( subexp, new PyCondition( cond ) );
	}
	

	
	public ParseCondition getCondition()
	{
		return cond;
	}
	

	protected ParseResult evaluateNode(ParserState state, Object input)
	{
		ParseResult res = subexp.handleNode( state, input );
		
		if ( res.isValid() )
		{
			if ( cond.test( input, 0, 1, res.value, res.bindings ) )
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

	protected ParseResult evaluateStringChars(ParserState state, String input, int start)
	{
		ParseResult res = subexp.handleStringChars( state, input, start );
		
		if ( res.isValid() )
		{
			if ( cond.test( input, start, res.end, res.value, res.bindings ) )
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

	protected ParseResult evaluateRichStringItems(ParserState state, RichStringAccessor input, int start)
	{
		ParseResult res = subexp.handleRichStringItems( state, input, start );
		
		if ( res.isValid() )
		{
			if ( cond.test( input, start, res.end, res.value, res.bindings ) )
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
	
	protected ParseResult evaluateListItems(ParserState state, List<Object> input, int start)
	{
		ParseResult res = subexp.handleListItems( state, input, start );
		
		if ( res.isValid() )
		{
			if ( cond.test( input, start, res.end, res.value, res.bindings ) )
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

	
	
	public boolean isEquivalentTo(ParserExpression x)
	{
		if ( x instanceof Condition )
		{
			Condition xc = (Condition)x;
			return super.isEquivalentTo( x )  &&  cond == xc.cond;
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
