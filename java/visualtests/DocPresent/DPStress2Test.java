//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package visualtests.DocPresent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;

import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPSpan;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class DPStress2Test
{
	// Version							Platform		# lines	Process size			Measured mem usage		Mem usage at start		Widget tree creation time		typeset time
	//
	// [1] With collation					A			10240	62.7MB				38.6MB				0.6MB				0.611s					0.536s
	// [2] With collation finished			A			10240	63.4MB				37.9MB				0.6MB				0.624s					0.524s
	//
	//
	// States:
	// [A] Defunct element tree version, for comparison
	// [1] HBoxes, VBoxes and Paragraphs can collate contents from children that are span elements
	// [2] Collation system finished
	//
	// Platform A:
	//	CPU: Intel Core Duo 1.86GHz
	//	RAM: 2GB
	//	OS: WinXP 32-bit
	//	JVM: 1.6.0_14 32-bit
	
	private static int NUMLINES = 10240;
	
	
	Font f0 = new Font( "Sans serif", Font.PLAIN, 12 );
	TextStyleSheet nameStyle = new TextStyleSheet( f0, Color.black );
	TextStyleSheet puncStyle = new TextStyleSheet( f0, Color.blue );
	ParagraphStyleSheet paraStyle = new ParagraphStyleSheet();

	public DPWidget name(String n)
	{
		return new DPText( nameStyle, n );
	}
	
	public DPWidget attr(DPWidget x, String a)
	{
		DPText dot = new DPText( puncStyle, "." );
		DPText attrName = new DPText( nameStyle, a );
		DPSpan span = new DPSpan();
		span.setChildren( Arrays.asList( new DPWidget[] { x, dot, attrName } ) );
		return span;
	}
	
	public DPWidget call(DPWidget x, DPWidget... args)
	{
		DPText openParen = new DPText( puncStyle, "(" );
		DPText closeParen = new DPText( puncStyle, ")" );
		ArrayList<DPWidget> elems = new ArrayList<DPWidget>();
		elems.add( x );
		elems.add( openParen );
		for (int i = 0; i < args.length; i++)
		{
			if ( i > 0 )
			{
				elems.add( new DPText( puncStyle, "," ) );
				elems.add( new DPText( puncStyle, " " ) );
			}
			elems.add( args[i] );
		}
		elems.add( closeParen );
		DPSpan span = new DPSpan();
		span.extend( elems );
		return span;
	}
	
	
	
	
	
	
	
	
	protected DPWidget createContentNode()
	{
		VBoxStyleSheet boxs = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.LEFT, 0.0, false, 0.0 );
		DPVBox box = new DPVBox( boxs );
		ArrayList<DPWidget> children = new ArrayList<DPWidget>();
		
		for (int i = 0; i < NUMLINES; i++)
		{
			DPWidget child = call( attr( name( "obj" ), "method" ), name( "a" ), name( "b" ), name( "c" ), name( "d" ), name( "e" ), name( "f" ) );
			DPParagraph p = new DPParagraph( paraStyle );
			p.append( child );
			children.add( p );
		}
		
		box.setChildren( children );
		
		return box;
	}



	public DPStress2Test()
	{
		JFrame frame = new JFrame( "Document presentation system stress test 2 - " + NUMLINES + " lines" );

		//This stops the app on window close.
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		DPPresentationArea area = new DPPresentationArea();
	     
		System.out.println( "Start memory usage = "  + ( Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ) );
	     
	     
		long t1 = System.nanoTime();
		DPWidget w = createContentNode();
		long t2 = System.nanoTime();
		System.out.println( "Widget tree creation time: " + (double)( t2 - t1 ) / 1000000000.0 );
		area.setChild( w );
	     
	     
	     
		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}
	
	
	public static void main(String[] args)
	{
		new DPStress2Test();
	}
}
