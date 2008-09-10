//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Bind extends ParserExpression
{
	protected ParserExpression subexp;
	protected String bindName;
	
	
	public Bind(ParserExpression subexp, String bindName)
	{
		this.subexp = subexp;
		this.bindName = bindName;
	}
	

	@SuppressWarnings("unchecked")
	protected ParseResult evaluate(ParserState state, String input, int start, int stop)
	{
		ParseResult res = subexp.evaluate( state, input, start, stop );
		
		if ( res.isValid() )
		{
			HashMap<String, Object> b = (HashMap<String, Object>)res.bindings.clone();
			b.put( bindName, res.value );
			return new ParseResult( res.value, start, res.end, b );
		}
		else
		{
			return res;
		}
	}



	public List<ParserExpression> getChildren()
	{
		ParserExpression[] children = { subexp };
		return Arrays.asList( children );
	}
	
	public boolean compareTo(Bind x)
	{
		return subexp.compareTo( x.subexp )  &&  bindName.equals( x.bindName );
	}
	
	public String toString()
	{
		return "Bind( '" + bindName + "' = " + subexp.toString() + " )";
	}
}
