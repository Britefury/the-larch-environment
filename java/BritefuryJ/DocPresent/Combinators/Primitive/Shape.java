//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPShape;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public class Shape extends Pres
{
	private java.awt.Shape shape;
	
	
	public Shape(java.awt.Shape shape)
	{
		this.shape = shape;
	}
	
	
	public static Shape ellipse(double x, double y, double w, double h)
	{
		return new Shape( DPShape.ellipse( x, y, w, h ) );
	}

	public static Shape ellipse(Point2 pos, Vector2 size)
	{
		return new Shape( DPShape.ellipse( pos, size ) );
	}

	
	public static Shape rectangle(double x, double y, double w, double h)
	{
		return new Shape( DPShape.rectangle( x, y, w, h ) );
	}

	public static Shape rectangle(Point2 pos, Vector2 size)
	{
		return new Shape( DPShape.rectangle( pos, size ) );
	}

	
	public static Shape roundRectangle(double x, double y, double w, double h, double roundingX, double roundingY)
	{
		return new Shape( DPShape.roundRectangle( x, y, w, h, roundingX, roundingY ) );
	}

	public static Shape roundRectangle(Point2 pos, Vector2 size, Vector2 rounding)
	{
		return new Shape( DPShape.roundRectangle( pos, size, rounding ) );
	}

	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		return new DPShape( ctx.getStyle().getShapeParams(), "", shape );
	}
}
