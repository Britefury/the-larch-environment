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

public class ParserSpeedTest
{
	private static class Result
	{
		int numChars;
		double time;
		
		Result(int numChars, double time)
		{
			this.numChars = numChars;
			this.time = time;
		}
		
		
		
		public String toString()
		{
			return String.valueOf( numChars ) + "\t" + String.valueOf( time );
		}
		
		
		static Result avg(Result results[])
		{
			double tAccum = 0.0;
			for (Result r: results)
			{
				tAccum += r.time;
			}
			return new Result( results[0].numChars, tAccum / results.length );
		}
	}
	
	
	public static double timeIt(ParserExpression parser, String sourceText)
	{
		long t1 = System.nanoTime();
		parser.parseStringChars( sourceText );
		long t2 = System.nanoTime();
		return ( t2 - t1 ) / 1000000000.0;
	}
	
	public static void timeAll(ParserExpression parser, int warmupRepeats, int numRepeats, int stepSize, int maxLength)
	{
		StringBuilder sourceText = new StringBuilder();
		for (int i = 0; i <= maxLength; i++)
		{
			sourceText.append( "x" );
			
			if ( i >= stepSize )
			{
				if ( i % stepSize == 0 )
				{
					String source = sourceText.toString();
					for (int j = 0; j < numRepeats; j++)
					{
						timeIt( parser, source );
					}

					Result results[] = new Result[numRepeats];
					for (int j = 0; j < numRepeats; j++)
					{
						double time = timeIt( parser, source );
						results[j] = new Result( i, time );
					}
					
					System.out.println( Result.avg( results ) );
				}
			}
		}
	}
	
	
	public static void main(String args[]) throws CannotOverwriteProductionExpressionException
	{
		Production A = new Production( "A" );
		Production B = new Production( "B" );
		A.setExpression( B.__add__( new Literal( "x" ) ).__or__( new Literal( "x" ) ) );
		B.setExpression( A );
		
		ParserExpression parser = A;
		
		System.out.println( "Warm up..." );

		String testSource = "";
		for (int i = 0; i < 100; i++)
		{
			testSource += "x";
		}
		for (int i = 0; i < 100; i++)
		{
			parser.parseStringChars( testSource );
		}

		System.out.println( "Running..." );
		System.out.println( "#chars\tavg time" );
		
		
		// arguments:
		//   parser, warmupRepeats, repeats, stepSize, maxLength
		timeAll( parser, 10, 20, 500, 25000 );
	}
}
