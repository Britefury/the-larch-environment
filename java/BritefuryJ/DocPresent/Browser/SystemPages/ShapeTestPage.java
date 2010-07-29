//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser.SystemPages;

import java.awt.Color;

import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Box;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.Shape;
import BritefuryJ.DocPresent.Combinators.Primitive.StaticText;
import BritefuryJ.DocPresent.Combinators.Primitive.VBox;
import BritefuryJ.DocPresent.Painter.FillPainter;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;

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
	
	
	
	private static StyleSheet2 styleSheet = StyleSheet2.instance.withAttr( Primitive.shapePainter, new FillPainter( Color.black ) ).withAttr(
			Primitive.hoverShapePainter, new FillPainter( new Color( 0.0f, 0.5f, 0.5f ) ) );

	
	
	protected Pres createContents()
	{
		return styleSheet.applyTo( new VBox( new Pres[] {
				new StaticText( "Box 50x10; 1 pixel padding" ),
				new Box( 50.0, 10.0 ).pad( 1.0, 1.0 ),
				new StaticText( "Box 50x10; 10 pixel padding" ),
				new Box( 50.0, 10.0 ).pad( 10.0, 10.0 ),
				new StaticText( "Box 50x10; 10 pixel padding, h-expand" ),
				new Box( 50.0, 10.0 ).alignHExpand().pad( 10.0, 10.0 ).alignHExpand(),
				new StaticText( "Rectangle 50x20  @  0,0; 1 pixel padding" ),
				Shape.rectangle( 0.0, 0.0, 50.0, 20.0 ).pad( 1.0, 1.0 ),
				new StaticText( "Rectangle 50x20  @  -10,-10; 1 pixel padding" ),
				Shape.rectangle( -10.0, -10.0, 50.0, 20.0 ).pad( 1.0, 1.0 ),
				new StaticText( "Ellipse 25x25  @  0,0; 1 pixel padding" ),
				Shape.ellipse( 0.0, 0.0, 25.0, 25.0 ).pad( 1.0, 1.0 ),
				} ) );
	}
}
