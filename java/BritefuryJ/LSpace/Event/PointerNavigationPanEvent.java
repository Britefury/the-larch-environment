//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.Event;

import java.awt.geom.AffineTransform;

import BritefuryJ.LSpace.Input.PointerInterface;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Xform2;

public class PointerNavigationPanEvent extends PointerNavigationEvent
{
	protected Vector2 pan;
	
	
	public PointerNavigationPanEvent(PointerInterface device, Vector2 pan)
	{
		super( device );
		
		this.pan = pan;
	}
	
	
	
	public Xform2 createXform()
	{
		return new Xform2( pan );
	}

	
	public PointerNavigationPanEvent transformed(Xform2 xToLocal)
	{
		return new PointerNavigationPanEvent( pointer.transformed( xToLocal ), xToLocal.transform( pan ) );
	}

	public PointerNavigationPanEvent transformed(AffineTransform xToLocal)
	{
		return new PointerNavigationPanEvent( pointer.transformed( xToLocal ), pan.transform( xToLocal ) );
	}
}
