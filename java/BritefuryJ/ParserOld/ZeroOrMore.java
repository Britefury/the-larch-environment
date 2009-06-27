//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.ParserOld;


public class ZeroOrMore extends Repetition
{
	public ZeroOrMore(ParserExpression subexp)
	{
		this( subexp, false );
	}

	public ZeroOrMore(ParserExpression subexp, boolean bNullIfZero)
	{
		super( subexp, 0, -1, bNullIfZero );
	}

	public ZeroOrMore(Object subexp) throws ParserCoerceException
	{
		this( subexp, false );
	}

	public ZeroOrMore(Object subexp, boolean bNullIfZero) throws ParserCoerceException
	{
		super( subexp, 0, -1, bNullIfZero );
	}
	


	public String toString()
	{
		return "ZeroOrMore( " + subexp.toString() + " )";
	}
}
