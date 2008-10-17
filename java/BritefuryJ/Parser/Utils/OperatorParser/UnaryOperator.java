//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.Arrays;

import BritefuryJ.Parser.ParserExpression;

abstract class UnaryOperator extends Operator
{
	protected static class DefaultUnaryOperatorParseAction implements UnaryOperatorParseAction
	{
		private String operator;
		
		
		public DefaultUnaryOperatorParseAction(String operator)
		{
			this.operator = operator;
		}
		
		
		public Object invoke(String input, int begin, Object x)
		{
			Object[] xs = { operator, x };
			return Arrays.asList( xs );
		}
	}




	protected UnaryOperator(ParserExpression opExpression)
	{
		super( opExpression );
	}
}
