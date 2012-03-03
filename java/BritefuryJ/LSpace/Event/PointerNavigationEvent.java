//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.Event;

import BritefuryJ.LSpace.Input.PointerInterface;
import BritefuryJ.Math.Xform2;

public abstract class PointerNavigationEvent extends PointerEvent
{
	public PointerNavigationEvent(PointerInterface pointer)
	{
		super( pointer );
	}
	
	
	public abstract Xform2 createXform();
}
