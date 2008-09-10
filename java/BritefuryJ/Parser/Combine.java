//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.List;

public class Combine extends ParserExpression
{
	ParserExpression[] children;
	
	public Combine(List<ParserExpression> children)
	{
		this.children = (ParserExpression[])children.toArray();
	}
	
	
	protected ParseResult evaluate(ParserState state, String input, int start, int stop)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
