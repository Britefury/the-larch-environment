//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import BritefuryJ.Parser.ParserExpression;

public abstract class Operator
{
	protected ParserExpression opExpression;


	
	protected Operator(ParserExpression opExpression)
	{
		this.opExpression = opExpression;
	}

	protected Operator(String operator)
	{
		this.opExpression = ParserExpression.coerce( operator );
	}
	
	
	
	protected ParserExpression getOperatorExpression()
	{
		return opExpression;
	}
}
