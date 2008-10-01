//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.List;

import BritefuryJ.Parser.Forward;
import BritefuryJ.Parser.ParserExpression;

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
	private List<PrecedenceLevel> levels;
	
	
	//
	// Constructor
	//
	
	
	
	protected ParserExpression getLowestPrecedenceUnaryOperatorLevelParserAbove(List<Forward> levelParserForwardDeclarations, PrecedenceLevel thisLevel, OperatorFilter opFilter)
	{
		return null;
	}
}
