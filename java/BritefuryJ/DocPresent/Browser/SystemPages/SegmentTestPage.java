//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.util.ArrayList;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class SegmentTestPage extends SystemPage
{
	protected SegmentTestPage()
	{
		register( "tests.segment" );
	}
	
	
	public String getTitle()
	{
		return "Segment test";
	}

	protected String getDescription()
	{
		return "The segment element is used to control caret movement; this is typically used in situations such as fraction elements; in the case of a nested fraction, " +
		"the caret must stop after exiting the child fraction, before leaving the parent fraction; see the fraction test for an example.";
	}


	private static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	
	
	
	protected DPElement text(String t, Color colour)
	{
		return styleSheet.withForeground( colour ).text( t );
	}
	
	protected DPElement text(String t)
	{
		return text( t, Color.black );
	}
	
	
	protected DPElement segment(DPElement x, boolean bGuardBegin, boolean bGuardEnd)
	{
		return styleSheet.segment( bGuardBegin, bGuardEnd, x );
	}
	
	
	protected DPElement span(DPElement... x)
	{
		return styleSheet.span( x );
	}
	
	
	protected DPElement line(DPElement... x)
	{
		return styleSheet.paragraph( x );
	}
	
	
	protected DPElement createContents()
	{
		ArrayList<DPElement> children = new ArrayList<DPElement>();
		
		children.add( line( text( "Bars (|) indicate segment boundaries" ) ) );
		children.add( line( text( "One segment in middle of text |" ), segment( text( "no guards", Color.red ), false, false ), text( "| finish." ) ) );
		children.add( line( text( "One segment in middle of text |" ), segment( text( "begin guard", Color.red ), true, false ), text( "| finish." ) ) );
		children.add( line( text( "One segment in middle of text |" ), segment( text( "end guard", Color.red ), false, true ), text( "| finish." ) ) );
		children.add( line( text( "One segment in middle of text |" ), segment( text( "begin & end guard", Color.red ), true, true ), text( "| finish." ) ) );
		children.add( line( text( "Nested segment in middle outer seg |" ), segment( span( text( "....", Color.red ), segment( text( "both guards", Color.blue ), true, true ), text( "....", Color.red ) ), true, true ), text( "| finish." ) ) );
		children.add( line( text( "Nested segment at beginning outer seg |" ), segment( span( segment( text( "both guards", Color.blue ), true, true ), text( "....", Color.red ) ), true, true ), text( "| finish." ) ) );
		children.add( line( text( "Nested segment at end outer seg |" ), segment( span( text( "....", Color.red ), segment( text( "both guards", Color.blue ), true, true ) ), true, true ), text( "| finish." ) ) );
		
		return styleSheet.vbox( children.toArray( new DPElement[0] ) );
	}
}
