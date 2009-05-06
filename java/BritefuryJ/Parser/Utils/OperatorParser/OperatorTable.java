//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.List;
import java.util.ArrayList;

import BritefuryJ.Parser.Forward;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Utils.OperatorParser.PrecedenceLevel.OperatorParserPrecedenceLevelCannotMixOperatorTypesError;

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
		ParserExpression parser = rootParser;
		ArrayList<Forward> levelParserForwardDeclarations = new ArrayList<Forward>();
		ArrayList<ParserExpression> levelParsers = new ArrayList<ParserExpression>();
		for (int i = 0; i < levels.size(); i++)
		{
			levelParserForwardDeclarations.add( new Forward() );
		}
		PrecedenceLevel prevLevel = null;
		for (int i = 0; i < levels.size(); i++)
		{
			Forward f = levelParserForwardDeclarations.get( i );
			PrecedenceLevel lvl = levels.get( i );
			lvl.buildParser( this, levelParserForwardDeclarations, f, prevLevel, parser, "oplvl_" + i );
			parser = f;
			prevLevel = lvl;
			levelParsers.add( f );
		}
		
		return levelParsers;
	}
}
