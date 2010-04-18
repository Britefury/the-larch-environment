//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Event;

import java.awt.geom.AffineTransform;

import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Xform2;

public class PointerNavigationZoomEvent extends PointerNavigationEvent
{
	public Point2 pos;
	public double zoom;
	
	
	public PointerNavigationZoomEvent(PointerInterface device, Point2 pos, double zoom)
	{
		super( device );
		
		this.pos = pos;
		this.zoom = zoom;
	}
	
	
	
	public Xform2 createXform()
	{
		return new Xform2( pos.toVector2().negate() ).concat( new Xform2( zoom ) ).concat( new Xform2( pos.toVector2() ) );
	}
	
	
	public PointerNavigationZoomEvent transformed(Xform2 xToLocal)
	{
		return new PointerNavigationZoomEvent( pointer.transformed( xToLocal ), xToLocal.transform( pos ), zoom );
	}

	public PointerNavigationZoomEvent transformed(AffineTransform xToLocal)
	{
		return new PointerNavigationZoomEvent( pointer.transformed( xToLocal ), pos.transform( xToLocal ), zoom );
	}
}
