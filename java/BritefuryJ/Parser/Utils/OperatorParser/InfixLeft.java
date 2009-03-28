//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.ArrayList;

import org.python.core.PyObject;

import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMObjectClass.InvalidFieldNameException;
import BritefuryJ.Parser.Forward;
import BritefuryJ.Parser.ParserExpression;

public class InfixLeft extends BinaryOperator
{
	//
	// Constructors
	//
	
	public InfixLeft(ParserExpression opExpression, BinaryOperatorParseAction action)
	{
		super( opExpression, action );
	}

	public InfixLeft(String operator, DMObjectClass nodeClass, String leftFieldName, String rightFieldName) throws InvalidFieldNameException
	{
		this( ParserExpression.coerce( operator ), new BuildASTNodeAction( nodeClass, leftFieldName, rightFieldName ) );
	}

	public InfixLeft(String operator, PyObject callable)
	{
		this( ParserExpression.coerce( operator ), new PyBinaryOperatorParseAction( callable ) );
	}

	public InfixLeft(ParserExpression opExpression, DMObjectClass nodeClass, String leftFieldName, String rightFieldName) throws InvalidFieldNameException
	{
		this( opExpression, new BuildASTNodeAction( nodeClass, leftFieldName, rightFieldName ) );
	}

	public InfixLeft(ParserExpression opExpression, PyObject callable)
	{
		this( opExpression, new PyBinaryOperatorParseAction( callable ) );
	}

	
	
	protected ParserExpression buildOperatorParser(OperatorTable operatorTable,
			ArrayList<Forward> levelParserForwardDeclarations, PrecedenceLevel thisLevel,
			ParserExpression thisLevelParser, PrecedenceLevel previousLevel,
			ParserExpression previousLevelParser)
	{
		ParserExpression prefix = operatorTable.getLowestPrecedenceUnaryOperatorLevelParserAbove( levelParserForwardDeclarations, thisLevel, new OperatorTable.PrefixFilter() );
		ParserExpression right;
		
		if ( prefix != null )
		{
			right = previousLevelParser.__or__( prefix );
		}
		else
		{
			right = previousLevelParser;
		}
		
		return thisLevelParser.__add__( opExpression ).__add__( right );
	}
}
