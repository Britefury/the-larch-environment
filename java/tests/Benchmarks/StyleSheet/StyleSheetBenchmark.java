//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package tests.Benchmarks.StyleSheet;

import BritefuryJ.DocPresent.DPBox;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Box;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.VBox;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;

public class StyleSheetBenchmark
{
//	private static final int RUN_LENGTH = 16;
//	private static final int NUM_REPEATS = 65536;
	
	private static final int RUN_LENGTH = 64;
	private static final int NUM_REPEATS = 64;
	
	private static final PrimitiveStyleSheet oldStyle = PrimitiveStyleSheet.instance.withFontFace( "Serif" );
	private static final StyleSheet2 newStyle = StyleSheet2.instance.withAttr( Primitive.fontFace, "Serif" );

	
	
	public static DPElement directConstructionRun()
	{
		DPElement elements[] = new DPElement[RUN_LENGTH];
		for (int i = 0; i < elements.length; i++)
		{
			//elements[i] = new DPText( "Hello world" );
			elements[i] = new DPBox( "", 10.0, 10.0 );
		}
		
		DPVBox box = new DPVBox();
		box.setChildren( elements );
		return box;
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

	
	
	public static DPElement oldStyleSheetRun()
	{
		DPElement elements[] = new DPElement[RUN_LENGTH];
		for (int i = 0; i < elements.length; i++)
		{
			//elements[i] = oldStyle.staticText( "Hello world" );
			elements[i] = oldStyle.box( 10.0, 10.0 );
		}
		
		return PrimitiveStyleSheet.instance.vbox( elements );
	}
	
	public static double oldStyleSheetTest()
	{
		long start = System.nanoTime();
		for (int i = 0; i < NUM_REPEATS; i++)
		{
			oldStyleSheetRun();
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
		
		return new VBox( elements );
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

	
	public static DPElement combinatorStyleSheetRun()
	{
		Pres elements[] = new Pres[RUN_LENGTH];
		for (int i = 0; i < elements.length; i++)
		{
			//elements[i] = newStyle.applyTo( new StaticText( "Hello world" ) );
			elements[i] = newStyle.applyTo( new Box( 10.0, 10.0 ) );
		}
		
		return new VBox( elements ).present();
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
		
		return new VBox( elements );
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
		// Direct construction test
		System.out.println( "Direction construction" );
		directConstructionTest();
		System.out.println( "Initial run complete" );
		double directTime = directConstructionTest();
		System.out.println( "Direction construction time = " + directTime );

		// Old test
		System.out.println( "Old style sheets" );
		oldStyleSheetTest();
		System.out.println( "Initial run complete" );
		double oldTime = oldStyleSheetTest();
		System.out.println( "Old style sheets time = " + oldTime );

		// Combinators only test
		System.out.println( "Combinators only" );
		combinatorsOnlyTest();
		System.out.println( "Initial run complete" );
		double combinatorsOnlyTime = combinatorsOnlyTest();
		System.out.println( "Combinators only time = " + combinatorsOnlyTime );

		// Combinator style sheet test
		System.out.println( "Combinator style sheets" );
		combinatorStyleSheetTest();
		System.out.println( "Initial run complete" );
		double combinatorTime = combinatorStyleSheetTest();
		System.out.println( "Combinator style sheets time = " + combinatorTime );

		// Repeated combinator style sheet test
		System.out.println( "Repeated combinator style sheets" );
		repeatedCombinatorStyleSheetTest();
		System.out.println( "Initial run complete" );
		double repeatedCombinatorTime = repeatedCombinatorStyleSheetTest();
		System.out.println( "Repeated combinator style sheets time = " + repeatedCombinatorTime );
	}
}
