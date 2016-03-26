//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Event;

import java.awt.geom.AffineTransform;

import BritefuryJ.LSpace.Input.PointerInterface;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Xform2;

public class PointerNavigationZoomEvent extends PointerNavigationEvent
{
	protected Point2 pos;
	protected double zoom;
	
	
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
