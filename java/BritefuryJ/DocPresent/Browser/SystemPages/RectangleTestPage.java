//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;
import java.util.Arrays;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Painter.FillPainter;
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
	private static PrimitiveStyleSheet shapeStyle = styleSheet.withShapePainter( new FillPainter( Color.black ) ).withHoverShapePainter( new FillPainter( new Color( 0.0f, 0.5f, 0.5f ) ) );

	
	
	protected DPElement createContents()
	{
		return styleSheet.vbox( Arrays.asList( new DPElement[] {
				styleSheet.staticText( "Rectangle 50x10; 1 pixel padding" ),
				shapeStyle.rectangle( 50.0, 10.0 ).pad( 1.0, 1.0 ),
				styleSheet.staticText( "Rectangle 50x10; 10 pixel padding" ),
				shapeStyle.rectangle( 50.0, 10.0 ).pad( 10.0, 10.0 ),
				styleSheet.staticText( "Rectangle 50x10; 10 pixel padding, h-expand" ),
				shapeStyle.rectangle( 50.0, 10.0 ).alignHExpand().pad( 10.0, 10.0 ).alignHExpand() } ) );
	}
}
