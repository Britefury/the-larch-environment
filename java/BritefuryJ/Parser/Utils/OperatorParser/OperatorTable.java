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
import BritefuryJ.Parser.Forward;
import BritefuryJ.Parser.Literal;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Utils.Tokens;
import BritefuryJ.Parser.Utils.OperatorParser.PrecedenceLevel.OperatorParserPrecedenceLevelCannotMixOperatorTypesError;
import BritefuryJ.ParserDebugViewer.ParseViewFrame;

public class OperatorTable
{
	protected static interface OperatorFilter
	{
		public boolean test(Operator op);
	}
	
	protected static class PrefixFilter implements OperatorFilter
	{
		public boolean test(Operator op)
		{
			return op instanceof Prefix;
		}
	}

	protected static class SuffixFilter implements OperatorFilter
	{
		public boolean test(Operator op)
		{
			return op instanceof Suffix;
		}
	}
	
	
	
	private ParserExpression rootParser;
	private ArrayList<PrecedenceLevel> levels;
	
	
	//
	// Constructor
	//
	
	public OperatorTable(List<PrecedenceLevel> levels, ParserExpression rootParser) throws OperatorParserPrecedenceLevelCannotMixOperatorTypesError
	{
		this.rootParser = rootParser;
		this.levels = new ArrayList<PrecedenceLevel>();
		this.levels.addAll( levels );
	}
	
	
	
	protected ParserExpression getLowestPrecedenceUnaryOperatorLevelParserAbove(ArrayList<Forward> levelParserForwardDeclarations, PrecedenceLevel aboveLevel, OperatorFilter opFilter)
	{
		int index = levels.indexOf( aboveLevel );
		for (int i = levels.size() - 1; i >= index; i--)
		{
			if ( levels.get( i ).checkOperators( opFilter ) )
			{
				return levelParserForwardDeclarations.get( i );
			}
		}
		return null;
	}
	
	
	public List<ParserExpression> buildParsers()
	{
		if ( hasReachUpLevels() )
		{
			ParserExpression prevLevelParser = rootParser;
			ArrayList<Forward> levelParserOpOnlyForwardDeclarations = new ArrayList<Forward>();
			ArrayList<ParserExpression> levelParsers = new ArrayList<ParserExpression>();
			for (int i = 0; i < levels.size(); i++)
			{
				levelParserOpOnlyForwardDeclarations.add( new Forward() );
			}
			PrecedenceLevel prevLevel = null;
			for (int i = 0; i < levels.size(); i++)
			{
				Forward opOnlyForward = levelParserOpOnlyForwardDeclarations.get( i );
				PrecedenceLevel lvl = levels.get( i );
				
				ParserExpression levelParser = lvl.buildParserWithReachUp( this, levelParserOpOnlyForwardDeclarations, opOnlyForward, prevLevel, prevLevelParser, "oplvl_" + i );
				levelParsers.add( levelParser );
	
				prevLevel = lvl;
				prevLevelParser = levelParser;
			}
			
			return levelParsers;
		}
		else
		{
			ParserExpression prevLevelParser = rootParser;
			ArrayList<ParserExpression> levelParsers = new ArrayList<ParserExpression>();
			PrecedenceLevel prevLevel = null;
			for (int i = 0; i < levels.size(); i++)
			{
				PrecedenceLevel lvl = levels.get( i );
				
				ParserExpression levelParser = lvl.buildParser( this, prevLevel, prevLevelParser, "oplvl_" + i );
				levelParsers.add( levelParser );
	
				prevLevel = lvl;
				prevLevelParser = levelParser;
			}
			
			return levelParsers;
		}
	}
	
	
	
	protected boolean hasReachUpLevels()
	{
		PrefixFilter pf = new PrefixFilter();
		SuffixFilter sf = new SuffixFilter();

		for (PrecedenceLevel lvl: levels)
		{
			if ( lvl.checkOperators( pf )  ||  lvl.checkOperators( sf ) )
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	
	public static void main(String[] args) throws OperatorParserPrecedenceLevelCannotMixOperatorTypesError
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
		
		
		InfixLeft mul = new InfixLeft( new Literal( "*" ), mulAction );
		Suffix inv = new Suffix( new Literal( "!" ), notAction );
		
		PrecedenceLevel l0 = new PrecedenceLevel( Arrays.asList( new Operator[] { mul } ) );
		PrecedenceLevel l1 = new PrecedenceLevel( Arrays.asList( new Operator[] { inv } ) );
		
		OperatorTable t = new OperatorTable( Arrays.asList( new PrecedenceLevel[] { l0, l1 } ), Tokens.identifier );
		List<ParserExpression> parsers = t.buildParsers();
		ParserExpression e = parsers.get( parsers.size() - 1 );
		
		DebugParseResult r = e.debugParseString( "a! * b * c" );
		
		ParseViewFrame f = new ParseViewFrame( r );
	}
}
