//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;

import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPSegment;
import BritefuryJ.DocPresent.DPSpan;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VTypesetting;
import BritefuryJ.DocPresent.StyleSheets.TextStyleSheet;
import BritefuryJ.DocPresent.StyleSheets.VBoxStyleSheet;

public class SegmentTestPage extends SystemPage
{
	protected static void initialise()
	{
		new SegmentTestPage().register( "segment" );
	}
	
	
	private SegmentTestPage()
	{
	}
	
	
	protected String getTitle()
	{
		return "Segment test";
	}

	private static Font defaultFont = new Font( "Sans serif", Font.PLAIN, 14 );

	
	
	protected DPWidget text(String t, Color colour)
	{
		TextStyleSheet s0 = new TextStyleSheet( defaultFont, colour );
		return new DPText( s0, t );
	}
	
	protected DPWidget text(String t)
	{
		return text( t, Color.black );
	}
	
	
	protected DPWidget segment(DPWidget x, boolean bGuardBegin, boolean bGuardEnd)
	{
		DPSegment e = new DPSegment( bGuardBegin, bGuardEnd );
		e.setChild( x );
		return e;
	}
	
	
	protected DPWidget span(DPWidget... x)
	{
		DPSpan s = new DPSpan();
		s.setChildren( Arrays.asList( x ) );
		return s;
	}
	
	
	protected DPWidget line(DPWidget... x)
	{
		DPParagraph para = new DPParagraph();
		para.setChildren( Arrays.asList( x ) );
		return para;
	}
	
	
	protected DPWidget createContents()
	{
		VBoxStyleSheet boxs = new VBoxStyleSheet( VTypesetting.NONE, HAlignment.LEFT, 0.0, false, 0.0 );
		DPVBox box = new DPVBox( boxs );
		ArrayList<DPWidget> children = new ArrayList<DPWidget>();
		
		
		
		children.add( line( text( "Bars (|) indicate segment boundaries" ) ) );
		children.add( line( text( "One segment in middle of text |" ), segment( text( "no guards", Color.red ), false, false ), text( "| finish." ) ) );
		children.add( line( text( "One segment in middle of text |" ), segment( text( "begin guard", Color.red ), true, false ), text( "| finish." ) ) );
		children.add( line( text( "One segment in middle of text |" ), segment( text( "end guard", Color.red ), false, true ), text( "| finish." ) ) );
		children.add( line( text( "One segment in middle of text |" ), segment( text( "begin & end guard", Color.red ), true, true ), text( "| finish." ) ) );
		children.add( line( text( "Nested segment in middle outer seg |" ), segment( span( text( "....", Color.red ), segment( text( "both guards", Color.blue ), true, true ), text( "....", Color.red ) ), true, true ), text( "| finish." ) ) );
		children.add( line( text( "Nested segment at beginning outer seg |" ), segment( span( segment( text( "both guards", Color.blue ), true, true ), text( "....", Color.red ) ), true, true ), text( "| finish." ) ) );
		children.add( line( text( "Nested segment at end outer seg |" ), segment( span( text( "....", Color.red ), segment( text( "both guards", Color.blue ), true, true ) ), true, true ), text( "| finish." ) ) );
		
		box.setChildren( children );
		
		return box;
	}
}
