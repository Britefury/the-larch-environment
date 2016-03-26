//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Browser.TestPages;

import java.awt.Color;

import BritefuryJ.Graphics.FillPainter;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Box;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Shape;
import BritefuryJ.StyleSheet.StyleSheet;

public class ShapeTestPage extends TestPage
{
	protected ShapeTestPage()
	{
	}
	
	
	public String getTitle()
	{
		return "Shape test";
	}

	protected String getDescription()
	{
		return "The box element covers all space given to it, with a specified minimum size. The shape element displays a java.awt.shape.";
	}


	private static StyleSheet styleSheet = StyleSheet.style( Primitive.shapePainter.as( new FillPainter( Color.black ) ), Primitive.hoverShapePainter.as( new FillPainter( new Color( 0.0f, 0.5f, 0.5f ) ) ) );

	
	
	protected Pres createContents()
	{
		return styleSheet.applyTo( new Column( new Pres[] {
				new Label( "Box 50x10; 1 pixel padding" ),
				new Box( 50.0, 10.0 ).pad( 1.0, 1.0 ),
				new Label( "Box 50x10; 10 pixel padding" ),
				new Box( 50.0, 10.0 ).pad( 10.0, 10.0 ),
				new Label( "Box 50x10; 10 pixel padding, h-expand" ),
				new Box( 50.0, 10.0 ).pad( 10.0, 10.0 ).alignHExpand(),
				new Label( "Rectangle 50x20  @  0,0; 1 pixel padding" ),
				Shape.rectangle( 0.0, 0.0, 50.0, 20.0 ).pad( 1.0, 1.0 ),
				new Label( "Rectangle 50x20  @  -10,-10; 1 pixel padding" ),
				Shape.rectangle( -10.0, -10.0, 50.0, 20.0 ).pad( 1.0, 1.0 ),
				new Label( "Ellipse 25x25  @  0,0; 1 pixel padding" ),
				Shape.ellipse( 0.0, 0.0, 25.0, 25.0 ).pad( 1.0, 1.0 ),
				} ) );
	}
}
