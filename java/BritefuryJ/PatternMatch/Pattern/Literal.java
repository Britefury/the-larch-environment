//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.PatternMatch.Pattern;

import java.util.Map;


public class Literal extends Pattern
{
	private Object value;
	

	public Literal(Object value)
	{
		this.value = value;
	}
	
	
	public boolean test(Object x, Map<String, Object> bindings)
	{
		return x.equals( value );
	}


	public boolean equals(Object x)
	{
		if ( x instanceof Literal )
		{
			Literal l = (Literal)x;
			return value.equals( l.value );
		}
		else
		{
			return false;
		}
	}
}
