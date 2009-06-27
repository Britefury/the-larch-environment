//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.Parser.ItemStream.ItemStreamAccessor;
import BritefuryJ.Parser.Utils.OperatorParser.UnaryOperator.UnaryOperatorResultBuilder;
import BritefuryJ.ParserOld.Choice;
import BritefuryJ.ParserOld.ParseAction;
import BritefuryJ.ParserOld.ParserExpression;
import BritefuryJ.ParserOld.Production;

public class PrefixLevel extends UnaryOperatorLevel
{
	private static class PrefixOpLevelAction implements ParseAction
	{
		public PrefixOpLevelAction()
		{
		}
		
		
		@SuppressWarnings("unchecked")
		public Object invoke(ItemStreamAccessor input, int begin, Object x)
		{
			List<Object> xs = (List<Object>)x;
			
			List<UnaryOperatorResultBuilder> builders = (List<UnaryOperatorResultBuilder>)xs.get( 0 );
			Object result = xs.get( 1 );
			
			for (int i = builders.size() - 1; i >= 0; i--)
			{
				UnaryOperatorResultBuilder b = builders.get( i );
				result = b.buildResult( input, b.getOperatorPos(), result );
			}
			
			return result;
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
		
		return ops.oneOrMore().__add__( previousLevelParser ).action( levelAction );
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
		
		return ops.oneOrMore().__add__( previousLevelParser ).action( levelAction );
	}
}
