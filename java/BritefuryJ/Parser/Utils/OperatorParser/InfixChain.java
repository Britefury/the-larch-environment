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
import BritefuryJ.Parser.Choice;
import BritefuryJ.Parser.Forward;
import BritefuryJ.Parser.ParseAction;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Sequence;

public class InfixChain extends Operator
{
	// Infix chain action interface
	public interface InfixChainParseAction
	{
		public Object invoke(String input, int begin, Object x, List<Object> ys);
	}
	
	// Build AST Node infix chain action
	private static class BuildASTNodeInfixChainAction implements InfixChainParseAction
	{
		private DMObjectClass nodeClass;
		private String fieldNames[];
		
		
		public BuildASTNodeInfixChainAction(DMObjectClass nodeClass, String leftFieldName, String rightListFieldName) throws InvalidFieldNameException
		{
			this.nodeClass = nodeClass;
			this.fieldNames = new String[] { leftFieldName, rightListFieldName };

			if ( nodeClass.getFieldIndex( leftFieldName ) == -1 )
			{
				throw new InvalidFieldNameException( leftFieldName );
			}

			if ( nodeClass.getFieldIndex( rightListFieldName ) == -1 )
			{
				throw new InvalidFieldNameException( rightListFieldName );
			}
		}
		
		
		public Object invoke(String input, int begin, Object x, List<Object> ys)
		{
			try
			{
				return nodeClass.newInstance( fieldNames, new Object[] { x, ys } );
			}
			catch (InvalidFieldNameException e)
			{
				throw new RuntimeException( "This should not have happened." );
			}
		}
	}

	// Python infix chain action
	private static class PyInfixChainParseAction implements InfixChainParseAction
	{
		private PyObject callable;
		
		
		public PyInfixChainParseAction(PyObject callable)
		{
			this.callable = callable;
		}

		public Object invoke(String input, int begin, Object x, List<Object> ys)
		{
			return callable.__call__( Py.java2py( input ), new PyInteger( begin ), Py.java2py( x ), Py.java2py( ys ) );
		}
	}

	
	// The parse action to supply to the Action node
	private static class InfixChainAction implements ParseAction
	{
		private InfixChainParseAction action;
		
		
		public InfixChainAction(InfixChainParseAction action)
		{
			this.action = action;
		}
		
		
		@SuppressWarnings("unchecked")
		public Object invoke(String input, int begin, Object x)
		{
			List<Object> xs = (List<Object>)x;
			List<Object> ys = (List<Object>)xs.get( 1 );

			return action.invoke( input, begin, xs.get( 0 ), ys );
		}
	}

	
	
	
	
	// Chain operator action interface
	public interface ChainOperatorParseAction
	{
		public Object invoke(String input, int begin, Object x);
	}
	
	// Build AST Node chain action
	private static class BuildASTNodeChainOperatorAction implements ChainOperatorParseAction
	{
		private DMObjectClass nodeClass;
		private String fieldNames[];
		
		
		public BuildASTNodeChainOperatorAction(DMObjectClass nodeClass, String fieldName) throws InvalidFieldNameException
		{
			this.nodeClass = nodeClass;
			this.fieldNames = new String[] { fieldName };

			if ( nodeClass.getFieldIndex( fieldName ) == -1 )
			{
				throw new InvalidFieldNameException( fieldName );
			}
		}
		
		
		public Object invoke(String input, int begin, Object x)
		{
			try
			{
				return nodeClass.newInstance( fieldNames, new Object[] { x } );
			}
			catch (InvalidFieldNameException e)
			{
				throw new RuntimeException( "This should not have happened." );
			}
		}
	}

	// Python chain operator action
	private static class PyChainOperatorParseAction implements ChainOperatorParseAction
	{
		private PyObject callable;
		
		
		public PyChainOperatorParseAction(PyObject callable)
		{
			this.callable = callable;
		}

		public Object invoke(String input, int begin, Object x)
		{
			return callable.__call__( Py.java2py( input ), new PyInteger( begin ), Py.java2py( x ) );
		}
	}

	
	
	// The parse action to supply to the Action node
	private static class ChainOpAction implements ParseAction
	{
		private ChainOperatorParseAction action;
		
		
		public ChainOpAction(ChainOperatorParseAction action)
		{
			this.action = action;
		}
		
		
		@SuppressWarnings("unchecked")
		public Object invoke(String input, int begin, Object x)
		{
			List<Object> xs = (List<Object>)x;

			return action.invoke( input, begin, xs.get( 1 ) );
		}
	}

	

