//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Diagram;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public abstract class DiagramEntity
{
	public abstract void draw(Graphics2D graphics);
	public abstract boolean containsPoint(Point2 p);
	
	
	public abstract void stroke(Stroke s);
	public abstract void paint(Paint p);
	
	
	public abstract void onHover(HoverListener listener);
	public abstract void hoverMonitor(HoverMonitor monitor);
	public abstract void hoverHighlight(Paint p);
	
	
	public abstract void onInteraction(InteractionListener listener);
	
	
	public abstract DiagramEntity transform(AffineTransform x);

	
	public DiagramEntity translate(double x, double y)
	{
		return transform( AffineTransform.getTranslateInstance( x, y ) );
	}

	public DiagramEntity translate(Vector2 x)
	{
		return transform( AffineTransform.getTranslateInstance( x.x, x.y ) );
	}


	public DiagramEntity scale(double x, double y)
	{
		return transform( AffineTransform.getScaleInstance( x, y ) );
	}

	public DiagramEntity scale(Vector2 s)
	{
		return transform( AffineTransform.getScaleInstance( s.x, s.y ) );
	}

	public DiagramEntity scale(double s)
	{
		return transform( AffineTransform.getScaleInstance( s, s ) );
	}

	
	public DiagramEntity rotate(double r)
	{
		return transform( AffineTransform.getRotateInstance( r ) );
	}
}
