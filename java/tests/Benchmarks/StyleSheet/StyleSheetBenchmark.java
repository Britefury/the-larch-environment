//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package tests.Benchmarks.StyleSheet;

import BritefuryJ.LSpace.LSBox;
import BritefuryJ.LSpace.LSColumn;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Box;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;

public class StyleSheetBenchmark
{
	private static final int RUN_LENGTH = 16;
	private static final int NUM_REPEATS = 65536;
	
//	private static final int RUN_LENGTH = 64;
//	private static final int NUM_REPEATS = 64;

	private static final StyleSheet newStyle = StyleSheet.style( Primitive.fontFace.as( "Serif" ) );

	
	
	public static LSElement directConstructionRun()
	{
		LSElement elements[] = new LSElement[RUN_LENGTH];
		for (int i = 0; i < elements.length; i++)
		{
			//elements[i] = new DPText( "Hello world" );
			elements[i] = new LSBox( 10.0, 10.0 );
		}
		
		return new LSColumn( elements );
	}
	
	public static double directConstructionTest()
	{
		long start = System.nanoTime();
		for (int i = 0; i < NUM_REPEATS; i++)
		{
			directConstructionRun();
		}
		long end = System.nanoTime();
		return ( end - start ) * 1.0e-9;
	}

	
	
	public static Pres combinatorsOnlyRun()
	{
		Pres elements[] = new Pres[RUN_LENGTH];
		for (int i = 0; i < elements.length; i++)
		{
			//elements[i] = newStyle.applyTo( new StaticText( "Hello world" ) );
			elements[i] = newStyle.applyTo( new Box( 10.0, 10.0 ) );
		}
		
		return new Column( elements );
	}
	
	public static double combinatorsOnlyTest()
	{
		long start = System.nanoTime();
		for (int i = 0; i < NUM_REPEATS; i++)
		{
			combinatorsOnlyRun();
		}
		long end = System.nanoTime();
		return ( end - start ) * 1.0e-9;
	}

	
	public static LSElement combinatorStyleSheetRun()
	{
		Pres elements[] = new Pres[RUN_LENGTH];
		for (int i = 0; i < elements.length; i++)
		{
			//elements[i] = newStyle.applyTo( new StaticText( "Hello world" ) );
			elements[i] = newStyle.applyTo( new Box( 10.0, 10.0 ) );
		}
		
		return new Column( elements ).present();
	}
	
	public static double combinatorStyleSheetTest()
	{
		long start = System.nanoTime();
		for (int i = 0; i < NUM_REPEATS; i++)
		{
			combinatorStyleSheetRun();
		}
		long end = System.nanoTime();
		return ( end - start ) * 1.0e-9;
	}

	
	public static Pres repeatedCombinatorStyleSheetRun()
	{
		Pres elements[] = new Pres[RUN_LENGTH];
		for (int i = 0; i < elements.length; i++)
		{
			//elements[i] = newStyle.applyTo( new StaticText( "Hello world" ) );
			elements[i] = newStyle.applyTo( new Box( 10.0, 10.0 ) );
		}
		
		return new Column( elements );
	}
	
	public static double repeatedCombinatorStyleSheetTest()
	{
		long start = System.nanoTime();
		Pres p = repeatedCombinatorStyleSheetRun();
		for (int i = 0; i < NUM_REPEATS; i++)
		{
			p.present();
		}
		long end = System.nanoTime();
		return ( end - start ) * 1.0e-9;
	}

	
	public static void main(final String[] args)
	{
		System.out.println( "BEGIN" );
		// Direct construction test
		directConstructionTest();
		double directTime = directConstructionTest();
		System.out.println( "Direction construction time = " + directTime );

		// Combinators only test
		combinatorsOnlyTest();
		double combinatorsOnlyTime = combinatorsOnlyTest();
		System.out.println( "Combinators only time = " + combinatorsOnlyTime );

		// Combinator style sheet test
		combinatorStyleSheetTest();
		double combinatorTime = combinatorStyleSheetTest();
		System.out.println( "Combinator style sheets time = " + combinatorTime );

		// Repeated combinator style sheet test
		repeatedCombinatorStyleSheetTest();
		double repeatedCombinatorTime = repeatedCombinatorStyleSheetTest();
		System.out.println( "Repeated combinator style sheets time = " + repeatedCombinatorTime );
	}
}
