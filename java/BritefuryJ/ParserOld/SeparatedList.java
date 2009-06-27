//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ParserOld;

import java.util.ArrayList;
import java.util.List;

import org.python.core.Py;
import org.python.core.PyInteger;
import org.python.core.PyObject;

import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;

public class SeparatedList extends ParserExpression
{
	public enum TrailingSeparatorPolicy
	{
		NEVER,
		OPTIONAL,
		REQUIRED
	}
	
	
	public static interface ListCondition
	{
		public boolean test(ItemStreamAccessor input, int begin, List<Object> elements, boolean bGotTrailingSeparator);
	}
	
	private static class PyListCondition implements ListCondition
	{
		private PyObject callable;
		
		public PyListCondition(PyObject callable)
		{
			this.callable = callable;
		}
		
		public boolean test(ItemStreamAccessor input, int begin, List<Object> elements, boolean bGotTrailingSeparator)
		{
			return Py.py2boolean( callable.__call__( Py.java2py( input ), new PyInteger( begin ), Py.java2py( elements ), Py.java2py( bGotTrailingSeparator ) ) );
		}
	}
	
	public static class CannotApplyMoreThanOneConditionException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}

	public static class CannotApplyConditionAfterActionException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}

	
	public static interface ListAction
	{
		public Object invoke(ItemStreamAccessor input, int begin, List<Object> elements, boolean bGotTrailingSeparator);
	}
	
	private static class PyListAction implements ListAction
	{
		private PyObject callable;
		
		public PyListAction(PyObject callable)
		{
			this.callable = callable;
		}
		
		public Object invoke(ItemStreamAccessor input, int begin, List<Object> elements, boolean bGotTrailingSeparator)
		{
			return callable.__call__( Py.java2py( input ), new PyInteger( begin ), Py.java2py( elements ), Py.java2py( bGotTrailingSeparator ) );
		}
	}

	public static class CannotApplyMoreThanOneActionException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}

	
	
	
	protected ParserExpression element, separator, beginDelim, endDelim;
	protected int minElements, maxElements;
	protected TrailingSeparatorPolicy trailingSepPolicy;
	protected ListCondition condition;
	protected ListAction action;
	
	
	private SeparatedList(ParserExpression element, ParserExpression separator, ParserExpression beginDelim, ParserExpression endDelim, int minElements, int maxElements, TrailingSeparatorPolicy trailingSepPolicy,
			ListCondition condition, ListAction action)
	{
		this.element = element;
		this.separator = separator;
		this.beginDelim = beginDelim;
		this.endDelim = endDelim;
		this.minElements = minElements;
		this.maxElements = maxElements;
		this.trailingSepPolicy = trailingSepPolicy;
		this.condition = condition;
		this.action = action;
	}
	
	public SeparatedList(ParserExpression element, ParserExpression separator, ParserExpression beginDelim, ParserExpression endDelim, int minElements, int maxElements, TrailingSeparatorPolicy trailingSepPolicy)
	{
		this( element, separator, beginDelim, endDelim, minElements, maxElements, trailingSepPolicy, null, null );
	}
	
	public SeparatedList(ParserExpression element, ParserExpression beginDelim, ParserExpression endDelim, int minElements, int maxElements, TrailingSeparatorPolicy trailingSepPolicy)
	{
		this( element, new Literal( "," ), beginDelim, endDelim, minElements, maxElements, trailingSepPolicy );
	}
	
	public SeparatedList(ParserExpression element, ParserExpression separator, int minElements, int maxElements, TrailingSeparatorPolicy trailingSepPolicy)
	{
		this( element, separator, null, null, minElements, maxElements, trailingSepPolicy );
	}

	public SeparatedList(ParserExpression element, int minElements, int maxElements, TrailingSeparatorPolicy trailingSepPolicy)
	{
		this( element, new Literal( "," ), null, null, minElements, maxElements, trailingSepPolicy );
	}

	
	
	public SeparatedList(Object element, Object separator, Object beginDelim, Object endDelim, int minElements, int maxElements, TrailingSeparatorPolicy trailingSepPolicy) throws ParserCoerceException
	{
		this( coerce( element ), coerce( separator ), coerce( beginDelim ), coerce( endDelim ), minElements, maxElements, trailingSepPolicy );
	}
	
	public SeparatedList(Object element, Object beginDelim, Object endDelim, int minElements, int maxElements, TrailingSeparatorPolicy trailingSepPolicy) throws ParserCoerceException
	{
		this( coerce( element ), new Literal( "," ), coerce( beginDelim ), coerce( endDelim ), minElements, maxElements, trailingSepPolicy );
	}
	
	public SeparatedList(Object element, Object separator, int minElements, int maxElements, TrailingSeparatorPolicy trailingSepPolicy) throws ParserCoerceException
	{
		this( coerce( element ), coerce( separator ), null, null, minElements, maxElements, trailingSepPolicy );
	}

	public SeparatedList(Object element, int minElements, int maxElements, TrailingSeparatorPolicy trailingSepPolicy) throws ParserCoerceException
	{
		this( coerce( element ), new Literal( "," ), null, null, minElements, maxElements, trailingSepPolicy );
	}


	public ParserExpression getElementExpression()
	{
		return element;
	}
	
	public ParserExpression getSeparatorExpression()
	{
		return separator;
	}
	
	public ParserExpression getBeginDelimExpression()
	{
		return beginDelim;
	}
	
	public ParserExpression getEndDelimExpression()
	{
		return endDelim;
	}
	
	public int getMinElements()
	{
		return minElements;
	}
	
	public int getMaxElements()
	{
		return maxElements;
	}
	
	public TrailingSeparatorPolicy getTrailingSeparatorPolicy()
	{
		return trailingSepPolicy;
	}
	

	
	
	protected ParseResult parseStream(ParserState state, ItemStreamAccessor input, int start)
	{
		ArrayList<Object> values = new ArrayList<Object>();
		
		int pos = start;
		int errorPos = start;
		int i = 0;
		
		
		// Consume the begin delimiter
		if ( beginDelim != null )
		{
			ParseResult res = beginDelim.evaluateStream( state, input, pos );
			errorPos = res.end;
			
			if ( res.isValid() )
			{
				pos = res.end;
			}
			else
			{
				return ParseResult.failure( errorPos );
			}
		}
		
		
		
		// Consume the contents of the list
		boolean bGotTrailingSeparator = false;
		int elementPos = pos;
		while ( ( i < maxElements  ||  maxElements == -1 ) )
		{
			int itemPos = pos;
			
			// Consume the element
			ParseResult res = element.evaluateStream( state, input, itemPos );
			errorPos = res.end;
			
			if ( res.isValid() )
			{
				itemPos = res.end;
				// Haven't got a value unless we don't *need* the separator 
				if ( trailingSepPolicy != TrailingSeparatorPolicy.REQUIRED )
				{
					// Do not move onwards yet, as we need to consume the separator
					// Got a value; add it to the list
					if ( !res.isSuppressed() )
					{
						values.add( res.value );
					}
					i++;
					// Trailing separator not required, so we can advance the current position
					pos = itemPos;
				}
			}
			else
			{
				// Cannot consume more values
				break;
			}
			
			// Note position after consuming element
			elementPos = itemPos;
			
			
			// Try to consume a separator
			ParseResult sepRes = separator.evaluateStream( state, input, itemPos );
			
			if ( sepRes.isValid() )
			{
				// We have reached position @sepRes.end
				itemPos = sepRes.end;
				
				// Got a separator; advance the current position
				pos = itemPos;
				
				// We have found a separator
				bGotTrailingSeparator = true;
				
				if ( trailingSepPolicy == TrailingSeparatorPolicy.REQUIRED )
				{
					// This separator was required, so the value won't have been added after consuming the element,
					// so we must add it now
					if ( !res.isSuppressed() )
					{
						values.add( res.value );
					}
					i++;
				}
			}
			else
			{
				errorPos = sepRes.end;
				
				if ( trailingSepPolicy != TrailingSeparatorPolicy.REQUIRED )
				{
					// Advance to the current position
					// Trailing separator not found, but not required; advance the current position
					pos = sepRes.end;
				}
				
				bGotTrailingSeparator = false;
				// Could not get separator; stop
				break;
			}
			
			pos = itemPos;
		}
		
		
		if ( ( i < minElements )  ||  ( maxElements != -1  &&  i > maxElements ) )
		{
			return ParseResult.failure( errorPos );
		}
		else
		{
			if ( trailingSepPolicy == TrailingSeparatorPolicy.NEVER  &&  bGotTrailingSeparator )
			{
				// We have a trailing separator, but we don't want one; backtrack to the position after consuming
				// the element
				pos = elementPos;
				bGotTrailingSeparator = false;
			}
			
			
			// Consume the end delimiter
			if ( endDelim != null )
			{
				ParseResult res = endDelim.evaluateStream( state, input, pos );
				errorPos = res.end;
				
				if ( res.isValid() )
				{
					pos = res.end;
				}
				else
				{
					return ParseResult.failure( errorPos );
				}
			}
			
			
			if ( condition != null )
			{
				if ( !condition.test( input, start, values, bGotTrailingSeparator ) )
				{
					return ParseResult.failure( errorPos );
				}
			}
			
			if ( action != null )
			{
				Object v = action.invoke( input, start, values, bGotTrailingSeparator );
				return new ParseResult( v, start, pos );
			}
			else
			{
				return new ParseResult( values, start, pos );
			}
		}
	}
	
	
	
	public SeparatedList listCondition(ListCondition c) throws CannotApplyMoreThanOneConditionException, CannotApplyConditionAfterActionException
	{
		if ( condition != null )
		{
			throw new CannotApplyMoreThanOneConditionException();
		}
		else if ( action != null )
		{
			throw new CannotApplyConditionAfterActionException();
		}
		else
		{
			return new SeparatedList( element, separator, beginDelim, endDelim, minElements, maxElements, trailingSepPolicy, c, null );
		}
	}
	
	public SeparatedList listCondition(PyObject c) throws CannotApplyMoreThanOneConditionException, CannotApplyConditionAfterActionException
	{
		return listCondition( new PyListCondition( c ) );
	}
	

	public SeparatedList listAction(ListAction a) throws CannotApplyMoreThanOneActionException
	{
		if ( action != null )
		{
			throw new CannotApplyMoreThanOneActionException();
		}
		else
		{
			return new SeparatedList( element, separator, beginDelim, endDelim, minElements, maxElements, trailingSepPolicy, condition, a );
		}
	}

	public SeparatedList listAction(PyObject a) throws CannotApplyMoreThanOneActionException
	{
		return listAction( new PyListAction( a ) );
	}
	
	
	
	public List<ParserExpression> getChildren()
	{
		ArrayList<ParserExpression> children = new ArrayList<ParserExpression>();
		children.add( element );
		children.add( separator );
		if ( beginDelim != null )
		{
			children.add( beginDelim );
		}
		if ( endDelim != null )
		{
			children.add( endDelim );
		}
		return children;
	}
	
	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof SeparatedList )
		{
			SeparatedList lx = (SeparatedList)x;
			
			return compareExpressions( element, lx.element )  &&  compareExpressions( separator, lx.separator )  &&
						compareExpressions( beginDelim, lx.beginDelim )  &&  compareExpressions( endDelim, lx.endDelim )  &&
						minElements == lx.minElements  &&  maxElements == lx.maxElements  &&  trailingSepPolicy == lx.trailingSepPolicy  &&
						condition == lx.condition  &&  action == lx.action;
		}
		else
		{
			return false;
		}
	}
	

	public String toString()
	{
		String elemString = element.toString();
		String sepString = separator.toString();
		String beginDelimString = beginDelim != null  ?  beginDelim.toString()  :  "<null>";
		String endDelimString = endDelim != null  ?  endDelim.toString()  :  "<null>";
		String conditionString = condition != null  ?  condition.toString()  :  "<null>";
		String actionString = action != null  ?  action.toString()  :  "<null>";
		return "SeparatedList( element=" + elemString + ", separator=" + sepString + ", beginDelim=" + beginDelimString + ", endDelim=" + endDelimString +
				", minElements=" + minElements + ", maxElements=" + maxElements + ", trailingSepPolicy=" + trailingSepPolicy + ", condition=" + conditionString + ", action=" + actionString + " )";
	}
}
