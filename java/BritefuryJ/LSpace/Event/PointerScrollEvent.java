//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Event;

import java.awt.geom.AffineTransform;

import BritefuryJ.LSpace.Input.PointerInterface;
import BritefuryJ.Math.Xform2;

public class PointerScrollEvent extends PointerEvent
{
	protected int scrollX, scrollY;
	
	
	public PointerScrollEvent(PointerInterface pointer, int scrollX, int scrollY)
	{
		super( pointer );
		
		this.scrollX = scrollX;
		this.scrollY = scrollY;
	}
	
	
	public int getScrollX()
	{
		return scrollX;
	}
	
	public int getScrollY()
	{
		return scrollY;
	}
	
	
	
	public PointerScrollEvent transformed(Xform2 xToLocal)
	{
		return new PointerScrollEvent( pointer.transformed( xToLocal ), scrollX, scrollY );
	}

	public PointerEvent transformed(AffineTransform xToLocal)
	{
		return new PointerScrollEvent( pointer.transformed( xToLocal ), scrollX, scrollY );
	}
}
