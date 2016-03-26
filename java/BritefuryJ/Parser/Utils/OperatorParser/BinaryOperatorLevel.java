//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.ArrayList;
import java.util.Arrays;

import BritefuryJ.Parser.ParserExpression;

public abstract class BinaryOperatorLevel extends OperatorLevel
{
	protected ArrayList<BinaryOperator> operators;
	
	
	//
	//
	// Constructor
	//
	//
	
	public BinaryOperatorLevel(BinaryOperator ops[])
	{
		operators = new ArrayList<BinaryOperator>();
		operators.addAll( Arrays.asList( ops ) );
	}


	protected ParserExpression buildParserForReachUp(OperatorTable operatorTable, ParserExpression previousLevelParser)
	{
		return null;
	}
}
