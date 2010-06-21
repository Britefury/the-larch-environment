//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.Map;

import org.python.core.Py;
import org.python.core.PyInteger;
import org.python.core.PyObject;

import BritefuryJ.DocModel.DMObjectClass;
import BritefuryJ.DocModel.DMObjectClass.UnknownFieldNameException;
import BritefuryJ.Parser.ParseAction;
import BritefuryJ.Parser.ParserExpression;

public class UnaryOperator extends Operator
{
	protected static class BuildASTNodeAction implements UnaryOperatorParseAction
	{
		private DMObjectClass nodeClass;
		private String fieldNames[];
		
		
		public BuildASTNodeAction(DMObjectClass nodeClass, String fieldName)
		{
			this.nodeClass = nodeClass;
			this.fieldNames = new String[] { fieldName };

			if ( nodeClass.getFieldIndex( fieldName ) == -1 )
			{
				throw new UnknownFieldNameException( fieldName );
			}
		}
		
		
		public Object invoke(Object input, int begin, int end, Object x)
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
	
	protected static class PyUnaryOperatorParseAction implements UnaryOperatorParseAction
	{
		private PyObject callable;
		
		
		public PyUnaryOperatorParseAction(PyObject callable)
		{
			this.callable = callable;
		}

		public Object invoke(Object input, int begin, int end, Object x)
		{
			return callable.__call__( new PyObject[] { Py.java2py( input ), new PyInteger( begin ), new PyInteger( end ), Py.java2py( x ) } );
		}
	}
	
	
	protected static class UnaryOperatorResultBuilder
	{
		private UnaryOperatorParseAction action;
		private int operatorBegin, operatorEnd;
		private Map<String, Object> opBindings;
		
		public UnaryOperatorResultBuilder(UnaryOperatorParseAction action, int operatorBegin, int operatorEnd, Map<String, Object> opBindings)
		{
			this.action = action;
			this.operatorBegin = operatorBegin;
			this.operatorEnd = operatorEnd;
			this.opBindings = opBindings;
		}
		
		
		public int getOperatorBegin()
		{
			return operatorBegin;
		}
		
		public int getOperatorEnd()
		{
			return operatorEnd;
		}
		
		public Map<String, Object> getOperatorBindings()
		{
			return opBindings;
		}
		

		public Object buildResult(Object input, int begin, int end, Object x)
		{
			return action.invoke( input, begin, end, x );
		}
	}
	

	private static class UnaryOpAction implements ParseAction
	{
		private UnaryOperatorParseAction action;
		
		
		public UnaryOpAction(UnaryOperatorParseAction action)
		{
			this.action = action;
		}
		
		
		public Object invoke(Object input, int begin, int end, Object x, Map<String, Object> bindings)
		{
			return new UnaryOperatorResultBuilder( action, begin, end, bindings );
		}
	}



	protected UnaryOpAction action;

	
	
	public UnaryOperator(ParserExpression opExpression, UnaryOperatorParseAction action)
	{
		super( opExpression );
		this.action = new UnaryOpAction( action );
	}

	public UnaryOperator(ParserExpression opExpression, DMObjectClass nodeClass, String fieldName)
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

	public UnaryOperator(String operator, DMObjectClass nodeClass, String fieldName)
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
