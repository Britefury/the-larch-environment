//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.Event;

import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.Math.Xform2;



public class PointerMotionEvent extends PointerEvent
{
	public enum Action
	{
		ENTER,
		LEAVE,
		MOTION
	}
	
	public Action action;
	
	
	public PointerMotionEvent(PointerInterface device, Action action)
	{
		super( device );
		
		this.action = action;
	}
	
	
	
	public PointerMotionEvent transformed(Xform2 xToLocal)
	{
		return new PointerMotionEvent( pointer.transformed( xToLocal ), action );
	}
}
