//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.LSpace.Event;

import java.awt.geom.AffineTransform;

import BritefuryJ.LSpace.Input.Modifier;
import BritefuryJ.LSpace.Input.PointerInterface;
import BritefuryJ.Math.Xform2;
import BritefuryJ.Util.Platform;


public class PointerButtonEvent extends AbstractPointerButtonEvent
{
	public enum Action
	{
		DOWN,
		DOWN2,
		DOWN3,
		UP
	}
	
	protected Action action;
	
	
	public PointerButtonEvent(PointerInterface pointer, int button, Action action)
	{
		super( pointer, button );
		
		this.action = action;
	}
	
	
	public Action getAction()
	{
		return action;
	}
	
	
	
	public PointerButtonEvent transformed(Xform2 xToLocal)
	{
		return new PointerButtonEvent( pointer.transformed( xToLocal ), button, action );
	}

	public PointerEvent transformed(AffineTransform xToLocal)
	{
		return new PointerButtonEvent( pointer.transformed( xToLocal ), button, action );
	}




	public boolean isContextButtonEvent() {
		int mods = pointer.getModifiers();

		boolean contextClick;
		if (Platform.getPlatform() == Platform.MAC) {
			// Mac: context click in response to:
			// - button 1 & meta (CMD)
			// - button 3
			return button == 1  &&  (mods & Modifier.META) != 0  ||  button == 3;
		}
		else {
			// Windows / Linux
			// button 3
			return button == 3;
		}
	}
}
