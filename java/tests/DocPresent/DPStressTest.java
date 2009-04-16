//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocPresent;

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
	// Used about 421MB ram on a 32-bit VM
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
		DPParagraph attr = new DPParagraph( paraStyle );
		attr.extend( Arrays.asList( new DPWidget[] { x, dot, attrName } ) );
		return attr;
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
	     
	     
	     
	     
		area.setChild( createContentNode() );
	     
	     
	     
		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}
	
	
	public static void main(String[] args)
	{
		new DPStressTest();
	}

}
