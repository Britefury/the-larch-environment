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
import BritefuryJ.DocPresent.DPViewport;
import BritefuryJ.DocPresent.PresentationComponent;
import BritefuryJ.DocPresent.DPSpan;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPColumn;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.PersistentState.PersistentState;
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;
import BritefuryJ.DocPresent.StyleParams.ParagraphStyleParams;
import BritefuryJ.DocPresent.StyleParams.TextStyleParams;

public class DPStress2Test
{
	// Version							Platform		# lines	Process size			Measured mem usage		Mem usage at start		Element tree creation time		typeset time
	//
	// [1] With collation					A			10240	62.7MB				38.6MB				0.6MB				0.611s					0.536s
	// [2] With collation finished			A			10240	63.4MB				37.9MB				0.6MB				0.624s					0.524s
	// [3] Element tree functionality			A			10240	69.4MB				44.8MB				0.7MB				0.631s					0.563s
	// [4] Minor optimisations				A			10240	69.0MB				43.3MB				0.9MB				0.653s					0.566s
	// [5] Pointer refactor				A			10240	68.8MB				43.9MB				1.0MB				0.639s					0.585s
	// [6] As of 2010-01-30				B			10240	89.1MB				54.7MB				1.9MB				0.620s					0.334s
	// [7] Layout tree refactor				B			10240	95.4MB				60.2MB				1.9MB				0.530s					0.563s
	//
	//
	// States:
	// [A] Defunct element tree version, for comparison
	// [1] HBoxes, VBoxes and Paragraphs can collate contents from children that are span elements
	// [2] Collation system finished
	// [3] All ElementTree functionality merged into document presentation system
	// [4] Minor optimisations - pointer containment (pointer within bounds of element) moved to root (DPPresentationArea)
	// [5] Pointer refactor - moved all pointer containment state out of the element tree and into the input system
	// [6] As of 2010-01-30; no significant changes, different platform
	// [7] Refactored layout system into separate layout tree
	//
	// Platform A:
	//	CPU: Intel Core Duo 1.86GHz
	//	RAM: 2GB
	//	OS: WinXP 32-bit
	//	JVM: 1.6.0_14 32-bit
	//
	// Platform B:
	//	CPU: Intel Core Duo 1.86GHz
	//	RAM: 2GB
	//	OS: Windows 7 32-bit
	//	JVM: 1.6.0_18 32-bit
	
	private static int NUMLINES = 10240;
	
	
	Font f0 = new Font( "Sans serif", Font.PLAIN, 12 );
	TextStyleParams nameStyle = new TextStyleParams( null, null, null, true, true, f0, Color.black, null, null, false, false, false );
	TextStyleParams puncStyle = new TextStyleParams( null, null, null, true, true, f0, Color.blue, null, null, false, false, false );
	ParagraphStyleParams paraStyle = new ParagraphStyleParams( null, null, null, 0.0, 0.0, 0.0 );

	public DPElement name(String n)
	{
		return new DPText( nameStyle, n );
	}
	
	public DPElement attr(DPElement x, String a)
	{
		DPText dot = new DPText( puncStyle, "." );
		DPText attrName = new DPText( nameStyle, a );
		DPSpan span = new DPSpan( ContainerStyleParams.defaultStyleParams );
		span.setChildren( Arrays.asList( new DPElement[] { x, dot, attrName } ) );
		return span;
	}
	
	public DPElement call(DPElement x, DPElement... args)
	{
		DPText openParen = new DPText( puncStyle, "(" );
		DPText closeParen = new DPText( puncStyle, ")" );
		ArrayList<DPElement> elems = new ArrayList<DPElement>();
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
		DPSpan span = new DPSpan( ContainerStyleParams.defaultStyleParams );
		span.extend( elems );
		return span;
	}
	
	
	
	
	
	
	
	
	protected DPElement createContentNode()
	{
		DPColumn box = new DPColumn( );
		ArrayList<DPElement> children = new ArrayList<DPElement>();
		
		for (int i = 0; i < NUMLINES; i++)
		{
			DPElement child = call( attr( name( "obj" ), "method" ), name( "a" ), name( "b" ), name( "c" ), name( "d" ), name( "e" ), name( "f" ) );
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

		PresentationComponent presentation = new PresentationComponent();
	     
		System.out.println( "Start memory usage = "  + ( Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() ) );
	     
	     
		long t1 = System.nanoTime();
		DPElement w = createContentNode();
		long t2 = System.nanoTime();
		System.out.println( "Element tree creation time: " + (double)( t2 - t1 ) / 1000000000.0 );
		DPViewport viewport = new DPViewport( new PersistentState() );
		viewport.setChild( w );
		presentation.setChild( viewport.alignHExpand().alignVExpand() );
	     
	     
	     
		presentation.setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( presentation );
		frame.pack();
		frame.setVisible(true);
	}
	
	
	public static void main(String[] args)
	{
		new DPStress2Test();
	}
}
