//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Primitive;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPShape;
import BritefuryJ.Math.Point2;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class Arrow extends Pres
{
	private static Path2D.Double[] createArrowPaths(double size, double filletSize)
	{
		double arrowRadius = size * 0.5;
		
		Point2 a = new Point2( 0.0, arrowRadius );
		Point2 b = new Point2( arrowRadius * Math.sin( Math.toRadians( 120.0 ) ), arrowRadius * Math.cos( Math.toRadians( 120.0 ) ) );
		Point2 c = new Point2( arrowRadius * Math.sin( Math.toRadians( 240.0 ) ), arrowRadius * Math.cos( Math.toRadians( 240.0 ) ) );
		
		Path2D.Double arrowShape = DPShape.filletedPath( new Point2[] { a, b, c }, true, filletSize );
		//Path2D.Double arrowShape = DPShape.path( new Point2[] { a, b, c }, true );
		
		Path2D.Double down = arrowShape;
		Path2D.Double up = (Path2D.Double)arrowShape.clone();
		Path2D.Double right = (Path2D.Double)arrowShape.clone();
		Path2D.Double left = (Path2D.Double)arrowShape.clone();
		
		right.transform( AffineTransform.getQuadrantRotateInstance( 3 ) );
		up.transform( AffineTransform.getQuadrantRotateInstance( 2 ) );
		left.transform( AffineTransform.getQuadrantRotateInstance( 1 ) );
		
		up.transform( AffineTransform.getTranslateInstance( -up.getBounds2D().getMinX(), -up.getBounds2D().getMinY() ) );
		right.transform( AffineTransform.getTranslateInstance( -right.getBounds2D().getMinX(), -right.getBounds2D().getMinY() ) );
		down.transform( AffineTransform.getTranslateInstance( -down.getBounds2D().getMinX(), -down.getBounds2D().getMinY() ) );
		left.transform( AffineTransform.getTranslateInstance( -left.getBounds2D().getMinX(), -left.getBounds2D().getMinY() ) );
			
		return new Path2D.Double[] { left, right, up, down }; 
	}
	
	
	private static Path2D.Double unitArrowPaths[] = createArrowPaths( 1.0, 0.18 );
	private static Path2D.Double unitLeft = unitArrowPaths[0];
	private static Path2D.Double unitRight = unitArrowPaths[1];
	private static Path2D.Double unitUp = unitArrowPaths[2];
	private static Path2D.Double unitDown = unitArrowPaths[3];
	
	
	public enum Direction
	{
		LEFT,
		RIGHT,
		UP,
		DOWN
	}
	
	
	
	private Path2D.Double shape;
	
	public Arrow(Direction direction, double size)
	{
		if ( direction == Direction.LEFT )
		{
			shape = unitLeft;
		}
		else if ( direction == Direction.RIGHT )
		{
			shape = unitRight;
		}
		else if ( direction == Direction.UP )
		{
			shape = unitUp;
		}
		else if ( direction == Direction.DOWN )
		{
			shape = unitDown;
		}
		
		shape = (Path2D.Double)shape.clone();
		shape.transform( AffineTransform.getScaleInstance( size, size ) );
	}


	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		return new DPShape( Primitive.shapeParams.get( style ), "", shape );
	}
}
