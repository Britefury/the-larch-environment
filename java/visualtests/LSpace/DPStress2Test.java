//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package visualtests.LSpace;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;

import BritefuryJ.LSpace.LSColumn;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSParagraph;
import BritefuryJ.LSpace.LSSpan;
import BritefuryJ.LSpace.LSText;
import BritefuryJ.LSpace.LSViewport;
import BritefuryJ.LSpace.PresentationComponent;
import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.LSpace.PersistentState.PersistentState;
import BritefuryJ.LSpace.StyleParams.ContainerStyleParams;
import BritefuryJ.LSpace.StyleParams.ParagraphStyleParams;
import BritefuryJ.LSpace.StyleParams.TextStyleParams;

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
	TextStyleParams nameStyle = new TextStyleParams( HAlignment.PACK, VAlignment.REFY, null, null, null, true, true, f0, Color.black, null, null, false, false, false );
	TextStyleParams puncStyle = new TextStyleParams( HAlignment.PACK, VAlignment.REFY, null, null, null, true, true, f0, Color.blue, null, null, false, false, false );
	ParagraphStyleParams paraStyle = new ParagraphStyleParams( HAlignment.PACK, VAlignment.REFY, null, null, null, 0.0, 0.0, 0.0 );

	public LSElement name(String n)
	{
		return new LSText( nameStyle, n );
	}
	
	public LSElement attr(LSElement x, String a)
	{
		LSText dot = new LSText( puncStyle, "." );
		LSText attrName = new LSText( nameStyle, a );
		LSSpan span = new LSSpan( ContainerStyleParams.defaultStyleParams );
		span.setChildren( Arrays.asList( x, dot, attrName ) );
		return span;
	}
	
	public LSElement call(LSElement x, LSElement... args)
	{
		LSText openParen = new LSText( puncStyle, "(" );
		LSText closeParen = new LSText( puncStyle, ")" );
		ArrayList<LSElement> elems = new ArrayList<LSElement>();
		elems.add( x );
		elems.add( openParen );
		for (int i = 0; i < args.length; i++)
		{
			if ( i > 0 )
			{
				elems.add( new LSText( puncStyle, "," ) );
				elems.add( new LSText( puncStyle, " " ) );
			}
			elems.add( args[i] );
		}
		elems.add( closeParen );
		LSSpan span = new LSSpan( ContainerStyleParams.defaultStyleParams );
		span.extend( elems );
		return span;
	}
	
	
	
	
	
	
	
	
	protected LSElement createContentNode()
	{
		LSColumn box = new LSColumn( );
		ArrayList<LSElement> children = new ArrayList<LSElement>();
		
		for (int i = 0; i < NUMLINES; i++)
		{
			LSElement child = call( attr( name( "obj" ), "method" ), name( "a" ), name( "b" ), name( "c" ), name( "d" ), name( "e" ), name( "f" ) );
			LSParagraph p = new LSParagraph( paraStyle );
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
		LSElement w = createContentNode();
		long t2 = System.nanoTime();
		System.out.println( "Element tree creation time: " + (double)( t2 - t1 ) / 1000000000.0 );
		LSViewport viewport = new LSViewport( new ContainerStyleParams( HAlignment.EXPAND, VAlignment.EXPAND, null, null, null ), new PersistentState() );
		viewport.setChild( w );
		presentation.getRootElement().setChild( viewport );
	     
	     
	     
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
