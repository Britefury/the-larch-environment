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

public class PointerScrollEvent extends PointerEvent
{
	public int scrollX, scrollY;
	
	
	public PointerScrollEvent(PointerInterface pointer, int scrollX, int scrollY)
	{
		super( pointer );
		
		this.scrollX = scrollX;
		this.scrollY = scrollY;
	}
	
	
	
	public PointerScrollEvent transformed(Xform2 xToLocal)
	{
		return new PointerScrollEvent( pointer.transformed( xToLocal ), scrollX, scrollY );
	}
}
