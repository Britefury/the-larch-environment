//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch.Pattern;

import java.util.Map;

import org.python.core.Py;
import org.python.core.PyObject;

public class Condition extends Pattern
{
	private static class PyPatternCondition implements PatternCondition
	{
		private PyObject callable;
		
		
		public PyPatternCondition(PyObject callable)
		{
			this.callable = callable;
		}
		
		public boolean test(Object x)
		{
			return Py.py2boolean( callable.__call__( Py.java2py( x ) ) );
		}
		
	}
	
	
	
	private Pattern pattern;
	private PatternCondition condition;
	
	
	public Condition(Pattern pattern, PatternCondition condition)
	{
		this.pattern = pattern;
		this.condition = condition;
	}
	
	public Condition(Pattern pattern, PyObject condition)
	{
		this.pattern = pattern;
		this.condition = new PyPatternCondition( condition );
	}
	
	
	public boolean test(Object x, Map<String, Object> bindings)
	{
		if ( condition.test( x ) )
		{
			return pattern.test( x, bindings );
		}
		else
		{
			return false;
		}
	}


	protected RepeatInterface getRepeatInterface()
	{
		return pattern.getRepeatInterface();
	}


	public boolean equals(Object x)
	{
		if ( x instanceof Condition )
		{
			Condition c = (Condition)x;
			return condition == c.condition  &&  pattern.equals( c.pattern );
		}
		else
		{
			return false;
		}
	}
}
