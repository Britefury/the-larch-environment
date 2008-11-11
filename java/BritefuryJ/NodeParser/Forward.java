//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.NodeParser;

import java.util.Arrays;
import java.util.List;

public class Forward extends ParserExpression
{
	protected ParserExpression subexp;
	
	
	public Forward()
	{
		this.subexp = null;
	}
	
	
	
	public ParserExpression setExpression(Object exp)
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
	

	
	public ParserExpression __lshift__(Object exp)
	{
		subexp = coerce( exp );
		return this;
	}
	
	public ParserExpression __lshift__(ParserExpression exp)
	{
		subexp = exp;
		return this;
	}
	

	
	protected ParseResult parseNode(ParserState state, Object input, int start, int stop)
	{
		return subexp.evaluateNode( state, input, start, stop );
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
