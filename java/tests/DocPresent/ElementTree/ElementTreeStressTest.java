//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocPresent.ElementTree;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;

import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.ParagraphElement;
import BritefuryJ.DocPresent.ElementTree.TextElement;
import BritefuryJ.DocPresent.ElementTree.VBoxElement;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class ElementTreeStressTest extends ElementTreeTestBase
{
	private static int NUMLINES = 10240;
	
	
	Font f0 = new Font( "Sans serif", Font.PLAIN, 12 );
	TextStyleSheet nameStyle = new TextStyleSheet( f0, Color.black );
	TextStyleSheet puncStyle = new TextStyleSheet( f0, Color.blue );
	ParagraphStyleSheet paraStyle = new ParagraphStyleSheet();

	public Element name(String n)
	{
		return new TextElement( nameStyle, n );
	}
	
	public Element attr(Element x, String a)
	{
		TextElement dot = new TextElement( puncStyle, "." );
		TextElement attrName = new TextElement( nameStyle, a );
		ParagraphElement attr = new ParagraphElement( paraStyle );
		attr.setChildren( Arrays.asList( new Element[] { x, dot, attrName } ) );
		return attr;
	}
	
	public Element call(Element x, Element... args)
	{
		TextElement openParen = new TextElement( puncStyle, "(" );
		TextElement closeParen = new TextElement( puncStyle, ")" );
		ArrayList<Element> elems = new ArrayList<Element>();
		elems.add( x );
		elems.add( openParen );
		for (int i = 0; i < args.length; i++)
		{
			if ( i > 0 )
			{
				elems.add( new TextElement( puncStyle, "," ) );
				elems.add( new TextElement( puncStyle, " " ) );
			}
			elems.add( args[i] );
		}
		elems.add( closeParen );
		ParagraphElement call = new ParagraphElement( paraStyle );
		call.setChildren( elems );
		return call;
	}
	
	
	
	
	
	
	
	
	protected Element createContentNode()
	{
		VBoxStyleSheet boxs = new VBoxStyleSheet( DPVBox.Typesetting.NONE, DPVBox.Alignment.LEFT, 0.0, false, 0.0 );
		VBoxElement box = new VBoxElement( boxs );
		ArrayList<Element> children = new ArrayList<Element>();
		
		for (int i = 0; i < NUMLINES; i++)
		{
			children.add( call( attr( name( "obj" ), "method" ), name( "a" ), name( "b" ), name( "c" ), name( "d" ), name( "e" ), name( "f" ) ) );
		}
		
		box.setChildren( children );
		
		return box;
	}



	public ElementTreeStressTest()
	{
		JFrame frame = new JFrame( "Element tree stress test - " + NUMLINES + " lines" );
		initFrame( frame );
		
		System.gc();
	}
	
	
	public static void main(String[] args)
	{
		new ElementTreeStressTest();
	}
}
