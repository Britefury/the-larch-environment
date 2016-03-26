//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import BritefuryJ.Parser.Choice;
import BritefuryJ.Parser.ParseAction;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Production;

public class InfixRightLevel extends BinaryOperatorLevel
{
	protected static class InfixRightOperatorResultBuilder
	{
		private BinaryOperatorParseAction action;
		private int leftPos;
		private Object left;
		
		public InfixRightOperatorResultBuilder(BinaryOperatorParseAction action, int leftPos, Object left)
		{
			this.action = action;
			this.leftPos = leftPos;
			this.left = left;
		}
		
		
		public int getLeftPos()
		{
			return leftPos;
		}
		

		public Object buildResult(Object input, int begin, int rightEnd, Object right)
		{
			return action.invoke( input, begin, rightEnd, left, right );
		}
	}

	
	
	private static class InfixRightOpLevelAction implements ParseAction
	{
		public InfixRightOpLevelAction()
		{
		}
		
		
		@SuppressWarnings("unchecked")
		public Object invoke(Object input, int begin, int end, Object x, Map<String, Object> bindings)
		{
			List<Object> xs = (List<Object>)x;
			
			List<InfixRightOperatorResultBuilder> builders = (List<InfixRightOperatorResultBuilder>)xs.get( 0 );
			Object expression = xs.get( 1 );
			
			for (int i = builders.size() - 1; i >= 0; i--)
			{
				InfixRightOperatorResultBuilder b = builders.get( i );
				expression = b.buildResult( input, b.getLeftPos(), end, expression );
			}
			
			return expression;
		}
	}

	
	
	private static class InfixRightOpAction implements ParseAction
	{
		private BinaryOperatorParseAction action;
		
		
		public InfixRightOpAction(BinaryOperatorParseAction action)
		{
			this.action = action;
		}
		
		
		@SuppressWarnings("unchecked")
		public Object invoke(Object input, int begin, int end, Object x, Map<String, Object> bindings)
		{
			List<Object> xs = (List<Object>)x;
			Object left = xs.get( 0 );
			return new InfixRightOperatorResultBuilder( action, begin, left );
		}
	}
	
	
	InfixRightOpLevelAction levelAction;

	
	//
	//
	// Constructor
	//
	//
	
	public InfixRightLevel(BinaryOperator ops[])
	{
		super( ops );
		
		levelAction = new InfixRightOpLevelAction();
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
			ParserExpression opLeft = left.__add__( operator.getOperatorExpression() );
			choices[i] = opLeft.action( new InfixRightOpAction( operator.getAction() ) );
		}
		ParserExpression ops = new Choice( choices );
		
		// clear bindings from ops; this means that levelAction only sees bindings from right sub-exp
		return ops.oneOrMore().__add__( right ).action( levelAction );
	}
}
