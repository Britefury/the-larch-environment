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



public class PointerEvent extends Event
{
	protected PointerInterface pointer;
	
	
	public PointerEvent(PointerInterface pointer)
	{
		this.pointer = pointer;
	}
	
	
	public PointerInterface getPointer()
	{
		return pointer;
	}
	
	
	public PointerEvent transformed(Xform2 xToLocal)
	{
		return new PointerEvent( pointer.transformed( xToLocal ) );
	}

	public PointerEvent transformed(AffineTransform xToLocal)
	{
		return new PointerEvent( pointer.transformed( xToLocal ) );
	}
	
	
	public Point2 getLocalPointerPos()
	{
		return pointer.getLocalPos();
	}
	
	public int getModifiers()
	{
		return pointer.getModifiers();
	}
}
