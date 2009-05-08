//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.Parser.DebugParseResult;
import BritefuryJ.Parser.Literal;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Production;
import BritefuryJ.Parser.Utils.Tokens;
import BritefuryJ.ParserDebugViewer.ParseViewFrame;

public class OperatorTable
{
	private ParserExpression rootParser;
	private ArrayList<OperatorLevel> levels;
	
	
	//
	// Constructor
	//
	
	public OperatorTable(List<OperatorLevel> levels, ParserExpression rootParser)
	{
		this.rootParser = rootParser;
		this.levels = new ArrayList<OperatorLevel>();
		this.levels.addAll( levels );
	}
	
	
	
	protected ParserExpression getPrefixLevelForReachUp(ArrayList<Production> reachupForwardDeclarations, OperatorLevel aboveLevel)
	{
		int index = levels.indexOf( aboveLevel );
		for (int i = levels.size() - 1; i >= index; i--)
		{
			OperatorLevel lvl = levels.get( i );
			if ( lvl instanceof PrefixLevel )
			{
				PrefixLevel p = (PrefixLevel)lvl;
				if ( p.isReachUpEnabled() )
				{
					return reachupForwardDeclarations.get( i );
				}
			}
		}
		return null;
	}
	
	
	public List<ParserExpression> buildParsers() throws Production.CannotOverwriteProductionExpressionException
	{
		ParserExpression prevLevelParser = rootParser;
		ArrayList<ParserExpression> levelParsers = new ArrayList<ParserExpression>();
		ArrayList<Production> reachupForwardDeclarations = new ArrayList<Production>();

		for (int i = 0; i < levels.size(); i++)
		{
			reachupForwardDeclarations.add( new Production( "oplvl_reachup_" + i ) );
		}

		for (int i = 0; i < levels.size(); i++)
		{
			OperatorLevel lvl = levels.get( i );
			
			ParserExpression levelParser = new Production( "oplvl_" + i, lvl.buildParser( this, prevLevelParser, reachupForwardDeclarations ).__or__( prevLevelParser ) );
			ParserExpression reachupParser = lvl.buildParserForReachUp( this, prevLevelParser );
			levelParsers.add( levelParser );
			reachupForwardDeclarations.get( i ).setExpression( reachupParser );
			
			prevLevelParser = levelParser;
		}
		
		return levelParsers;
	}
	
	
	
	public static void main(String[] args) throws BritefuryJ.Parser.Production.CannotOverwriteProductionExpressionException
	{
		BinaryOperatorParseAction mulAction = new BinaryOperatorParseAction()
		{
			public Object invoke(String input, int begin, Object left, Object right)
			{
				return Arrays.asList( new Object[] { '*', left, right } );
			}
		};

		UnaryOperatorParseAction notAction = new UnaryOperatorParseAction()
		{
			public Object invoke(String input, int begin, Object x)
			{
				return Arrays.asList( new Object[] { '!', x } );
			}
		};
		
		
		BinaryOperator mul = new BinaryOperator( new Literal( "*" ), mulAction );
		UnaryOperator inv = new UnaryOperator( new Literal( "!" ), notAction );
		
		InfixRightLevel l0 = new InfixRightLevel( Arrays.asList( new BinaryOperator[] { mul } ) );
		//PrefixLevel l1 = new PrefixLevel( Arrays.asList( new UnaryOperator[] { inv } ) );
		SuffixLevel l1 = new SuffixLevel( Arrays.asList( new UnaryOperator[] { inv } ) );
		
		OperatorTable t = new OperatorTable( Arrays.asList( new OperatorLevel[] { l0, l1 } ), Tokens.identifier );
		List<ParserExpression> parsers = t.buildParsers();
		ParserExpression e = parsers.get( parsers.size() - 1 );
		
		DebugParseResult r = e.debugParseString( "a * b * c!" );
//		DebugParseResult r = e.debugParseString( "a!!!" );
		
		new ParseViewFrame( r );
	}
}
