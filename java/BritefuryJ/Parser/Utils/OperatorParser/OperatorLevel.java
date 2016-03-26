//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.ArrayList;

import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Production;


public abstract class OperatorLevel
{
	protected abstract ParserExpression buildParser(OperatorTable operatorTable, ParserExpression previousLevelParser, ArrayList<Production> reachupForwardDeclarations);
	protected abstract ParserExpression buildParserForReachUp(OperatorTable operatorTable, ParserExpression previousLevelParser);
}
