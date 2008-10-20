//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch.Pattern;

import java.util.Map;

public class Bind extends Pattern
{
	private String name;
	private Pattern pattern;
	
	
	public Bind(String name, Pattern pattern)
	{
		this.name = name;
		this.pattern = pattern;
	}
	
	
	public boolean test(Object x, Map<String, Object> bindings)
	{
		boolean bHasValue = bindings.containsKey( name );
		if ( bHasValue )
		{
			// Name already bound; ensure that the value of the binding is the same as @x, otherwise match failure
			if ( !x.equals( bindings.get( name ) ) )
			{
				return false;
			}
		}
		
		if ( pattern.test( x, bindings ) )
		{
			if ( !bHasValue )
			{
				bindings.put( name, x );
			}
			return true;
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
		if ( x instanceof Bind )
		{
			Bind b = (Bind)x;
			return name.equals( b.name )  &&  pattern.equals( b.pattern );
		}
		else
		{
			return false;
		}
	}
}
