//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent.Event;

import java.awt.geom.AffineTransform;

import BritefuryJ.DocPresent.Input.PointerInterface;
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
}
