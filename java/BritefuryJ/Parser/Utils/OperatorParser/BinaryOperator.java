//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.ArrayList;
import java.util.List;

import org.python.core.Py;
import org.python.core.PyInteger;
import org.python.core.PyObject;

import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMObjectClass.InvalidFieldNameException;
import BritefuryJ.Parser.Forward;
import BritefuryJ.Parser.ParseAction;
import BritefuryJ.Parser.ParserExpression;

abstract class BinaryOperator extends Operator
{
	private class BinaryOpAction implements ParseAction
	{
		private BinaryOperatorParseAction action;
		
		
		public BinaryOpAction(BinaryOperatorParseAction action)
		{
			this.action = action;
		}
		
		
		@SuppressWarnings("unchecked")
		public Object invoke(String input, int begin, Object x)
		{
			List<Object> xs = (List<Object>)x;
			
			return action.invoke( input, begin, xs.get( 0 ), xs.get( 2 ) );
		}
	}
	
	
	protected static class BuildASTNodeAction implements BinaryOperatorParseAction
	{
		private DMObjectClass nodeClass;
		private String fieldNames[];
		
		
		public BuildASTNodeAction(DMObjectClass nodeClass, String leftFieldName, String rightFieldName) throws InvalidFieldNameException
		{
			this.nodeClass = nodeClass;
			this.fieldNames = new String[] { leftFieldName, rightFieldName };
			
			if ( nodeClass.getFieldIndex( leftFieldName ) == -1 )
			{
				throw new InvalidFieldNameException( leftFieldName );
			}

			if ( nodeClass.getFieldIndex( rightFieldName ) == -1 )
			{
				throw new InvalidFieldNameException( rightFieldName );
			}
		}
		
		
		public Object invoke(String input, int begin, Object x, Object y)
		{
			try
			{
				return nodeClass.newInstance( fieldNames, new Object[] { x, y } );
			}
			catch (InvalidFieldNameException e)
			{
				throw new RuntimeException( "This should not have happened." );
			}
		}
	}
	
	protected static class PyBinaryOperatorParseAction implements BinaryOperatorParseAction
	{
		private PyObject callable;
		
		
		public PyBinaryOperatorParseAction(PyObject callable)
		{
			this.callable = callable;
		}

		public Object invoke(String input, int begin, Object x, Object y)
		{
			return callable.__call__( Py.java2py( input ), new PyInteger( begin ), Py.java2py( x ), Py.java2py( y ) );
		}
	}

	
	
	protected ParserExpression opExpression;
	protected BinaryOperatorParseAction action;
	
	
	
	
	//
	// Constructors
	//
	
	protected BinaryOperator(ParserExpression opExpression, BinaryOperatorParseAction action)
	{
		this.opExpression = opExpression;
		this.action = action;
	}

	
	
	
	protected abstract ParserExpression buildOperatorParser(ParserExpression thisLevelParser, ParserExpression previousLevelParser);

	protected abstract ParserExpression buildOperatorParserWithReachUp(OperatorTable operatorTable, ArrayList<Forward> levelParserForwardDeclarations, PrecedenceLevel thisLevel,
			ParserExpression thisLevelParser, PrecedenceLevel previousLevel, ParserExpression previousLevelParser);


	protected ParserExpression buildParser(ParserExpression thisLevelParser, ParserExpression previousLevelParser)
	{
		ParserExpression p = buildOperatorParser( thisLevelParser, previousLevelParser );
		return p.action( new BinaryOpAction( action ) );
	}

	protected ParserExpression buildParserWithReachUp(OperatorTable operatorTable,
			ArrayList<Forward> levelParserForwardDeclarations, PrecedenceLevel thisLevel,
			ParserExpression thisLevelParser, PrecedenceLevel previousLevel,
			ParserExpression previousLevelParser)
	{
		ParserExpression p = buildOperatorParserWithReachUp( operatorTable, levelParserForwardDeclarations, thisLevel, thisLevelParser, previousLevel, previousLevelParser );
		return p.action( new BinaryOpAction( action ) );
	}
}
