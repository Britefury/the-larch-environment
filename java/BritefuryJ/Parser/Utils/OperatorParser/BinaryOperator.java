//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import org.python.core.Py;
import org.python.core.PyInteger;
import org.python.core.PyObject;

import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMObjectClass.InvalidFieldNameException;
import BritefuryJ.Parser.ParserExpression;

public class BinaryOperator extends Operator
{
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

	
	
	protected BinaryOperatorParseAction action;
	
	
	
	//
	// Constructors
	//
	
	protected BinaryOperator(ParserExpression opExpression, BinaryOperatorParseAction action)
	{
		super( opExpression );
		this.action = action;
	}

	public BinaryOperator(ParserExpression opExpression, DMObjectClass nodeClass, String leftFieldName, String rightFieldName) throws InvalidFieldNameException
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

	public BinaryOperator(String operator, DMObjectClass nodeClass, String leftFieldName, String rightFieldName) throws InvalidFieldNameException
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
