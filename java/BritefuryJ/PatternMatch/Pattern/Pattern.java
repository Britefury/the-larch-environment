//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch.Pattern;

import java.util.List;
import java.util.Map;

import org.python.core.PyObject;
import org.python.core.PyString;

import BritefuryJ.PatternMatch.Pattern.ListPattern.OnlyOneRepeatAllowedException;

public abstract class Pattern
{
	public abstract boolean test(Object x, Map<String, Object> bindings);
	
	
	protected RepeatInterface getRepeatInterface()
	{
		return null;
	}
	
	
	
	
	@SuppressWarnings("unchecked")
	public static Pattern asPattern(Object x) throws OnlyOneRepeatAllowedException
	{
		if ( x instanceof Pattern )
		{
			return (Pattern)x;
		}
		else if ( x instanceof List )
		{
			List<Object> xs = (List<Object>)x;
			Pattern[] ps = new Pattern[xs.size()];
			for (int i = 0; i < xs.size(); i++)
			{
				ps[i] = asPattern( xs.get( i ) );
			}
			return new ListPattern( ps );
		}
		if ( x instanceof PyString )
		{
			return new Literal( ((PyString)x).toString() );
		}
		else
		{
			return new Literal( x );
		}
	}
	
	
	
	
	public Pattern condition(PatternCondition c)
	{
		return new Condition( this, c );
	}
	
	public Pattern condition(PyObject c)
	{
		return new Condition( this, c );
	}
	

	
	public Pattern __and__(PatternCondition c)
	{
		return condition( c );
	}
	
	public Pattern __and__(PyObject c)
	{
		return condition( c );
	}
	
	
	public Pattern bindTo(String name)
	{
		return new Bind( name, this );
	}

	public Pattern __rlshift__(String name)
	{
		return bindTo( name );
	}
}
