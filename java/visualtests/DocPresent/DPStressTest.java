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
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class DPStressTest
{
	// Memory usage history (10240 lines): (java 1.6.0_7 32-bit)
	// ~480MB: initial
	// 357MB: packed DPContainer.ChildEntry
	// 353MB: packed TextVisual
	// 164MB: shared TextLayout objects
	// 135MB: emptied out DPContainer.ChildEntry
	// 126MB: removed DPContainer.ChildEntry
	// 120MB: shared TextVisual objects
	// 117MB: created pointer->child table in DPContainer on demand
	// 117MB: replaced paragraph ArrayLists with arrays
	// 111MB: replaced Vector2 and Point2 objects in DPWidget with doubles
	// 83MB: cached metrics in TextVisual objects, and modified Metrics.scaled() (and subclass) methods to only create a new metrics object in cases scale != 1.0
	private static int NUMLINES = 10240;
	
	
	Font f0 = new Font( "Sans serif", Font.PLAIN, 12 );
	TextStyleSheet nameStyle = new TextStyleSheet( f0, Color.black );
	TextStyleSheet puncStyle = new TextStyleSheet( f0, Color.blue );
	ParagraphStyleSheet paraStyle = new ParagraphStyleSheet();

	public DPWidget name(String n)
	{
		return new DPText( nameStyle, n );
	}
	
	public DPWidget[] attr(DPWidget x, String a)
	{
		DPText dot = new DPText( puncStyle, "." );
		DPText attrName = new DPText( nameStyle, a );
		return new DPWidget[] { x, dot, attrName };
	}
	
	public DPWidget call(DPWidget[] x, DPWidget... args)
	{
		DPText openParen = new DPText( puncStyle, "(" );
		DPText closeParen = new DPText( puncStyle, ")" );
		ArrayList<DPWidget> elems = new ArrayList<DPWidget>();
		elems.addAll( Arrays.asList( x ) );
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
		DPParagraph call = new DPParagraph( paraStyle );
		call.extend( elems );
		return call;
	}
	
	
	
	
	
	
	
	
	protected DPWidget createContentNode()
	{
		VBoxStyleSheet boxs = new VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.LEFT, 0.0, false, 0.0 );
		DPVBox box = new DPVBox( boxs );
		ArrayList<DPWidget> children = new ArrayList<DPWidget>();
		
		for (int i = 0; i < NUMLINES; i++)
		{
			children.add( call( attr( name( "obj" ), "method" ), name( "a" ), name( "b" ), name( "c" ), name( "d" ), name( "e" ), name( "f" ) ) );
		}
		
		box.setChildren( children );
		
		return box;
	}



	public DPStressTest()
	{
		JFrame frame = new JFrame( "Document presentation system stress test - " + NUMLINES + " lines" );

		//This stops the app on window close.
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		DPPresentationArea area = new DPPresentationArea();
	     
	     
	     
		long t1 = System.currentTimeMillis();
		DPWidget w = createContentNode();
		long t2 = System.currentTimeMillis();
		System.out.println( "Widget tree creation time: " + (double)( t2 - t1 ) / 1000.0 );
		area.setChild( w );
	     
	     
	     
		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
		
		System.gc();
	}
	
	
	public static void main(String[] args)
	{
		new DPStressTest();
	}

}
