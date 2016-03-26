//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package tests.Benchmarks.Parser;

import BritefuryJ.Parser.Literal;
import BritefuryJ.Parser.ParserExpression;
import BritefuryJ.Parser.Production;
import BritefuryJ.Parser.Production.CannotOverwriteProductionExpressionException;

public class ParserProfileTest
{
	public static double timeIt(ParserExpression parser, String sourceText)
	{
		long t1 = System.nanoTime();
		parser.parseStringChars( sourceText );
		long t2 = System.nanoTime();
		return ( t2 - t1 ) / 1000000000.0;
	}
	
	
	public static void main(String args[]) throws CannotOverwriteProductionExpressionException
	{
		Production A = new Production( "A" );
		Production B = new Production( "B" );
		A.setExpression( B.__add__( new Literal( "x" ) ).__or__( new Literal( "x" ) ) );
		B.setExpression( A );
		
		ParserExpression parser = A;
		
		StringBuilder testSource = new StringBuilder();
		for (int i = 0; i < 500000; i++)
		{
			testSource.append( "x" );
		}
		
		// Warm up
		timeIt( parser, testSource.toString() );
		// Run
		double time = timeIt( parser, testSource.toString() );
		
		System.out.println( "Parsing " + testSource.length() + " characters using a left-recursive parser took " + time + " seconds; " + testSource.length() / time + " characters per second." );

	
		ParserExpression parser2 = new Literal( "x" ).zeroOrMore();
		
		// Warm up
		timeIt( parser2, testSource.toString() );
		// Run
		double time2 = timeIt( parser2, testSource.toString() );

		System.out.println( "Parsing " + testSource.length() + " characters using a repeating parser took " + time2 + " seconds; " + testSource.length() / time2 + " characters per second." );
	}
}
