//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Event;

import java.awt.geom.AffineTransform;

import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.Math.Xform2;

public class PointerButtonClickedEvent extends PointerEvent
{
	public int button, clickCount;
	
	
	public PointerButtonClickedEvent(PointerInterface pointer, int button, int clickCount)
	{
		super( pointer );
		
		this.button = button;
		this.clickCount = clickCount;
	}
	
	
	public int getButton()
	{
		return button;
	}
	
	public int getClickCount()
	{
		return clickCount;
	}
	
	
	
	public PointerButtonClickedEvent transformed(Xform2 xToLocal)
	{
		return new PointerButtonClickedEvent( pointer.transformed( xToLocal ), button, clickCount );
	}

	public PointerEvent transformed(AffineTransform xToLocal)
	{
		return new PointerButtonClickedEvent( pointer.transformed( xToLocal ), button, clickCount );
	}
}
