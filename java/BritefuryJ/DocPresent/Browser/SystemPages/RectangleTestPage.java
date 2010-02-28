//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.util.Arrays;

import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class RectangleTestPage extends SystemPage
{
	protected RectangleTestPage()
	{
		register( "tests.rectangle" );
	}
	
	
	public String getTitle()
	{
		return "Rectangle test";
	}

	protected String getDescription()
	{
		return "The rectangle element covers all space given it, with a specified minimum size.";
	}
	
	
	
	private static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;

	
	
	protected DPWidget createContents()
	{
		return styleSheet.vbox( Arrays.asList( new DPWidget[] {
				styleSheet.text( "Rectangle 50x2; 1 pixel padding" ),
				styleSheet.rectangle( 50.0, 2.0 ).pad( 1.0, 1.0 ),
				styleSheet.text( "Rectangle 50x2; 10 pixel padding" ),
				styleSheet.rectangle( 50.0, 2.0 ).pad( 10.0, 10.0 ),
				styleSheet.text( "Rectangle 50x2; 10 pixel padding, h-expand" ),
				styleSheet.rectangle( 50.0, 2.0 ).alignHExpand().pad( 10.0, 10.0 ).alignHExpand() } ) );
	}
}
