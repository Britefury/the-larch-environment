//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Font;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Controls.ControlsStyleSheet;
import BritefuryJ.DocPresent.Controls.ScrollBar;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;
import BritefuryJ.DocPresent.Util.Range;

public class ScrollBarTestPage extends SystemPage
{
	protected ScrollBarTestPage()
	{
		register( "tests.controls.scrollbar" );
	}
	
	
	public String getTitle()
	{
		return "Scroll bar test";
	}
	
	protected String getDescription()
	{
		return "Scroll bar control: used to take the browser to a different location, or perform an action";
	}
	

	private static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static PrimitiveStyleSheet headingStyleSheet = styleSheet.withFont( new Font( "Sans serif", Font.PLAIN, 18) );

	private static ControlsStyleSheet controlsStyleSheet = ControlsStyleSheet.instance;

	
	
	protected DPElement section(String title, DPElement contents)
	{
		DPElement heading = headingStyleSheet.staticText( title );
		
		return styleSheet.vbox( new DPElement[] { heading.padY( 10.0 ), contents.alignHExpand() } ).alignHExpand();
	}
	
	protected DPElement createContents()
	{
		Range range = new Range( 0.0, 100.0, 0.0, 10.0, 1.0 );
		ScrollBar horizontal = controlsStyleSheet.horizontalScrollBar( range );
		ScrollBar vertical = controlsStyleSheet.verticalScrollBar( range );
		
		DPElement horizontalSection = section( "Horizontal scroll bar", horizontal.getElement() );
		DPElement verticalSection = section( "Vertical scroll bar", styleSheet.withHBoxSpacing( 40.0 ).hbox( new DPElement[] { styleSheet.box( 1.0, 300.0 ),
				vertical.getElement().alignVExpand() } ) );
		
		return styleSheet.withVBoxSpacing( 30.0 ).vbox( new DPElement[] { horizontalSection, verticalSection } ).alignHExpand();
	}
}
