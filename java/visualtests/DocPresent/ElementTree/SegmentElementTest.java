//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package visualtests.DocPresent.ElementTree;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFrame;

import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.ParagraphElement;
import BritefuryJ.DocPresent.ElementTree.SegmentElement;
import BritefuryJ.DocPresent.ElementTree.TextElement;
import BritefuryJ.DocPresent.ElementTree.VBoxElement;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class SegmentElementTest extends ElementTreeTestBase
{
	private static Font defaultFont = new Font( "Sans serif", Font.PLAIN, 14 );

	
	
	protected Element text(String t, Color colour)
	{
		TextStyleSheet s0 = new TextStyleSheet( defaultFont, colour );
		return new TextElement( s0, t );
	}
	
	protected Element text(String t)
	{
		return text( t, Color.black );
	}
	
	
	protected Element segment(Element x, boolean bGuardBegin, boolean bGuardEnd)
	{
		SegmentElement e = new SegmentElement( bGuardBegin, bGuardEnd );
		e.setChild( x );
		return e;
	}
	
	
	protected Element line(Element... x)
	{
		ParagraphElement para = new ParagraphElement();
		para.setChildren( Arrays.asList( x ) );
		return para;
	}
	
	
	protected Element createContentNode()
	{
		VBoxStyleSheet boxs = new VBoxStyleSheet( HAlignment.LEFT, 0.0, false, 0.0 );
		VBoxElement box = new VBoxElement( boxs );
		ArrayList<Element> children = new ArrayList<Element>();
		
		
		
		children.add( line( text( "Bars (|) indicate segment boundaries" ) ) );
		children.add( line( text( "One segment in middle of text |" ), segment( text( "no guards", Color.red ), false, false ), text( "| finish." ) ) );
		children.add( line( text( "One segment in middle of text |" ), segment( text( "begin guard", Color.red ), true, false ), text( "| finish." ) ) );
		children.add( line( text( "One segment in middle of text |" ), segment( text( "end guard", Color.red ), false, true ), text( "| finish." ) ) );
		children.add( line( text( "One segment in middle of text |" ), segment( text( "begin & end guard", Color.red ), true, true ), text( "| finish." ) ) );
		children.add( line( text( "Nested segment in middle outer seg |" ), segment( line( text( "....", Color.red ), segment( text( "both guards", Color.blue ), true, true ), text( "....", Color.red ) ), true, true ), text( "| finish." ) ) );
		children.add( line( text( "Nested segment at beginning outer seg |" ), segment( line( segment( text( "both guards", Color.blue ), true, true ), text( "....", Color.red ) ), true, true ), text( "| finish." ) ) );
		children.add( line( text( "Nested segment at end outer seg |" ), segment( line( text( "....", Color.red ), segment( text( "both guards", Color.blue ), true, true ) ), true, true ), text( "| finish." ) ) );
		
		box.setChildren( children );
		
		return box;
	}



	public SegmentElementTest()
	{
		JFrame frame = new JFrame( "Segment test" );
		initFrame( frame );
	}
	
	
	public static void main(String[] args)
	{
		new SegmentElementTest();
	}
}
