//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Input.Keyboard;

import java.awt.event.KeyEvent;

public class KeyboardInteractor
{
	public void addToKeyboard(Keyboard keyboard)
	{
		keyboard.addInteractor( this );
	}
	
	public void addToKeyboard(Keyboard keyboard, int priority)
	{
		keyboard.addInteractor( priority, this );
	}
	
	public void removeFromKeyboard(Keyboard keyboard)
	{
		keyboard.removeInteractor( this );
	}
	
	public void grabPointer(Keyboard keyboard)
	{
		keyboard.interactorGrab( this );
	}
	
	public void ungrabPointer(Keyboard keyboard)
	{
		keyboard.interactorUngrab( this );
	}
	
	
	
	
	public boolean keyPressed(Keyboard keyboard, KeyEvent event)
	{
		return false;
	}


	public boolean keyReleased(Keyboard keyboard, KeyEvent event)
	{
		return false;
	}


	public boolean keyTyped(Keyboard keyboard, KeyEvent event)
	{
		return false;
	}
}
