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

public class ShapeTestPage extends SystemPage
{
	protected ShapeTestPage()
	{
		register( "tests.shape" );
	}
	
	
	public String getTitle()
	{
		return "Shape test";
	}

	protected String getDescription()
	{
		return "The box element covers all space given to it, with a specified minimum size. The shape element displays a java.awt.shape.";
	}
	
	
	
	private static PrimitiveStyleSheet styleSheet = PrimitiveStyleSheet.instance;
	private static PrimitiveStyleSheet shapeStyle = styleSheet.withShapePainter( new FillPainter( Color.black ) ).withHoverShapePainter( new FillPainter( new Color( 0.0f, 0.5f, 0.5f ) ) );

	
	
	protected DPElement createContents()
	{
		return styleSheet.vbox( Arrays.asList( new DPElement[] {
				styleSheet.staticText( "Box 50x10; 1 pixel padding" ),
				shapeStyle.box( 50.0, 10.0 ).pad( 1.0, 1.0 ),
				styleSheet.staticText( "Box 50x10; 10 pixel padding" ),
				shapeStyle.box( 50.0, 10.0 ).pad( 10.0, 10.0 ),
				styleSheet.staticText( "Box 50x10; 10 pixel padding, h-expand" ),
				shapeStyle.box( 50.0, 10.0 ).alignHExpand().pad( 10.0, 10.0 ).alignHExpand(),
				styleSheet.staticText( "Rectangle 50x20  @  0,0; 1 pixel padding" ),
				shapeStyle.rectangle( 0.0, 0.0, 50.0, 20.0 ).pad( 1.0, 1.0 ),
				styleSheet.staticText( "Rectangle 50x20  @  -10,-10; 1 pixel padding" ),
				shapeStyle.rectangle( -10.0, -10.0, 50.0, 20.0 ).pad( 1.0, 1.0 ),
				styleSheet.staticText( "Ellipse 25x25  @  0,0; 1 pixel padding" ),
				shapeStyle.ellipse( 0.0, 0.0, 25.0, 25.0 ).pad( 1.0, 1.0 ),
				} ) );
	}
}
