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

abstract class UnaryOperator extends Operator
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
	
	protected static class PyUnaryOperatorParseAction implements UnaryOperatorParseAction
	{
		private PyObject callable;
		
		
		public PyUnaryOperatorParseAction(PyObject callable)
		{
			this.callable = callable;
		}

		public Object invoke(String input, int begin, Object x)
		{
			return callable.__call__( Py.java2py( input ), new PyInteger( begin ), Py.java2py( x ) );
		}
	}




	protected ParserExpression opExpression;

	protected UnaryOperator(ParserExpression opExpression)
	{
		this.opExpression = opExpression;
	}
}
