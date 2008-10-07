//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.ArrayList;

import BritefuryJ.Parser.Forward;
import BritefuryJ.Parser.ParserExpression;

public abstract class Operator
{
	protected ParserExpression opExpression;
	
	
	protected Operator(ParserExpression opExpression)
	{
		this.opExpression = opExpression;
	}
	
	
	
	protected abstract ParserExpression buildParser(OperatorTable operatorTable, ArrayList<Forward> levelParserForwardDeclarations, PrecedenceLevel thisLevel,
			ParserExpression thisLevelParser, PrecedenceLevel previousLevel, ParserExpression previousLevelParser);
}
