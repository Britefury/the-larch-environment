//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.Input.Keyboard;

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
