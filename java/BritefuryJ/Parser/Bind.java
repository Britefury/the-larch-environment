//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.HashMap;
import java.util.List;

public class Bind extends UnaryBranchExpression
{
	protected String name;
	
	
	public Bind(String subexp, String name)
	{
		super( subexp );
		this.name = name;
	}
	
	public Bind(List<Object> subexp, String name) throws ParserCoerceException
	{
		super( subexp );
		this.name = name;
	}
		
	public Bind(ParserExpression subexp, String name)
	{
		super( subexp );
		this.name = name;
	}
	
	
	public String getName()
	{
		return name;
	}
	

	protected ParseResult evaluate(ParserState state, String input, int start, int stop)
	{
		ParseResult res = subexp.evaluate( state, input, start, stop );
		
		if ( res.isValid() )
		{
			HashMap<String, Object> b = new HashMap<String, Object>();
			b.putAll( res.bindings );
			b.put( name, res.value );
			return new ParseResult( res.value, res.begin, res.end, b );
		}
		else
		{
			return res;
		}
	}



	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof Bind )
		{
			Bind xx = (Bind)x;
			return super.compareTo( x )  &&  name.equals( xx.name );
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "Bind( '" + name + "' = " + subexp.toString() + " )";
	}
}
