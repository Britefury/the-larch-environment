//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Parser.Utils.OperatorParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BritefuryJ.Parser.TracedParseResult;
import BritefuryJ.Parser.Literal;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Production;
import BritefuryJ.Parser.Utils.Tokens;

public class OperatorTable
{
	private ArrayList<OperatorLevel> levels;
	
	
	//
	// Constructor
	//
	
	public OperatorTable(OperatorLevel levels[])
	{
		this.levels = new ArrayList<OperatorLevel>();
		this.levels.addAll( Arrays.asList( levels ) );
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

	private ArrayList<Production> buildForwardDeclarations(String namePrefix, int n) {
		ArrayList<Production> levelParsers = new ArrayList<Production>();
		for (int i = 0; i < n; i++)
		{
			Production levelParser = new Production( namePrefix + i );
			levelParsers.add( levelParser );
		}
		return levelParsers;
	}


	public void buildParsers(List<Production> levelForwardDeclarations, ParserExpression rootParser) throws Production.CannotOverwriteProductionExpressionException {
		if (levelForwardDeclarations.size() != levels.size()) {
			throw new RuntimeException("the number of levels (" + levels.size() + ") and forward declarations (" + levelForwardDeclarations.size() + ") not match");
		}

		ParserExpression prevLevelParser = rootParser;
		ArrayList<Production> reachupForwardDeclarations = buildForwardDeclarations("oplvl_reachup_", levels.size());

		for (int i = 0; i < levels.size(); i++)
		{
			OperatorLevel lvl = levels.get( i );
			Production levelParser = levelForwardDeclarations.get(i);
			levelParser.setExpression(lvl.buildParser( this, prevLevelParser, reachupForwardDeclarations ).__or__( prevLevelParser ));

			ParserExpression reachupParser = lvl.buildParserForReachUp( this, prevLevelParser );
			reachupForwardDeclarations.get( i ).setExpression( reachupParser );

			prevLevelParser = levelParser;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<ParserExpression> createAndBuildParsers(ParserExpression rootParser) throws Production.CannotOverwriteProductionExpressionException
	{
		ArrayList<Production> forwardDeclarations = buildForwardDeclarations("oplvl_", levels.size());

		buildParsers(forwardDeclarations, rootParser);

		return (ArrayList<ParserExpression>)(ArrayList<?>)forwardDeclarations;
	}
	
	
	
	public static TracedParseResult getOperatorTableTestDebugParseResult() throws BritefuryJ.Parser.Production.CannotOverwriteProductionExpressionException
	{
		BinaryOperatorParseAction mulAction = new BinaryOperatorParseAction()
		{
			public Object invoke(Object input, int begin, int end, Object left, Object op, Object right)
			{
				return Arrays.asList( op, left, right );
			}
		};

		UnaryOperatorParseAction notAction = new UnaryOperatorParseAction()
		{
			public Object invoke(Object input, int begin, int end, Object x, Object op)
			{
				return Arrays.asList( op, x );
			}
		};
		
		
		BinaryOperator mul = new BinaryOperator( new Literal( "*" ), mulAction );
		UnaryOperator inv = new UnaryOperator( new Literal( "!" ), notAction );
		
		InfixRightLevel l0 = new InfixRightLevel( new BinaryOperator[] { mul } );
		//PrefixLevel l1 = new PrefixLevel( Arrays.asList( new UnaryOperator[] { inv } ) );
		SuffixLevel l1 = new SuffixLevel( Arrays.asList( inv ) );
		
		OperatorTable t = new OperatorTable( new OperatorLevel[] { l0, l1 } );
		List<ParserExpression> parsers = t.createAndBuildParsers(Tokens.identifier);
		ParserExpression e = parsers.get( parsers.size() - 1 );
		
		return e.traceParseStringChars( "a * b * c!" );
//		DebugParseResult r = e.debugParseString( "a!!!" );
	}
}
