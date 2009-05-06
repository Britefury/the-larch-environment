//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.List;
import java.util.ArrayList;

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
	
	
	private ArrayList<Operator> operators;
	
	
	
	//
	//
	// Constructor
	//
	//
	
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
		
		operators = new ArrayList<Operator>();
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
	
	
	protected ParserExpression buildParser(OperatorTable operatorTable, PrecedenceLevel previousLevel, ParserExpression previousLevelParser, String debugName)
	{
		Forward thisLevelForward = new Forward();

		ParserExpression[] choices = new ParserExpression[operators.size()+1];
		for (int i = 0; i < operators.size(); i++)
		{
			choices[i] = operators.get( i ).buildParser( thisLevelForward, previousLevelParser );
		}
		choices[operators.size()] = previousLevelParser;
		
		thisLevelForward.setExpression( new Production( new Choice( choices ) ).debug( debugName ) );
		
		return thisLevelForward;
	}


	protected ParserExpression buildParserWithReachUp(OperatorTable operatorTable, ArrayList<Forward> levelParserOpOnlyForwardDeclarations, Forward opOnlyForwardDeclaration,
			PrecedenceLevel previousLevel, ParserExpression previousLevelParser, String debugName)
	{
		Forward thisLevelForward = new Forward();

		ParserExpression[] choices = new ParserExpression[operators.size()];
		for (int i = 0; i < operators.size(); i++)
		{
			choices[i] = operators.get( i ).buildParserWithReachUp( operatorTable, levelParserOpOnlyForwardDeclarations, this, thisLevelForward, previousLevel, previousLevelParser );
		}
		
		// Parser expression representing a choice between the various operators, but *NOT* the previous level parser
		ParserExpression thisLevelOperators = new Choice( choices );
		
		ParserExpression thisLevel = new Production( thisLevelOperators ).debug( debugName + "_oponly" );
		
		opOnlyForwardDeclaration.setExpression( thisLevel );
		thisLevelForward.setExpression( new Production( new Choice( new ParserExpression[] { thisLevel, previousLevelParser } ) ).debug( debugName ) );
		
		return thisLevelForward;
	}
}
