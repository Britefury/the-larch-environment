//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Event;

import java.awt.geom.AffineTransform;

import BritefuryJ.LSpace.Input.PointerInterface;
import BritefuryJ.Math.Xform2;



public class PointerMotionEvent extends PointerEvent
{
	public enum Action
	{
		ENTER,
		LEAVE,
		MOTION
	}
	
	protected Action action;
	
	
	public PointerMotionEvent(PointerInterface device, Action action)
	{
		super( device );
		
		this.action = action;
	}
	
	
	
	public PointerMotionEvent transformed(Xform2 xToLocal)
	{
		return new PointerMotionEvent( pointer.transformed( xToLocal ), action );
	}

	public PointerEvent transformed(AffineTransform xToLocal)
	{
		return new PointerMotionEvent( pointer.transformed( xToLocal ), action );
	}
	
	
	public PointerMotionEvent withAction(Action action)
	{
		return new PointerMotionEvent( pointer, action );
	}
}
