//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSShape;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class Shape extends Pres
{
	private java.awt.Shape shape;
	
	
	public Shape(java.awt.Shape shape)
	{
		this.shape = shape;
	}
	
	
	public static Shape ellipse(double x, double y, double w, double h)
	{
		return new Shape( LSShape.ellipse( x, y, w, h ) );
	}

	public static Shape ellipse(Point2 pos, Vector2 size)
	{
		return new Shape( LSShape.ellipse( pos, size ) );
	}

	
	public static Shape rectangle(double x, double y, double w, double h)
	{
		return new Shape( LSShape.rectangle( x, y, w, h ) );
	}

	public static Shape rectangle(Point2 pos, Vector2 size)
	{
		return new Shape( LSShape.rectangle( pos, size ) );
	}

	
	public static Shape roundRectangle(double x, double y, double w, double h, double roundingX, double roundingY)
	{
		return new Shape( LSShape.roundRectangle( x, y, w, h, roundingX, roundingY ) );
	}

	public static Shape roundRectangle(Point2 pos, Vector2 size, Vector2 rounding)
	{
		return new Shape( LSShape.roundRectangle( pos, size, rounding ) );
	}

	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		return new LSShape( Primitive.shapeParams.get( style ), shape );
	}
}
