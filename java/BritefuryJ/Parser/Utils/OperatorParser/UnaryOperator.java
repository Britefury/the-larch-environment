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
import BritefuryJ.Parser.ParseAction;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;

public class UnaryOperator extends Operator
{
	protected static class BuildASTNodeAction implements UnaryOperatorParseAction
	{
		private DMObjectClass nodeClass;
		private String fieldNames[];
		
		
		public BuildASTNodeAction(DMObjectClass nodeClass, String fieldName) throws InvalidFieldNameException
		{
			this.nodeClass = nodeClass;
			this.fieldNames = new String[] { fieldName };

			if ( nodeClass.getFieldIndex( fieldName ) == -1 )
			{
				throw new InvalidFieldNameException( fieldName );
			}
		}
		
		
		public Object invoke(ItemStreamAccessor input, int begin, Object x)
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
	
	protected static class PyUnaryOperatorParseAction implements UnaryOperatorParseAction
	{
		private PyObject callable;
		
		
		public PyUnaryOperatorParseAction(PyObject callable)
		{
			this.callable = callable;
		}

		public Object invoke(ItemStreamAccessor input, int begin, Object x)
		{
			return callable.__call__( Py.java2py( input ), new PyInteger( begin ), Py.java2py( x ) );
		}
	}
	
	
	protected static class UnaryOperatorResultBuilder
	{
		private UnaryOperatorParseAction action;
		private int operatorPos;
		
		public UnaryOperatorResultBuilder(UnaryOperatorParseAction action, int operatorPos)
		{
			this.action = action;
			this.operatorPos = operatorPos;
		}
		
		
		public int getOperatorPos()
		{
			return operatorPos;
		}
		

		public Object buildResult(ItemStreamAccessor input, int begin, Object x)
		{
			return action.invoke( input, begin, x );
		}
	}
	

	private static class UnaryOpAction implements ParseAction
	{
		private UnaryOperatorParseAction action;
		
		
		public UnaryOpAction(UnaryOperatorParseAction action)
		{
			this.action = action;
		}
		
		
		public Object invoke(ItemStreamAccessor input, int begin, Object x)
		{
			return new UnaryOperatorResultBuilder( action, begin );
		}
	}



	protected UnaryOpAction action;

	
	
	public UnaryOperator(ParserExpression opExpression, UnaryOperatorParseAction action)
	{
		super( opExpression );
		this.action = new UnaryOpAction( action );
	}

	public UnaryOperator(ParserExpression opExpression, DMObjectClass nodeClass, String fieldName) throws InvalidFieldNameException
	{
		this( opExpression, new BuildASTNodeAction( nodeClass, fieldName ) );
	}

	public UnaryOperator(ParserExpression opExpression, PyObject callable)
	{
		this( opExpression, new PyUnaryOperatorParseAction( callable ) );
	}

	public UnaryOperator(String operator, UnaryOperatorParseAction action)
	{
		super( operator );
		this.action = new UnaryOpAction( action );
	}

	public UnaryOperator(String operator, DMObjectClass nodeClass, String fieldName) throws InvalidFieldNameException
	{
		this( ParserExpression.coerce( operator ), new BuildASTNodeAction( nodeClass, fieldName ) );
	}

	public UnaryOperator(String operator, PyObject callable)
	{
		this( ParserExpression.coerce( operator ), new PyUnaryOperatorParseAction( callable ) );
	}
	
	
	
	
	protected ParserExpression applyAction(ParserExpression x)
	{
		return x.action( action );
	}
}
