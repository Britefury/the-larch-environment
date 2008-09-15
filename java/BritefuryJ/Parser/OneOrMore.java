//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser;

import java.util.List;

public class OneOrMore extends Repetition
{
	public OneOrMore(String subexp)
	{
		super( subexp, 1, -1 );
	}

	public OneOrMore(List<Object> subexp) throws ParserCoerceException
	{
		super( subexp, 1, -1 );
	}

	public OneOrMore(ParserExpression subexp)
	{
		super( subexp, 1, -1 );
	}



	public String toString()
	{
		return "OneOrMore( " + subexp.toString() + " )";
	}
}
