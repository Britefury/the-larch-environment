//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.Arrays;
import java.util.List;

public class Forward extends ParserExpression
{
	protected ParserExpression subexp;
	
	
	public Forward()
	{
		this.subexp = null;
	}
	
	
	
	public ParserExpression setExpression(String exp)
	{
		subexp = coerce( exp );
		return this;
	}
	
	public ParserExpression setExpression(List<Object> exp) throws ParserCoerceException
	{
		subexp = coerce( exp );
		return this;
	}
	
	public ParserExpression setExpression(ParserExpression exp)
	{
		subexp = exp;
		return this;
	}
	
	public ParserExpression getExpression()
	{
		return subexp;
	}
	

	
	public ParserExpression __lshift__(String exp)
	{
		subexp = coerce( exp );
		return this;
	}
	
	public ParserExpression __lshift__(List<Object> exp) throws ParserCoerceException
	{
		subexp = coerce( exp );
		return this;
	}
	
	public ParserExpression __lshift__(ParserExpression exp)
	{
		subexp = exp;
		return this;
	}
	

	
	protected ParseResult evaluate(ParserState state, String input, int start, int stop)
	{
		return subexp.evaluate( state, input, start, stop );
	}



	public List<ParserExpression> getChildren()
	{
		ParserExpression[] children = { subexp };
		return Arrays.asList( children );
	}
	
	public boolean compareTo(ParserExpression x)
	{
		if ( x instanceof Forward )
		{
			Forward xf = (Forward)x;
			return subexp.compareTo( xf.subexp );
		}
		else
		{
			return false;
		}
	}
	
	public String toString()
	{
		return "Forward( <" + subexp.getDebugName() + "> )";
	}
}
