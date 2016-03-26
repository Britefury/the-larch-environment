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
import BritefuryJ.Parser.Utils.OperatorParser.UnaryOperator.UnaryOperatorResultBuilder;

public class PrefixLevel extends UnaryOperatorLevel
{
	private static class PrefixOpLevelAction implements ParseAction
	{
		public PrefixOpLevelAction()
		{
		}
		
		
		@SuppressWarnings("unchecked")
		public Object invoke(Object input, int begin, int end, Object x, Map<String, Object> bindings)
		{
			// Bindings will be from sub-expression only
			List<Object> xs = (List<Object>)x;
			
			List<UnaryOperatorResultBuilder> builders = (List<UnaryOperatorResultBuilder>)xs.get( 0 );
			Object expression = xs.get( 1 );
			
			for (int i = builders.size() - 1; i >= 0; i--)
			{
				UnaryOperatorResultBuilder b = builders.get( i );
				expression = b.buildResult( input, b.getOperatorBegin(), end, expression );
			}
			
			return expression;
		}
	}
	
	
	private PrefixOpLevelAction levelAction;
	private boolean bReachUp;

	
	//
	//
	// Constructor
	//
	//
	
	public PrefixLevel(List<UnaryOperator> ops, boolean bReachUp)
	{
		super( ops );
		
		levelAction = new PrefixOpLevelAction();
		this.bReachUp = bReachUp;
	}
	
	
	protected boolean isReachUpEnabled()
	{
		return bReachUp;
	}
	
	
	public PrefixLevel(List<UnaryOperator> ops)
	{
		this( ops, false );
	}
	
	
	protected ParserExpression buildParser(OperatorTable operatorTable, ParserExpression previousLevelParser, ArrayList<Production> reachupForwardDeclarations)
	{
		ParserExpression[] choices = new ParserExpression[operators.size()];
		for (int i = 0; i < operators.size(); i++)
		{
			UnaryOperator operator = operators.get( i );
			choices[i] = operator.applyAction( operator.getOperatorExpression() );
		}
		ParserExpression ops = new Choice( choices );
		
		return ops.oneOrMore().clearBindings().__add__( previousLevelParser ).action( levelAction );
	}


	protected ParserExpression buildParserForReachUp(OperatorTable operatorTable, ParserExpression previousLevelParser)
	{
		ParserExpression[] choices = new ParserExpression[operators.size()];
		for (int i = 0; i < operators.size(); i++)
		{
			UnaryOperator operator = operators.get( i );
			choices[i] = operator.applyAction( operator.getOperatorExpression() );
		}
		ParserExpression ops = new Choice( choices );
		
		return ops.oneOrMore().clearBindings().__add__( previousLevelParser ).action( levelAction );
	}
}
