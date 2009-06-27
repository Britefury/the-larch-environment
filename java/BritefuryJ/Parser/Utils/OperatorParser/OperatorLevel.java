//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.ArrayList;

import BritefuryJ.ParserOld.ParserExpression;
import BritefuryJ.ParserOld.Production;


public abstract class OperatorLevel
{
	protected abstract ParserExpression buildParser(OperatorTable operatorTable, ParserExpression previousLevelParser, ArrayList<Production> reachupForwardDeclarations);
	protected abstract ParserExpression buildParserForReachUp(OperatorTable operatorTable, ParserExpression previousLevelParser);
}
