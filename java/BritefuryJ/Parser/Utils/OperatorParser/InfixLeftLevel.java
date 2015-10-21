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

import BritefuryJ.Parser.Choice;
import BritefuryJ.Parser.ParseAction;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Production;

public class InfixLeftLevel extends BinaryOperatorLevel
{
	protected static class InfixLeftOperatorResultBuilder
	{
		private BinaryOperatorParseAction action;
		private int operatorPos, rightEnd;
		private Object opValue, right;
		
		public InfixLeftOperatorResultBuilder(BinaryOperatorParseAction action, int operatorPos, int rightEnd, Object opValue, Object right)
		{
			this.action = action;
			this.operatorPos = operatorPos;
			this.rightEnd = rightEnd;
			this.opValue = opValue;
			this.right = right;
		}
		
		
		public int getOperatorPos()
		{
			return operatorPos;
		}
		

		public Object buildResult(Object input, int begin, Object left)
		{
			return action.invoke( input, begin, rightEnd, left, opValue, right );
		}
	}
	

	private static class InfixLeftOpLevelAction implements ParseAction
	{
		public InfixLeftOpLevelAction()
		{
		}
		
		
		@SuppressWarnings("unchecked")
		public Object invoke(Object input, int begin, int end, Object x, Map<String, Object> bindings)
		{
			List<Object> xs = (List<Object>)x;
			
			Object expression = xs.get( 0 );		// Left sub-expression
			List<InfixLeftOperatorResultBuilder> builders = (List<InfixLeftOperatorResultBuilder>)xs.get( 1 );		// Builders
			
			for (InfixLeftOperatorResultBuilder b: builders)
			{
				expression = b.buildResult( input, begin, expression );
			}
			
			return expression;
		}
	}
	
	
	//
	//
	// Constructs a InfixLeftOperatorResultBuilder for each operator-right pair  
	//
	//
	private static class InfixLeftOpAction implements ParseAction
	{
		private BinaryOperatorParseAction action;
		
		
		public InfixLeftOpAction(BinaryOperatorParseAction action)
		{
			this.action = action;
		}
		
		
		@SuppressWarnings("unchecked")
		public Object invoke(Object input, int begin, int end, Object x, Map<String, Object> bindings)
		{
			List<Object> xs = (List<Object>)x;
			Object opValue = xs.get(0);
			Object right = xs.get( 1 );
			return new InfixLeftOperatorResultBuilder( action, begin, end, opValue, right );
		}
	}


	
	
	
	InfixLeftOpLevelAction levelAction;

	
	//
	//
	// Constructor
	//
	//
	
	public InfixLeftLevel(BinaryOperator ops[])
	{
		super( ops );
		
		levelAction = new InfixLeftOpLevelAction();
	}
	
	
	protected ParserExpression buildParser(OperatorTable operatorTable, ParserExpression previousLevelParser, ArrayList<Production> reachupForwardDeclarations)
	{
		ParserExpression prefix = operatorTable.getPrefixLevelForReachUp( reachupForwardDeclarations, this );
		
		ParserExpression left = previousLevelParser, right = null;
		
		if ( prefix != null )
		{
			right = prefix.__or__( previousLevelParser );
		}
		else
		{
			right = previousLevelParser;
		}


		ParserExpression[] choices = new ParserExpression[operators.size()];
		for (int i = 0; i < operators.size(); i++)
		{
			BinaryOperator operator = operators.get( i );
			ParserExpression opRight = operator.getOperatorExpression().__add__( right );
			choices[i] = opRight.action( new InfixLeftOpAction( operator.getAction() ) );
		}
		ParserExpression ops = new Choice( choices );
		
		return left.__add__( ops.oneOrMore() ).action( levelAction );
	}
}
