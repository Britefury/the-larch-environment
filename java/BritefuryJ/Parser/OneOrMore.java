//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser;


public class OneOrMore extends Repetition
{
	public OneOrMore(ParserExpression subexp)
	{
		super( subexp, 1, -1 );
	}

	public OneOrMore(Object subexp) throws ParserCoerceException
	{
		super( subexp, 1, -1 );
	}



	public String toString()
	{
		return "OneOrMore( " + subexp.toString() + " )";
	}
}