	// Chain operator - defines an operator that can be used - supply a list of these to the InfixChain constructor
	public static class ChainOperator extends Object
	{
		ParserExpression opExpression;
		ChainOperatorParseAction action;
		
		public ChainOperator(ParserExpression opExpression, ChainOperatorParseAction action)
		{
			this.opExpression = opExpression;
			this.action = action;
		}

		public ChainOperator(String operator, DMObjectClass nodeClass, String fieldName) throws InvalidFieldNameException
		{
			this( ParserExpression.coerce( operator ), new BuildASTNodeChainOperatorAction( nodeClass, fieldName ) );
		}

		public ChainOperator(String operator, PyObject callable)
		{
			this( ParserExpression.coerce( operator ), new PyChainOperatorParseAction( callable ) );
		}

		public ChainOperator(ParserExpression opExpression, DMObjectClass nodeClass, String fieldName) throws InvalidFieldNameException
		{
			this( opExpression, new BuildASTNodeChainOperatorAction( nodeClass, fieldName ) );
		}

		public ChainOperator(ParserExpression opExpression, PyObject callable)
		{
			this( opExpression, new PyChainOperatorParseAction( callable ) );
		}
		
		
		private ParserExpression buildParseExpression(ParserExpression right)
		{
			ParserExpression e = new Sequence( new ParserExpression[] { opExpression, right } );
			return e.action( new ChainOpAction( action ) );
		}
	}

	
	
	
	
	
	
	
	
	private List<ChainOperator> operators;
	private InfixChainAction action;
	
	
	public InfixChain(List<ChainOperator> operators, InfixChainParseAction action)
	{
		this.operators = operators;
		
		this.action = new InfixChainAction( action );
	}
	
	public InfixChain(List<ChainOperator> operators, DMObjectClass nodeClass, String leftFieldName, String rightListFieldName) throws InvalidFieldNameException
	{
		this( operators, new BuildASTNodeInfixChainAction( nodeClass, leftFieldName, rightListFieldName ) );
	}

	public InfixChain(List<ChainOperator> operators, PyObject callable)
	{
		this( operators, new PyInfixChainParseAction( callable ) );
	}


	
	
	protected ParserExpression buildParser(ParserExpression thisLevelParser, ParserExpression previousLevelParser)
	{
		// Copied from InfixLeft
		ParserExpression rightSubexp = previousLevelParser;
		
		// <rightExp0> | <rightExp1> | ... | <rightExpN>
		ParserExpression rightOpExps[] = new ParserExpression[operators.size()];
		int i = 0;
		for (ChainOperator operator: operators)
		{
			rightOpExps[i++] = operator.buildParseExpression( rightSubexp );
		}
		Choice rightChoice = new Choice( rightOpExps );
		
		
		// <thisLeverParser> <rightChoice>+
		ParserExpression p = new Sequence( new ParserExpression[] { thisLevelParser, rightChoice.oneOrMore() } );
		// => action
		return p.action( action );
	}

	
	protected ParserExpression buildParserWithReachUp(OperatorTable operatorTable,
			ArrayList<Forward> levelParserForwardDeclarations, PrecedenceLevel thisLevel,
			ParserExpression thisLevelParser, PrecedenceLevel previousLevel,
			ParserExpression previousLevelParser)
	{
		// Copied from InfixLeft
		ParserExpression prefix = operatorTable.getLowestPrecedenceUnaryOperatorLevelParserAbove( levelParserForwardDeclarations, thisLevel, new OperatorTable.PrefixFilter() );
		ParserExpression rightSubexp;
		
		if ( prefix != null )
		{
			rightSubexp = previousLevelParser.__or__( prefix );
		}
		else
		{
			rightSubexp = previousLevelParser;
		}
		

		// <rightExp0> | <rightExp1> | ... | <rightExpN>
		ParserExpression rightOpExps[] = new ParserExpression[operators.size()];
		int i = 0;
		for (ChainOperator operator: operators)
		{
			rightOpExps[i++] = operator.buildParseExpression( rightSubexp );
		}
		Choice rightChoice = new Choice( rightOpExps );
		
		
		// <thisLeverParser> <rightChoice>+
		ParserExpression p = new Sequence( new ParserExpression[] { thisLevelParser, rightChoice.oneOrMore() } );
		// => action
		return p.action( action );
	}
}
