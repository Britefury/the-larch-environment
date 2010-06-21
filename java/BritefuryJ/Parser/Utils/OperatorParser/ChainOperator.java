//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import org.python.core.PyObject;

import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Sequence;

public class ChainOperator extends Operator
{
	InfixChainOperatorParseAction action;
	
	public ChainOperator(ParserExpression opExpression, InfixChainOperatorParseAction action)
	{
		super( opExpression );
		this.action = action;
	}

	public ChainOperator(ParserExpression opExpression, DMObjectClass nodeClass, String fieldName)
	{
		this( opExpression, new InfixChainLevel.BuildASTNodeChainOperatorAction( nodeClass, fieldName ) );
	}

	public ChainOperator(ParserExpression opExpression, PyObject callable)
	{
		this( opExpression, new InfixChainLevel.PyChainOperatorParseAction( callable ) );
	}
	
	public ChainOperator(String operator, InfixChainOperatorParseAction action)
	{
		this( ParserExpression.coerce( operator ), action );
	}

	public ChainOperator(String operator, DMObjectClass nodeClass, String fieldName)
	{
		this( ParserExpression.coerce( operator ), new InfixChainLevel.BuildASTNodeChainOperatorAction( nodeClass, fieldName ) );
	}

	public ChainOperator(String operator, PyObject callable)
	{
		this( ParserExpression.coerce( operator ), new InfixChainLevel.PyChainOperatorParseAction( callable ) );
	}

	
	protected ParserExpression buildParseExpression(ParserExpression right)
	{
		ParserExpression e = new Sequence( new ParserExpression[] { opExpression, right } );
		return e.action( new InfixChainLevel.ChainOpAction( action ) );
	}
}
