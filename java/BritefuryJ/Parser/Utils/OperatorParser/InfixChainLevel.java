//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.python.core.Py;
import org.python.core.PyInteger;
import org.python.core.PyObject;

import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMObjectClass.UnknownFieldNameException;
import BritefuryJ.Parser.Choice;
import BritefuryJ.Parser.ParseAction;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Production;
import BritefuryJ.Parser.Sequence;

public class InfixChainLevel extends OperatorLevel
{
	// Build AST Node infix chain action
	protected static class BuildASTNodeInfixChainAction implements InfixChainParseAction
	{
		private DMObjectClass nodeClass;
		private String fieldNames[];
		
		
		public BuildASTNodeInfixChainAction(DMObjectClass nodeClass, String leftFieldName, String rightListFieldName)
		{
			this.nodeClass = nodeClass;
			this.fieldNames = new String[] { leftFieldName, rightListFieldName };

			if ( nodeClass.getFieldIndex( leftFieldName ) == -1 )
			{
				throw new UnknownFieldNameException( leftFieldName );
			}

			if ( nodeClass.getFieldIndex( rightListFieldName ) == -1 )
			{
				throw new UnknownFieldNameException( rightListFieldName );
			}
		}
		
		
		public Object invoke(Object input, int begin, int end, Object x, List<Object> ys)
		{
			try
			{
				return nodeClass.newInstance( fieldNames, new Object[] { x, ys } );
			}
			catch (UnknownFieldNameException e)
			{
				throw new RuntimeException( "This should not have happened." );
			}
		}
	}

	// Python infix chain action
	protected static class PyInfixChainParseAction implements InfixChainParseAction
	{
		private PyObject callable;
		
		
		public PyInfixChainParseAction(PyObject callable)
		{
			this.callable = callable;
		}

		public Object invoke(Object input, int begin, int end, Object x, List<Object> ys)
		{
			return callable.__call__( new PyObject[] { Py.java2py( input ), new PyInteger( begin ), new PyInteger( end ), Py.java2py( x ), Py.java2py( ys ) } );
		}
	}

	
	
	
	// A parse action that wraps an InfixChainParseAction
	private static class InfixChainAction implements ParseAction
	{
		private InfixChainParseAction action;
		
		
		public InfixChainAction(InfixChainParseAction action)
		{
			this.action = action;
		}
		
		
		@SuppressWarnings("unchecked")
		public Object invoke(Object input, int begin, int end, Object x, Map<String, Object> bindings)
		{
			List<Object> xs = (List<Object>)x;
			List<Object> ys = (List<Object>)xs.get( 1 );

			return action.invoke( input, begin, end, xs.get( 0 ), ys );
		}
	}

	
	
	
	
	// Build AST Node chain action
	protected static class BuildASTNodeChainOperatorAction implements InfixChainOperatorParseAction
	{
		private DMObjectClass nodeClass;
		private String fieldNames[];
		
		
		public BuildASTNodeChainOperatorAction(DMObjectClass nodeClass, String fieldName)
		{
			this.nodeClass = nodeClass;
			this.fieldNames = new String[] { fieldName };

			if ( nodeClass.getFieldIndex( fieldName ) == -1 )
			{
				throw new UnknownFieldNameException( fieldName );
			}
		}
		
		
		public Object invoke(Object input, int begin, int end, Object x, Object opValue)
		{
			try
			{
				return nodeClass.newInstance( fieldNames, new Object[] { x } );
			}
			catch (UnknownFieldNameException e)
			{
				throw new RuntimeException( "This should not have happened." );
			}
		}
	}

	// Python chain operator action
	protected static class PyChainOperatorParseAction implements InfixChainOperatorParseAction
	{
		private PyObject callable;
		
		
		public PyChainOperatorParseAction(PyObject callable)
		{
			this.callable = callable;
		}

		public Object invoke(Object input, int begin, int end, Object x, Object opValue)
		{
			return callable.__call__( new PyObject[] { Py.java2py( input ), new PyInteger( begin ), new PyInteger( end ), Py.java2py( x ), Py.java2py( opValue ) } );
		}
	}

	
	
	// The parse action to supply to the Action node
	protected static class ChainOpAction implements ParseAction
	{
		private InfixChainOperatorParseAction action;
		
		
		public ChainOpAction(InfixChainOperatorParseAction action)
		{
			this.action = action;
		}
		
		
		@SuppressWarnings("unchecked")
		public Object invoke(Object input, int begin, int end, Object x, Map<String, Object> bindings)
		{
			List<Object> xs = (List<Object>)x;

			return action.invoke( input, begin, end, xs.get( 1 ), xs.get(0) );
		}
	}

	
	
	

	
	
	
	private List<ChainOperator> operators;
	private InfixChainAction action;
	
	
	public InfixChainLevel(List<ChainOperator> operators, InfixChainParseAction action)
	{
		this.operators = operators;
		
		this.action = new InfixChainAction( action );
	}
	
	public InfixChainLevel(List<ChainOperator> operators, DMObjectClass nodeClass, String leftFieldName, String rightListFieldName)
	{
		this( operators, new BuildASTNodeInfixChainAction( nodeClass, leftFieldName, rightListFieldName ) );
	}

	public InfixChainLevel(List<ChainOperator> operators, PyObject callable)
	{
		this( operators, new PyInfixChainParseAction( callable ) );
	}


	
	
	protected ParserExpression buildParser(OperatorTable operatorTable, ParserExpression previousLevelParser, ArrayList<Production> reachupForwardDeclarations)
	{
		ParserExpression rightSubexp = previousLevelParser;

		// <rightExp0> | <rightExp1> | ... | <rightExpN>
		ParserExpression[] rightOpExps = new ParserExpression[operators.size()];
		for (int i = 0; i < operators.size(); i++)
		{
			ChainOperator operator = operators.get( i );
			rightOpExps[i] = operator.buildParseExpression( rightSubexp );
		}
		Choice rightChoice = new Choice( rightOpExps );
		

		// <thisLeverParser> <rightChoice>+
		ParserExpression p = new Sequence( new ParserExpression[] { previousLevelParser, rightChoice.oneOrMore() } );
		// => action
		return p.action( action );
	}

	protected ParserExpression buildParserForReachUp(OperatorTable operatorTable, ParserExpression previousLevelParser)
	{
		return null;
	}
}
