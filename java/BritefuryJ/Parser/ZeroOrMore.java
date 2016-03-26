//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser;


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
