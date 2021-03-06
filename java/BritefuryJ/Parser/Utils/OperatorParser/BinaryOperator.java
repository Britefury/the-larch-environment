//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import org.python.core.Py;
import org.python.core.PyInteger;
import org.python.core.PyObject;

import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMObjectClass.UnknownFieldNameException;
import BritefuryJ.Parser.ParserExpression;

public class BinaryOperator extends Operator
{
	protected static class BuildASTNodeAction implements BinaryOperatorParseAction
	{
		private DMObjectClass nodeClass;
		private String fieldNames[];
		
		
		public BuildASTNodeAction(DMObjectClass nodeClass, String leftFieldName, String rightFieldName)
		{
			this.nodeClass = nodeClass;
			this.fieldNames = new String[] { leftFieldName, rightFieldName };
			
			if ( nodeClass.getFieldIndex( leftFieldName ) == -1 )
			{
				throw new UnknownFieldNameException( leftFieldName );
			}

			if ( nodeClass.getFieldIndex( rightFieldName ) == -1 )
			{
				throw new UnknownFieldNameException( rightFieldName );
			}
		}
		
		
		public Object invoke(Object input, int begin, int end, Object x, Object y)
		{
			try
			{
				return nodeClass.newInstance( fieldNames, new Object[] { x, y } );
			}
			catch (UnknownFieldNameException e)
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

		public Object invoke(Object input, int begin, int end, Object left, Object right)
		{
			return callable.__call__( new PyObject[] { Py.java2py( input ), new PyInteger( begin ), new PyInteger( end ), Py.java2py( left ), Py.java2py( right ) } );
		}
	}

	
	
	protected BinaryOperatorParseAction action;
	
	
	
	//
	// Constructors
	//
	
	public BinaryOperator(ParserExpression opExpression, BinaryOperatorParseAction action)
	{
		super( opExpression );
		this.action = action;
	}

	public BinaryOperator(ParserExpression opExpression, DMObjectClass nodeClass, String leftFieldName, String rightFieldName)
	{
		this( opExpression, new BuildASTNodeAction( nodeClass, leftFieldName, rightFieldName ) );
	}

	public BinaryOperator(ParserExpression opExpression, PyObject callable)
	{
		this( opExpression, new PyBinaryOperatorParseAction( callable ) );
	}
	
	public BinaryOperator(String operator, BinaryOperatorParseAction action)
	{
		super( operator );
		this.action = action;
	}

	public BinaryOperator(String operator, DMObjectClass nodeClass, String leftFieldName, String rightFieldName)
	{
		this( ParserExpression.coerce( operator ), new BuildASTNodeAction( nodeClass, leftFieldName, rightFieldName ) );
	}

	public BinaryOperator(String operator, PyObject callable)
	{
		this( ParserExpression.coerce( operator ), new PyBinaryOperatorParseAction( callable ) );
	}
	
	
	protected BinaryOperatorParseAction getAction()
	{
		return action;
	}
}
