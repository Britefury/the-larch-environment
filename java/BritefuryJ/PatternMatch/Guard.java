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

import BritefuryJ.PatternMatch.Pattern.Pattern;
import BritefuryJ.PatternMatch.Pattern.ListPattern.OnlyOneRepeatAllowedException;

public class Guard
{
	private static class PyAction implements MatchAction
	{
		private PyObject callable;
		
		
		public PyAction(PyObject callable)
		{
			this.callable = callable;
		}


		public Object invoke(Object x, Map<String, Object> bindings)
		{
			String[] keywords = new String[bindings.size()];
			PyObject[] values = new PyObject[bindings.size() + 1];
			
			keywords = bindings.keySet().toArray( keywords );
			values[0] = Py.java2py( x );
			int i = 1;
			for (Object v: bindings.values())
			{
				values[i] = Py.java2py( v );
				i++;
			}
			return callable.__call__( values, keywords );
		}
	}

	
	protected Pattern pattern;
	protected MatchAction action;
	
	
	public Guard(Pattern pattern, MatchAction action)
	{
		this.pattern = pattern;
		this.action = action;
	}
	
	public Guard(Pattern pattern, PyObject action)
	{
		this( pattern, new PyAction( action ) );
	}
	
	public Guard(Object pattern, MatchAction action) throws OnlyOneRepeatAllowedException
	{
		this( Pattern.asPattern( pattern ), action );
	}
	
	public Guard(Object pattern, PyObject action) throws OnlyOneRepeatAllowedException
	{
		this( Pattern.asPattern( pattern ), action );
	}
	
	
	protected boolean testPattern(Object x, Map<String, Object> bindings)
	{
		return pattern.test( x, bindings );
	}
	
	protected Object invokeAction(Object x, Map<String, Object> bindings)
	{
		return action.invoke( x, bindings );
	}
}
