//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.List;
import java.util.Vector;

import BritefuryJ.Parser.Choice;
import BritefuryJ.Parser.Forward;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Production;

public class PrecedenceLevel
{
	public static class OperatorParserPrecedenceLevelCannotMixOperatorTypesError extends Exception
	{
		private static final long serialVersionUID = 1L;
	}
	
	
	private Vector<Operator> operators;
	
	
	public PrecedenceLevel(List<Operator> ops) throws OperatorParserPrecedenceLevelCannotMixOperatorTypesError
	{
		Object operatorClass = null;
		for (Operator op: ops)
		{
			Object c = op.getClass();
			if ( operatorClass == null )
			{
				operatorClass = c;
			}
			else
			{
				if ( c != operatorClass )
				{
					throw new OperatorParserPrecedenceLevelCannotMixOperatorTypesError();
				}
			}
		}
		
		operators = new Vector<Operator>();
		operators.addAll( ops );
	}
	
	
	protected boolean checkOperators(OperatorTable.OperatorFilter filter)
	{
		if ( operators.size() > 0 )
		{
			return filter.test( operators.get( 0 ) );
		}
		else
		{
			return false;
		}
	}
	
	
	protected void buildParser(OperatorTable operatorTable, Vector<Forward> levelParserForwardDeclarations, Forward forwardDeclaration,
			PrecedenceLevel previousLevel, ParserExpression previousLevelParser)
	{
		ParserExpression[] choices = new ParserExpression[operators.size() + 1];
		for (int i = 0; i < operators.size(); i++)
		{
			choices[i] = operators.get( i ).buildParser( operatorTable, levelParserForwardDeclarations, this, forwardDeclaration, previousLevel, previousLevelParser );
		}
		choices[operators.size()] = previousLevelParser;
		
		forwardDeclaration.setExpression( new Production( new Choice( choices ) ) );
	}
}
