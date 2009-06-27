//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.ParserOld.ParserExpression;

public abstract class BinaryOperatorLevel extends OperatorLevel
{
	protected ArrayList<BinaryOperator> operators;
	
	
	//
	//
	// Constructor
	//
	//
	
	public BinaryOperatorLevel(List<BinaryOperator> ops)
	{
		operators = new ArrayList<BinaryOperator>();
		operators.addAll( ops );
	}


	protected ParserExpression buildParserForReachUp(OperatorTable operatorTable, ParserExpression previousLevelParser)
	{
		return null;
	}
}
